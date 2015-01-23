 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
  
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.manager;
import java.util.List;

import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.json.JSONObject;

import oracle.adfmf.util.Utility;
import oracle.adfmf.util.XmlAnyDefinition;

import oracle.ateam.sample.mobile.v2.persistence.cache.EntityCache;
import oracle.ateam.sample.mobile.v2.persistence.db.BindParamInfo;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingDirect;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.Method;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;

 /**
 * Abstract class that provides generic implementation of some of the methods of the
 * RemotePersistenceManager interface that can be used by concrete subclasses.
 */
public abstract class AbstractRemotePersistenceManager
  extends AbstractPersistenceManager
  implements RemotePersistenceManager
{

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(AbstractRemotePersistenceManager.class);
  public static final String ACTION_INSERT = "insert";
  public static final String ACTION_UPDATE = "update";
  public static final String ACTION_MERGE = "merge";
  public static final String ACTION_REMOVE = "remove";

  DBPersistenceManager localPersistenceManager = new DBPersistenceManager();
  
  public AbstractRemotePersistenceManager()
  {
    super();
  }

  /**
   * This method returns true when the create-method is specified in the corresponding
   * ClassMappingDescriptor in the persistenceMapping.xml file
   */
  public boolean isCreateSupported(Class clazz)
  {
    return ClassMappingDescriptor.getInstance(clazz).isCreateSupported();
  }

  /**
   * This method returns true when the update-method is specified in the corresponding
   * ClassMappingDescriptor in the persistenceMapping.xml file
   */
  public boolean isUpdateSupported(Class clazz)
  {
    return ClassMappingDescriptor.getInstance(clazz).isUpdateSupported();
  }

  /**
   * This method returns true when the merge-method is specified in the corresponding
   * ClassMappingDescriptor in the persistenceMapping.xml file
   */
  public boolean isMergeSupported(Class clazz)
  {
    return ClassMappingDescriptor.getInstance(clazz).isMergeSupported();
  }

  /**
   * This method returns true when the remove-method is specified in the corresponding
   * ClassMappingDescriptor in the persistenceMapping.xml file
   */
  public boolean isRemoveSupported(Class clazz)
  {
    return ClassMappingDescriptor.getInstance(clazz).isRemoveSupported();
  }

  /**
   * This method returns true when the find-all-method is specified in the corresponding
   * ClassMappingDescriptor in the persistenceMapping.xml file
   */
  public boolean isFindAllSupported(Class clazz)
  {
    return ClassMappingDescriptor.getInstance(clazz).isFindAllSupported();
  }

  /**
   * This method returns true when the find-all-in-parent-method is specified in the corresponding
   * ClassMappingDescriptor in the persistenceMapping.xml file
   */
  public boolean isFindAllInParentSupported(Class clazz, String accessorAttribute)
  {
    return ClassMappingDescriptor.getInstance(clazz).isFindAllInParentSupported(accessorAttribute);
  }

  /**
   * This method returns true when the get-as-parent-method is specified in the corresponding
   * ClassMappingDescriptor in the persistenceMapping.xml file
   */
  public boolean isGetAsParentSupported(Class clazz, String accessorAttribute)
  {
    return ClassMappingDescriptor.getInstance(clazz).isGetAsParentSupported(accessorAttribute);
  }

  /**
   * This method returns true when the find-method is specified in the corresponding
   * ClassMappingDescriptor in the persistenceMapping.xml file
   */
  public boolean isFindSupported(Class clazz)
  {
    return ClassMappingDescriptor.getInstance(clazz).isFindSupported();
  }

  /**
   * Do web service call that inserts or updates data of the entity passed in.
   * This method calls sendWriteRequest method.
   * @param entity
   * @param doCommit
   */
  public void mergeEntity(Entity entity, boolean doCommit)
  {
    sendWriteRequest(entity, ClassMappingDescriptor.getInstance(entity.getClass()).getMergeMethod(),ACTION_MERGE);
  }

  /**
   * Do web service call that inserts data of the entity passed in.
   * This method calls sendWriteRequest method.  If a create-method is specified in 
   * peristyenceMapping.xml, this method will be used, otherwise the merge-method will be used
   * @param entity
   * @param doCommit
   */
  public void insertEntity(Entity entity, boolean doCommit)
  {
    // there might be only a merge method, not an insert method
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
    Method method =
      descriptor.getCreateMethod() != null? descriptor.getCreateMethod(): descriptor.getMergeMethod();
    sendWriteRequest(entity, method,ACTION_INSERT);
  }

  /**
   * Do web service call that updates data of the entity passed in.
   * This method calls sendWriteRequest method. 
   * @param entity
   * @param doCommit
   */
  public void updateEntity(Entity entity, boolean doCommit)
  {
    // do merge action instead of update if possible. A merge action at ADF BC SDO service
    // can also insert new children, an update cannot.
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
    Method method =
      descriptor.isMergeSupported()? descriptor.getMergeMethod(): descriptor.getUpdateMethod();
    sendWriteRequest(entity, method,ACTION_UPDATE);
  }

  public void removeEntity(Entity entity, boolean doCommit)
  {
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
    sendRemoveRequest(entity, descriptor.getRemoveMethod());
  }

  public abstract void sendWriteRequest(Entity entity, Method method, String action);

  public abstract void sendRemoveRequest(Entity entity, Method method);

  /**
   * This method creates BindParamInfo instances that populate attributes in a child entity that have been added to set up a parent-child relationship
   * between two entities. The parentAttribute element in persistence-mapping.xml defines the parent entity attribute from
   * which the value should be obtained and applied to the child attribute.
   * @param entityClass
   * @param parentBindParamInfos
   * @param bindParamInfos
   * @param descriptor
   */
  protected void addParentPopulatedBindParamInfos(Class entityClass, List<BindParamInfo> parentBindParamInfos, List<BindParamInfo> bindParamInfos,
                                                  ClassMappingDescriptor descriptor)
  {
    if (parentBindParamInfos != null)
    {
      List<AttributeMappingDirect> mappings = descriptor.getAttributeMappingsDirectParentPopulated();
      for (AttributeMappingDirect mapping : mappings)
      {
        String parentAttr = mapping.getParentAttribute();
        // find corresponding parent bind param info
        BindParamInfo parentBindParamInfo = null;
        for (BindParamInfo bpInfo : parentBindParamInfos)
        {
          if (bpInfo.getAttributeName().equals(parentAttr))
          {
            parentBindParamInfo = bpInfo;
            break;
          }
        }
        if (parentBindParamInfo != null)
        {
          BindParamInfo childBindParamInfo = constructBindParamInfo(entityClass, mapping);
          childBindParamInfo.setValue(parentBindParamInfo.getValue());
          bindParamInfos.add(childBindParamInfo);
        }
      }
    }
  }

  protected BindParamInfo createBindParamInfoFromPayloadAttribute(Class entityClass, AttributeMapping mapping,
                                                                  Object rawValue)
  {
    if (rawValue == null || "".equals(rawValue.toString().trim()) || rawValue==JSONObject.NULL)
    {
      return null;
    }
    BindParamInfo bpInfo = constructBindParamInfo(entityClass, mapping);
    Object convertedValue = rawValue;
    if (!mapping.isOneToOneMapping())
    {
      // no need to convert when 1:1 mapping because foreign key is typically String or Number type
      // and Number type can be inserted using string value
      try
      {
        convertedValue = EntityUtils.convertColumnValueToAttributeTypeIfNeeded(entityClass, mapping.getAttributeName(),
                                                                               rawValue);
      }
      catch (Exception e)
      {
        convertedValue = null;
        Class attrType = EntityUtils.getJavaType(entityClass, mapping.getAttributeName());
        // report to user that attribute has not been been converted properly
        MessageUtils.handleError("Error populating attribute " + mapping.getAttributeName() +
                                 ", cannot convert value " + rawValue + " of type " + rawValue.getClass().getName() +
                                 " to type " + attrType);
      }
    }
    bpInfo.setValue(convertedValue);
    return bpInfo;
  }

  protected void checkRequired(AttributeMapping attributeMapping, Object rawValue)
  {
    if (attributeMapping instanceof AttributeMappingDirect && ((AttributeMappingDirect)attributeMapping).isRequired())
    {
      if (rawValue == null || "".equals(rawValue.toString().trim()) || rawValue==JSONObject.NULL)
      {
        MessageUtils.handleError("Attribute "+attributeMapping.getAttributeName()+" is required, but is null in payload.");
      }      
    }
  }

  public void setLocalPersistenceManager(DBPersistenceManager dBPersistenceManager)
  {
    this.localPersistenceManager = dBPersistenceManager;
  }

  public DBPersistenceManager getLocalPersistenceManager()
  {
    return localPersistenceManager;
  }
  
  /**
   * Find a connection in connections.xml
   * @param name
   * @return
   */
  public XmlAnyDefinition findConnection(String name)
  {
    List<XmlAnyDefinition> connections = Utility.getConnections().getChildDefinitions("Reference");
    XmlAnyDefinition connection = null;
    for (XmlAnyDefinition currConnection : connections)
    {
      if (name.equals(currConnection.getAttributeValue("name")))
      {
        connection = currConnection;
        break;
      }
    }
    return connection;
  }


  /**
   * Create an entity instance from a list of bind param infos (which are really attribute infos).
   * If an entity instance with the same key already exists in the entity cache, this existing instance is
   * updated and returned. If no such instance is found, and the currentEntity passed is not null, it
   * implies that the primary key of the current entity is changed. In that case, the currentEntity is updated
   * with the new primary key, and the row with old key is removed from SQLite DB, and the cache is updated
   * with the new key pointing to the currentEntity instance.
   * @param entityClass
   * @param bindParamInfos
   * @param currentEntity - the entity instantiated from DataSynchAction JSON created b serializing the original 
   * entity. This is NOT the same instance as in the entity list!!!
   * @return
   */
  protected <E extends Entity> E createOrUpdateEntityInstance(Class entityClass, List<BindParamInfo> bindParamInfos, E currentEntity)
  {
    E newEntity = EntityUtils.getNewEntityInstance(entityClass);
    for (BindParamInfo bpInfo : bindParamInfos)
    {
      newEntity.setAttributeValue(bpInfo.getAttributeName(), bpInfo.getValue());
    }
    // get the key of the new instance and check the cache, if an instance is already present in the cache
    // we need to update this instance and return it, instead of the new entity instance just created
    E existingEntity = EntityCache.getInstance().findByUID(entityClass, EntityUtils.getEntityKey(newEntity));
    if (existingEntity != null)
    {
      // do not copy null values, currentInstance might contain attributes that are only saved
      // locally in the SQLite db
      copyAttributeValues(existingEntity, newEntity,false);
      return existingEntity;
    }
    else if (currentEntity != null)
    {
      // pk has changed, update the old instance with values of new instance, so
      // changes are relected correctly in UI. Delete the row with the old PK from DB
      // a new row has already been added when processing the payload
      // update the cache with the new key pointing to the old instance
      
      // first get the current entity instance from the cache, this is the instance used in the UI
      // this is a DIFFERENT instance than the currentEntity which is the DataSyncAction instance
      // created from the serialized origin entity that triggered the transaction.
      existingEntity = EntityCache.getInstance().findByUID(entityClass, EntityUtils.getEntityKey(currentEntity));    
//      refreshEntityKeyValuesIfNeeded(currentEntity, newEntity);
      if (existingEntity!=null)
      {
        // existingEntity should never be null
        refreshEntityKeyValuesIfNeeded(existingEntity, newEntity);
        // also copy the new values to the DataSynch instance (currentEntity), so we can see the server-side
        // derived values as well in callbacl method EntityCRUDService.dataSynchFinished
        copyAttributeValues(currentEntity,existingEntity,true);
        return existingEntity;
      }
      // should never end up here!
      return currentEntity;
    }
    else
    {
      // add new entity to cache
      EntityCache.getInstance().addEntity(newEntity);
      return newEntity;
    }
  }

  /**
   * The way web service invocation errors are handled depends on the &lt;show-web-service-invocation-errors&gt;
   * flag in the persistenceMapping.xml file for the specified decriptor. If this flag is true, 
   * the error message will be shown to the user. In a production environment, you typically don't 
   * want to show such errors to a user, however, during testing it is convenient to directly see the
   * reason why a web service call failed.
   * If the rethrow flag is set to true, the exception will be rethrown, which is used for write actions
   * which should be registered as a pending data sync action when they fail.
   * @param descriptor
   * @param e
   */
  protected void handleWebServiceInvocationError(ClassMappingDescriptor descriptor, Exception e, boolean rethrow)
  {
    Throwable t = e.getCause()!=null ? e.getCause() : e;
    // always log the message so we can see it in error logs
    String message = t.getLocalizedMessage();
    sLog.severe(message);
    if (descriptor.isShowWebServiceInvocationErrors())
    {
      // this will register error message, show message to user and rethrows the exception
      MessageUtils.handleError(message);
    }
    else if (rethrow)
    {
      // register error message if tracking service is set up
      getUsageTracker().registerErrorMessage(message);
      // rethrow exception so a data sync action will be created      
      throw new AdfException(t);
    }
  }
}
