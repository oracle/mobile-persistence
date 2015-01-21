 /*******************************************************************************
  Copyright © 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.service;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import oracle.adfmf.framework.api.JSONBeanSerializationHelper;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.util.Utility;

import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.util.MessageUtils;


public class DataSynchAction extends Entity
{
  public static final String INSERT_ACTION = "Create";
  public static final String UPDATE_ACTION = "Update";
  public static final String REMOVE_ACTION = "Remove";
  public static final String CUSTOM_ACTION = "Custom";
  
  private Integer id;
  private String entityClassString;
  private String serviceClass;
  private transient Entity entity;
  private String entityAsJSONString;
  private String action;
  private String customMethodName;

  private Date dateCreated;
  private Date lastSynchAttempt;
  private String lastSynchError;
  private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  
  // dummy attrs to prevent json serialization of convenience get methods 
  private transient Object keyValue;
  private transient String[] attributeNames;
  private transient Map attributeValues;
  private transient Class entityClass;
  private transient String data;

  public DataSynchAction()
  {
    super();
  }

  public DataSynchAction(String action, Entity entity, String serviceClass)
  {
    super();
    this.action = action;
    this.entityClassString = entity.getClass().getName();
    this.serviceClass = serviceClass;
    // we do not store the enity passed in directly, because it might be changed later on, and
    // we need to save the state as it is now. So we convert it to jsonobject, which we need anyway for
    // serialization, and then we convert it back to entity instance
//    this.entity = entity;
    try
    {
            
      String json = JSONBeanSerializationHelper.toJSON(entity).toString();
      setEntityAsJSONString(json);
      createEntityFromJSONString();
    }
    catch (Exception e)
    {
      MessageUtils.handleError(e);
    }
    this.dateCreated = new Date();
    this.lastSynchAttempt = new Date();
  }

  public void setEntityClassString(String entityClassString)
  {
    this.entityClassString = entityClassString;
  }

  public String getEntityClassString()
  {
    return entityClassString;
  }

  public String getEntityShortName()
  {
    int pos = entityClassString.lastIndexOf(".");
    return entityClassString.substring(pos+1);
  }

  public Class getEntityClass()
  {
    try
    {
      return Utility.loadClass(getEntityClassString());
    }
    catch (ClassNotFoundException e)
    {
      MessageUtils.handleError(e);
    }
    return null;
  }

  public void setAction(String action)
  {
    this.action = action;
  }

  public String getAction()
  {
    return action;
  }

  public void setLastSynchAttempt(Date lastAttempt)
  {
    Date oldLastSynchAttempt = this.lastSynchAttempt;
    this.lastSynchAttempt = lastAttempt;
//    getPropertyChangeSupport().firePropertyChange("lastSynchAttempt", oldLastSynchAttempt, lastSynchAttempt);
  }

  public Date getLastSynchAttempt()
  {
    return lastSynchAttempt;
  }

  public void setDateCreated(Date dateCreated)
  {
    this.dateCreated = dateCreated;
  }

  public Date getDateCreated()
  {
    return dateCreated;
  }

  public void setEntity(Entity entity)
  {
    this.entity = entity;
  }

  public Entity getEntity()
  {
    return entity;
  }

  public void setLastSynchError(String lastSynchError)
  {
    String oldLastSynchError = this.lastSynchError;
    this.lastSynchError = lastSynchError;
//    getPropertyChangeSupport().firePropertyChange("lastSynchError", oldLastSynchError, lastSynchError);
  }

  public String getLastSynchError()
  {
    return lastSynchError;
  }
  
  public Map<String,Object> getAttributeValues()
  {
    Map<String,Object> attributeValues = EntityUtils.getEntityAttributeValues(getEntity());
    return attributeValues;
  }

  public String getData()
  {
    StringBuffer data = new StringBuffer("");
    Map<String,Object> values = getAttributeValues();
    Iterator<String> attrs = values.keySet().iterator();
    boolean firstAttr= true;
    while(attrs.hasNext())
    {
      String attr = attrs.next();
      Object value = values.get(attr);
      if (value!=null)
      {
        String stringValue = value.toString();
        if (value instanceof Date)
        {
          stringValue = value.toString();
        }
        if (!firstAttr)
        {
          data.append(", ");          
        }
        else
        {
          firstAttr= false;
        }
        data.append(stringValue);
      }
    }
    return data.toString();
  }

  public String[] getAttributeNames()
  {
    return (String[]) getAttributeValues().keySet().toArray();
  }

  public Object getKeyValue()
  {
    return EntityUtils.getEntityKey(getEntity());
  }

  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    propertyChangeSupport.addPropertyChangeListener(l);
  }

  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    propertyChangeSupport.removePropertyChangeListener(l);
  }

  public void setEntityAsJSONString(String entityAsJSONString)
  {
    this.entityAsJSONString = entityAsJSONString;
//    try
//    {
//      entity = (Entity) JSONBeanSerializationHelper.fromJSON(getEntityClass(), entityAsJSONString);
//    }
//    catch (Exception e)
//    {
//      // the question is what we do in this case,
//      // as the JSON data is invalid... This should just never happen... so let's throw message
//      // to end user
//      MessageUtils.handleError(e);
//    }
  }

  public void createEntityFromJSONString()
  {
    if (entityAsJSONString!=null)
    {
      try
      {
        entity = (Entity) JSONBeanSerializationHelper.fromJSON(getEntityClass(), entityAsJSONString);
      }
      catch (Exception e)
      {
        // the question is what we do in this case,
        // as the JSON data is invalid... This should just never happen... so let's throw message
        // to end user
        MessageUtils.handleError(e);
      }      
    }
  }

  public String getEntityAsJSONString()
  {
    return entityAsJSONString;
  }

  public void setId(Integer id)
  {
    this.id = id;
  }

  public Integer getId()
  {
    return id;
  }

  public boolean equals(Object obj)
  {
    if (obj instanceof DataSynchAction)
    {
      DataSynchAction other = (DataSynchAction) obj;
      return getId().equals(other.getId());
    }
    return false;
  }

  public void setCustomMethodName(String customMethodName)
  {
    this.customMethodName = customMethodName;
  }

  public String getCustomMethodName()
  {
    return customMethodName;
  }

  public void setServiceClass(String serviceClass)
  {
    this.serviceClass = serviceClass;
  }

  public String getServiceClass()
  {
    return serviceClass;
  }
  
}
