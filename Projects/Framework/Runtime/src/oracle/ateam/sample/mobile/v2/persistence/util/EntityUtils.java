 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  21-aug-2015   Steven Davelaar
  1.3           Added check ib.getIterator is not null in refreshCurrentEntity
  19-mar-2015   Steven Davelaar
  1.2           Added method refreshCurrentEntity
  20-jan-2015   Steven Davelaar
  1.1           Fixed bug in getLocalPersistenceManager, local pm might not be set against (child) descriptor
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.math.BigDecimal;

import java.sql.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.adfmf.bindings.DataControl;
import oracle.adfmf.bindings.dbf.AmxBindingContext;
import oracle.adfmf.bindings.dbf.AmxIteratorBinding;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.MafExecutorService;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.java.beans.ProviderChangeSupport;
import oracle.adfmf.util.Utility;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.DateUtils;
import oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.manager.PersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingDirect;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ObjectPersistenceMapping;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.service.EntityCRUDService;
import oracle.ateam.sample.mobile.v2.persistence.service.ValueHolderInterface;

/**
 * Utility class with helper methods related to entity instances
 */
public class EntityUtils
{

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(EntityUtils.class);

  public static Method getSetMethod(Class entityClass, String attrName, boolean valueHolder)
  {
    String methodName = "set" + attrName.substring(0, 1).toUpperCase() + attrName.substring(1);
    Class type = getJavaType(entityClass, attrName);
    if (valueHolder)
    {
      methodName = methodName + "Holder";
      type = ValueHolderInterface.class;
    }
    // valueHolder is protected mehod, so pass false as last arg in that case!
    return getMethod(entityClass, methodName, new Class[]
  {
    type
  }, !valueHolder);
  }

  public static Class getJavaType(Class entityClass, String attrName)
  {
    Method getter = getGetMethod(entityClass, attrName);
    if (getter == null)
    {
      throw new AdfException(" No getter method found for attribute " + attrName + " in class " + entityClass.getName(),
                             AdfException.ERROR);
    }
    return getter.getReturnType();
  }

  public static Method getGetMethod(Class entityClass, String attrName)
  {
    String methodName = "get" + attrName.substring(0, 1).toUpperCase() + attrName.substring(1);
    return getMethod(entityClass, methodName, null, true);
  }

  private static Method getMethod(Class entityClass, String methodName, Class[] paramTypes, boolean publicOnly)
  {
    try
    {
      Method getter = entityClass.getMethod(methodName, paramTypes);
      return getter;
    }
    catch (NoSuchMethodException e)
    {
      if (!publicOnly)
      {
        // valueHolder method is protected, lookup through getDeclaredMethod!
        Method found = null;
        Method[] methods = entityClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++)
        {
          Method method = methods[i];
          if (method.getName().equals(methodName))
          {
            found = method;
            break;
          }
        }
        if (found == null)
        {
          throw new AdfException("Cannot find method " + methodName + " in " + entityClass.getName(),
                                 AdfException.ERROR);
        }
        else
        {
          return found;
        }
      }
      else
      {
        throw new AdfException("Cannot find public method " + methodName + " in " + entityClass.getName(),
                               AdfException.ERROR);
      }
    }
  }

  public static Object convertColumnValueToAttributeTypeIfNeeded(Class entityClass, String attrName, Object columnValue)
  {
    Object rawValue = columnValue;
    if (rawValue == null)
    {
      return null;
    }
    Object convertedValue = rawValue;
    Class attrType = EntityUtils.getJavaType(entityClass, attrName);
    if (attrType == Object.class)
    {
      return rawValue;
    }
    else if (attrType == String.class)
    {
      return rawValue.toString();
    }
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    if (columnValue instanceof String && attrType.isAssignableFrom(Date.class))
    {
      // return attr date format instead of descriptor dateTime. If attr dateFormat is not set in
      // persistence-mapping.xml, it will return the descritor dateTime format anyway
      //      return DateUtils.convertToDate(attrType, (String) columnValue, descriptor.getDateFormat(),
      //                                     descriptor.getDateTimeFormat());
      AttributeMapping am = descriptor.findAttributeMappingByName(attrName);
      return DateUtils.convertToDate(attrType, (String) columnValue, descriptor.getDateFormat(), am.getDateFormat());
    }
    boolean conversionNeeded = true;
    boolean valueHolder = columnValue instanceof ValueHolderInterface;
    if (columnValue == null || attrType.isAssignableFrom(columnValue.getClass()) || valueHolder)
    {
      conversionNeeded = false;
    }
    try
    {
      if (columnValue instanceof Timestamp)
      {
        // convert to long so we can create date types as needed
        rawValue = new Long(((Timestamp) columnValue).getTime());
      }
      if (columnValue != null && conversionNeeded)
      {
        // use the constructor of the attribute type and pass in the raw value. If there is no constructor
        // using that type we use string value
        Constructor[] ctors = attrType.getDeclaredConstructors();
        Constructor stringConstructor = null;
        Constructor longConstructor = null;
        Constructor properTypeConstructor = null;
        for (int i = 0; i < ctors.length; i++)
        {
          Constructor constructor = ctors[i];
          if (constructor.getParameterTypes().length == 1)
          {
            if (constructor.getParameterTypes()[0] == attrType)
            {
              properTypeConstructor = constructor;
              break;
            }
            else if (constructor.getParameterTypes()[0] == String.class)
            {
              stringConstructor = constructor;
            }
            else if (constructor.getParameterTypes()[0] == Long.TYPE && rawValue.getClass() == Long.class)
            {
              longConstructor = constructor;
            }
          }
        }
        if (properTypeConstructor != null)
        {
          convertedValue = properTypeConstructor.newInstance(new Object[]
          {
            rawValue
          });
        }
        else if (longConstructor != null)
        {
          convertedValue = longConstructor.newInstance(new Object[]
          {
            rawValue
          });
        }
        else if (stringConstructor != null)
        {
          convertedValue = stringConstructor.newInstance(new Object[]
          {
            rawValue.toString()
          });
        }
        else
        {
          throw new AdfException(" No proper constructor found to instantiate attribute value for " + attrName +
                                 " with type " + attrType + " in class " + entityClass.getName() +
                                 " and raw value of type " + rawValue.getClass().getName(), AdfException.ERROR);
        }
      }
    }
    catch (InstantiationException e)
    {
      throw new AdfException("Error creating instance for attribute " + attrName + " of type " + attrType.getName() +
                             " in class " + entityClass.getName() + " with value " + rawValue + ": " +
                             e.getLocalizedMessage(), AdfException.ERROR);
    }
    catch (IllegalAccessException e)
    {
      throw new AdfException("Error creating instance for attribute " + attrName + " of type " + attrType.getName() +
                             " in class " + entityClass.getName() + " with value " + rawValue + ": " +
                             e.getLocalizedMessage(), AdfException.ERROR);
    }
    catch (InvocationTargetException e)
    {
      throw new AdfException("Error creating instance for attribute " + attrName + " of type " + attrType.getName() +
                             " in class " + entityClass.getName() + " with value " + rawValue + ": " +
                             e.getLocalizedMessage(), AdfException.ERROR);
    }
    return convertedValue;
  }

  public static AttributeMapping findMapping(Class entityClass, String attribute)
  {
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = mapping.findClassMappingDescriptor(entityClass.getName());
    return descriptor.findAttributeMappingByName(attribute);
  }

  public static List<String> getEntityKeyAttributes(Class entityClass)
  {
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = mapping.findClassMappingDescriptor(entityClass.getName());
    List<String> keyAttrs = descriptor.getPrimaryKeyAttributeNames();
    return keyAttrs;
  }

  public static Object[] getEntityKey(Entity entity)
  {
    List<String> keyAttrs = getEntityKeyAttributes(entity.getClass());
    Object[] keyValues = new Object[keyAttrs.size()];
    for (int i = 0; i < keyAttrs.size(); i++)
    {
      keyValues[i] = entity.getAttributeValue(keyAttrs.get(i));
    }
    return keyValues;
  }

  public static Map<String, Object> getEntityAttributeValues(Entity entity)
  {
    HashMap<String, Object> attrs = new HashMap<String, Object>();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
    List<AttributeMappingDirect> attrMappings = descriptor.getAttributeMappingsDirect();
    for (AttributeMappingDirect attrMapping: attrMappings)
    {
      attrs.put(attrMapping.getAttributeName(), entity.getAttributeValue(attrMapping.getAttributeName()));
    }
    return attrs;
  }

  /**
   * Create new instance for given entityClass
   * @param <E>
   * @param entityClass
   * @return
   */
  public static <E extends Entity> E getNewEntityInstance(Class entityClass)
  {
    try
    {
      E entity = (E) entityClass.newInstance();
      return entity;
    }
    catch (InstantiationException e)
    {
      throw new AdfException("Error creating instance for class" + entityClass.getName() + ": " +
                             e.getLocalizedMessage(), AdfException.ERROR);
    }
    catch (IllegalAccessException e)
    {
      throw new AdfException("Error creating instance for class" + entityClass.getName() + ": " +
                             e.getLocalizedMessage(), AdfException.ERROR);
    }
  }

  /**
   * Compares two entity keys, returns true if they are the same
   * @param key1
   * @param key2
   * @return
   */
  public static boolean compareKeys(Object[] key1, Object[] key2)
  {
    boolean same = true;
    if (key1.length == key2.length)
    {
      for (int i = 0; i < key1.length; i++)
      {
        if (key1[i] == null || !key1[i].equals(key2[i]))
        {
          same = false;
          break;
        }
      }
    }
    else
    {
      same = false;
    }
    return same;
  }

  /**
   * Generate unique primary key for the entity, provided that the eneity is persistable, the autoIncrementPrimaryKey
   * property is set to true, and the primary key attribute is still null.
   * @param pm
   * @param entity
   * @param increment
   */
  public static void generatePrimaryKeyValue(Entity entity, int increment)
  {
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
    PersistenceManager pm = getLocalPersistenceManager(descriptor);
    generatePrimaryKeyValue(pm, entity, increment);
  }

  /**
   * Returns true when all promary key attributes are null, returns false otherwise
   * @param entity
   * @return
   */
  public static boolean primaryKeyIsNull(Entity entity)
  {
    boolean pknull = true;
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
    List<AttributeMapping> keyMappings = descriptor.getPrimaryKeyAttributeMappings();
    for (AttributeMapping keyMapping: keyMappings)
    {
      String attrName = keyMapping.getAttributeName();
      if (entity.getAttributeValue(attrName) != null)
      {
        pknull = false;
      }
    }
    return pknull;
  }

  /**
   * Generate unique primary key for the entity, provided that the eneity is persistable, the autoIncrementPrimaryKey
   * property is set to true, and the primary key attribute is still null.
   * @param pm
   * @param entity
   * @param increment
   */
  public static void generatePrimaryKeyValue(PersistenceManager pm, Entity entity, int increment)
  {
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
    if (!descriptor.isPersisted() || !descriptor.isAutoIncrementPrimaryKey() || !primaryKeyIsNull(entity))
    {
      return;
    }
    List<AttributeMapping> keyMappings = descriptor.getPrimaryKeyAttributeMappings();
    for (AttributeMapping keyMapping: keyMappings)
    {
      String attrName = keyMapping.getAttributeName();
      Class attrType = EntityUtils.getJavaType(entity.getClass(), attrName);
      if (attrType.isAssignableFrom(Date.class))
      {
        entity.setAttributeValue(attrName, new Date());
        continue;
      }
      Object columnValue = pm.getMaxValue(entity.getClass(), attrName);
      // set to "1" for now, we need to prepopulate otherwise we get errors because of ADF Mobile bug
      columnValue = columnValue == null? "1": columnValue;
      Object value = EntityUtils.convertColumnValueToAttributeTypeIfNeeded(entity.getClass(), attrName, columnValue);
      if (value instanceof Integer)
      {
        int intValue = ((Integer) value).intValue();
        entity.setAttributeValue(attrName, new Integer(intValue + increment));
      }
      else if (value instanceof Long)
      {
        long longValue = ((Long) value).longValue();
        entity.setAttributeValue(attrName, new Long(longValue + increment));
      }
      else if (value instanceof Double)
      {
        double doubleValue = ((Double) value).doubleValue();
        entity.setAttributeValue(attrName, new Double(doubleValue + increment));
      }
      else if (value instanceof BigDecimal)
      {
        BigDecimal bdValue = ((BigDecimal) value).add(new BigDecimal(increment + ""));
        entity.setAttributeValue(attrName, bdValue);
      }
      else if (value instanceof String)
      {
        // set to "0" for now, we need to prepopulate otherwise we get errors because of ADF Mobile bug
        entity.setAttributeValue(attrName, "0");
      }

    }

  }

  /**
   * Retrieve instance of EntityCRUDService for a specific class. We first try to retrieve the instance
   * through the associated data control, but this data control might not exist. In that case, we simply
   * instantiate a new instance.
   * @param clazz
   * @return
   */
  public static EntityCRUDService getEntityCRUDService(Class clazz)
  {
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(clazz);
    if (descriptor != null)
    {
      return getEntityCRUDService(descriptor);
    }
    return null;
  }

  /**
   * Retrieve instance of EntityCRUDService for a specific mapping descriptor. We first try to retrieve the instance
   * through the associated data control, but this data control might not exist. In that case, we simply
   * instantiate a new instance.
   * @param descriptor
   * @return
   */
  public static EntityCRUDService getEntityCRUDService(ClassMappingDescriptor descriptor)
  {
    String serviceClassName = descriptor.getCRUDServiceClassName();
    if (serviceClassName == null)
    {
      return null;
    }
    // execute accessor method web service call first!
    AmxBindingContext bc =
      (AmxBindingContext) AdfmfJavaUtilities.getAdfELContext().evaluateVariable(AmxBindingContext.BINDINGCONTEXT_SCOPE_NAME);
    int lastDot = serviceClassName.lastIndexOf(".");
    String dcName = serviceClassName.substring(lastDot + 1);
    // getDataControlById throws exception when DC does not exist
    //    DataControl dc = (DataControl) bc.getDataControlById(dcName);
    DataControl dc = null;
    if (bc != null)
    {
      dc = (DataControl) bc.get(dcName);
    }
    EntityCRUDService service = null;
    // first try to lookup the crud service through its data control, if it doesn't exist, just
    // instantiate the class
    if (dc == null)
    {
      //        MessageUtils.handleError("Cannot find data control usage for "+dcName+ " in DataBindings.cpx, unable to populate "+this.mapping.getAttributeName()+" child accessor");
      try
      {
        Class serviceClass = Utility.loadClass(serviceClassName);
        //        Class serviceClass = Class.forName(serviceClassName, false, Thread.currentThread().getContextClassLoader());
        service = (EntityCRUDService) serviceClass.newInstance();
      }
      catch (InstantiationException e)
      {
        throw new AdfException("Error creating instance for class" + serviceClassName + ": " + e.getLocalizedMessage(),
                               AdfException.ERROR);
      }
      catch (IllegalAccessException e)
      {
        throw new AdfException("Error creating instance for class" + serviceClassName + ": " + e.getLocalizedMessage(),
                               AdfException.ERROR);
      }
      catch (ClassNotFoundException e)
      {
        throw new AdfException("Error creating instance for class" + serviceClassName + ": " + e.getLocalizedMessage(),
                               AdfException.ERROR);
      }
    }
    else
    {
      service = (EntityCRUDService) dc.getDataProvider();
    }
    return service;
  }

  public static DBPersistenceManager getLocalPersistenceManager(ClassMappingDescriptor descriptor)
  {
    // do not obtain persistence manager through new service instance, ight
    // trgger unwanted (remote) findAll
    //    EntityCRUDService service = getEntityCRUDService(descriptor);
    //    if (service!=null)
    //    {
    //      return service.getLocalPersistenceManager();
    //    }
    DBPersistenceManager pm = null;
    String className = descriptor.getLocalPersistenceManagerClassName();
    if (className == null)
    {
      // might not be set for child entity
      className = DBPersistenceManager.class.getName();
    }
    try
    {
      Class pmClass = Utility.loadClass(className);
      //        Class pmClass = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
      pm = (DBPersistenceManager) pmClass.newInstance();
    }
    catch (InstantiationException e)
    {
      throw new AdfException("Error creating instance for class" + className + ": " + e.getLocalizedMessage(),
                             AdfException.ERROR);
    }
    catch (IllegalAccessException e)
    {
      throw new AdfException("Error creating instance for class" + className + ": " + e.getLocalizedMessage(),
                             AdfException.ERROR);
    }
    catch (ClassNotFoundException e)
    {
      throw new AdfException("Error creating instance for class" + className + ": " + e.getLocalizedMessage(),
                             AdfException.ERROR);
    }
    return pm;
  }


  /**
   * Calls the add[EntityName] method using reflection in the EntityCRUDService subclass for
   * the specified entity. The method takes two arguments, the index and the entity instance.
   * @param crudService
   * @param entity
   */
  public static void invokeAddMethod(EntityCRUDService crudService, int index, Entity entity)
  {
    Class beanClass = entity.getClass();
    String typeName = entity.getClass().getName();
    String addMethodName = "add" + typeName.substring(typeName.lastIndexOf(".") + 1);
    Class[] paramTypes = new Class[]
    {
      int.class, beanClass
    };
    Object[] params = new Object[]
    {
      new Integer(index), entity
    };
    Utility.invokeIfPossible(crudService, addMethodName, paramTypes, params);
  }


  /**
   * Calls the remove[EntityName] method using reflection in the EntityCRUDService subclass for
   * the specified entity. The method takes one argument, the entity instance.
   * @param crudService
   * @param entity
   */
  public static void invokeRemoveMethod(EntityCRUDService crudService, Entity entity)
  {
    Class beanClass = entity.getClass();
    String typeName = entity.getClass().getName();
    String removeMethodName = "remove" + typeName.substring(typeName.lastIndexOf(".") + 1);
    Class[] paramTypes = new Class[]
    {
      beanClass
    };
    Object[] params = new Object[]
    {
      entity
    };
    Utility.invokeIfPossible(crudService, removeMethodName, paramTypes, params);
  }

  /**
   * The standard technique to refresh UI using providerChangeSupport.fireProviderRefresh only refreshes
   * the UI correctly when a list view is used. With a form layout, we need to call
   * providerChangeSupport.fireProviderChange("listName", rowKey, entity) to get form fields refreshed correctly.
   * As we do not now the current row key (or index) nor current entity in the model layer, we need to access the iterator binding
   * to find out this information.
   * Not elegant to access ViewController objects but it works, as long as the iterator binding exists.
   * That's why we have to catch any exception because the actual iterator name might be different than what we assume,
   * in which case the developer has to override this method and use the correct iterator name.
   *
   * @param entityListName name of the collection attribute that needs to be refreshed. We assume the iterator binding is
   * named after the entityListName suffixed with "Iterator".
   * @param entities list f entities that holds the current enity that needs to be refreshed
   * @param providerChangeSupport instance used to invoke the fireProviderChange method
   */
  public static <E extends Entity> void refreshCurrentEntity(String entityListName, List<E> entities,
                                                             ProviderChangeSupport providerChangeSupport)
  {
        String iteratorBindingName = entityListName + "Iterator";
        try
        {
          AmxIteratorBinding ib =
            (AmxIteratorBinding) AdfmfJavaUtilities.evaluateELExpression("#{bindings." + iteratorBindingName + "}");
          // when initializing iterator which results in calling findAll and this refresh the iterator is not yet
          // available
          if (ib != null && ib.getIterator() != null)
          {
            // we do not want to use ib.refresh because this resets the current row to the first row, potentially
            // firing some unwanted REST calls, instead we acll fireProviderChange passing in the current entity and row key
            // We can only use ib.refresh when the current row is the first row, which we do, because
            // only ib.refresh also refreshes DVT's correctly in the UI. (And when the iterator is used by a graph, the
            // current row will typically be the first row anyway)
            int index = ib.getIterator().getCurrentIndex();
            if (index == 0)
            {
              ib.refresh();
            }
            else if (index > -1 && index < entities.size())
            {
              Object rowKey = ib.getIterator().getCurrentRowKey();
              providerChangeSupport.fireProviderChange(entityListName, rowKey, entities.get(index));
            }
          }
        }
        catch (Exception e)
        {
          // assumed iterator binding expression is wrong, just do nothing
          sLog.info("Cannot find " + iteratorBindingName +
                    " binding, UI might not refresh correctly when form layout is used.");
        }
  }


}
