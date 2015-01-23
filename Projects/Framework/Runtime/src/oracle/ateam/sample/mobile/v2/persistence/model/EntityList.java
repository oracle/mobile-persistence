 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.model;


import java.util.ArrayList;

import oracle.adfmf.util.Utility;

import oracle.ateam.sample.mobile.v2.persistence.service.EntityCRUDService;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;

/**
 * This class is used to store entities, it is temporariliy needed until MAF 2.1 BUG is fixed.
 * This class ensures that the addEntity and removeEnity methods are called in either the crud service
 * class or parent entity when a new entity is added/removed through the UI.
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

  @Override
  public void add(int index, E element)
  {
    super.add(index, element);
    // if this is a new entity, then call the add[EntityName] method on the service class or parent entity
    if (EntityUtils.primaryKeyIsNull(element))
    {
      // call the add[EntityName] method on the service class
      EntityUtils.invokeAddMethod(crudService, index, element);
    }
  }

  @Override
  public E remove(int index)
  {
    E element = super.remove(index);
    if (element!=null)
    {
      // call the remove[EntityName] method on the service class
      EntityUtils.invokeRemoveMethod(crudService, element);
    }
    return element;
  }
}
