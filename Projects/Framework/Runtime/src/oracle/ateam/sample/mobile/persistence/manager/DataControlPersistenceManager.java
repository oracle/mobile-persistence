/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 18-mar-2015   Steven Davelaar
 1.2           Added method doSoapCallTimings
 18-jun-2014   Steven Davelaar
 1.1           Fix for change in how invokeDataControlMethod returns the data!
 07-jan-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.adfmf.dc.ws.soap.SoapGenericType;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.util.AttributeInfo;
import oracle.adfmf.util.GenericType;

import oracle.ateam.sample.mobile.persistence.db.BindParamInfo;
import oracle.ateam.sample.mobile.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.persistence.metadata.AttributeMappingOneToMany;
import oracle.ateam.sample.mobile.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.persistence.metadata.Method;
import oracle.ateam.sample.mobile.persistence.metadata.MethodParameter;
import oracle.ateam.sample.mobile.persistence.metadata.ObjectPersistenceMapping;
import oracle.ateam.sample.mobile.persistence.model.Entity;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;


/**
 * Implementation of persistence manager interface that provides basic CRUD operations using
 * a web service data control. This class uses the AdfmfJavaUtilities.invokeDataControlMethod method to
 * invoke the web service. To be able to use this class, you first need to run the Web Service Data Control wizard
 * in JDeveloper, even if you do not intend to do drag-and-drop actions using the web service data control.
 * You also need to make sure that an instance of the data control is defined in the dataControlUsages
 * section in DataBindings.cpx. You can add this usage manually, or do a (dummy) drag and drop action from
 * the data control palette which will automatically add such usage:
 * &lt;pre&gt;
 *  &lt;dataControlUsages&gt;
 *   &lt;dc id="HRServiceSOAP" path="mobile.HRServiceSOAP"/&gt;
 * &lt;/dataControlUsages&gt;
 * </pre>
 * To customize the behavior of this class, you can create a subclass and register this subclass in
 * the corresponding ClassMappingDescriptor in persistenceMapping.xml:
 * <pre>
 *   &lt;remote-persistence-manager&gt;demo.model.service.MyDataControlPersistenceManager&lt;/remote-persistence-manager&gt;
 * </pre>
 * 
 * @deprecated Use the class with same name in oracle.ateam.sample.mobile.v2.persistence.* instead
 */
public class DataControlPersistenceManager
  extends AbstractRemotePersistenceManager
{

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(DataControlPersistenceManager.class);

  /**
   * Constructor 
   */
  public DataControlPersistenceManager()
  {
    super();
  }

  /**
   * Do web service call that reads all data for the entity type passed in.
   * This method uses the information defined in the &lt;find-all&gt; method in
   * the persistenceMapping.xml to construct the payload and invoke the
   * web service. To customize the way the web service parameters are set up, you can override
   * method createMethodArguments.
   * @param entityClass
   * @return
   */
  public List findAll(Class entityClass)
  {
    sLog.fine("Executing findAll for entity "+entityClass.getName());
    List entities = new ArrayList();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    Method method = descriptor.getFindAllMethod();
    if (method ==null)
    {
      return entities;
    }
    long startTime = System.currentTimeMillis();
    try
    {
      List paramNames = new ArrayList();
      List paramValues = new ArrayList();
      List paramTypes = new ArrayList();
      
      createMethodArguments(null, method, paramNames, paramValues, paramTypes,null);

        GenericType result =
          (GenericType) AdfmfJavaUtilities.invokeDataControlMethod(method.getDataControlName(), null, method.getName(),
                                                                   paramNames, paramValues, paramTypes);
      doSoapCallTimings(method.getDataControlName(),method.getName(),startTime,null);
        // result returns first row, NOT the collection since M15!
        // Need to get to the parent to get the collection!!!
        result = result!=null && result.getParent()!=null ? result.getParent() : result;
      entities = handleReadResponse(result, entityClass, method.getPayloadElementName(), null,descriptor.isDeleteLocalRowsOnFindAll());
      // only if an order-by statement is specified, we execute the find method against the local DB to reorder
      // the entity list. Note that the entity instances are not recreated because they will be retrieved from the cache
      if (descriptor.isPersisted() && descriptor.getOrderBy()!=null)
      {
        DBPersistenceManager dbpm = getLocalPersistenceManager();
        entities = dbpm.findAll(entityClass);      
      }
      return entities;
    }
    catch (Exception e)
    {
      doSoapCallTimings(method.getDataControlName(),method.getName(),startTime,e);
      handleWebServiceInvocationError(descriptor, e, false);      
      return null;
    }
  }

  /**
   * Show info dialog with duration of SOAP service call when showWebServiceTimings is set to true in
   * persistence-mapping.xml, or has been turned on at runtime by setting #{applicationScope.showWebServiceTimings}
   * to true.
   * @param dataControlName
   * @param methodName
   * @param startTime
   * @param exception
   */
  protected void doSoapCallTimings(String dataControlName, String methodName, long startTime, Exception exception)
  {
    long endTime = System.currentTimeMillis();
    if (ObjectPersistenceMapping.getInstance().isShowWebServiceTimings())
    {
      long duration = endTime-startTime;
      MessageUtils.handleMessage("info", dataControlName+"."+methodName+": "+duration+" ms");
    }  
  }

  /**
   * Performs actual web service call using the method information passed in.
   * The payload returned by the web service call is processed in method
   * handleWriteResponse. Overwrite that method if you need to plug in custom
   * logic to process the response.
   * @param entity
   * @param method
   */
  public void sendWriteRequest(Entity entity, Method method, String action)
  {
    sLog.fine("Executing sendWriteRequest for entity "+entity.getClass().getName()+", method "+method.getName());
    try
    {
      List paramNames = new ArrayList();
      List paramValues = new ArrayList();
      List paramTypes = new ArrayList();
      
      createMethodArguments(entity, method, paramNames, paramValues, paramTypes,null);
      GenericType result =
        (GenericType) AdfmfJavaUtilities.invokeDataControlMethod(method.getDataControlName(), null, method.getName(),
                                                                        paramNames, paramValues, paramTypes);  
      // result returns first row, NOT the collection since M15!
      // Need to get to the parent to get the collection!!!
      result = result!=null && result.getParent()!=null ? result.getParent() : result;
      handleWriteResponse(entity, method, action, result);
    }
    catch (Exception e)
    {
      // throw exception so synch action can be registered to be processed for later
      ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
      handleWebServiceInvocationError(descriptor, e, true);
    }    
  }

  public void createMethodArguments(Entity entity, Method method, List paramNames, List paramValues,
                                     List paramTypes, Map searchValues)
  {
    List params = method.getParams();
    for (int i = 0; i < params.size(); i++)
    {
      MethodParameter param = (MethodParameter) params.get(i);
      if (param.isSerializedDataObject() && entity!=null)
      {
        GenericType genericType = convertToGenericType(entity,param.getName());
        paramNames.add(param.getName());
        paramValues.add(genericType);
        paramTypes.add(GenericType.class);          
      }
      else if (param.isDataObjectAttribute() && param.getDataObjectAttribute()!=null && entity!=null)
      {
        Object value = entity.getAttributeValue(param.getDataObjectAttribute());
        if (value!=null)
        {
          paramNames.add(param.getName());
          paramValues.add(value);
          paramTypes.add(param.getJavaTypeClass());                      
        }
      }
      else if (param.isLiteralValue() && param.getValue()!=null)
      {
        paramNames.add(param.getName());
        paramValues.add(param.getValue());
        paramTypes.add(param.getJavaTypeClass());                      
      }
      else if (param.isELExpression() && param.getValue()!=null)
      {
        Object value = AdfmfJavaUtilities.evaluateELExpression(param.getValue());
        if (value!=null)
        {
          paramNames.add(param.getName());
          paramValues.add(value);
          paramTypes.add(param.getJavaTypeClass());                                
        }
      }
      else if (param.isSearchValue() && searchValues!=null)
      {
        Object value = searchValues.get(param.getName());
        if (value!=null)
        {
          paramNames.add(param.getName());
          paramValues.add(value);
          paramTypes.add(param.getJavaTypeClass());                                
        }
      }
    }
  }

  /**
   * Helper method that converts an entity into a generic type that can be used as payload in web service invocation
   * using AdfmfJavaUtilities.invokeDataControlMethod
   * @param entity
   * @return
   */
  public GenericType convertToGenericType(Entity entity, String entityArgumentName)
  {
    SoapGenericType depSoapType = new SoapGenericType(null,entityArgumentName);
    String entityClass = entity.getClass().getName();
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = mapping.findClassMappingDescriptor(entityClass);
    List attributeMappings = descriptor.getAttributeMappingsDirect();
    for (int i = 0; i < attributeMappings.size(); i++)
    {
      AttributeMapping attrMapping = (AttributeMapping) attributeMappings.get(i);
      if (attrMapping.getAttributeNameInPayload()!=null)
      {
        Object value =entity.getAttributeValue(attrMapping.getAttributeName());
        String payloadAttr = attrMapping.getAttributeNameInPayload();
        Class type = value!=null ? value.getClass() : String.class;
        depSoapType.defineAttribute(null,payloadAttr,type, value);                      
      }
    }
    addChildGenericTypes(depSoapType,entity);
    return depSoapType;
  }
  
  public void addChildGenericTypes(SoapGenericType parentType, Entity parentEntity)
  {
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    String entityClass = parentEntity.getClass().getName();
    ClassMappingDescriptor descriptor = mapping.findClassMappingDescriptor(entityClass);
    List attributeMappings = descriptor.getAttributeMappingsOneToMany();
    for (int i = 0; i < attributeMappings.size(); i++)
    {
      AttributeMappingOneToMany attrMapping = (AttributeMappingOneToMany) attributeMappings.get(i);
      List children = (List) parentEntity.getAttributeValue(attrMapping.getAttributeName());
      for (int j = 0; j < children.size(); j++)
      {
        Entity child = (Entity) children.get(j);
        String payloadAttr = attrMapping.getAttributeNameInPayload();
        GenericType childType = convertToGenericType(child,payloadAttr);
        parentType.defineAttribute(null,payloadAttr,GenericType.class, childType);              
      }
    }
  }

  /**
   * Helper method that inserts the data of an entity type that is stored in a GenericType instance into
   * the database. 
   * @param result
   * @param entityClass
   * @param deleteAllRows
   */
  protected List handleReadResponse(GenericType result, Class entityClass, String payloadElementName, List parentBindParamInfos, boolean deleteAllRows)
  {
    List entities = new ArrayList();      
    try
    {
      if (deleteAllRows)
      {
        getLocalPersistenceManager().deleteAllRows(entityClass);      
      }
      findAndProcessPayloadElements(payloadElementName, result, entityClass, parentBindParamInfos,entities,null);
    }
    catch (Exception e)
    {
      //     Throw exception instead of call to MessageUtils, this allows us to catch the exception in calling method
      //       and to decide there where user should see error message
      //      MessageUtils.handleError(e.getLocalizedMessage());
      throw new AdfException(e);
    }
    return entities;
  }


  protected void findAndProcessPayloadElements(String elementName, GenericType result, Class entityClass, List parentBindParamInfos, List entities, Entity currentEntity)
  {
    for (int i = 0; i < result.getAttributeCount(); i++)
    {
      // Get each individual GenericType instance that holds the attribute key-value pairs of our entity
      Object resultObject = result.getAttribute(i);
      if (!(resultObject instanceof GenericType))
      {
//        break;
        // do continue instead of break, there might be a sibling elemnt that contains the payload.
        continue;
      }
      GenericType payloadGenericType = (GenericType) resultObject;
      // with ADF BC SDO services, there is no "real" element name, the top level element returned is named something
      // like "findDepartmentResponse" but in the DC palette it shows up as "result".
      if (payloadGenericType.getName().equals(elementName) || elementName==null || "result".equalsIgnoreCase(elementName))
      {
        Entity entity = processPayloadElement(payloadGenericType,entityClass,parentBindParamInfos,currentEntity);        
        if (entity!=null && !entities.contains(entity))
        {
          entities.add(entity);
        }
      }
      else
      {
        // actual element is contained by some container element, make recursive call to "unwrap"
        findAndProcessPayloadElements(elementName,payloadGenericType,entityClass, parentBindParamInfos, entities,currentEntity );
      }
    }
  }

  /**
   * This methods inserts/updates a row in SQLite database and/or creates a new entity instance from the genericType passed in.
   * @param payloadGenericType
   * @param entityClass
   * @param dbpm
   * @param parentBindParamInfos
   * @param currentEntity only has a value when this method called from handleWriteResponse 
   * @return
   */
  protected Entity processPayloadElement(GenericType payloadGenericType,Class entityClass, List parentBindParamInfos, Entity currentEntity)
  {
    List bindParamInfos = new ArrayList();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);    
    int attrCount = payloadGenericType.getAttributeInfoCount();
    // map contains mappign as key, and a list of instances as value
    Map oneToManyMappings = new HashMap();
    List attrMappings = descriptor.getAttributeMappings();
    // we need to loop over attr infos, we cannot loop over mappings and then get attr info, because
    // for 1-to-many mappings, each child type is returned as an attribute with the name of the child collection.
    // In other words: there can be multiple attrs with the same name!

    for (int j = 0; j < payloadGenericType.getAttributeInfoCount(); j++)
    {
      AttributeInfo attrInfo = payloadGenericType.getAttributeInfo(j);
      AttributeMapping mapping = (AttributeMapping) descriptor.findAttributeMappingByPayloadName(attrInfo.name);
      if (mapping == null)
      {
        sLog.warning(entityClass.getName() + ": no mapping found for payload attribute " +
                     attrInfo.name);
        continue;
      }
      Object rawValue = payloadGenericType.getAttribute(j);
//      checkRequired(mapping, rawValue);
      if (mapping.isOneToManyMapping())
      {
        List children = (List) oneToManyMappings.get(mapping);
        if (children==null)
        {
          children = new ArrayList();
          oneToManyMappings.put(mapping,children);
        }
        if (rawValue!=null)
        {
          children.add(rawValue);          
        }
        continue;
      }
      BindParamInfo bpInfo = createBindParamInfoFromPayloadAttribute(entityClass, mapping, rawValue);
      if (bpInfo!=null)
      {
        bindParamInfos.add(bpInfo);        
      }
    }
    // loop over parent-populated attributes without payload element and populate with corresponding parent attribute
    // if available in parent bindParamInfos
    addParentPopulatedBindParamInfos(entityClass, parentBindParamInfos, bindParamInfos, descriptor);

    if (descriptor.isPersisted() && !isAllPrimaryKeyBindParamInfosPopulated(descriptor,bindParamInfos))
    {
      // we cannot insert a row in SQLite when a PK value is null, so skip this row
      return null;
    }

    // get the primary key, and check the cache for existing entity instance with this key
    // if it exists, update this instance which is then always the same as currentEntity instance
    // otherwise, when currentEntity is not null, this means the PK has changed.
    Entity entity = createOrUpdateEntityInstance(entityClass, bindParamInfos, currentEntity);
    
    // insert or update the row in the database.
    if (descriptor.isPersisted())
    {
      DBPersistenceManager dbpm = getLocalPersistenceManager();
      dbpm.mergeRow(bindParamInfos, true);      
    }
    
    // loop over one-to-many mappings to do recursive call to process child entities
    // And pass in the parent bindParamInfos, because the child payload might not contain the attribute
    // referencing the parent as it is already sent in hierarchical format in the payload 
    Iterator mappings = oneToManyMappings.keySet().iterator();
    while (mappings.hasNext())
    {
      AttributeMappingOneToMany mapping = (AttributeMappingOneToMany) mappings.next(); 
      Class refClass = mapping.getReferenceClassMappingDescriptor().getClazz();
      List children = (List) oneToManyMappings.get(mapping);
      List childEntities = new ArrayList();
      List currentChildEntities = null;
      if (currentEntity!=null)
      {
        currentChildEntities = (List) currentEntity.getAttributeValue(mapping.getAttributeName());
        if (currentChildEntities.size()!=children.size())
        {
          // this should never happen, because current entity child list is send as payload for write action
          // and the number of rows returned by ws call should be the same, if it is not, we can no longer match
          // entities by index and we dont pass in currentChildEntity
          currentChildEntities = null;
        }
      }
      for (int i = 0; i < children.size(); i++)
      {
        Object rawValue = children.get(i);
        if (rawValue instanceof GenericType)
        {
          // recursive call to populate DB with child entity row. Note that
          // multiple child rows are NOT wrapped in own GenericType, instead each
          // child instance is just an additional attribute of type GenericType
          Entity currentChildEntity = (Entity) (currentChildEntities!=null ? currentChildEntities.get(i) : null);
          Entity childEntity = processPayloadElement((GenericType) rawValue, refClass, bindParamInfos,currentChildEntity);
          childEntities.add(childEntity);
        }              
      }
      if (childEntities.size()>0)
      {
        entity.setAttributeValue(mapping.getAttributeName(), childEntities);

      }
    }
    return entity;
  }


  public List find(Class entityClass, String searchValue)
  {
    List entities = new ArrayList();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    Method method = descriptor.getFindMethod();
    if (method ==null)
    {
      return entities;
    }
    // we don;t know for which param the seaarch value might be used, so we create a map
    // with all attrs as keys with this search value, so it can be applied to the proper
    // attr in createMethodArguments
    Map searchValues = new HashMap();
    if (searchValue!=null)
    {
      List params = method.getParams();
      for (int i = 0; i < params.size(); i++)
      {
        MethodParameter param = (MethodParameter) params.get(i);
        searchValues.put(param.getName(),searchValue);
      }      
    }
    List paramNames = new ArrayList();
    List paramValues = new ArrayList();
    List paramTypes = new ArrayList();
    
    createMethodArguments(null, method, paramNames, paramValues, paramTypes,searchValues);

    GenericType result;

    try
    {
      result =
          (GenericType) AdfmfJavaUtilities.invokeDataControlMethod(method.getDataControlName(), null, method.getName(),
                                                               paramNames, paramValues, paramTypes);
      // result returns first row, NOT the collection since M15!
      // Need to get to the parent to get the collection!!!
      result = result!=null && result.getParent()!=null ? result.getParent() : result;
      entities = handleReadResponse(result, entityClass, method.getPayloadElementName(), null,false);
      // only if an order-by statement is specified, we execute the find method against the local DB to reorder
      // the entity list. Note that the entity instances are not recreated because they will be retrieved from the cache
      if (descriptor.isPersisted() && descriptor.getOrderBy()!=null)
      {
        DBPersistenceManager dbpm = getLocalPersistenceManager();
        entities = dbpm.find(entityClass,searchValue);              
      }
      return entities;
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(descriptor, e, false);      
      return null;
    }
  }

  public List find(Class entityClass, String searchValue, List attrNamesToSearch)
  {
    return Collections.EMPTY_LIST;
  }

//  protected GenericType createFindCriteriaGenericType(Map attributeValuePairs, boolean casesInsensitive)
//  {
//    Boolean upperCaseCompare = casesInsensitive ? Boolean.TRUE : Boolean.FALSE;
//    SoapGenericType findCriteria = new SoapGenericType(null,"findCriteria");
//    SoapGenericType filter = new SoapGenericType(null,"filter");
//    findCriteria.defineAttribute(null,"filter",GenericType.class, filter);
//    SoapGenericType group = new SoapGenericType(null,"group");
//    filter.defineAttribute(null,"group",GenericType.class, group);
//    SoapGenericType item = new SoapGenericType(null,"item");
//    //    group.defineAttribute(null, "upperCaseCompare", Boolean.class,Boolean.FALSE);
//    group.defineAttribute(null,"item",GenericType.class, item);
//    item.defineAttribute(null, "attribute", String.class,"DepartmentName");    
//    item.defineAttribute(null, "operator", String.class,"like");    
//    item.defineAttribute(null, "value", String.class,"A%");    
//    // upperCaseCompare defaults to false, so case sensitive
//    item.defineAttribute(null, "upperCaseCompare", Boolean.class,upperCaseCompare);
//    return findCriteria;
//  }

  public Object getMaxValue(Class entityClass, String attrName)
  {
    return null;
  }

  public void commmit()
  {
  }

  public void rollback()
  {
  }

  public List findAllInParent(Class childEntityClass, Entity parent, String accessorAttribute)
  {
    List entities = new ArrayList();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(childEntityClass);
    Method method = descriptor.getFindAllInParentMethod(accessorAttribute);
    if (method ==null)
    {
      return entities;
    }
    try
    {
      List paramNames = new ArrayList();
      List paramValues = new ArrayList();
      List paramTypes = new ArrayList();
      
      createMethodArguments(parent, method, paramNames, paramValues, paramTypes,null);

      GenericType result =
        (GenericType) AdfmfJavaUtilities.invokeDataControlMethod(method.getDataControlName(), null, method.getName(),
                                                                 paramNames, paramValues, paramTypes);

      // result returns first row, NOT the collection since M15!
      // Need to get to the parent to get the collection!!!
      result = result!=null && result.getParent()!=null ? result.getParent() : result;
      List parentBindParamInfos = getBindParamInfos(parent);
      entities = handleReadResponse(result, childEntityClass, method.getPayloadElementName(), parentBindParamInfos, false);
      // only if an order-by statement is specified, we execute the find method against the local DB to reorder
      // the entity list. Note that the entity instances are not recreated because they will be retrieved from the cache
      if (descriptor.isPersisted() && descriptor.getOrderBy()!=null)
      {
        DBPersistenceManager dbpm = getLocalPersistenceManager();
        entities = dbpm.findAllInParent(childEntityClass,parent, accessorAttribute);      
      }
      return entities;   
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(descriptor, e, false);      
      return null;
    }
  }

  public void sendRemoveRequest(Entity entity, Method method)
  {
    sendWriteRequest(entity, method,ACTION_REMOVE);
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
  protected void handleWriteResponse(Entity entity, Method method, String action, GenericType response)
  {
    if (!ACTION_REMOVE.equals(action) && response!=null)
    {
      try
      {
        findAndProcessPayloadElements(method.getPayloadElementName(), response, entity.getClass(), null, new ArrayList(), entity);
          // processPayloadElement(payloadGenericType, entityClass, dbpm, parentBindParamInfos, currentEntity) handleReadResponse(response, entity.getClass(), method.getPayloadElementName(), null, false);      
      }
      catch (Exception e)
      {
        // do nothing response format of different, unexpected format
      }
    }
  }


  public Entity getAsParent(Class parentEntityClass, Entity child, String accessorAttribute)
  {
    List entities = new ArrayList();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(parentEntityClass);
    Method method = descriptor.getGetAsParentMethod(accessorAttribute);
    if (method ==null)
    {
      sLog.severe("No getAsParent method found for "+parentEntityClass.getName());
      return null;
    }
    List paramNames = new ArrayList();
    List paramValues = new ArrayList();
    List paramTypes = new ArrayList();    
    createMethodArguments(child, method, paramNames, paramValues, paramTypes,null);
    try
    {
      GenericType result =
        (GenericType) AdfmfJavaUtilities.invokeDataControlMethod(method.getDataControlName(), null, method.getName(),
                                                                                paramNames, paramValues, paramTypes);
      // result returns first row, NOT the collection since M15!
      // Need to get to the parent to get the collection!!!
      result = result!=null && result.getParent()!=null ? result.getParent() : result;
      List parentBindParamInfos = getBindParamInfos(child);
      entities = handleReadResponse(result, parentEntityClass, method.getPayloadElementName(),parentBindParamInfos,false);
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(descriptor, e, false);      
    }
    return (Entity) (entities.size()>0 ? entities.get(0) : null);
  }

  public void getCanonical(Entity entity)
  {
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
    Method method = descriptor.getGetCanonicalMethod();
    if (method ==null)
    {
      sLog.severe("No getCanonical method found for "+entity.getClass().getName());
      return;
    }
    List paramNames = new ArrayList();
    List paramValues = new ArrayList();
    List paramTypes = new ArrayList();    
    createMethodArguments(entity, method, paramNames, paramValues, paramTypes,null);
    try
    {
      GenericType result =
        (GenericType) AdfmfJavaUtilities.invokeDataControlMethod(method.getDataControlName(), null, method.getName(),
                                                                                paramNames, paramValues, paramTypes);
      // result returns first row, NOT the collection since M15!
      // Need to get to the parent to get the collection!!!
      result = result!=null && result.getParent()!=null ? result.getParent() : result;
      List parentBindParamInfos = getBindParamInfos(entity);
      List entities =handleReadResponse(result, entity.getClass(), method.getPayloadElementName(),parentBindParamInfos,false);
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(descriptor, e, false);      
    }
  }

  public void invokeCustomMethod(Entity entity, String methodName)
  {
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
    Method method = descriptor.getCustomMethod(methodName);
    if (method ==null)
    {
      sLog.severe("Custom method "+methodName+" not found for "+entity.getClass().getName());
      return;
    }
    List paramNames = new ArrayList();
    List paramValues = new ArrayList();
    List paramTypes = new ArrayList();    
    createMethodArguments(entity, method, paramNames, paramValues, paramTypes,null);
    try
    {
      GenericType result =
        (GenericType) AdfmfJavaUtilities.invokeDataControlMethod(method.getDataControlName(), null, method.getName(),
                                                                                paramNames, paramValues, paramTypes);
      // result returns first row, NOT the collection since M15!
      // Need to get to the parent to get the collection!!!
      result = result!=null && result.getParent()!=null ? result.getParent() : result;
      handleWriteResponse(entity, method,null, result);       
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(descriptor, e, true);
    }
  }

}
