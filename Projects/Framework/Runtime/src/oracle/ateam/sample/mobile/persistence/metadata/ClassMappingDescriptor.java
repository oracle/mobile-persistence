/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 20-mar-2015   Steven Davelaar
 1.4           Allow runtime config of showWebServiceInvocationErrors
 15-sep-2014   Steven Davelaar
 1.3           Use Utility.loadClass rather than loading through context class loader, 
               the latter can cause app to hang when used in feature lifecycle listener activate method
 30-may-2014   Steven Davelaar
 1.2           added ability to change orderBy at runtime
 13-dec-2013   Steven Davelaar
 1.1           added support for orderBy
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.XmlAnyDefinition;

import oracle.ateam.sample.mobile.persistence.manager.DBPersistenceManager;

/**
 * Class that returns entity-level information about the mapping of this entity to a database table.
 *
 * The information is read from the persistenceMapping xml file as configured in
 * mobile-persistence-config.properties 
 * 
 * @deprecated Use the class with same name in oracle.ateam.sample.mobile.v2.persistence.* instead
 */
public class ClassMappingDescriptor
  extends XmlAnyDefinition
{
  private static final String FIND_ALL_METHOD = "find-all-method";
  private static final String FIND_ALL_IN_PARENT_METHOD = "find-all-in-parent-method";
  private static final String GET_AS_PARENT_METHOD = "get-as-parent-method";
  private static final String GET_CANONICAL_METHOD = "get-canonical-method";
  private static final String FIND_METHOD = "find-method";
  private static final String CREATE_METHOD = "create-method";
  private static final String UPDATE_METHOD = "update-method";
  private static final String MERGE_METHOD = "merge-method";
  private static final String REMOVE_METHOD = "remove-method";
  private static final String CUSTOM_METHOD = "custom-method";
  private ArrayList attributeMappings = null;
  private ArrayList attributeMappingsDirect = null;
  private ArrayList attributeMappingsOneToOne = null;
  private ArrayList attributeMappingsOneToMany = null;
  private String orderBy = null;


  public static ClassMappingDescriptor getInstance(Class clazz)
  {
    return getInstance(clazz.getName());
  }

  public static ClassMappingDescriptor getInstance(String className)
  {
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = mapping.findClassMappingDescriptor(className);
    return descriptor;
  }

  public ClassMappingDescriptor(XmlAnyDefinition xmlAnyDefinition, String[] strings, String[] strings1)
  {
    super(xmlAnyDefinition, strings, strings1);
  }

  public ClassMappingDescriptor(XmlAnyDefinition xmlAnyDefinition)
  {
    super(xmlAnyDefinition);
  }

  public ClassMappingDescriptor()
  {
    super();
  }

  public String getClassName()
  {
    return getChildDefinition("class").getText();
  }

  public String getCRUDServiceClassName()
  {
    XmlAnyDefinition serviceNode = getChildDefinition("crud-service-class");
    String className = null;
    if (serviceNode!=null && serviceNode.getChildDefinition("class-name")!=null)
    {
      className = serviceNode.getChildDefinition("class-name").getText();      
    }
    else
    {
      // old XML structure: text node
       className = serviceNode.getText();      
    }
    return className;
  }

  public String getLocalPersistenceManagerClassName()
  {
    // old XML structure: text node
    String className = DBPersistenceManager.class.getName();
    XmlAnyDefinition serviceNode = getChildDefinition("crud-service-class");
    if (serviceNode!=null)
    {
      XmlAnyDefinition node= serviceNode.getChildDefinition("local-persistence-manager");
      if (node!=null)
      {
        className = node.getText();
      }      
    }
    return className;
  }

  public String getRemotePersistenceManagerClassName()
  {
    String className = null;
    XmlAnyDefinition serviceNode = getChildDefinition("crud-service-class");
    if (serviceNode!=null)
    {
      XmlAnyDefinition node= serviceNode.getChildDefinition("remote-persistence-manager");
      if (node!=null)
      {
        className = node.getText();
      }
    }
    return className;
  }

  public boolean isRemoteReadInBackground()
  {
    // old XML structure: text node
    boolean value = true;
    XmlAnyDefinition serviceNode = getChildDefinition("crud-service-class");
    if (serviceNode!=null)
    {
      XmlAnyDefinition node= serviceNode.getChildDefinition("remote-read-in-background");
      if (node!=null)
      {
        value = "true".equals(node.getText());
      }
    }  
    return value;
  }

  public boolean isRemoteWriteInBackground()
  {
    // old XML structure: text node
    boolean value = true;
    XmlAnyDefinition serviceNode = getChildDefinition("crud-service-class");
    if (serviceNode!=null)
    {
      XmlAnyDefinition node= serviceNode.getChildDefinition("remote-write-in-background");
      if (node!=null)
      {
        value = "true".equals(node.getText());
      }
    }  
    return value;
  }

  public boolean isAutoIncrementPrimaryKey()
  {
    boolean value = true;
    XmlAnyDefinition serviceNode = getChildDefinition("crud-service-class");
    if (serviceNode!=null)
    {
      XmlAnyDefinition node= serviceNode.getChildDefinition("auto-increment-primary-key");
      if (node!=null)
      {
        value = "true".equals(node.getText());
      }
    } 
    return value;
  }

  /**
   * returns the value of show-web-service-invocation-errors in persistenceMapping.xml. If the value is false
   * it also checks for EL expression #{applicationScope.showWebServiceInvocationErrors} allowing you to
   * temporarily show web service invocation errors at runtime.
   * @return
   */
    public boolean isShowWebServiceInvocationErrors()
    {
      boolean value = true;
      XmlAnyDefinition serviceNode = getChildDefinition("crud-service-class");
      if (serviceNode!=null)
      {
        XmlAnyDefinition node= serviceNode.getChildDefinition("show-web-service-invocation-errors");
        if (node!=null)
        {
          value = "true".equals(node.getText());
        }
      } 
      if (!value)
      {
        Object elValue = AdfmfJavaUtilities.evaluateELExpression("#{applicationScope.showWebServiceInvocationErrors}");
        if (elValue!=null)
        {
          value = (Boolean)elValue; 
        }      
      }
      return value;
    }
    
    public boolean isAutoQuery()
    {
      // old XML structure: text node
      boolean value = true;
      XmlAnyDefinition serviceNode = getChildDefinition("crud-service-class");
      if (serviceNode!=null)
      {
        XmlAnyDefinition node= serviceNode.getChildDefinition("auto-query");
        if (node!=null)
        {
          value = "true".equals(node.getText());
        }
      }  
      return value;
    }

  /**
   * Returns orderBy as defined in persistenceMapping.xml, or when setOrderByhas been
   * called, it returns this overridden orderBy.
   * @return
   */
  public String getOrderBy()
  { 
    if (this.orderBy!=null)
    {
      return orderBy;
    }
    XmlAnyDefinition xmlOrderBy = getChildDefinition("order-by");
    return xmlOrderBy!=null && !"".equals(xmlOrderBy) ? xmlOrderBy.getText() : null;
  }

  /**
   * Override the default order by as set in peristenceMapping.xml.
   * When calling this method with null as argument, the default order by will be used
   * again.
   * @param orderBy
   */
  public void setOrderBy(String orderBy)
  {
    this.orderBy = orderBy;
  }

  public Class getClazz()
  {
    try
    {
      return Utility.loadClass(getClassName());
    }
    catch (ClassNotFoundException e)
    {
      throw new AdfException(e);
    }
  }

  public String getTableName()
  {
    return getChildDefinition("table")!=null ? (String) getChildDefinition("table").getText() : null;
  }

  public boolean isPersisted()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition("persisted");
    return anyDefinition!=null ? !("false".equalsIgnoreCase(anyDefinition.getText())) : true;    
  }

  public List getAttributeMappings()
  {
    if (attributeMappings == null)
    {
      attributeMappings = new ArrayList();
      attributeMappingsDirect = new ArrayList();
      attributeMappingsOneToOne = new ArrayList();
      attributeMappingsOneToMany = new ArrayList();
      XmlAnyDefinition classDescriptorsContainer = this.getChildDefinition("attribute-mappings");
      List descriptors = classDescriptorsContainer.getChildDefinitions("attribute-mapping");
      List keyColumnNames = Arrays.asList(getPrimaryKeyColumnNames());
      for (int i = 0; i < descriptors.size(); i++)
      {
        XmlAnyDefinition descriptor = (XmlAnyDefinition) descriptors.get(i);
        AttributeMapping instance = null;
        String mappingType = (String) descriptor.getAttributeValue("xsi:type");
        if (mappingType==null)
        {
          mappingType = (String) descriptor.getAttributeValue("type");
        }
        if ("direct-mapping".equals(mappingType))
        {
          instance = new AttributeMappingDirect(descriptor);
          attributeMappings.add(instance);
          attributeMappingsDirect.add(instance);
        }
        else if ("one-to-one-mapping".equals(mappingType))
        {
          instance = new AttributeMappingOneToOne(descriptor);
          attributeMappings.add(instance);
          attributeMappingsOneToOne.add(instance);
        }
        else if ("one-to-many-mapping".equals(mappingType))
        {
          instance = new AttributeMappingOneToMany(descriptor);
          attributeMappings.add(instance);
          attributeMappingsOneToMany.add(instance);
        }
        instance.setClassMappingDescriptor(this);
        if (keyColumnNames.contains(instance.getColumnName()))
        {
          instance. setPrimaryKeyMapping(true);
        }
      }
    }
    return attributeMappings;
  }

  public List getAttributeMappingsDirect()
  {
    if (attributeMappings == null)
    {
      getAttributeMappings();
    }
    return attributeMappingsDirect;
  }

  public List getAttributeMappingsDirectParentPopulated()
  {
    List ppMappings = new ArrayList();
    List directs = getAttributeMappingsDirect();
    for (int i = 0; i < directs.size(); i++)
    {
      AttributeMappingDirect mapping = (AttributeMappingDirect) directs.get(i);
      if (mapping.getParentAttribute()!=null)
      {
        ppMappings.add(mapping);
      }
    }
    return ppMappings;
  }

  public List getAttributeMappingsOneToOne()
  {
    if (attributeMappings == null)
    {
      getAttributeMappings();
    }
    return attributeMappingsOneToOne;
  }

  public List getAttributeMappingsDirectAndOneToOne()
  {
    List mappings = new ArrayList();
    mappings.addAll(getAttributeMappingsDirect());
    mappings.addAll(getAttributeMappingsOneToOne());
    return mappings;
  }

  public List getAttributeMappingsOneToMany()
  {
    if (attributeMappings == null)
    {
      getAttributeMappings();
    }
    return attributeMappingsOneToMany;
  }

  public AttributeMapping findAttributeMappingByName(String attributeName)
  {
    AttributeMapping found = null;
    List descriptors = getAttributeMappings();
    for (int i = 0; i < descriptors.size(); i++)
    {
      AttributeMapping mapping = (AttributeMapping) descriptors.get(i);
      if (mapping.getAttributeName().equals(attributeName))
      {
        found = mapping;
        break;
      }
    }
    return found;
  }

  public AttributeMapping findAttributeMappingByPayloadName(String payloadName)
  {
    AttributeMapping found = null;
    List descriptors = getAttributeMappings();
    for (int i = 0; i < descriptors.size(); i++)
    {
      AttributeMapping mapping = (AttributeMapping) descriptors.get(i);
      if (payloadName.equals(mapping.getAttributeNameInPayload()))      
      {
        found = mapping;
        break;
      }
    }
    return found;
  }

  public AttributeMapping findAttributeMappingByColumnName(String columnName)
  {
    AttributeMapping found = null;
    List descriptors = getAttributeMappings();
    for (int i = 0; i < descriptors.size(); i++)
    {
      AttributeMapping mapping = (AttributeMapping) descriptors.get(i);
      if (columnName.equals(mapping.getColumnName()))
      {
        found = mapping;
        break;
      }
    }
    return found;
  }


  /**
   * Use this method for composite PK 
   * @return
   */
  public String[] getPrimaryKeyColumnNames()
  {
    List fields = getChildDefinition("primary-key").getChildDefinitions("column-name");
    String[] attrs = new String[fields.size()];
    for (int i = 0; i < fields.size(); i++)
    {
      XmlAnyDefinition field = (XmlAnyDefinition) fields.get(i);
      attrs[i] = (String) field.getText();      
    }
    return attrs;
  }

  /**
   * Use this method for composite PK 
   * @return
   */
  public AttributeMapping[] getPrimaryKeyAttributeMappings()
  {
    String[] cols = getPrimaryKeyColumnNames();
    AttributeMapping[] mappings = new AttributeMapping[cols.length];
    for (int i = 0; i < cols.length; i++)
    {
      mappings[i] = findAttributeMappingByColumnName(cols[i]);
    }
    return mappings;
  }


  /**
   * Use this method for composite PK 
   * @return
   */
  public String[] getPrimaryKeyAttributeNames()
  {
    AttributeMapping[] mappings =  getPrimaryKeyAttributeMappings();
    String[] attrs = new String[mappings.length];
    for (int i = 0; i < attrs.length; i++)
    {
      attrs[i] = mappings[i].getAttributeName();
    }
    return attrs;
  }
  
  public String getDateFormat()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition("date-format");
    return anyDefinition!=null ? anyDefinition.getText() : "yyyy-MM-dd";
  }

  public String getDateTimeFormat()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition("date-time-format");
    return anyDefinition!=null ? anyDefinition.getText() : "yyyy-MM-dd hh:mm";
  }

  public String getFindAllMethodName()
  {
    return (String) (isCreateSupported() ? getChildDefinition(FIND_ALL_METHOD).getAttributeValue("name") : null);
  }

  public String getFindMethodName()
  {
    return (String) (isCreateSupported() ? getChildDefinition(FIND_METHOD).getAttributeValue("name") : null);
  }

  public String getCreateMethodName()
  {
    return (String) (isCreateSupported() ? getChildDefinition(CREATE_METHOD).getAttributeValue("name") : null);
  }

  public String getUpdateMethodName()
  {
    return (String) (isCreateSupported() ? getChildDefinition(UPDATE_METHOD).getAttributeValue("name") : null);
  }

  public String getMergeMethodName()
  {
    return (String) (isCreateSupported() ? getChildDefinition(MERGE_METHOD).getAttributeValue("name") : null);
  }

  public String getRemoveMethodName()
  {
    return (String) (isCreateSupported() ? getChildDefinition(REMOVE_METHOD).getAttributeValue("name") : null);
  }

  public boolean isFindAllSupported()
  {
    return this.getChildDefinition(FIND_ALL_METHOD)!=null;    
  }

  public boolean isFindAllInParentSupported(String accessorAttribute)
  {
    List methods = getChildDefinitions(FIND_ALL_IN_PARENT_METHOD);
    boolean found = false;
    if (methods!=null)
    {
      for (int i = 0; i < methods.size(); i++)
      {
        XmlAnyDefinition method = (XmlAnyDefinition) methods.get(i);
        if (accessorAttribute.equals(method.getAttributeValue("name")))
        {
          found = true;
          break;
        }
      }
    }
    return found;
  }

  public boolean isGetAsParentSupported(String accessorAttribute)
  {
    List methods = getChildDefinitions(GET_AS_PARENT_METHOD);
    boolean found = false;
    if (methods!=null)
    {
      for (int i = 0; i < methods.size(); i++)
      {
        XmlAnyDefinition method = (XmlAnyDefinition) methods.get(i);
        if (accessorAttribute.equals(method.getAttributeValue("name")))
        {
          found = true;
          break;
        }
      }
    }
    return found;
  }

  public boolean isFindSupported()
  {
    return this.getChildDefinition(FIND_METHOD)!=null;    
  }

  public boolean isGetCanonicalSupported()
  {
    return this.getChildDefinition(GET_CANONICAL_METHOD)!=null;    
  }

  public boolean isCreateSupported()
  {
    return this.getChildDefinition(CREATE_METHOD)!=null || isMergeSupported();    
  }

  public boolean isUpdateSupported()
  {
    return this.getChildDefinition(UPDATE_METHOD)!=null || isMergeSupported();    
  }

  public boolean isMergeSupported()
  {
    return this.getChildDefinition(MERGE_METHOD)!=null;    
  }

  public boolean isRemoveSupported()
  {
    return this.getChildDefinition(REMOVE_METHOD)!=null;    
  }
  
  public Method getMethod(String methodName)
  {
    String[] elemNames = new String[]{FIND_ALL_METHOD,FIND_METHOD, CREATE_METHOD, UPDATE_METHOD,MERGE_METHOD,REMOVE_METHOD};
    return findMatchingMethod(elemNames, methodName);
  }
  
  public Method getFindAllMethod()
  {
    return isFindAllSupported() ? new Method(getChildDefinition(FIND_ALL_METHOD)) : null;
  }

  public Method getFindAllInParentMethod(String accessorAttribute)
  {
    List methods = getChildDefinitions(FIND_ALL_IN_PARENT_METHOD);
    Method method = null;
    if (methods!=null)
    {
      for (int i = 0; i < methods.size(); i++)
      {
        XmlAnyDefinition xmlMethod = (XmlAnyDefinition) methods.get(i);
        if (accessorAttribute.equals(xmlMethod.getAttributeValue("name")))
        {
          method = new Method(xmlMethod);
          break;
        }
      }
    }
    return method;
  }

  public Method getGetAsParentMethod(String accessorAttribute)
  {
    List methods = getChildDefinitions(GET_AS_PARENT_METHOD);
    Method method = null;
    if (methods!=null)
    {
      for (int i = 0; i < methods.size(); i++)
      {
        XmlAnyDefinition xmlMethod = (XmlAnyDefinition) methods.get(i);
        if (accessorAttribute.equals(xmlMethod.getAttributeValue("name")))
        {
          method = new Method(xmlMethod);
          break;
        }
      }
    }
    return method;
  }

  public Method getGetCanonicalMethod()
  {
    return isGetCanonicalSupported() ? new Method(getChildDefinition(GET_CANONICAL_METHOD)) : null;
  }

  public Method getFindMethod()
  {
    return isFindSupported() ? new Method(getChildDefinition(FIND_METHOD)) : null;
  }

  public Method getCreateMethod()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition(CREATE_METHOD);
    return anyDefinition!=null ? new Method(anyDefinition) : null;
  }

  public Method getUpdateMethod()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition(UPDATE_METHOD);
    return anyDefinition!=null ? new Method(anyDefinition) : null;
  }

  public Method getMergeMethod()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition(MERGE_METHOD);
    return anyDefinition!=null ? new Method(anyDefinition) : null;
  }

  public Method getRemoveMethod()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition(REMOVE_METHOD);
    return anyDefinition!=null ? new Method(anyDefinition) : null;
  }

  public Method getCustomMethod(String methodName)
  {
    List methods = getChildDefinitions(CUSTOM_METHOD);
    Method method = null;
    if (methods!=null)
    {
      for (int i = 0; i < methods.size(); i++)
      {
        XmlAnyDefinition xmlMethod = (XmlAnyDefinition) methods.get(i);
        if (methodName.equals(xmlMethod.getAttributeValue("name")))
        {
          method = new Method(xmlMethod);
          break;
        }
      }
    }
    return method;
  }

  public boolean isDeleteLocalRowsOnFindAll()
  {
    return isFindAllSupported() && "true".equals(getChildDefinition(FIND_ALL_METHOD).getAttributeValue("deleteLocalRows"));
  }
      
  private Method findMatchingMethod(String[] elemNames, String methodName)
  {
    Method method = null;
    for (int i = 0; i < elemNames.length; i++)
    {
      String elemName = elemNames[i];
      XmlAnyDefinition methodElem = getChildDefinition(elemName);
      if (methodElem!=null && methodElem.getAttributeValue("name").equals(methodName))
      {
        method = new Method(methodElem);
        break;
      }
    }
    return method;
  }
}
