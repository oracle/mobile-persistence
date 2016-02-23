/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
  
 $revision_history$
 29-dec-2015   Steven Davelaar
 1.4           - HTTP status codes < 300 are no longer treated as exception (work around  for MAF bug still throwing an exception 
                 if status code is not 200)
 08-nov-2015   Steven Davelaar
 1.3           - Fixed bug in deriving child attr name to exclude in getPayloadKeyValuePairs (thanks Vik Kumar for reporting)
 24-sep-2015   Steven Davelaar
 1.2           - Check requestType of custom method, when GET call handleReadResponse insteaf handleWriteResponse
 20-mar-2015   Steven Davelaar
 1.1           - Added method logRestCall
               - Now deleting local rows in findAll and findAllInParent methods (was done in handleResponse before)
 08-jan-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.manager;

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

import oracle.ateam.sample.mobile.exception.RestCallException;
import oracle.ateam.sample.mobile.logging.WebServiceCall;
import oracle.ateam.sample.mobile.logging.WebServiceCallService;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingOneToMany;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.Method;
import oracle.ateam.sample.mobile.v2.persistence.metadata.MethodHeaderParameter;
import oracle.ateam.sample.mobile.v2.persistence.metadata.MethodParameter;
import oracle.ateam.sample.mobile.v2.persistence.metadata.MethodParameterImpl;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ObjectPersistenceMapping;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.security.OAuthTokenManager;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;
import oracle.ateam.sample.mobile.util.StringUtils;
import oracle.ateam.sample.mobile.util.TaskExecutor;
import oracle.ateam.sample.mobile.v2.persistence.db.BindParamInfo;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingDirect;
import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;

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
//  private static final String AUTH_HEADER_PARAM_NAME = "Authorization";
  private Map lastResponseHeaders;
  private int lastResponseStatus;

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
    sLog.fine("Executing convertToStringValue");
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
// use dateTimeFormat so we don't loose any time component. the attribute data format first checks whether
// the attribute has its own dateFormat property, if it doesn't it returns the dateTimeFormat of the classMappingDescriptor
// and if that one is not set, the date format of the class mapping descriptor      
//      String format = attrMapping.getClassMappingDescriptor().getDateFormat();
      String format = attrMapping.getDateFormat();
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
  public Map<String,Object> getPayloadKeyValuePairs(Entity entity, List<String> attributesToExclude)
  {
    sLog.fine("Executing getPayloadKeyValuePairs for "+entity.getClass().getSimpleName());
    Map<String,Object> pairs = new HashMap<String,Object>();
    String entityClass = entity.getClass().getName();
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = mapping.findClassMappingDescriptor(entityClass);
    List<AttributeMappingDirect> attributeMappings = descriptor.getAttributeMappingsDirect();
    // to create nested maps to create the correct json payload
    // we assume that nesting is only one level deep.
    for (AttributeMappingDirect attrMapping : attributeMappings)
    {
      String attrName = attrMapping.getAttributeName();
      if (attributesToExclude!=null && attributesToExclude.contains(attrName))
      {
        sLog.fine("Attribute "+attrName+" excluded while creating payload for "+entity.getClass().getSimpleName());
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
          Map<String,Object> nestedObjectMap = (Map<String, Object>) pairs.get(nestedObjectName);
          if (nestedObjectMap == null)
          {
            // first attribute of this nested object, create the map and add it to
            // top-level map
            nestedObjectMap = new HashMap<String,Object>();
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
    List<AttributeMappingOneToMany> oneToManyMappings = descriptor.getAttributeMappingsOneToMany();
    for (AttributeMappingOneToMany attrMapping : oneToManyMappings)
    {
      String attrName2 = attrMapping.getAttributeName();
      if (attributesToExclude!=null && attributesToExclude.contains(attrName2))
      {
        continue;
      }
      //      if (attrMapping.getAccessorMethod() != null || attrMapping.getAttributeNameInPayload() == null)
      if (attrMapping.getAttributeNameInPayload() == null)
      {
        // skip child entities when no payload attr specified. There stiull might a finder method for reead operations,
        // so we only check payload attr to allow write operatiuons to include the detaild
        continue;
      }
      List<Entity> children = (List<Entity>) entity.getAttributeValue(attrMapping.getAttributeName());
      List<Map<String,Object>> childKeyValuePairs = new ArrayList<Map<String,Object>>();
      for (Entity child : children)
      {
        // recursive call
        // only include attributesToExclude that are prefixed with the child entityName
        List<String> childAttrsToIgnore = new ArrayList<String>();
        if (attributesToExclude!=null)
        {
          String childClassName = child.getClass().getName();
          String childDataObjectName = childClassName.substring(childClassName.lastIndexOf(".")+1);
          for (int k = 0; k < attributesToExclude.size(); k++)
          {
            String attrName = attributesToExclude.get(k);
            if (attrName.startsWith(childDataObjectName+"."))
            {
              childAttrsToIgnore.add(attrName.substring(childDataObjectName.length()+1));
            }            
          }
        }
        Map<String,Object> childMap = getPayloadKeyValuePairs(child,childAttrsToIgnore);
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
                                  Map<String,String> headerParamMap, int retryLimit, boolean secured)
  {
    sLog.fine("Executing invokeRestService for "+requestType+" "+requestUri+ ", headers:"+headerParamMap+" "+(payload!=null ? payload :""));
    boolean isGET = "GET".equals(requestType);
    RestServiceAdapter restService = setupRestServiceCall( connectionName,  requestType,  requestUri,  payload,
                                  headerParamMap,  retryLimit);

    String response = "";
    String uri = restService.getRequestURI();
    long startTime = System.currentTimeMillis();
    try
    {
      response = restService.send((isGET? null: payload));      
      sLog.fine("Response from "+requestType+" "+requestUri+ " :"+response);
      logRestCall(connectionName,restService.getRequestType(),uri,restService.getRequestProperties().toString(),payload,response,startTime,null);
      setLastResponseHeaders(restService.getResponseHeaders());
      setLastResponseStatus(restService.getResponseStatus());
      return response;
    }
    catch (Exception e)
    {
      setLastResponseHeaders(restService.getResponseHeaders());
      setLastResponseStatus(restService.getResponseStatus());
      // check whether response is in 200 range, MAF incorrevty throws an error when response status is 201 or 202
      // 304 is Not Modified, happens when eTag is same as on server
      if (restService.getResponseStatus() < 300 || restService.getResponseStatus()==304)
      {
        // return detail message as response if available
        String causeMessage = e.getCause() != null? e.getCause().getLocalizedMessage() : null;
        logRestCall(connectionName,restService.getRequestType(),uri,restService.getRequestProperties().toString(),payload,causeMessage,startTime,e);
        return causeMessage;
      }
      else
      {
        logRestCall(connectionName,restService.getRequestType(),uri,restService.getRequestProperties().toString(),payload,null,startTime,e);
        RestCallException re = new RestCallException(e);
        re.setRequestMethod(requestType);
        re.setRequestUri(uri);
        re.setResponseHeaders(restService.getResponseHeaders());
        re.setResponseStatus(restService.getResponseStatus());
        return handleInvokeRestServiceError(requestType,uri,re);        
      }
    }
  }

  public RestServiceAdapter setupRestServiceCall(String connectionName, String requestType, String requestUri, String payload,
                                  Map<String,String> headerParamMap, int retryLimit)
  {
    sLog.fine("Executing setupRestServiceCall for "+requestType+" "+requestUri);
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
      Iterator<String> keys = headerParamMap.keySet().iterator();
      while (keys.hasNext())
      {
        String key = keys.next();
        // Authorization header might already be added because mobile security is enabled
        //        if (!key.equals(AUTH_HEADER_PARAM_NAME) || !added)
        //        {
        restService.addRequestProperty(key, headerParamMap.get(key));
        //        }
      }
    }
    restService.setRetryLimit(retryLimit);
    // if connection url end swith "/" and request uri also starts with "/" we need to remove this starting slash from
    // requestUri, otherwise the rest invocation will fail with an error
    String uri = connectionUrlEndsWithSlash(connectionName)? requestUri.substring(1): requestUri;
    boolean payloadSet = payload != null && !"".equals(payload.trim());
    uri = isGET? uri + (payloadSet? "?" + payload: ""): uri;
    restService.setRequestURI(uri);
      return restService;    
  }

  public byte[] invokeByteArrayRestService(String connectionName, String requestType, String requestUri, String payload,
                                  Map<String,String> headerParamMap, int retryLimit)
  {

    RestServiceAdapter restService = setupRestServiceCall(connectionName,  requestType,  requestUri,  payload,
                                   headerParamMap,  retryLimit);
    byte[] response = null;
    long startTime = System.currentTimeMillis();
    boolean isGET = "GET".equals(requestType);
    String uri = restService.getRequestURI();
    try
    {
      response = restService.sendReceive((isGET? null: payload));      
      logRestCall(connectionName,restService.getRequestType(),uri,restService.getRequestProperties().toString(),payload,"byte[]",startTime,null);
      setLastResponseHeaders(restService.getResponseHeaders());
      setLastResponseStatus(restService.getResponseStatus());
      return response;
    }
    catch (Exception e)
    {
      setLastResponseHeaders(restService.getResponseHeaders());
      setLastResponseStatus(restService.getResponseStatus());
      logRestCall(connectionName,restService.getRequestType(),uri,restService.getRequestProperties().toString(),payload,null,startTime,e);
      RestCallException re = new RestCallException(e);
      re.setRequestMethod(requestType);
      re.setRequestUri(uri);
      re.setResponseHeaders(restService.getResponseHeaders());
      re.setResponseStatus(restService.getResponseStatus());
      throw re;       
    }
  }

  /**
   * Show info dialog with duration of REST service call when showWebServiceTimings is set to true in
   * persistence-mapping.xml, or has been turned on at runtime by setting #{applicationScope.showWebServiceTimings}
   * to true.
   * Log web service call details
   * @param method
   * @param uri
   * @param payload
   * @param startTime
   * @param exception
   */
  protected void logRestCall(String connection, String method, String uri, String requestHeaders, String requestPayload, String responsePayload, long startTime,Exception exception)
  {
    long endTime = System.currentTimeMillis();
    long duration = endTime-startTime;
    if (ObjectPersistenceMapping.getInstance().isShowWebServiceTimings())
    {
      String request = method+" "+uri;
      MessageUtils.handleMessage("info", request+": "+duration+" ms");
    }  
    if (ObjectPersistenceMapping.getInstance().isLogWebServiceCalls())
    {
      WebServiceCall wscall = new WebServiceCall();
      wscall.setIsNewEntity(true);
      wscall.setConnection(connection);
      wscall.setDuration(duration);
      wscall.setMethod(method);
      wscall.setRequest(uri);
      wscall.setRequestHeaders(requestHeaders);
      wscall.setRequestPayload(requestPayload);
      wscall.setResponsePayload(responsePayload);
      wscall.setTimestamp(new Date());
      if (exception!=null)
      {
        String rootError = exception.getLocalizedMessage();
        String causeError = exception.getCause() != null? exception.getCause().getLocalizedMessage() : null;
        // the cause exception can have a null or "" message, in that case we throw the root exception message
        String error = (causeError==null || "".equals(causeError)) ? rootError : causeError;
        wscall.setErrorMessage(error);        
      }
      // Execute on separate thread and sequentially so we don;t run into issues with PK value already exist
      TaskExecutor.getDBInstance().execute(true, () ->
      {
        new WebServiceCallService(false).saveWebServiceCall(wscall);        
      });
    }  
  }
  
  /**
   * This method is called when the REST service call returns a status other than 200. The default 
   * implementation is to throw an exception so calling method can take appropriate action, for example
   * registering a pending data sync action. However, in some situations the returned status code might
   * indicate a succesfull call. To handle such a situation, you need to overwrite this method and handle
   * the reponse as needed.
   * @param requestType
   * @param uri
   * @param e
   * @return
   */
  protected String handleInvokeRestServiceError(String requestType, String uri, Exception exception)
  {
    Exception e = exception;
    if (exception instanceof RestCallException)
    {
      e = ((RestCallException)exception).getWrappedException();
    }
    String rootError = e.getLocalizedMessage();
    String causeError = e.getCause() != null? e.getCause().getLocalizedMessage() : null;
    // the cause exception can have a null or "" message, in that case we throw the root exception message
    String error = (causeError==null || "".equals(causeError)) ? rootError : rootError + " Details: "+causeError;
    String message = "Error invoking REST " + requestType + " service " + uri + " : " + error;
    sLog.severe(message);
    // throw exception so any failed data synch actions can be registsred and processed later
    throw new AdfException(message, AdfException.ERROR);    
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

  public String invokeRestService(Method method, Map<MethodParameter, String> paramValues)
  {
    // parameters with pathParam=true are included between curly brackets in uri, we need
    // to substitute the param with the actual value in that case
    // Non-uri parameters are sent as queryString
    // buld the payload string
    String uri = method.getRequestUri();
    StringBuffer payload = new StringBuffer("");
    StringBuffer queryString = new StringBuffer("");
    Iterator<MethodParameter> paramIter = paramValues.keySet().iterator();
    while (paramIter.hasNext())
    {
      MethodParameter param = paramIter.next();
      String paramName = param.getName();
      if (param.isPathParam())
      {
        // substitute in uri
        String oldValue = "{" + paramName + "}";
        String value = paramValues.get(param);
        String newValue = value != null? value : "";
        uri = StringUtils.substitute(uri, oldValue, newValue);
      }
      else
      {        
        if (paramName != null && !"".equals(paramName.trim()))
        {
          if (param.isSerializedDataObject())
          {
            if (!payload.toString().equals(""))
            {
              payload.append("&");              
            }
            payload.append(paramName + "=" + paramValues.get(param));            
          }
          else
          {
            if (!queryString.toString().equals(""))
            {
              queryString.append("&");              
            }
            queryString.append(paramName + "=" + paramValues.get(param));                        
          }
        }
        else
        {
          // with Non-GET requests, we can send a raw payload without param name
          payload.append(paramValues.get(param));
        }
      }
    }
    Map<String,String> headerParamMap = new HashMap<String,String>();
    List<MethodHeaderParameter> headerParams = method.getHeaderParams();
    // add OAuth header params if needed
    addOAuthHeaderParamsIfNeeded(method, headerParams);
    
    for (MethodHeaderParameter param : headerParams)
    {
      // value might be EL expression
      Object value = AdfmfJavaUtilities.evaluateELExpression(param.getValue());
      // header request prop must be string value
      String strValue = value!=null ? value.toString() : null;
      headerParamMap.put(param.getName(), strValue);
    }
    if (!queryString.toString().equals(""))
    {
      uri = uri+"?"+queryString.toString();
    }
    return invokeRestService(method.getConnectionName(), method.getRequestType(), uri, payload.toString(),
                             headerParamMap, 0, method.isSecured());
  }

  /**
   * Add OAuth header params if oauthCongif attribute is set on header parameter
   * @param method
   * @param headerParams
   */
  protected void addOAuthHeaderParamsIfNeeded(Method method, List<MethodHeaderParameter> headerParams)
  {
    String oauthConfigName = method.getOAuthConfigName();
    if (oauthConfigName!=null && !"".equals(oauthConfigName))
    {
      headerParams.addAll(new OAuthTokenManager().getOAuthHeaderParams(oauthConfigName));
    }
  }

  public void sendWriteRequest(Entity entity, Method method, String action)
  {
    sLog.fine("Executing sendRemoveRequest for class "+entity.getClass().getSimpleName()+" and method "+method.getName());
    Map<MethodParameter,String> paramValues = createParameterMap(entity, method, null, action);
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
    sLog.fine("Executing sendRemoveRequest for class "+entity.getClass().getSimpleName());
    sendWriteRequest(entity, method, ACTION_REMOVE);
  }

  /**
   * Invoke the getCanonical method to fully populate the entity with all attributes, and any
   * child entities.
   * @param entity
   */
  public void getCanonical(Entity entity)
  {
    sLog.fine("Executing getCanonical for class "+entity.getClass().getSimpleName());
    Class entityClass = entity.getClass();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    Method getCanonicalMethod = descriptor.getGetCanonicalMethod();
    if (getCanonicalMethod == null)
    {
      sLog.severe("No getCanonical method found for " + entityClass.getName());
    }
    Map<MethodParameter,String> paramValues = createParameterMap(entity, getCanonicalMethod, null, null);
    try
    {
      String restResponse = invokeRestService(getCanonicalMethod, paramValues);
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
   * Execute a custom method Rest resource. 
   * The Rest call is executed using the definition of the method 
   * in persistence-mapping.xml for the entity class that has the same name as the methodName argument
   * @param entity
   * @param methodName
   */
  public void invokeCustomMethod(Entity entity, String methodName)
  {
    sLog.fine("Executing invokeCustomMethod for class "+ entity.getClass().getSimpleName()+" and method "+methodName);
    Class entityClass = entity.getClass();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    Method customMethod = descriptor.getCustomMethod(methodName);
    if (customMethod == null)
    {
      sLog.severe("Custom method " + customMethod + " not found for " + entityClass.getName());
      return;
    }
    Map<MethodParameter,String> paramValues = createParameterMap(entity, customMethod, null, null);
    try
    {
      String restResponse = invokeRestService(customMethod, paramValues);
      if (restResponse != null)
      {
        if ("GET".equalsIgnoreCase(customMethod.getRequestType()))
        {
          handleReadResponse(restResponse, entityClass, customMethod.getPayloadElementName(), customMethod.getPayloadRowElementName(), null, customMethod.isDeleteLocalRows());
        }
        else
        {
          handleWriteResponse(entity, customMethod, null, restResponse);          
        }
      }
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(descriptor, e, true);
    }
  }

  /**
   * Execute findAll Rest resource. 
   * The Rest call is executed using the findAllMethod definition in persistence-mapping.xml for
   * the entity class
   * @param <E>
   * @param entityClass
   * @return
   */
  public <E extends Entity> List<E> findAll(Class entityClass)
  {
    sLog.fine("Executing findAll for class "+entityClass.getSimpleName());
    List<E> entities = new ArrayList<E>();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    Method findAllMethod = descriptor.getFindAllMethod();
    if (findAllMethod == null)
    {
      sLog.severe("No findAll method found for " + entityClass.getName());
      return entities;
    }
    Map<MethodParameter,String> paramValues = createParameterMap(null, findAllMethod, null, null);
    try
    {
      String restResponse = invokeRestService(findAllMethod, paramValues);
      if (restResponse != null)
      {
        DBPersistenceManager dbpm = getLocalPersistenceManager();
        if (descriptor.isPersisted() && findAllMethod.isDeleteLocalRows())
        {
          dbpm.deleteAllRows(entityClass);
        }
        entities =
          handleReadResponse(restResponse, entityClass, findAllMethod.getPayloadElementName(),
                             findAllMethod.getPayloadRowElementName(), null, findAllMethod.isDeleteLocalRows());

        // do COmmit is using WAL
        if (PersistenceConfig.useWAL() )
        {
          dbpm.commmit();
        }
        // only if an order-by statement is specified, we execute the find method against the local DB to reorder
        // the entity list. Note that the entity instances are not recreated because they will be retrieved from the cache
        if (descriptor.isPersisted() && descriptor.getOrderBy() != null)
        {
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

  /**
   * Execute findAllInParent Rest resource. 
   * The Rest call is executed using the findAllInParent definition in persistence-mapping.xml for
   * the entity class and specified accessorAttribute
   * @param <E>
   * @param childEntityClass
   * @param parent
   * @param accessorAttribute
   * @return
   */
  public <E extends Entity> List<E> findAllInParent(Class childEntityClass, Entity parent, String accessorAttribute)
  {
    sLog.fine("Executing findAllInParent for child class "+childEntityClass.getSimpleName()+ " and parent class "+parent.getClass().getSimpleName());
    List<E> entities = new ArrayList<E>();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(childEntityClass);
    Method findAllInParentMethod = descriptor.getFindAllInParentMethod(accessorAttribute);
    if (findAllInParentMethod == null)
    {
      sLog.severe("No findAllInParent method found for " + childEntityClass.getName());
      return entities;
    }
    Map<MethodParameter,String> paramValues = createParameterMap(parent, findAllInParentMethod, null, null);
    try
    {
      String restResponse = invokeRestService(findAllInParentMethod, paramValues);
      if (restResponse != null)
      {
        DBPersistenceManager dbpm = getLocalPersistenceManager();
        if (descriptor.isPersisted() && findAllInParentMethod.isDeleteLocalRows())
        {
          dbpm.deleteAllInParent(childEntityClass, parent, accessorAttribute);
        }
        List<BindParamInfo> parentBindParamInfos = getBindParamInfos(parent);
        entities =
          handleReadResponse(restResponse, childEntityClass, findAllInParentMethod.getPayloadElementName(),
                             findAllInParentMethod.getPayloadRowElementName(), parentBindParamInfos, false);
        // only if an order-by statement is specified, we execute the find method against the local DB to reorder
        // the entity list. Note that the entity instances are not recreated because they will be retrieved from the cache
        if (descriptor.isPersisted() && descriptor.getOrderBy() != null)
        {
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

  /**
   * Execute find Rest resource. 
   * The Rest call is executed using the findMethod definition in persistence-mapping.xml for
   * the entity class
   * @param <E>
   * @param entityClass
   * @param searchValue
   * @return
   */
  public <E extends Entity> List<E> find(Class entityClass, String searchValue)
  {
    sLog.fine("Executing find for class "+entityClass.getSimpleName());
    List<E> entities = new ArrayList<E>();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    Method findMethod = descriptor.getFindMethod();
    if (findMethod == null)
    {
      sLog.severe("No find method found for " + entityClass.getName());
      return entities;
    }
    // we don't know for which param the search value might be used, so we create a map
    // with all attrs as keys with this search value, so it can be applied to the proper
    // attr in createParameterMap
    Map<String,String> searchValues = new HashMap<String,String>();
    if (searchValue!=null)
    {
      List<MethodParameter> params = findMethod.getParams();
      for (MethodParameter param : params)
      {
        searchValues.put(param.getName(),searchValue);
      }      
    }
    Map<MethodParameter,String> paramValues = createParameterMap(null, findMethod, searchValues, null);
    try
    {
      String restResponse = invokeRestService(findMethod, paramValues);
      if (restResponse != null)
      {
        DBPersistenceManager dbpm = getLocalPersistenceManager();
        entities =
          handleReadResponse(restResponse, entityClass, findMethod.getPayloadElementName(),
                             findMethod.getPayloadRowElementName(), null, findMethod.isDeleteLocalRows());

        // do Commit is using WAL
        if (PersistenceConfig.useWAL() )
        {
          dbpm.commmit();
        }
        // only if an order-by statement is specified, we execute the find method against the local DB to reorder
        // the entity list. Note that the entity instances are not recreated because they will be retrieved from the cache
        if (descriptor.isPersisted() && descriptor.getOrderBy() != null)
        {
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

  /**
   * This method is not implemented, it returns an empty list.
   * If your Rest service support search by attribute names, than make a subclass and override this
   * method to call your Rest service find method.
   * @param <E>
   * @param entityClass
   * @param searchValue
   * @param attrNamesToSearch
   * @return
   */
  public <E extends Entity> List<E> find(Class entityClass, String searchValue, List<String> attrNamesToSearch)
  {
    sLog.fine("Executing find for class "+entityClass.getSimpleName());
    return Collections.EMPTY_LIST;
  }

  public Map<MethodParameter,String> createParameterMap(Entity entity, Method method, Map<String,String> searchValues, String action)
  {
    sLog.fine("Executing createParameterMap for method "+method.getName());
    List<MethodParameter> params = method.getParams();
    Map<MethodParameter,String> paramValues = new HashMap<MethodParameter,String>();
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
    for (MethodParameter param : params)
    {
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
          paramValues.put(param, value.toString());
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
          paramValues.put(param, value.toString());
        }
      }
      else if (param.isSearchValue() && searchValues != null)
      {
        Object value = searchValues.get(param.getName());
        if (value != null)
        {
          paramValues.put(param, value.toString());
        }
      }
    }
    return paramValues;
  }

  public Entity getAsParent(Class parentEtityClass, Entity child, String accessorAttribute)
  {
    sLog.fine("Executing getAsParent for parent class "+parentEtityClass.getSimpleName()+ " and child class "+child.getClass().getSimpleName());
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(parentEtityClass);
    Method getAsParentMethod = descriptor.getGetAsParentMethod(accessorAttribute);
    if (getAsParentMethod == null)
    {
      sLog.severe("No getAsParent method found for " + parentEtityClass.getName());
      return null;
    }
    Map<MethodParameter,String> paramValues = createParameterMap(child, getAsParentMethod, null, null);
    try
    {
      String restResponse = invokeRestService(getAsParentMethod, paramValues);
      if (restResponse != null)
      {
        // only retrieve key values, otherwise we can get an endless loop because a getter method for parent attributes
        // typically causes this method to be called
        List<BindParamInfo> parentBindParamInfos = getBindParamInfos(child, true);
        List<Entity> entities =
          handleReadResponse(restResponse, parentEtityClass, getAsParentMethod.getPayloadElementName(),
                             getAsParentMethod.getPayloadRowElementName(), parentBindParamInfos, false);
        if (entities.size() > 0)
        {
          Entity parent = entities.get(0);
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

  public void setLastResponseHeaders(Map lastResponseHeaders)
  {
    this.lastResponseHeaders = lastResponseHeaders;
  }

  public Map getLastResponseHeaders()
  {
    return lastResponseHeaders;
  }


  public void setLastResponseStatus(int lastResponseStatus)
  {
    this.lastResponseStatus = lastResponseStatus;
  }

  public int getLastResponseStatus()
  {
    return lastResponseStatus;
  }

  protected abstract String getSerializedDataObject(Entity entity, String collectionElementName, String rowElementName,
                                                    boolean deleteRow);

  protected abstract String getSerializedDataObject(Entity entity, String collectionElementName, String rowElementName,
                                                    List<String> attributesToExclude, boolean deleteRow);

  protected abstract <E extends Entity> List<E> handleReadResponse(String restResponse, Class entityClass, String collectionElementName,
                                             String rowElementName, List<BindParamInfo> parentBindParamInfos,
                                             boolean deleteLocalRows);

  protected abstract <E extends Entity> List<E> handleResponse(String restResponse, Class entityClass, String collectionElementName,
                                             String rowElementName, List<BindParamInfo> parentBindParamInfos, E currentEntity,
                                             boolean deleteLocalRows);
}
