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
      Class beanClass = element.getClass();
      String typeName = element.getClass().getName();
      String addMethodName = "add" + typeName.substring(typeName.lastIndexOf(".") + 1);
      Class[] paramTypes = new Class[] { int.class, beanClass };
      Object[] params = new Object[] { new Integer(index), element};
      Utility.invokeIfPossible(crudService, addMethodName, paramTypes, params);        
    }
  }

  @Override
  public E remove(int index)
  {
    E element = super.remove(index);
    if (element!=null)
    {
      // call the remove[EntityName] method on the service class or parent entity
      Class beanClass = element.getClass();
      String typeName = element.getClass().getName();
      String removeMethodName = "remove" + typeName.substring(typeName.lastIndexOf(".") + 1);
      Class[] paramTypes = new Class[] { beanClass };
      Object[] params = new Object[] { element};
      Utility.invokeIfPossible(crudService, removeMethodName, paramTypes, params);        
    }
    return element;
  }
}
