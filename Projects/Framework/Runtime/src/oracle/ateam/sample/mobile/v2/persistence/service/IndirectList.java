 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  14-aug-2015   Steven Davelaar
  1.1           delegate check for offline to service class
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.service;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

import oracle.adfmf.util.Utility;

import oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingOneToMany;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.TaskExecutor;


/**
 * Class that implements lazy loading (aka as Indirection) of child collections in a domain object.
 * For example, the Department object can have a getEmployees method of type IndirectList, then
 * on the first call of the getEmployees method, this class will execute a SQL statement to retrieve
 * the employees from the database and populate the list.
 * If the child collection is not returned with the parent payload, the find-all-in-parent method as
 * defined in persistenceMapping.xml is executed.
 */
public class IndirectList<E extends Entity>
  implements List<E>
{

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(IndirectList.class);
  Entity entity;
  List<E> delegate;
  AttributeMappingOneToMany mapping;

  public IndirectList(Entity entity, AttributeMappingOneToMany mapping)
  {
    this.mapping = mapping;
    this.entity = entity;
  }


  public int size()
  {
    return getDelegate().size();
  }

  public boolean isEmpty()
  {
    return getDelegate().isEmpty();
  }

  public boolean contains(Object o)
  {
    return getDelegate().contains(o);
  }

  public Iterator<E> iterator()
  {
    return getDelegate().iterator();
  }

  public Object[] toArray()
  {
    return getDelegate().toArray();
  }

  public E[] toArray(E[] a)
  {
    return getDelegate().toArray(a);
  }

  public boolean add(E entity)
  {
    return getDelegate().add(entity);
  }

  public void add(int index, E childEntity)
  {
    // if this is a new entity, then call the add[EntityName] method on the service class or parent entity
    if (EntityUtils.primaryKeyIsNull(childEntity))
    {
      Class beanClass = childEntity.getClass();
      String typeName = childEntity.getClass().getName();
      String addMethodName = "add" + typeName.substring(typeName.lastIndexOf(".") + 1);
      Class[] paramTypes = new Class[] { int.class, beanClass };
      Object[] params = new Object[] { new Integer(index), childEntity};
      Utility.invokeIfPossible(this.entity, addMethodName, paramTypes, params);        
    }
    List<E> oldList = new ArrayList<E>();
    oldList.addAll(getDelegate());
    getDelegate().add(index, childEntity);    
    // MAF 2.1 no longer automatically refreshes iterator binding when using Create operation 
    // on typed collection from data control palette, need to log bug.
    entity.refreshChildEntityList(oldList, getDelegate(), mapping.getAttributeName());
  }

  public boolean remove(Object o)
  {
    return getDelegate().remove(o);
  }

  public E remove(int index)
  {
    E element = getDelegate().remove(index);
    if (element!=null)
    {
      // call the remove[EntityName] method on the service class or parent entity
      Class beanClass = element.getClass();
      String typeName = element.getClass().getName();
      String removeMethodName = "remove" + typeName.substring(typeName.lastIndexOf(".") + 1);
      Class[] paramTypes = new Class[] { beanClass };
      Object[] params = new Object[] { element};
      Utility.invokeIfPossible(entity, removeMethodName, paramTypes, params);        
      // MAF 2.1 does not correctly refresh the UI when using Delete operation 
      // in form layout, but refreshing the iterator binding does NOT fix this,
      // like ot does with Create operation
    }
    return element;
  }

  public boolean containsAll(Collection c)
  {
    return getDelegate().containsAll(c);
  }

  public boolean addAll(Collection c)
  {
    if (delegate==null)
    {
      delegate = new ArrayList<E>();
    }
    return getDelegate().addAll(c);
  }

  public boolean addAll(int index, Collection c)
  {
    return getDelegate().addAll(index, c);
  }

  public boolean removeAll(Collection c)
  {
    return getDelegate().removeAll(c);
  }

  public boolean retainAll(Collection c)
  {
    return getDelegate().retainAll(c);
  }

  /**
   * When this method is called, the next time the list is accessed, the database will be queried again
   * to refresh the list.
   */
  public void clear()
  {
    delegate = null;
  }

  public E get(int index)
  {
    return getDelegate().get(index);
  }

  public E set(int index, E element)
  {
    return getDelegate().set(index, element);
  }

  public int indexOf(Object o)
  {
    return getDelegate().indexOf(o);
  }

  public int lastIndexOf(Object o)
  {
    return getDelegate().lastIndexOf(o);
  }

  public ListIterator listIterator()
  {
    return getDelegate().listIterator();
  }

  public ListIterator listIterator(int index)
  {
    return getDelegate().listIterator(index);
  }

  public java.util.List subList(int fromIndex, int toIndex)
  {
    return getDelegate().subList(fromIndex, toIndex);
  }


  protected List<E> getDelegate()
  {
    if (delegate == null)
    {
      if (entity.getIsNewEntity())
      {
        delegate = new ArrayList<E>();
      }
      else
      {
        delegate = buildDelegate();        
      }
    }
    return delegate;
  }

  protected List<E> buildDelegate()
  {
    final ClassMappingDescriptor referenceDescriptor = mapping.getReferenceClassMappingDescriptor();
    final EntityCRUDService service = EntityUtils.getEntityCRUDService(referenceDescriptor);  
    boolean offline = service.isOffline();
    if (mapping.getAccessorMethod() != null && !offline)
    {
      sLog.fine("Getter method for attribute " + this.mapping.getAttributeName() +
                " called for the first time, calling find-all-in-parent web service method");
      boolean inBackground = service.isDoRemoteReadInBackground();
      if (inBackground)
      {
        TaskExecutor.getInstance().execute(true
            , () -> {
                      DBPersistenceManager pm = EntityUtils.getLocalPersistenceManager(referenceDescriptor);
                      List oldList = pm.findAllInParent(referenceDescriptor.getClazz(), entity, mapping);   
                      // we don't want the child service to start another backgrounf thread, so we temporarily switch
                      // off background read
                      service.setDoRemoteReadInBackground(false);        
                      service.doRemoteFindAllInParent(entity,mapping.getAttributeName());        
                      service.setDoRemoteReadInBackground(true);        
                      List newList = service.getEntityList(); 
                      delegate = newList;
                      entity.refreshChildEntityList(oldList, newList, mapping.getAttributeName());
                    });    
      }
      else
      {
        service.doRemoteFindAllInParent(entity,mapping.getAttributeName());        
      }
    }
    sLog.fine("Getter method for attribute " + this.mapping.getAttributeName() +
                " called for the first time, querying database to retrieve the content");
    DBPersistenceManager pm = EntityUtils.getLocalPersistenceManager(referenceDescriptor);
    List result = pm.findAllInParent(referenceDescriptor.getClazz(), entity, mapping);      
    return result;
  }

  @Override
  public <T extends Object> T[] toArray(T[] a)
  {
    return getDelegate().toArray(a);
  }

  @Override
  public boolean equals(Object o)
  {
    return false;
  }

  @Override
  public int hashCode()
  {
    // TODO Implement this method
    return 0;
  }
}
