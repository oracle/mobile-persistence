 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  09-feb-2016   Steven Davelaar
  1.2           Commented out add/remove methods, bug that was reason for this class is fixed in MAF 2.2.1
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
 * This class was used to add/remove entities, 
 * This class ensures that the addEntity and removeEnity methods are called in either the crud service
 * class or parent entity when a new entity is added/removed through the UI.
 * It was temporariliy needed because of bug in MAF 2.1.This bug has been fixed in MAF 2.2.1, so all
 * code is commented out in this class, otherwise add or remove would be called twice!
 * 
 * @deprecated
 * @param <E>
 */
public class EntityList<E extends Entity>
  extends ArrayList<E>
{
  private EntityCRUDService crudService;

  public EntityList(EntityCRUDService crudService)
  {
    super();
    this.crudService = crudService;
  }

//  @Override
//  public void add(int index, E element)
//  {
//    super.add(index, element);
//    // if this is a new entity, then call the add[EntityName] method on the service class or parent entity
//    if (EntityUtils.primaryKeyIsNull(element))
//    {
//      // call the add[EntityName] method on the service class
//      List<E> oldList = new ArrayList<E>();
//      oldList.addAll(this);
//      EntityUtils.invokeAddMethod(crudService, index, element);
//      // MAF 2.1 no longer automatically refreshes iterator binding when using Create operation 
//      // on typed collection from data control palette, need to log bug.
//      crudService.refreshEntityList(oldList);
//    }
//  }
//
//  @Override
//  public E remove(int index)
//  {
//    E element = super.remove(index);
//    if (element!=null)
//    {
//      // call the remove[EntityName] method on the service class
//      EntityUtils.invokeRemoveMethod(crudService, element);
//      // MAF 2.1 does not correctly refresh the UI when using Delete operation 
//      // in form layout, but refreshing the iterator does NOT fix this, as is the
//      // the case with Create operation. need to log bug
//    }
//    return element;
//  }
}
