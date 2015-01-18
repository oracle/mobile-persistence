 /*******************************************************************************
  Copyright © 2015, Oracle and/or its affiliates. All rights reserved.
  
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.Map;

import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;

import oracle.ateam.sample.mobile.v2.persistence.cache.EntityCache;
import oracle.ateam.sample.mobile.v2.persistence.db.BindParamInfo;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingOneToOne;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ObjectPersistenceMapping;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.util.UsageTracker;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;

/**
 * Abstract class that provides generic implementation of some of the methods of the
 * PersistenceManager interface that can be used by concrete subclasses.
 * The Persistence manager interface provides basic CRUD operations for a given entity instance. 
 */
public abstract class AbstractPersistenceManager implements PersistenceManager

{
  private static UsageTracker usageTracker = new UsageTracker();
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(AbstractPersistenceManager.class);
  private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  
  /**
   * Create BindParamInfos instances with the values populated taken from the primary
   * key attributes of the entity passed in.
   * @param entity
   * @return
   */
  protected List getPrimaryKeyBindParamInfo(Entity entity)
  {
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = mapping.findClassMappingDescriptor(entity.getClass().getName());
    List bindParamInfos = new ArrayList();
    List<AttributeMapping> keyMappings = descriptor.getPrimaryKeyAttributeMappings();
    for (AttributeMapping keyMapping : keyMappings)
    {
      BindParamInfo primaryKeyValue = constructBindParamInfo(entity, keyMapping);
      bindParamInfos.add(primaryKeyValue);
    }
    return bindParamInfos;
  }

  /**
   * Create BindParamInfo instances for primary key attributes without the value populated
   *
   * @param entity
   * @return
   */
  protected List getPrimaryKeyBindParamInfo(Class entityClass)
  {
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = mapping.findClassMappingDescriptor(entityClass.getName());
    List bindParamInfos = new ArrayList();
    List<AttributeMapping> keyMappings = descriptor.getPrimaryKeyAttributeMappings();
    for (AttributeMapping keyMapping : keyMappings)
    {
      BindParamInfo primaryKeyValue = constructBindParamInfo(entityClass, keyMapping);
      bindParamInfos.add(primaryKeyValue);
    }
    return bindParamInfos;
  }

  /**
   * Construct BindParamInfo instance without the value being set.
   * @param entityClass
   * @param attrMapping
   * @return
   */
  public BindParamInfo constructBindParamInfo(Class entityClass, AttributeMapping attrMapping)
  {
    BindParamInfo bindParamInfo = getNewBindParamInfoInstance();
    String attrName = attrMapping.getAttributeName();
    bindParamInfo.setAttributeName(attrName);
    bindParamInfo.setColumnName(attrMapping.getColumnName());
    bindParamInfo.setTableName(attrMapping.getClassMappingDescriptor().getTableName());
    bindParamInfo.setPrimaryKey(attrMapping.isPrimaryKeyMapping());
    bindParamInfo.setJavaType(EntityUtils.getJavaType(entityClass, attrName));
    return bindParamInfo;
  }

  /**
   * Create an instance of BindParamInfo that holds all information needed for this
   * attribute and underlying column to be part of a SQL statement.
   * @param entity
   * @param attrMapping
   * @return
   */
  public BindParamInfo constructBindParamInfo(Entity entity, AttributeMapping attrMapping)
  {
    BindParamInfo bindParamInfo = constructBindParamInfo(entity.getClass(), attrMapping);
    String attrName = attrMapping.getAttributeName();
    Object value = entity.getAttributeValue(attrName);
    bindParamInfo.setValue(value);
    return bindParamInfo;
  }

  /**
   * Returns  a list of instances of BindParamInfo.
   * This list can be used to easily construct insert and update statements as it contains
   * all necessary information.
   * @param entity
   * @return
   */
   public List getBindParamInfos(Entity entity)
   {
     return getBindParamInfos(entity,false,false);
   }

  public List getBindParamInfos(Entity entity, boolean keyValuesOnly)
  {
    return getBindParamInfos(entity,keyValuesOnly,false);
  }

  public List getBindParamInfos(Entity entity, boolean keyValuesOnly, boolean persistedOnly)
  {
    List bindParamInfos = new ArrayList();
    String entityClass = entity.getClass().getName();
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = mapping.findClassMappingDescriptor(entityClass);
    List attributeMappings = descriptor.getAttributeMappingsDirect();
    for (int i = 0; i < attributeMappings.size(); i++)
    {
      AttributeMapping attrMapping = (AttributeMapping) attributeMappings.get(i);
      if ((!keyValuesOnly || attrMapping.isPrimaryKeyMapping())  
           && (!persistedOnly || attrMapping.isPersisted()))
      {
        BindParamInfo bindParamInfo = constructBindParamInfo(entity, attrMapping);
        bindParamInfos.add(bindParamInfo);        
      }
    }
    return bindParamInfos;
  }

  /**
   * Convenience method hat returns new instance of BindParamInfo used
   * by method getBindParamInfos. If you have a need to subclass BindParamInfo
   * than simply override this method and return your subclass instance.
   * @return
   */
  public BindParamInfo getNewBindParamInfoInstance()
  {
    return new BindParamInfo();
  }

  /** 
   * This default implementation checks entity cache for the given class and key
   */ 
  public Entity findByKey(Class entityClass, Object key)
  {
    Object[] keyValues = new Object[]{key};
    return findByKey(entityClass,keyValues);
  } 

  /** 
   * This default implementation checks entity cache for the given class and key
   */ 
  public Entity findByKey(Class entityClass, Object[] key)
  {
    Entity entity = EntityCache.getInstance().findByUID(entityClass, key);
    if (entity==null)
    {
      sLog.fine("Entity with key "+key+" NOT found in entity cache");      
    }
    else
    {
      sLog.fine("Entity with key "+key+" found in entity cache");            
    }
    return entity;
  }

  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    propertyChangeSupport.addPropertyChangeListener(l);
  }

  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    propertyChangeSupport.removePropertyChangeListener(l);
  }

  /**
   * If a remote insert or update causes the primary key values to be updated, the processing of the
   * payload has created a new row in te DB and a new entity in the cache. In this method we "repair" this
   * by removing the row with the old key from the DB, and we update the entity cache to contain the existing
   * entity instance with the new key.
   * @param currentInstance
   * @param newInstance
   */
  protected void refreshEntityKeyValuesIfNeeded(Entity currentInstance, Entity newInstance)
  {
    if (currentInstance != newInstance)
    {
      Object[] oldKey = EntityUtils.getEntityKey(currentInstance);
      Object[] newKey = EntityUtils.getEntityKey(newInstance);
      if (!EntityUtils.compareKeys(oldKey, newKey))
      {
        // pk has changed, update the old instance with values of new instance, so
        // changes are relected correctly in UI. Delete the row with the old PK from DB
        // a new row has already been added when processing the payload
        // update the cache with the new key pointing to the old instance
        EntityCache.getInstance().removeEntity(currentInstance);
        ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(currentInstance.getClass());
        if (descriptor.isPersisted())
        {
          DBPersistenceManager dbpm = EntityUtils.getLocalPersistenceManager(descriptor);
          dbpm.removeEntity(currentInstance, true);
        }
        // do not copy null values, currentInstance might contain attributes that are only saved
        // locally in the SQLite db
        copyAttributeValues(currentInstance, newInstance, false);
        EntityCache.getInstance().removeEntity(newInstance);
        EntityCache.getInstance().addEntity(currentInstance);
        // merge the new instance with DB because currentIntance might have held local-only attr values
        // that are only stred on-device in SQLIte, the new row created before for the new instance should be
        // updated with these local values.
        if (descriptor.isPersisted())
        {
          DBPersistenceManager dbpm = EntityUtils.getLocalPersistenceManager(descriptor);
          dbpm.mergeEntity(currentInstance, true);
        }
      }
    }
  }

  /**
   * Copies attribute values between two entity instances. if copyNullvalues is true, then attributes that are null
   * in the fromInstance will be nullified in the toInstance. Otherwise the value will be left as is in toInstance.
   * @param toInstance
   * @param fromInstance
   * @param copyNullValues
   */
  protected void copyAttributeValues(Entity toInstance, Entity fromInstance, boolean copyNullValues)
  {
    // first set the value of canoncalGetExecuted of toInstance on fromInstance
    // This is to prevent endless loop when we invoke getCanonical from a attribute getter
    // method
    fromInstance.setCanonicalGetExecuted(toInstance.canonicalGetExecuted());
    Map attrValues = EntityUtils.getEntityAttributeValues(fromInstance);
    Iterator attrNames = attrValues.keySet().iterator();
    while (attrNames.hasNext())
    {
      String attrName = (String) attrNames.next();
      Object value = attrValues.get(attrName);
      if (value!=null || copyNullValues)
      {
        toInstance.setAttributeValue(attrName, value);        
      }
    }
  }

  /**
   * Return true when all primary key attributes are included in the list of bind param infos and have a value.
   * Return false otherwise.
   * @param descriptor
   * @param bindParamInfos
   * @return
   */
  protected boolean isAllPrimaryKeyBindParamInfosPopulated(ClassMappingDescriptor descriptor, List bindParamInfos)
  {
    List<String> attrs = descriptor.getPrimaryKeyAttributeNames();
    boolean OK = true;
    for (int i = 0; i < attrs.size(); i++)
    {
      boolean attrPopulated = false;
      for (int j = 0; j < bindParamInfos.size(); j++)
      {
        BindParamInfo bpInfo = (BindParamInfo) bindParamInfos.get(j);
        if (bpInfo.getAttributeName().equals(attrs.get(i)) && bpInfo.getValue() != null)
        {
          attrPopulated = true;
          break;
        }
      }
      if (!attrPopulated)
      {
        OK = false;
      }
    }
    return OK;
  }

  protected UsageTracker getUsageTracker()
  {
    return usageTracker;
  }
}
