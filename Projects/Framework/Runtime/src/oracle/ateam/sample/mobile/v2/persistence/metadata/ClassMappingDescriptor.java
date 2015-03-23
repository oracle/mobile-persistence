 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  19-mar-2015   Steven Davelaar
  1.1           - Default dateTimeFormat to dateFormat
                - Allow runtime config of showWebServiceInvocationErrors
                - Added isEnableOfflineTransactions
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;

import oracle.adfmf.util.Utility;
import oracle.adfmf.util.XmlAnyDefinition;

import oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager;

/**
 * Class that returns entity-level information about the mapping of this entity to a database table.
 *
 * The information is read from the persistenceMapping xml file as configured in
 * mobile-persistence-config.properties 
 */
public class ClassMappingDescriptor
  extends XmlAnyDefinition
{
  private static final String FIND_ALL_METHOD = "findAllMethod";
  private static final String FIND_ALL_IN_PARENT_METHOD = "findAllInParentMethod";
  private static final String GET_AS_PARENT_METHOD = "getAsParentMethod";
  private static final String GET_CANONICAL_METHOD = "getCanonicalMethod";
  private static final String FIND_METHOD = "findMethod";
  private static final String CREATE_METHOD = "createMethod";
  private static final String UPDATE_METHOD = "updateMethod";
  private static final String MERGE_METHOD = "mergeMethod";
  private static final String REMOVE_METHOD = "removeMethod";
  private static final String CUSTOM_METHOD = "customMethod";
  private List<AttributeMapping> attributeMappings = null;
  private List<AttributeMappingDirect> attributeMappingsDirect = null;
  private List<AttributeMappingOneToOne> attributeMappingsOneToOne = null;
  private List<AttributeMappingOneToMany> attributeMappingsOneToMany = null;
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
    return getAttributeStringValue("className");
  }

  public String getCRUDServiceClassName()
  {
    XmlAnyDefinition serviceNode = getCrudServiceClassNode();
    String className = null;
    if (serviceNode!=null)
    {
      className = serviceNode.getAttributeStringValue("className");      
    }
    return className;
  }

  protected XmlAnyDefinition getCrudServiceClassNode()
  {
    return getChildDefinition("crudServiceClass");
  }

  public String getLocalPersistenceManagerClassName()
  {
    // old XML structure: text node
    String className = DBPersistenceManager.class.getName();
    XmlAnyDefinition serviceNode = getCrudServiceClassNode();
    if (serviceNode!=null)
    {
        className = serviceNode.getAttributeStringValue("localPersistenceManager");
    }
    return className;
  }

  public String getRemotePersistenceManagerClassName()
  {
    String className = null;
    XmlAnyDefinition serviceNode = getCrudServiceClassNode();
    if (serviceNode!=null)
    {
      className = serviceNode.getAttributeStringValue("remotePersistenceManager");
    }
    return className;
  }

  public boolean isRemoteReadInBackground()
  {
    // old XML structure: text node
    boolean value = true;
    XmlAnyDefinition serviceNode = getCrudServiceClassNode();
    if (serviceNode!=null)
    {
      value = serviceNode.getAttributeBooleanValue("remoteReadInBackground",true);
    }  
    return value;
  }

  public boolean isRemoteWriteInBackground()
  {
    // old XML structure: text node
    boolean value = true;
    XmlAnyDefinition serviceNode = getCrudServiceClassNode();
    if (serviceNode!=null)
    {
      value = serviceNode.getAttributeBooleanValue("remoteWriteInBackground",true);
    }  
    return value;
  }

  public boolean isAutoIncrementPrimaryKey()
  {
    boolean value = true;
    XmlAnyDefinition serviceNode = getCrudServiceClassNode();
    if (serviceNode!=null)
    {
      value = serviceNode.getAttributeBooleanValue("autoIncrementPrimaryKey",true);
    } 
    return value;
  }

  /**
   * returns the value of showWebServiceInvocationErrors in persistence-mapping.xml. If the value is false
   * it also checks for EL expression #{applicationScope.showWebServiceInvocationErrors} allowing you to
   * temporarily show web service invocation errors at runtime.
   * @return
   */
    public boolean isShowWebServiceInvocationErrors()
    {
      boolean value = true;
      XmlAnyDefinition serviceNode = getCrudServiceClassNode();
      if (serviceNode!=null)
      {
        value = serviceNode.getAttributeBooleanValue("showWebServiceInvocationErrors",true);
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
      XmlAnyDefinition serviceNode = getCrudServiceClassNode();
      if (serviceNode!=null)
      {
        value = serviceNode.getAttributeBooleanValue("autoQuery",true);
      }  
      return value;
    }

  public boolean isEnableOfflineTransactions()
  {
    // old XML structure: text node
    boolean value = true;
    XmlAnyDefinition serviceNode = getCrudServiceClassNode();
    if (serviceNode!=null)
    {
      value = serviceNode.getAttributeBooleanValue("enableOfflineTransactions",true);
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
    String orderBy = getAttributeStringValue("orderBy");
    return !"".equals(orderBy) ? orderBy : null;
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
    return getChildDefinition("table")!=null ? (String) getChildDefinition("table").getAttributeStringValue("name") : null;
  }

  public boolean isPersisted()
  {
    return  !("false".equalsIgnoreCase(getAttributeStringValue("persisted")));
  }

  public List<AttributeMapping> getAttributeMappings()
  {
    if (attributeMappings == null)
    {
      attributeMappings = new ArrayList<AttributeMapping>();
      attributeMappingsDirect = new ArrayList<AttributeMappingDirect>();
      attributeMappingsOneToOne = new ArrayList<AttributeMappingOneToOne>();
      attributeMappingsOneToMany = new ArrayList<AttributeMappingOneToMany>();
      XmlAnyDefinition attributeMappingsNode = this.getChildDefinition("attributeMappings");
      attributeMappingsDirect = new ArrayList<AttributeMappingDirect>();
      List<XmlAnyDefinition> directNodes = attributeMappingsNode.getChildDefinitions("directMapping");
      for (XmlAnyDefinition directNode : directNodes)
      {
        AttributeMappingDirect mapping = new AttributeMappingDirect(directNode);
        attributeMappingsDirect.add(mapping);
        mapping.setClassMappingDescriptor(this);
      }

      List<XmlAnyDefinition> oneToOneNodes = attributeMappingsNode.getChildDefinitions("oneToOneMapping");
      for (XmlAnyDefinition oneToOneNode : oneToOneNodes)
      {
        AttributeMappingOneToOne mapping = new AttributeMappingOneToOne(oneToOneNode);
        attributeMappingsOneToOne.add(mapping);
        mapping.setClassMappingDescriptor(this);
      }

      List<XmlAnyDefinition> oneToManyNodes = attributeMappingsNode.getChildDefinitions("oneToManyMapping");
      for (XmlAnyDefinition oneToManyNode : oneToManyNodes)
      {
        AttributeMappingOneToMany mapping = new AttributeMappingOneToMany(oneToManyNode);
        attributeMappingsOneToMany.add(mapping);
        mapping.setClassMappingDescriptor(this);
      }
      attributeMappings.addAll(attributeMappingsDirect);
      attributeMappings.addAll(attributeMappingsOneToOne);
      attributeMappings.addAll(attributeMappingsOneToMany);
    }
    return attributeMappings;
  }

  public List<AttributeMappingDirect> getAttributeMappingsDirect()
  {
    if (attributeMappings == null)
    {
      getAttributeMappings();
    }
    return attributeMappingsDirect;
  }

  public List<AttributeMappingDirect> getAttributeMappingsDirectParentPopulated()
  {
    List<AttributeMappingDirect> ppMappings = new ArrayList<AttributeMappingDirect>();
    List<AttributeMappingDirect> directs = getAttributeMappingsDirect();
    for (AttributeMappingDirect mapping : directs)
    {
      if (mapping.getParentAttribute()!=null)
      {
        ppMappings.add(mapping);
      }
    }
    return ppMappings;
  }

  public List<AttributeMappingOneToOne> getAttributeMappingsOneToOne()
  {
    if (attributeMappings == null)
    {
      getAttributeMappings();
    }
    return attributeMappingsOneToOne;
  }

  public List<AttributeMapping> getAttributeMappingsDirectAndOneToOne()
  {
    List<AttributeMapping> mappings = new ArrayList<AttributeMapping>();
    mappings.addAll(getAttributeMappingsDirect());
    mappings.addAll(getAttributeMappingsOneToOne());
    return mappings;
  }

  public List<AttributeMappingOneToMany> getAttributeMappingsOneToMany()
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
    List<AttributeMapping> mappings = getAttributeMappings();
    for (AttributeMapping mapping : mappings)
    {
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
    List<AttributeMapping> mappings = getAttributeMappings();
    for (AttributeMapping mapping : mappings)
    {
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
    List<AttributeMapping> mappings = getAttributeMappings();
    for (AttributeMapping mapping : mappings)
    {
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
  public List<String> getPrimaryKeyColumnNames()
  {
    List<XmlAnyDefinition> fields = getChildDefinition("table").getChildDefinitions("primaryKeyColumn");
    List<String> attrs = new ArrayList<String>();
    for (XmlAnyDefinition field : fields)
    {
      attrs.add(field.getAttributeStringValue("name"));      
    }
    return attrs;
  }

  /**
   * Use this method for composite PK 
   * @return
   */
  public List<AttributeMapping> getPrimaryKeyAttributeMappings()
  {
    List<AttributeMapping> keyAttrs = new ArrayList<AttributeMapping>();
    for (AttributeMapping mapping : getAttributeMappings())
    {
      if (mapping.isPrimaryKeyMapping())
      {
        keyAttrs.add(mapping);
      }
    }
    return keyAttrs;
  }


  /**
   * Use this method for composite PK 
   * @return
   */
  public List<String> getPrimaryKeyAttributeNames()
  {
    List<AttributeMapping> mappings =  getPrimaryKeyAttributeMappings();
    List<String> attrs = new ArrayList<String>();
    for (AttributeMapping mapping : mappings)
    {
      attrs.add(mapping.getAttributeName());
    }
    return attrs;
  }
  
  public String getDateFormat()
  {
    return getAttributeStringValue("dateFormat");
  }

  public String getDateTimeFormat()
  {
    String format = getAttributeStringValue("dateTimeFormat");
    return format!=null ? format : getDateFormat();
  }

  public XmlAnyDefinition getMethodNode(String methodName)
  {
    if (getChildDefinition("methods")!=null)
    {
      return getChildDefinition("methods").getChildDefinition(methodName);      
    }
    return null;
  }

  public List<XmlAnyDefinition> getMethodNodes(String methodName)
  {
    if (getChildDefinition("methods")!=null)
    {
      return getChildDefinition("methods").getChildDefinitions(methodName);      
    }
    return new ArrayList<XmlAnyDefinition>();
  }

  public String getFindAllMethodName()
  {
    return (String) (isCreateSupported() ? getMethodNode(FIND_ALL_METHOD).getAttributeValue("name") : null);
  }

  public String getFindMethodName()
  {
    return (String) (isCreateSupported() ? getMethodNode(FIND_METHOD).getAttributeValue("name") : null);
  }

  public String getCreateMethodName()
  {
    return (String) (isCreateSupported() ? getMethodNode(CREATE_METHOD).getAttributeValue("name") : null);
  }

  public String getUpdateMethodName()
  {
    return (String) (isCreateSupported() ? getMethodNode(UPDATE_METHOD).getAttributeValue("name") : null);
  }

  public String getMergeMethodName()
  {
    return (String) (isCreateSupported() ? getMethodNode(MERGE_METHOD).getAttributeValue("name") : null);
  }

  public String getRemoveMethodName()
  {
    return (String) (isCreateSupported() ? getMethodNode(REMOVE_METHOD).getAttributeValue("name") : null);
  }

  public boolean isFindAllSupported()
  {
    return this.getMethodNode(FIND_ALL_METHOD)!=null;    
  }

  public boolean isFindAllInParentSupported(String accessorAttribute)
  {
    List<XmlAnyDefinition> methods = getMethodNodes(FIND_ALL_IN_PARENT_METHOD);
    boolean found = false;
    for (XmlAnyDefinition method : methods)
    {
      if (accessorAttribute.equals(method.getAttributeValue("name")))
      {
        found = true;
        break;
      }
    }
    return found;
  }

  public boolean isGetAsParentSupported(String accessorAttribute)
  {
    List<XmlAnyDefinition> methods = getMethodNodes(GET_AS_PARENT_METHOD);
    boolean found = false;
    for (XmlAnyDefinition method : methods)
    {
      if (accessorAttribute.equals(method.getAttributeValue("name")))
      {
        found = true;
        break;
      }
    }
    return found;
  }

  public boolean isFindSupported()
  {
    return this.getMethodNode(FIND_METHOD)!=null;    
  }

  public boolean isGetCanonicalSupported()
  {
    return this.getMethodNode(GET_CANONICAL_METHOD)!=null;    
  }

  public boolean isCreateSupported()
  {
    return this.getMethodNode(CREATE_METHOD)!=null || isMergeSupported();    
  }

  public boolean isUpdateSupported()
  {
    return this.getMethodNode(UPDATE_METHOD)!=null || isMergeSupported();    
  }

  public boolean isMergeSupported()
  {
    return this.getMethodNode(MERGE_METHOD)!=null;    
  }

  public boolean isRemoveSupported()
  {
    return this.getMethodNode(REMOVE_METHOD)!=null;    
  }
  
  public Method getMethod(String methodName)
  {
    String[] elemNames = new String[]{FIND_ALL_METHOD,FIND_METHOD, CREATE_METHOD, UPDATE_METHOD,MERGE_METHOD,REMOVE_METHOD};
    return findMatchingMethod(elemNames, methodName);
  }
  
  public Method getFindAllMethod()
  {
    return isFindAllSupported() ? new Method(getMethodNode(FIND_ALL_METHOD)) : null;
  }

  public Method getFindAllInParentMethod(String accessorAttribute)
  {
    List<XmlAnyDefinition> xmlMethods = getMethodNodes(FIND_ALL_IN_PARENT_METHOD);
    Method method = null;
    for (XmlAnyDefinition xmlMethod : xmlMethods)
    {
      if (accessorAttribute.equals(xmlMethod.getAttributeValue("name")))
      {
        method = new Method(xmlMethod);
        break;
      }
    }
    return method;
  }

  public Method getGetAsParentMethod(String accessorAttribute)
  {
    List<XmlAnyDefinition> xmlMethods = getMethodNodes(GET_AS_PARENT_METHOD);
    Method method = null;
    for (XmlAnyDefinition xmlMethod : xmlMethods)
    {  
      if (accessorAttribute.equals(xmlMethod.getAttributeValue("name")))
      {
        method = new Method(xmlMethod);
        break;
      }
    }
    return method;
  }

  public Method getGetCanonicalMethod()
  {
    return isGetCanonicalSupported() ? new Method(getMethodNode(GET_CANONICAL_METHOD)) : null;
  }

  public Method getFindMethod()
  {
    return isFindSupported() ? new Method(getMethodNode(FIND_METHOD)) : null;
  }

  public Method getCreateMethod()
  {
    XmlAnyDefinition anyDefinition = getMethodNode(CREATE_METHOD);
    return anyDefinition!=null ? new Method(anyDefinition) : null;
  }

  public Method getUpdateMethod()
  {
    XmlAnyDefinition anyDefinition = getMethodNode(UPDATE_METHOD);
    return anyDefinition!=null ? new Method(anyDefinition) : null;
  }

  public Method getMergeMethod()
  {
    XmlAnyDefinition anyDefinition = getMethodNode(MERGE_METHOD);
    return anyDefinition!=null ? new Method(anyDefinition) : null;
  }

  public Method getRemoveMethod()
  {
    XmlAnyDefinition anyDefinition = getMethodNode(REMOVE_METHOD);
    return anyDefinition!=null ? new Method(anyDefinition) : null;
  }

  public Method getCustomMethod(String methodName)
  {
    List<XmlAnyDefinition> methods = getMethodNodes(CUSTOM_METHOD);
    Method method = null;
    for (XmlAnyDefinition xmlMethod : methods)
    {
      if (methodName.equals(xmlMethod.getAttributeValue("name")))
      {
        method = new Method(xmlMethod);
        break;
      }
    }
    return method;
  }

  public boolean isDeleteLocalRowsOnFindAll()
  {
    return isFindAllSupported() && getMethodNode(FIND_ALL_METHOD).getAttributeBooleanValue("deleteLocalRows");
  }
      
  private Method findMatchingMethod(String[] elemNames, String methodName)
  {
    Method method = null;
    for (String elemName : elemNames)
    {
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
