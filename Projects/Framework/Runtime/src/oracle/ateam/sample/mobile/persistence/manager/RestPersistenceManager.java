/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 03-nov-2014   Steven Davelaar
 1.6           Added method addOAuthHeaderParamsIfNeeded
 25-sep-2014   Steven Davelaar
 1.5           Added support for attributesToExclude in getPayloadKeyValuePairs
               Added abstract method handleResponse to pass in currentEntity
 12-sep-2014   Steven Davelaar
 1.4           - Numeric values should not be stored as string in key-value map in
                 method getPayloadKeyValuePairs
 29-may-2014   Steven Davelaar
 1.3           - Added support for ested objects in getPayloadKeyValuePairs
 05-feb-2014   Steven Davelaar
 1.2           - Removed all config info, now stored in persistenceMapping.xml
 23-dec-2013   Steven Davelaar
 1.1           - connectionName added as member with getter/setter
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.manager;

import java.math.BigDecimal;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.adfmf.dc.ws.rest.RestServiceAdapter;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.Model;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.XmlAnyDefinition;

import oracle.ateam.sample.mobile.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.persistence.metadata.AttributeMappingOneToMany;
import oracle.ateam.sample.mobile.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.persistence.metadata.Method;
import oracle.ateam.sample.mobile.persistence.metadata.MethodHeaderParameter;
import oracle.ateam.sample.mobile.persistence.metadata.MethodParameter;
import oracle.ateam.sample.mobile.persistence.metadata.MethodParameterImpl;
import oracle.ateam.sample.mobile.persistence.metadata.ObjectPersistenceMapping;
import oracle.ateam.sample.mobile.persistence.model.Entity;
import oracle.ateam.sample.mobile.security.OAuthTokenManager;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.StringUtils;

/**
 * Abstract class that provides generic implementation of some of the methods of the
 * PersistenceManager interface that can be used by concrete subclasses that use RESTful web services.
 * Also provides helper methods that can be used by subclasses.
 *
 */
public abstract class RestPersistenceManager
  extends AbstractRemotePersistenceManager
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(RestPersistenceManager.class);
  private static final String AUTH_HEADER_PARAM_NAME = "Authorization";

  public RestPersistenceManager()
  {
  }


  /**
   * Convert object value to a string. If the value is a Date or TimeStamp the
   * date and datetime format as defined against the dscriptor in persistenceMapping.xml is used
   * to do the conversion.
   * This method also includes a work around for bug 18523176: clearing number field in amx page sets the value to 0
   * instead of null. If the attribute is a BigDecimal or Integer and the value is 0, we now return null.
   * Override this method if you do need to return "0".
   * @param attrMapping
   * @param value
   * @return
   */
  protected String convertToStringValue(AttributeMapping attrMapping, Object value)
  {
    if (value == null)
    {
      return getRestNullValue();
    }
    Class javaType = value.getClass();
    String stringValue = value.toString();
    // work around for bug 18523176: clearing number field in amx page sets the value to 0
    // instead of null.
    if ("0".equals(stringValue) && (javaType == Integer.class || javaType == BigDecimal.class))
    {
      return getRestNullValue();
    }
    if (Date.class.isAssignableFrom(javaType))
    {
      Date date = (Date) value;
      String format = attrMapping.getClassMappingDescriptor().getDateFormat();
      SimpleDateFormat sdf = new SimpleDateFormat(format);
      stringValue = sdf.format(date);
    }
    return stringValue;
  }


  /**
   * Returns a map with as key the attribute name as used in the payload, and
   * as value the string representation of the entity attribute value
   * @param entity
   * @return
   */
  public Map getPayloadKeyValuePairs(Entity entity, List attributesToExclude)
  {
    Map pairs = new HashMap();
    String entityClass = entity.getClass().getName();
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = mapping.findClassMappingDescriptor(entityClass);
    List attributeMappings = descriptor.getAttributeMappingsDirect();
    // when attributes have a dot in the attribute name, they are nested, and we have
    // to create nested maps to create the correct json payload
    // we assume that nesting is only one level deep.
    for (int i = 0; i < attributeMappings.size(); i++)
    {
      AttributeMapping attrMapping = (AttributeMapping) attributeMappings.get(i);
      String attrName = attrMapping.getAttributeName();
      if (attributesToExclude!=null && attributesToExclude.contains(attrName))
      {
        continue;
      }
      String payloadAttr = attrMapping.getAttributeNameInPayload();
      if (payloadAttr != null)
      {
        Object value = entity.getAttributeValue(attrMapping.getAttributeName());
        String stringValue = convertToStringValue(attrMapping, value);
        int dotpos = payloadAttr.indexOf(".");
        if (dotpos > 0)
        {
          String nestedObjectName = payloadAttr.substring(0, dotpos);
          String nestedAttributeName = payloadAttr.substring(dotpos + 1);
          Map nestedObjectMap = (Map) pairs.get(nestedObjectName);
          if (nestedObjectMap == null)
          {
            // first attribute of this nested object, create the map and add it to
            // top-level map
            nestedObjectMap = new HashMap();
            pairs.put(nestedObjectName, nestedObjectMap);
          }
          // add the attribute to the nested map
          nestedObjectMap.put(nestedAttributeName, stringValue);
        }
        else
        {
          if (value instanceof BigDecimal || value instanceof Double || value instanceof Long ||
              value instanceof Integer)
          {
            // prevent adding quotes around the value in json payload
            // can cause issues with some back ends like MongoDB
            pairs.put(attrMapping.getAttributeNameInPayload(), value);
          }
          else
          {
            pairs.put(attrMapping.getAttributeNameInPayload(), stringValue);
          }
        }
      }
    }
    // loop over one-to-many relationships to add detail data objects
    List oneToManyMappings = descriptor.getAttributeMappingsOneToMany();
    for (int i = 0; i < oneToManyMappings.size(); i++)
    {
      AttributeMappingOneToMany attrMapping = (AttributeMappingOneToMany) oneToManyMappings.get(i);
      String attrName2 = attrMapping.getAttributeName();
      if (attributesToExclude!=null && attributesToExclude.contains(attrName2))
      {
        continue;
      }
      if (attrMapping.getAccessorMethod() != null || attrMapping.getAttributeNameInPayload() == null)
      {
        // skip child entities when they have their own finder method or no payload attr specified
        // continue;
      }
      List children = (List) entity.getAttributeValue(attrMapping.getAttributeName());
      List childKeyValuePairs = new ArrayList();
      for (int j = 0; j < children.size(); j++)
      {
        Entity child = (Entity) children.get(j);
        // recursive call
        // only include attributesToExclude that are prefixed with the child entityName
        List childAttrsToIgnore = new ArrayList();
        if (attributesToExclude!=null)
        {
          String childClassName = child.getClass().getName();
          String childDataObjectName = childClassName.substring(childClassName.lastIndexOf(".")+1);
          for (int k = 0; k < attributesToExclude.size(); k++)
          {
            String attrName = (String) attributesToExclude.get(k);
            if (attrName.startsWith(childDataObjectName+"."))
            {
              childAttrsToIgnore.add(attrName.substring(childDataObjectName.length()));
            }            
          }
        }
        Map childMap = getPayloadKeyValuePairs(child,childAttrsToIgnore);
        childKeyValuePairs.add(childMap);
      }
      // if there is only one child, we check property sendAsArrayIfOnlyOneEntry
      if (childKeyValuePairs.size() == 1 && !attrMapping.isSendAsArrayIfOnlyOneEntry())
      {
        pairs.put(attrMapping.getAttributeNameInPayload(), childKeyValuePairs.get(0));
      }
      else
      {
        pairs.put(attrMapping.getAttributeNameInPayload(), childKeyValuePairs);
      }
    }
    return pairs;
  }

  /**
   * Method required by PersistenceManager interface
   * This implementation does nothing
   */
  public void rollback()
  {
  }

  /**
   * Method required by PersistenceManager interface
   * This implementation does nothing
   */
  public void commmit()
  {
    //NOOP
  }


  /**
   * Return the string that should be used to indicate a null value in Rest payload.
   * Returns null by default. Returns '' in RestXMLPersistenceManager
   * Override this method if you need a different null value.
   * Note that the value "null" between quotes is translated to null without quotes in method
   * RestJSONPersistenceManager.getSerializedDataObject to work around MAF bug 18523199
   * @return
   */
  public String getRestNullValue()
  {
    return null;
  }


  public Object getMaxValue(Class entityClass, String attrName)
  {
    return null;
  }

  public String invokeRestService(String connectionName, String requestType, String requestUri, String payload,
                                  Map headerParamMap, int retryLimit, boolean secured)
  {
    boolean isGET = "GET".equals(requestType);
    RestServiceAdapter restService = Model.createRestServiceAdapter();
    restService.clearRequestProperties();
    restService.setConnectionName(connectionName);
    if ("PATCH".equals(requestType))
    {
      restService.setRequestType("POST");
      restService.addRequestProperty("X-HTTP-Method-Override", "PATCH");
    }
    else
    {
      restService.setRequestType(requestType);
    }
    //    boolean added = addAuthorizationHeaderIfNeeded(connectionName, requestUri, secured, restService);
    if (headerParamMap != null)
    {
      Iterator keys = headerParamMap.keySet().iterator();
      while (keys.hasNext())
      {
        String key = (String) keys.next();
        // Authorization header might already be added because mobile security is enabled
        //        if (!key.equals(AUTH_HEADER_PARAM_NAME) || !added)
        //        {
        restService.addRequestProperty(key, (String) headerParamMap.get(key));
        //        }
      }
    }
    restService.setRetryLimit(retryLimit);
    // if connection url end swith "/" and request uri also starts with "/" we need to remove this starting slash from
    // requestUri, otherwise the rest invocation will fail with an error
    String uri = connectionUrlEndsWithSlash(connectionName)? requestUri.substring(1): requestUri;
    boolean payloadSet = payload != null && !"".equals(payload);
    uri = isGET? uri + (payloadSet? "?" + payload: ""): uri;
    restService.setRequestURI(uri);
    String response = "";
    try
    {
      response = restService.send((isGET? null: payload));
      return response;
    }
    catch (Exception e)
    {
      Throwable t = e.getCause() != null? e.getCause(): e;
      String message = "Error invoking REST " + requestType + " service " + uri + " error: " + t.getLocalizedMessage();
      sLog.severe(message);
      // throw exception so any failed data synch actions can be registsred and processed later
      throw new AdfException(message, AdfException.ERROR);
    }
  }

  //  /**
  //   * When connection in connections.xml is secured, this method adds an Authorization header to the
  //   * Rest HTTP request
  //   * @param connectionName
  //   * @param requestUri
  //   * @param secured
  //   * @param restService
  //   */
  //  protected boolean addAuthorizationHeaderIfNeeded(String connectionName, String requestUri, boolean secured,
  //                                              RestServiceAdapter restService)
  //  {
  //    if (secured || isConnectionSecured(connectionName))
  //    {
  //      String cred = getCredentials(connectionName, requestUri);
  //      String auth ="Basic "+ new BASE64Encoder().encode(cred.getBytes());
  //      restService.addRequestProperty(AUTH_HEADER_PARAM_NAME, auth);
  //      return true;
  //    }
  //    return false;
  //  }

  //  /**
  //   * Return String "username:password" which can then be encoded and send as authorization request property
  //   * @param connectionName
  //   * @param requestUri
  //   * @return
  //   */
  //  protected String getCredentials(String connectionName, String requestUri)
  //  {
  //    UserContextBean uc = new UserContextBean();
  //    if (uc.getUserName()==null || uc.getPassword()==null)
  //    {
  //      MessageUtils.handleError("Connection "+connectionName+" is secured, you need to enable feature security to be able to call REST web service "+requestUri);
  //    }
  //    String cred = uc.getUserName()+":"+uc.getPassword();
  //    return cred;
  //  }

  protected boolean connectionUrlEndsWithSlash(String connectionName)
  {
    // 12.1.3 M11 struct
    //    <Reference name="StevenPC" className="oracle.adf.model.connection.rest.RestConnection" xmlns="">
    //      <Factory className="oracle.adf.model.connection.rest.RestConnectionFactory"/>
    //      <RefAddresses>
    //        <XmlRefAddr addrType="StevenPC">
    //          <Contents>
    //            <restconnection name="StevenPC" url="http://192.168.1.112:7101/bpmrest/rest/bpm"/>
    //          </Contents>
    //        </XmlRefAddr>
    //      </RefAddresses>
    //    </Reference>
    boolean endsWithSlash = false;
    XmlAnyDefinition connectionNode = Utility.getConnection(connectionName);
    if (connectionNode != null)
    {
      // the lookup to connection doesn't work when feature is included through FAR
      // so we catch the NPE is that case and just continue
      try
      {
        String url =
          (String) connectionNode.getChildDefinition("RefAddresses").getChildDefinition("XmlRefAddr").getChildDefinition("Contents").getChildDefinition("restconnection").getAttributeValue("url");
        endsWithSlash = url.endsWith("/");
      }
      catch (Exception e)
      {
        sLog.warning("Check for slash in connection URL failed");
      }
    }
    return endsWithSlash;
    // 11.1.2.4 struc:
    //    XmlAnyDefinition  connectionNode = Utility.getConnections().getChildDefinition(connectionName);
    //    List connections = Utility.getConnections().getChildDefinitions("Reference");
    //    for (int i = 0; i < connections.size(); i++)
    //    {
    //      XmlAnyDefinition connection = (XmlAnyDefinition) connections.get(i);
    //      if (connectionName.equals(connection.getAttributeValue("name")))
    //      {
    //        connectionNode = connection;
    //        break;
    //      }
    //    }
    //    <Reference name="Aura" className="oracle.adf.model.connection.url.HttpURLConnection" xmlns="">
    //       <Factory className="oracle.adf.model.connection.url.URLConnectionFactory"/>
    //       <RefAddresses>
    //          <XmlRefAddr addrType="Aura">
    //             <Contents>
    //                <urlconnection name="Aura" url="http://ec2-54-203-157-52.us-west-2.compute.amazonaws.com:7001/"/>
    //             </Contents>
    //          </XmlRefAddr>
    //       </RefAddresses>
    //    </Reference>
    //    if (connectionNode!=null)
    //    {
    //      String url = (String) connectionNode.getChildDefinition("RefAddresses").getChildDefinition("XmlRefAddr").getChildDefinition("Contents").getChildDefinition("urlconnection").getAttributeValue("url");
    //      boolean endsWithSlash = url.endsWith("/");
    //      return endsWithSlash;
    //    }
    //    return false;
  }

  public String invokeRestService(Method method, Map paramValues)
  {
    // parameters with pathParam=true are included between curly brackets in uri, we need
    // to substitute the param with the actual value in that case
    // Non-uri parameters are sent as queryString
    // buld the payload string
    String uri = method.getRequestUri();
    StringBuffer queryStringOrPayload = new StringBuffer();
    boolean firstParam = true;
    Iterator paramIter = paramValues.keySet().iterator();
    while (paramIter.hasNext())
    {
      MethodParameter param = (MethodParameter) paramIter.next();
      String paramName = param.getName();
      if (param.isPathParam())
      {
        // substitute in uri
        String oldValue = "{" + paramName + "}";
        Object value = paramValues.get(param);
        String newValue = value != null? value.toString(): "";
        uri = StringUtils.substitute(uri, oldValue, newValue);
      }
      else
      {
        if (firstParam)
        {
          firstParam = false;
        }
        else
        {
          queryStringOrPayload.append("&");
        }
        // with Non-GET requests, we can send a raw payload without param name
        if (paramName != null && !"".equals(paramName.trim()))
        {
          queryStringOrPayload.append(paramName + "=" + paramValues.get(param));
        }
        else
        {
          queryStringOrPayload.append(paramValues.get(param));
        }
      }
    }
    Map headerParamMap = new HashMap();
    List headerParams = method.getHeaderParams();
    // add OAuth header params if needed
    addOAuthHeaderParamsIfNeeded(method, headerParams);
    
    for (int i = 0; i < headerParams.size(); i++)
    {
      MethodHeaderParameter param = (MethodHeaderParameter) headerParams.get(i);
      headerParamMap.put(param.getName(), param.getValue());
    }
    return invokeRestService(method.getConnectionName(), method.getRequestType(), uri, queryStringOrPayload.toString(),
                             headerParamMap, 0, method.isSecured());
  }

  /**
   * Add OAuth header params if oauthCongif attribute is set on header parameter
   * @param method
   * @param headerParams
   */
  protected void addOAuthHeaderParamsIfNeeded(Method method, List headerParams)
  {
    String oauthConfigName = method.getOAuthConfigName();
    if (oauthConfigName!=null && !"".equals(oauthConfigName))
    {
      headerParams.addAll(new OAuthTokenManager().getOAuthHeaderParams(oauthConfigName));
    }
  }

  public void sendWriteRequest(Entity entity, Method method, String action)
  {
    Map paramValues = createParameterMap(entity, method, null, action);
    try
    {
      String response = invokeRestService(method, paramValues);
      handleWriteResponse(entity, method, action, response);
    }
    catch (Exception e)
    {
      // throw exception so synch action can be registered to be processed for later
      ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
      handleWebServiceInvocationError(descriptor, e, true);
    }
  }


  /**
   * When the write action is not a remove action, this method assumes that the payload returned contains
   * the inserted/updated entity instance and updates the entity and database row with the latest values
   * as returned by the web service. If the payload is a different format, nothing will happen.
   *
   * Override this method to handle the specific format in which application specific errors are returned in response
   * @param entity
   * @param method
   * @param action
   * @param response
   */
  protected void handleWriteResponse(Entity entity, Method method, String action, String response)
  {
    if (!ACTION_REMOVE.equals(action) && response != null && !"".equals(response.trim()))
    {
      if (this instanceof RestJSONPersistenceManager && (response.startsWith("{") || response.startsWith("[")) ||
          this instanceof RestXMLPersistenceManager && response.startsWith("<"))
      {
        try
        {
          handleResponse(response, entity.getClass(), method.getPayloadElementName(),
                             method.getPayloadRowElementName(), null,entity, false);
        }
        catch (Exception e)
        {
          // do nothing, return payload of different format
        }
      }

    }
  }

  public void sendRemoveRequest(Entity entity, Method method)
  {
    sendWriteRequest(entity, method, ACTION_REMOVE);
  }

  /**
   * Invoke the getCanonical method to fully populate the entity with all attributes, and any
   * child entities.
   * @param entity
   */
  public void getCanonical(Entity entity)
  {

    Class entityClass = entity.getClass();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    Method getCanonicalMethod = descriptor.getGetCanonicalMethod();
    if (getCanonicalMethod == null)
    {
      sLog.severe("No getCanonical method found for " + entityClass.getName());
    }
    Map paramValues = createParameterMap(entity, getCanonicalMethod, null, null);
    try
    {
      String restResponse = invokeRestService(getCanonicalMethod, paramValues);
      entity.setCanonicalGetExecuted(true);
      if (restResponse != null)
      {
        handleReadResponse(restResponse, entityClass, getCanonicalMethod.getPayloadElementName(),
                           getCanonicalMethod.getPayloadRowElementName(), null, false);
      }
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(descriptor, e, false);
    }
  }

  /**
   * Invoke a custom method
   * @param entity
   */
  public void invokeCustomMethod(Entity entity, String methodName)
  {
    Class entityClass = entity.getClass();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    Method customMethod = descriptor.getCustomMethod(methodName);
    if (customMethod == null)
    {
      sLog.severe("Custom method " + customMethod + " not found for " + entityClass.getName());
      return;
    }
    Map paramValues = createParameterMap(entity, customMethod, null, null);
    try
    {
      String restResponse = invokeRestService(customMethod, paramValues);
      if (restResponse != null)
      {
        handleWriteResponse(entity, customMethod, null, restResponse);
      }
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(descriptor, e, true);
    }
  }


  public List findAll(Class entityClass)
  {
    List entities = new ArrayList();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    Method findAllMethod = descriptor.getFindAllMethod();
    if (findAllMethod == null)
    {
      sLog.severe("No findAll method found for " + entityClass.getName());
      return Collections.EMPTY_LIST;
    }
    Map paramValues = createParameterMap(null, findAllMethod, null, null);
    try
    {
      String restResponse = invokeRestService(findAllMethod, paramValues);
      if (restResponse != null)
      {
        entities =
          handleReadResponse(restResponse, entityClass, findAllMethod.getPayloadElementName(),
                             findAllMethod.getPayloadRowElementName(), null, descriptor.isDeleteLocalRowsOnFindAll());
        // only if an order-by statement is specified, we execute the find method against the local DB to reorder
        // the entity list. Note that the entity instances are not recreated because they will be retrieved from the cache
        if (descriptor.isPersisted() && descriptor.getOrderBy() != null)
        {
          DBPersistenceManager dbpm = getLocalPersistenceManager();
          entities = dbpm.findAll(entityClass);
        }
      }
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(descriptor, e, false);
    }
    return entities;
  }

  public List findAllInParent(Class childEntityClass, Entity parent, String accessorAttribute)
  {
    List entities = new ArrayList();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(childEntityClass);
    Method findAllInParentMethod = descriptor.getFindAllInParentMethod(accessorAttribute);
    if (findAllInParentMethod == null)
    {
      sLog.severe("No findAllInParent method found for " + childEntityClass.getName());
      return Collections.EMPTY_LIST;
    }
    Map paramValues = createParameterMap(parent, findAllInParentMethod, null, null);
    try
    {
      String restResponse = invokeRestService(findAllInParentMethod, paramValues);
      if (restResponse != null)
      {
        List parentBindParamInfos = getBindParamInfos(parent);
        entities =
          handleReadResponse(restResponse, childEntityClass, findAllInParentMethod.getPayloadElementName(),
                             findAllInParentMethod.getPayloadRowElementName(), parentBindParamInfos, false);
        // only if an order-by statement is specified, we execute the find method against the local DB to reorder
        // the entity list. Note that the entity instances are not recreated because they will be retrieved from the cache
        if (descriptor.isPersisted() && descriptor.getOrderBy() != null)
        {
          DBPersistenceManager dbpm = getLocalPersistenceManager();
          entities = dbpm.findAllInParent(childEntityClass, parent, accessorAttribute);
        }
      }
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(descriptor, e, false);
    }
    return entities;
  }

  public List find(Class entityClass, String searchValue)
  {
    return Collections.EMPTY_LIST;
  }

  public List find(Class entityClass, String searchValue, List attrNamesToSearch)
  {
    return Collections.EMPTY_LIST;
  }

  public Map createParameterMap(Entity entity, Method method, Map searchValues, String action)
  {
    List params = method.getParams();
    Map paramValues = new HashMap();
    // if we need to send serialized DO as payload, we add it to parameter map with param name as ""
    if (method.isSendDataObjectAsPayload())
    {
      boolean remove = ACTION_REMOVE.equals(action);
      String serializedDO =
        getSerializedDataObject(entity, method.getPayloadElementName(), method.getPayloadRowElementName(), method.getAttributesToIgnore(), remove);
      // the payload parameter does not have a name, that's how it is identfied when calling the rest service
      MethodParameter payloadParam = new MethodParameterImpl("");
      paramValues.put(payloadParam, serializedDO);
    }
    for (int i = 0; i < params.size(); i++)
    {
      MethodParameter param = (MethodParameter) params.get(i);
      if (param.isSerializedDataObject() && entity != null)
      {
        boolean remove = ACTION_REMOVE.equals(action);
        String serializedDO =
          getSerializedDataObject(entity, method.getPayloadElementName(), method.getPayloadRowElementName(), method.getAttributesToIgnore(), remove);
        paramValues.put(param, serializedDO);
      }
      else if (param.isDataObjectAttribute() && param.getDataObjectAttribute() != null && entity != null)
      {
        Object value = entity.getAttributeValue(param.getDataObjectAttribute());
        if (value != null)
        {
          paramValues.put(param, value);
        }
      }
      else if (param.isLiteralValue() && param.getValue() != null)
      {
        paramValues.put(param, param.getValue());
      }
      else if (param.isELExpression() && param.getValue() != null)
      {
        Object value = AdfmfJavaUtilities.evaluateELExpression(param.getValue());
        if (value != null)
        {
          paramValues.put(param, value);
        }
      }
      else if (param.isSearchValue() && searchValues != null)
      {
        Object value = searchValues.get(param.getName());
        if (value != null)
        {
          paramValues.put(param, value);
        }
      }
    }
    return paramValues;
  }

  public Entity getAsParent(Class parentEtityClass, Entity child, String accessorAttribute)
  {
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(parentEtityClass);
    Method getAsParentMethod = descriptor.getGetAsParentMethod(accessorAttribute);
    if (getAsParentMethod == null)
    {
      sLog.severe("No getAsParent method found for " + parentEtityClass.getName());
      return null;
    }
    Map paramValues = createParameterMap(child, getAsParentMethod, null, null);
    try
    {
      String restResponse = invokeRestService(getAsParentMethod, paramValues);
      if (restResponse != null)
      {
        // only retrieve key values, otherwise we can get an endless loop because a getter method for parent attributes
        // typically causes this method to be called
        List parentBindParamInfos = getBindParamInfos(child, true);
        List entities =
          handleReadResponse(restResponse, parentEtityClass, getAsParentMethod.getPayloadElementName(),
                             getAsParentMethod.getPayloadRowElementName(), parentBindParamInfos, false);
        if (entities.size() > 0)
        {
          Entity parent = (Entity) entities.get(0);
          return parent;
        }
      }
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(descriptor, e, false);
    }
    return null;
  }

  protected abstract String getSerializedDataObject(Entity entity, String collectionElementName, String rowElementName,
                                                    boolean deleteRow);

  protected abstract String getSerializedDataObject(Entity entity, String collectionElementName, String rowElementName,
                                                    List attributesToExclude, boolean deleteRow);

  protected abstract List handleReadResponse(String restResponse, Class entityClass, String collectionElementName,
                                             String rowElementName, List parentBindParamInfos,
                                             boolean deleteLocalRowsOnFindAll);

  protected abstract List handleResponse(String restResponse, Class entityClass, String collectionElementName,
                                             String rowElementName, List parentBindParamInfos, Entity currentEntity,
                                             boolean deleteLocalRowsOnFindAll);
}
