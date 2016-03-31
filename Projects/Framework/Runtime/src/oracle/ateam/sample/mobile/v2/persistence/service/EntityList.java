 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  09-feb-2016   Steven Davelaar
  1.2           Changed add/remove methods, now only refreshing entity list, no longer calling add/remove methods
                since bug 20995016/20567530 has now been fixed.
  17-feb-2015   Steven Davelaar
  1.1           Moved to service pckage so we can call protected method on crudService to refresh entity lisy
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.service;


import java.util.ArrayList;

import java.util.List;

import oracle.adfmf.util.Utility;

import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;

/**
 * This class intercepts add/remove actions on entity list.
 * Prior to MAF 2.2, this class was used to call the addEntity and removeEntity methods that MAF no longer
 * called when it started supporting typed collections. This bug 20995016 has now been fixed, which means MAF
 * framework now does call the addEntity/removeEntity methods again, however, it calls these methods
 * BEFORE actually adding/removing the instance from the list, so before the add/remove methods in this
 * entity list class are called. In other words, the sequence of the method calls is now reverse as to what it 
 * was when bug was not yet fixed.
 * As a result, we can no longer use add/remove methods to updated related info shown in the UI, like
 * the total number of records. To addres this, the add/remove methods in this EntityList class, now call the
 * entityAdded/Removed on the EntityCrudService so you can override these method in your service class
 * to trigger additional UI refresh logic that is needed as a result of adding or removing an entity.
 *
 * @param <E>
 */
public class EntityList<E extends Entity>
  extends ArrayList<E>
{
  private transient EntityCRUDService<E> crudService;

  public EntityList(EntityCRUDService<E> crudService)
  {
    super();
    this.crudService = crudService;
  }

  @Override
  public void add(int index, E element)
  {
//    List<E> oldList = new ArrayList<E>();
//    oldList.addAll(this);
    super.add(index, element);
    crudService.entityAdded(element);
//    crudService.refreshEntityList(oldList);
    // if this is a new entity, then call the add[EntityName] method on the service class or parent entity
//    if (EntityUtils.primaryKeyIsNull(element))
//    {
//      // call the add[EntityName] method on the service class
//      List<E> oldList = new ArrayList<E>();
//      oldList.addAll(this);
//      EntityUtils.invokeAddMethod(crudService, index, element);
//      // MAF 2.1 no longer automatically refreshes iterator binding when using Create operation 
//      // on typed collection from data control palette, see bug 20567530/20995016.
//      crudService.refreshEntityList(oldList);
//    }
  }

  @Override
  public E remove(int index)
  {
//    List<E> oldList = new ArrayList<E>();
//    oldList.addAll(this);
    E element = super.remove(index);
    crudService.entityRemoved(element);
//    crudService.refreshEntityList(oldList);
//    if (element!=null)
//    {
//      // call the remove[EntityName] method on the service class
//      EntityUtils.invokeRemoveMethod(crudService, element);
//      // MAF 2.1 does not correctly refresh the UI when using Delete operation 
//      // in form layout, but refreshing the iterator does NOT fix this, as is the
//      // the case with Create operation. need to log bug
//    }
    return element;
  }
}
