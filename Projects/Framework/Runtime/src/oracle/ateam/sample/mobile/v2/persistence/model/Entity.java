 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  23-mar-2016   Steven Davelaar
  1.3           Added method getKey 
  20-feb-2016   Steven Davelaar
  1.2           Added callback method childEntityAdded/removed 
  19-mar-2015   Steven Davelaar
  1.1           Added call to EntityUtils.refreshCurrentEntity in refreshChildEntityList method
                to ensure UI is also refreshed correctly when child entities are shown in form layout 
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.model;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.atomic.AtomicLong;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;

import oracle.ateam.sample.mobile.v2.persistence.service.IndirectList;
import oracle.ateam.sample.mobile.v2.persistence.service.ValueHolderInterface;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingOneToMany;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingOneToOne;
import oracle.ateam.sample.mobile.v2.persistence.service.ValueHolder;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.TaskExecutor;


/**
 *  Abstract class that must be extended by all data object classes that need to be persisted, either remotely or
 *  local on mobile device in SQLite database.
 */
public abstract class Entity<E> extends ChangeEventSupportable
{

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(Entity.class);
  private static final AtomicLong sKeySequence = new AtomicLong(0);
  
  private transient boolean isNewEntity = false;
  private transient boolean canonicalGetExecuted = false;
  private Long key;

  public void setCanonicalGetExecuted(boolean canonicalGetExecuted)
  {
    this.canonicalGetExecuted = canonicalGetExecuted;
  }

  /**
   * Method does not start with "is" to prevent property from showing up in DC palette
   * @return
   */
  public boolean canonicalGetExecuted()
  {
    return canonicalGetExecuted;
  }

  public Object getAttributeValue(String attrName)
  {
    try
    {
      Method getter = EntityUtils.getGetMethod(this.getClass(),attrName);
      Object value = getter.invoke(this, null);
      return value;
    }
    catch (IllegalAccessException e)
    {
      throw new AdfException("Error invoking getter method for attribute " + attrName + " in class " +
                                 this.getClass().getName() + " " +
                                 e.getLocalizedMessage(),AdfException.ERROR);
    }
    catch (InvocationTargetException e)
    {
     throw new AdfException("Error invoking getter method for attribute " + attrName + " in class " +
                                this.getClass().getName() + " " +
                                e.getLocalizedMessage(),AdfException.ERROR);
   }
  }

  public void setAttributeValue(String attrName, Object value)
  {
    boolean valueHolder = value instanceof ValueHolderInterface;
    Method setter = EntityUtils.getSetMethod(this.getClass(),attrName,valueHolder);
    String valueType = value!=null ? value.getClass().getName() : "Null";
    if (setter == null)
    {
      throw new AdfException("No setter method found for attribute " + attrName + " in class " +
                                 this.getClass().getName(), AdfException.ERROR);
    }
    try
    {
      setter.setAccessible(true);
      setter.invoke(this, new Object[]
          { value });
    }
    catch (IllegalAccessException e)
    {
      throw new AdfException("Error invoking setter method for attribute " + attrName + " in class " +
                              this.getClass().getName() + " with value of type "+valueType+ ": " 
                               + e.getLocalizedMessage(),AdfException.ERROR);
    }
    catch (InvocationTargetException e)
    {
      throw new AdfException("Error invoking setter method for attribute " + attrName + " in class " +
                                 this.getClass().getName() + " with value of type "+valueType + ": " 
                                 + e.getTargetException().getClass().getName() + " " +
                                 e.getTargetException().getLocalizedMessage(),AdfException.ERROR);
    }
  }

  public void setIsNewEntity(boolean isNewEntity)
  {
    this.isNewEntity = isNewEntity;
  }

//  public boolean isIsNewEntity()
//  {
//    return isNewEntity;
//  }
  
  public boolean getIsNewEntity()
  {
    return isNewEntity;
  }
  
//  public void fireAttributeChangeEvent(Entity oldEntity, String attrName)
//  {
//    if (oldEntity!=null)
//    {
//      Object oldValue = oldEntity.getAttributeValue(attrName);
//      Object newValue = this.getAttributeValue(attrName);
//      getPropertyChangeSupport().firePropertyChange(attrName, oldValue, newValue);      
//      sLog.fine("Fired propertyChange event for entity attribute "+this.getClass().getName()+" "+attrName+" old value: "+oldValue+", new value: "+newValue);
//    }
//    getProviderChangeSupport().fireProviderRefresh(attrName);    
//    sLog.fine("Fired providerRefresh event for entity attribute "+this.getClass().getName()+" "+attrName);
//  }

  public boolean equals(Object obj)
  {
    if (obj.getClass()!=this.getClass())
    {
      return false;
    }
    Entity compareEntity = (Entity) obj;
    Object[] compareKey = EntityUtils.getEntityKey(compareEntity);
    Object[] thisKey = EntityUtils.getEntityKey(this); 
    return EntityUtils.compareKeys(compareKey, thisKey);
  }
  
  /**
   * Creates an IndirectList instance that encapsulates a AttributeMappingOneToMany
   * mapping so we can lazily load the list from DB when child collection is requested for the first time
   * @param accessorAttribute
   * @return
   */
  protected <E extends Entity> List<E> createIndirectList(String accessorAttribute)
  {
      AttributeMapping mapping = EntityUtils.findMapping(getClass(), accessorAttribute);
      if (mapping!=null && mapping instanceof AttributeMappingOneToMany)
      {
        return new IndirectList<E>(this, (AttributeMappingOneToMany)mapping);          
      }
      // fallback:  return simple array list
      return new ArrayList<E>();
  }


  /**
   * Creates a ValueHolder instance that encapsulates a AttributeMappingOneToOne
   * mapping so we can lazily load the row from DB when parent row is requested for the first time
   * @param accessorAttribute
   * @return
   */
  protected ValueHolderInterface createValueHolder(String accessorAttribute)
  {
    AttributeMapping mapping = EntityUtils.findMapping(getClass(), accessorAttribute);
    if (mapping!=null && mapping instanceof AttributeMappingOneToOne)
    {
      return new ValueHolder(this, (AttributeMappingOneToOne)mapping);          
    }
    return null;
  }

  /**
   * This method is called from IndirectList.buildDelegate when child rows for an entity are retrieved
   * through a remote server call executed in the background
   * @param oldList
   * @param newList
   * @param childClass NOT USED ANYMORE
   * @param childAttribute
   */
  public void refreshChildEntityList(List oldList, List newList, Class childClass, String childAttribute)
  {
    refreshChildEntityList(oldList,newList,childAttribute);
  }

  /**
   * This method is called from IndirectList.buildDelegate when child rows for an entity are retrieved
   * through a remote server call executed in the background
   * @param oldList
   * @param newList
   * @param childAttribute
   */
  public void refreshChildEntityList(List oldList, List newList, String childAttribute)
  {
    TaskExecutor.getInstance().executeUIRefreshTask(() -> 
    {
      getPropertyChangeSupport().firePropertyChange(childAttribute, oldList, newList);
      getProviderChangeSupport().fireProviderRefresh(childAttribute);
      // the above two statements do NOT refresh the UI when the UI displays a form layout instead of
      // a list view. So, we als refresh the first entity in the list                  
      // refreshCurrentEntity uses iterator refresh and can cause endless loop                      
//      EntityUtils.refreshCurrentEntity(childAttribute,newList,getProviderChangeSupport());
      if (newList!=null && newList.size()>0)
      {
        EntityUtils.refreshEntity((Entity) newList.get(0));
      }
      AdfmfJavaUtilities.flushDataChangeEvent();                        
    });
//    getPropertyChangeSupport().firePropertyChange(childAttribute, oldList, newList);
//    getProviderChangeSupport().fireProviderRefresh(childAttribute);
//    // the above two statements do NOT refresh the UI when the UI displays a form layout instead of
//    // a list view. So, we als refresh the current entity. 
//    EntityUtils.refreshCurrentEntity(childAttribute,newList,getProviderChangeSupport());
//    TaskExecutor.flushDataChangeEvent();
  }

  /**
   * This method is called after the entity has been added to the child entity list.
   * Override this method to execute UI refresh logic like recomputing totals
   * @param entity
   */
  public void childEntityAdded(E entity)
  {
  }

  /**
   * This method is called after the entity has been removed from the child entity list.
   * Override this method to execute UI refresh logic like recomputing totals
   * @param entity
   */
  public void childEntityRemoved(E entity)
  {
  }
  

  /**
   * Fire property change events for the attributes passed in. We use null as old value.
   * The change events are fired using TaskExecutor.executeUIRefreshTask which ensures use
   * of the MafExecutorService when runnning in the background.
   * If canonicalGetExecuted is false, we temporarily set it to true, to prevent any REST calls
   * to fire as a result of invoking the getter methods for refresh event.
   * 
   * @param attrsToRefresh
   */
  public void refreshUI(List<String> attrsToRefresh)
  {
    sLog.fine("Executing refreshUI");
    TaskExecutor.getInstance().executeUIRefreshTask(() ->
    {
      boolean oldCanonicalGet = canonicalGetExecuted();
      setCanonicalGetExecuted(true);                                                                                                        
      for(String attrName : attrsToRefresh)
      {
        getPropertyChangeSupport().firePropertyChange(attrName, null, getAttributeValue(attrName));
      }
      setCanonicalGetExecuted(oldCanonicalGet);
      AdfmfJavaUtilities.flushDataChangeEvent();
      
    });
  }

  /**
   * This method returns a unique "iterator" key which is required by AmxCollectionModel so it can correctly manage
   * new and removed instances and send data change events to refresh the UI accordingly. MAF runtime explicitly
   * checks for a method by this name. If such a method is not present, it will check whether an attribute is marked as
   * key attribute in the persdef info of the data control collection. In other words, this getKey method 
   * saves AMPA users from having to set such a key attribute manually.
   * The unique key is assigned when the getKey method is called for the first time.
   * This "iterator" key should NOT be confused with the entity primary key specified in AMPA persistence-mapping.xml. 
   * That primary key is used to uniqely identify a row in SQLite DB and if the auto-generate flag is set to true, 
   * it is assigned just before the row is inserted in QSQLite DB to ensure uniqueness.
   * @return unique iterator key
   */
  public Long getKey()
  {
    if (key == null)
    {
      key = sKeySequence.getAndIncrement();
    }
    return key;
  }

}
