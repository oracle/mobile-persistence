/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import oracle.adf.model.adapter.dataformat.MethodDef;

import oracle.ateam.sample.mobile.dt.util.StringUtils;

import oracle.binding.meta.AccessorDefinition;
import oracle.binding.meta.AttributeDefinition;
import oracle.binding.meta.Definition;
import oracle.binding.meta.DefinitionContainer;
import oracle.binding.meta.NamedDefinition;
import oracle.binding.meta.OperationDefinition;
import oracle.binding.meta.StructureDefinition;

import oracle.ide.panels.TraversalException;

import oracle.javatools.db.Table;
import oracle.javatools.db.Column;

import oracle.jbo.common.JboNameUtil;

import oracle.toplink.workbench.addin.mappings.spi.db.JDeveloperTable;
import oracle.toplink.workbench.mappingsmodel.spi.db.ExternalColumn;

import org.w3c.dom.Node;


public class DataObjectInfo
{
  private Boolean generate = true;
  private String name;
  private String payloadListElementName;
  private String payloadRowElementName;
  private String className;
  private String serviceClassName;
  private String packageName;
  private String servicePackageName;
  private String accessorPath;
  private boolean collection;
  private DataObjectInfo parent = null;
  private List<AttributeInfo> attributeInfos;
  private String accessorMethodInParent;
  private String accessorMethodInChild;
  private List<AccessorInfo> children = new ArrayList<AccessorInfo>();
  private List<AccessorInfo> parents = new ArrayList<AccessorInfo>();
  private DCMethod findAllMethod;
  private List<DCMethod> findAllInParentMethods = new ArrayList<DCMethod>();
  private List<DCMethod> getAsParentMethods = new ArrayList<DCMethod>();
  private DCMethod findMethod;
  private DCMethod getCanonicalMethod;
  private DCMethod createMethod;
  private DCMethod updateMethod;
  private DCMethod mergeMethod;
  private DCMethod deleteMethod;
  private boolean crudMethodsInitialized = false;
  private boolean deleteLocalRows = true;
  private String sortOrder;
  private String payloadDateFormat;
  private String payloadDateTimeFormat;
  private String orderBy;
  private boolean xmlPayload = true;
  private boolean persisted = false;
  private boolean existing = false;

//  Map<AttributeInfo,AttributeInfo> parentAttrMappings = new HashMap<AttributeInfo,AttributeInfo>();
    
  public DataObjectInfo(NamedDefinition accessorDef, StructureDefinition beanDef, boolean isCollection,DataObjectInfo parent)
  {
    this.name = accessorDef.getName();
    this.payloadListElementName  = name;
    this.collection = isCollection;
    this.parent = parent;
//    deriveFindMethod(accessorDef);
    this.className = initClassName(name);

    attributeInfos = new ArrayList<AttributeInfo>();
    DefinitionContainer attributeDefinitions = beanDef.getAttributeDefinitions();
    
    Iterator iterator = attributeDefinitions.iterator();    
    while (iterator.hasNext())
    {
      AttributeDefinition attr = (AttributeDefinition) iterator.next();
      // JDev adds _ROWNUM__ATTR_ dummy attribute
      if (!attr.getName().startsWith("_ROWNUM"))
      {
        AttributeInfo attributeInfo = new AttributeInfo(attr);
        attributeInfos.add(attributeInfo);        
      }
    }
    cleanUpAttrNames();
    deriveAccessorPathAndFindMethod(accessorDef);

    if (getKeyAttributes().size()==0)
    {
      getAttributeDefs().get(0).setKeyAttribute(true);      
    }
    if (parent!=null)
    {
      parent.addChild(new AccessorInfo(parent,this));
    }
  }

  public DataObjectInfo(JDeveloperTable table)
  {
    this.collection = true;
    this.className = StringUtils.toCamelCase(table.getName());
    this.className = initClassName(className);
    this.name = table.getName();
    this.accessorPath = table.getName();

    attributeInfos = new ArrayList<AttributeInfo>();
    ExternalColumn[] columns =table.getColumns();
    for(ExternalColumn column : columns)
    {
      attributeInfos.add(new AttributeInfo(column));        
    }
    if (getKeyAttributes().size()==0)
    {
      getAttributeDefs().get(0).setKeyAttribute(true);      
    }
  }

  public DataObjectInfo(String name, String accessorPath)
  {
    this.collection = true;
    this.className = StringUtils.toCamelCase(name);
    this.className = initClassName(className);
    this.name = name;
    this.payloadListElementName = name;
    this.accessorPath = accessorPath;
    attributeInfos = new ArrayList<AttributeInfo>();
  }

  private String initClassName(String defaultName)
  {
    String name = defaultName;
    String[] prefixes = new String[]{"findAll","readAll","getAll","find","read","get"};
    for(String prefix: prefixes)
    {
      if (name.toUpperCase().startsWith(prefix.toUpperCase()))
      {
        name = name.substring(prefix.length());
        break;
      }
    }
    name = name.substring(0,1).toUpperCase()+name.substring(1);
    return name;
  }

  public void setServiceClassName(String serviceClassName)
  {
    this.serviceClassName = serviceClassName;
  }

  public String getServiceClassName()
  {
    if (serviceClassName==null)
    {
      return getClassName()+"Service";
    }
    return serviceClassName;
  }

  public String getFullyQualifiedClassName()
  {
    return getPackageName()+"."+getClassName();
  }

  public String getFullyQualifiedServiceClassName()
  {
    return getServicePackageName()+"."+getServiceClassName();
  }

  public void setPackageName(String packageName)
  {
    this.packageName = packageName;
  }

  public String getPackageName()
  {
    return packageName;
  }

  public void setServicePackageName(String servicePackageName)
  {
    this.servicePackageName = servicePackageName;
  }

  public String getServicePackageName()
  {
    return servicePackageName;
  }

  public void setExisting(boolean existing)
  {
    this.existing = existing;
  }

  public boolean isExisting()
  {
    return existing;
  }

  public void setGenerate(Boolean generate)
  {
    this.generate = generate;
  }

  public Boolean isGenerate()
  {
    return generate;
  }

  public void setName(String wsName)
  {
    this.name = wsName;
  }

  public String getName()
  {
    return name;
  }

  public void setClassName(String className)
  {
    this.className = className;
  }

  public String getClassName()
  {
    return className;
  }

  public String getClassVariableName()
  {
    return className.substring(0,1).toLowerCase()+className.substring(1);
  }

  private boolean listContains(List<NamedDefinition> beansProcessed, NamedDefinition currentBean)
  {
    boolean found = false;
    for (NamedDefinition sd : beansProcessed)
    {
      if (sd.getFullName().equals(currentBean.getFullName()))
      {
        found = true;
        break;
      }
    }
    return found;
  }

  private void deriveAccessorPathAndFindMethod(NamedDefinition namedDefinition)
  {
    String name = null;
    List<NamedDefinition> parentsProcessed = new ArrayList<NamedDefinition>();
    NamedDefinition parent = (NamedDefinition) namedDefinition.getDefinitionParent();
    NamedDefinition topParent = namedDefinition;
    while (parent!=null)
    {
      if (listContains(parentsProcessed,parent))
      {
        // can happen with recursive calls
        break;
      }
      parentsProcessed.add(parent);
      if (parent.getDefinitionParent()!=null)
      {
        name = parent.getName()+ (name!=null ? "."+name : "");     
        topParent = parent;
      }
      parent = (NamedDefinition) parent.getDefinitionParent();
    }
    name = name!=null ? name+"." + this.name: this.name;
    setAccessorPath(name);
    if (parent==null)
    {
      DCMethod method = null;
      if (topParent instanceof OperationDefinition)
      {
        method = new DCMethod((OperationDefinition) topParent);        
      }
      else if (topParent instanceof AccessorDefinition)
      {
        method = new DCMethod((AccessorDefinition) topParent);        
      }
      if (method!=null)
      {
        method.setDataObject(this);
        setFindAllMethod(method);              
      }
    }
  }

  public void setAccessorPath(String accessorPath)
  {
    this.accessorPath = accessorPath;
  }

  public String getAccessorPath()
  {
    return accessorPath;
  }

  public List<AttributeInfo> getKeyAttributes()
  {
    List<AttributeInfo> attrs = new ArrayList<AttributeInfo>();
    for(AttributeInfo attr : getAttributeDefs())
    {
      if (attr.isKeyAttribute())
      {
        attrs.add(attr);                
      }
    }
    return attrs;                   
  }
  
  public List<String> getAttributeNames()
  {
    List<String> attrs = new ArrayList<String>();
    for(AttributeInfo attr : getAttributeDefs())
    {
      attrs.add(attr.getAttrName());        
    }
    return attrs;                   
  }

  public List<String> getAttributeImportTypes()
  {
    List<String> imports = new ArrayList<String>();
    for(AttributeInfo attr :getAttributeDefs())
    {
      if (attr.getImportType()!=null && !imports.contains(attr.getImportType()))
      {
        imports.add(attr.getImportType());
      }
    }
    return imports;                   
  }

  public List<AttributeInfo> getAttributeDefs()
  {
    return attributeInfos;                   
  }

  public AttributeInfo getAttributeDef(String name)
  {
    AttributeInfo attr = null;
    for(AttributeInfo attrInfo : getAttributeDefs())
    {
      if (attrInfo.getAttrName().equals(name))
      {
        attr = attrInfo;
        break;
      }
    }
    return attr;                   
  }

  public AttributeInfo getAttributeDefByColumnName(String name)
  {
    AttributeInfo attr = null;
    for(AttributeInfo attrInfo : getAttributeDefs())
    {
      if (attrInfo.getColumnName().equals(name))
      {
        attr = attrInfo;
        break;
      }
    }
    return attr;                   
  }

  public AttributeInfo getAttributeDefByPayloadName(String name)
  {
    AttributeInfo attr = null;
    for(AttributeInfo attrInfo : getAttributeDefs())
    {
      if (name.equals(attrInfo.getPayloadName()))
      {
        attr = attrInfo;
        break;
      }
    }
    return attr;                   
  }
  
  public String getTableName()
  {
    String name = StringUtils.camelCaseToUpperCase(getClassName());
    if (JboNameUtil.isSQLReservedWord(name) || "ORDER".equalsIgnoreCase(name))
    {
      // append with "1"
      name = name+"1";
    }
    return name;
  }

  public List<String> getKeyColumnNames()
  {
    ArrayList<String> columns = new ArrayList<String>();
    for(AttributeInfo attr : getKeyAttributes())
    {
      columns.add( attr.getColumnName());
    }
    return columns;
  }
  
  public String getKeyColumnNamesAsString()
  {
    return StringUtils.listToString(getKeyColumnNames(), ",");
  }

  public void setCollection(boolean collection)
  {
    this.collection = collection;
  }

  public boolean isCollection()
  {
    return collection;
  }

  public void setParent(DataObjectInfo parent)
  {
    this.parent = parent;
  }

  /**
   * Returns parent data object if the parent is also selected for generation
   * @return
   */
  public DataObjectInfo getParent()
  {
    if (parent!=null && !parent.isGenerate())
    {
      return null;
    }
    return parent;
  }

//  public void addParentAttrMapping(AttributeInfo childAttr, AttributeInfo parentAttr)
//  {
//    parentAttrMappings.put(childAttr, parentAttr);
//  }
//
//  public Map<AttributeInfo, AttributeInfo> getParentAttrMappings()
//  {
//    return parentAttrMappings;
//  }

  public void setAccessorMethodInParent(String accessorMethodInParent)
  {
    this.accessorMethodInParent = accessorMethodInParent;
  }

  public String getAccessorMethodInParent()
  {
    if (accessorMethodInParent==null)
    {
      return "get"+getClassName();
    }
    return accessorMethodInParent;
  }

  public String getAccessorMethodInChild()
  {
    if (accessorMethodInChild==null)
    {
      return "get"+getClassName();
    }
    return accessorMethodInChild;
  }

  public void addChild(AccessorInfo child)
  {
    children.add(child);
  }

  public void addParent(AccessorInfo parent)
  {
    parents.add(parent);
  }

  /**
   * Returns all parents, including those without attribute mappings
   * @return
   */
  public List<AccessorInfo> getAllParents()
  {
    return parents;
  }

  /**
   * Returns only AccessorInfo parents that have at least one attribute mapping
   * @return
   */
  public List<AccessorInfo> getParents()
  {
    List<AccessorInfo> parents = new ArrayList<AccessorInfo>();
    for(AccessorInfo parent : getAllParents())
    {
      if (parent.isHasAttributeMappings())
      {
        parents.add(parent);
      }
    }
    return parents;
  }

  /**
   * Returns only AccessorInfo children that have at least one attribute mapping
   * @return
   */
  public List<AccessorInfo> getChildren()
  {
    List<AccessorInfo> kids = new ArrayList<AccessorInfo>();
    for(AccessorInfo kid : getAllChildren())
    {
      if (kid.isHasAttributeMappings())
      {
        kids.add(kid);
      }
    }
    return kids;
  }

  /**
   * Returns all possible children, including those without attribute mappings
   * @return
   */
  public List<AccessorInfo> getAllChildren()
  {
    return children;
  }

  public void initCrudMethodsIfNeeded(Map<String, DCMethod> crudMethods)
  {
    if (!crudMethodsInitialized)
    {
      crudMethodsInitialized = true;
      String[] names = new String[]{"create"+getClassName(),"createNew"+getClassName(),"insert"+getClassName(),"add"+getClassName()
                                    ,"create"+getName(),"createNew"+getName(),"insert"+getName(),"add"+getName()};
      String methodName = findMethodStartingWith(crudMethods.keySet().iterator(), names);
      if (methodName!=null)
      {
        DCMethod method = crudMethods.get(methodName);
        setCreateMethod(method);
      }

      names = new String[]{"update"+getClassName(),"edit"+getClassName(),"change"+getClassName()
                                    ,"update"+getName(),"edit"+getName(),"change"+getName()};
      methodName = findMethodStartingWith(crudMethods.keySet().iterator(), names);
      if (methodName!=null)
      {
        DCMethod method = crudMethods.get(methodName);
        setUpdateMethod(method);
      }

      names = new String[]{"merge"+getClassName(),"save"+getClassName(),"upsert"+getClassName()
                                    ,"merge"+getName(),"save"+getName(),"upsert"+getName()};
      methodName = findMethodStartingWith(crudMethods.keySet().iterator(), names);
      if (methodName!=null)
      {
        DCMethod method = crudMethods.get(methodName);
        setMergeMethod(method);
      }


      names = new String[]{"delete"+getClassName(),"remove"+getClassName(),"erase"+getClassName()
                                    ,"delete"+getName(),"remove"+getName(),"erase"+getName()};
      methodName = findMethodStartingWith(crudMethods.keySet().iterator(), names);
      if (methodName!=null)
      {
        DCMethod method = crudMethods.get(methodName);
        setDeleteMethod(method);
      }
    }
  }

  private String findMethodStartingWith(Iterator<String> iterator, String[] names)
  {
    String found = null;
    while (iterator.hasNext())
    {
      String method = iterator.next();
      for(String name : names)
      {
        if (method.toUpperCase().startsWith(name.toUpperCase()) || method.toUpperCase().endsWith(name.toUpperCase()))
        {
          found = method;
          break;
        }        
      }
      if (found!=null)
      {
        break;
      }
    }
    return found;
  }

  public void setPayloadListElementName(String payloadName)
  {
    this.payloadListElementName = payloadName;
  }

  public String getPayloadListElementName()
  {
    return payloadListElementName;
  }

  public void setFindAllMethod(DCMethod findAllMethod)
  {
    this.findAllMethod = findAllMethod;
    if (findAllMethod!=null)
    {
      findAllMethod.setIsFindAllMethod(true);
      findAllMethod.setDataObject(this);      
    }
  }

  public void setGetCanonicalMethod(DCMethod getCanonicalMethod)
  {
    this.getCanonicalMethod = getCanonicalMethod;
    if (getCanonicalMethod!=null)
    {
      getCanonicalMethod.setIsGetCanonicalMethod(true);
      getCanonicalMethod.setDataObject(this);      
    }
  }

  public DCMethod getGetCanonicalMethod()
  {
    return getCanonicalMethod;
  }

  public DCMethod getFindAllMethod()
  {
    return findAllMethod;
  }

  public void setFindMethod(DCMethod findMethod)
  {
    this.findMethod = findMethod;
    if (findMethod!=null)
    {
      findMethod.setIsFindMethod(true);
      findMethod.setDataObject(this);    
    }
  }

  public DCMethod getFindMethod()
  {
    return findMethod;
  }


  public void setCreateMethod(DCMethod createMethod)
  {
    this.createMethod = createMethod;
    if (createMethod!=null)
    {
      createMethod.setIsWriteMethod(true);
      createMethod.setDataObject(this);      
    }
  }

  public DCMethod getCreateMethod()
  {
    return createMethod;
  }

  public void setUpdateMethod(DCMethod updateMethod)
  {
    this.updateMethod = updateMethod;
    if (updateMethod!=null)
    {
      updateMethod.setIsWriteMethod(true);
      updateMethod.setDataObject(this);      
    }
  }

  public DCMethod getUpdateMethod()
  {
    return updateMethod;
  }

  public void setMergeMethod(DCMethod mergeMethod)
  {
    this.mergeMethod = mergeMethod;
    if (mergeMethod!=null)
    {
      mergeMethod.setIsWriteMethod(true);
      mergeMethod.setDataObject(this);      
    }
  }

  public DCMethod getMergeMethod()
  {
    return mergeMethod;
  }

  public void setDeleteMethod(DCMethod deleteMethod)
  {
    this.deleteMethod = deleteMethod;
    if (deleteMethod!=null)
    {
      deleteMethod.setIsWriteMethod(true);
      deleteMethod.setDataObject(this);      
    }
  }

  public DCMethod getDeleteMethod()
  {
    return deleteMethod;
  }

  public void setDeleteLocalRows(boolean deleteLocalRows)
  {
    this.deleteLocalRows = deleteLocalRows;
  }

  public boolean isDeleteLocalRows()
  {
    return deleteLocalRows;
  }
  
  public void addAttribute(AttributeInfo attr)
  {
    attributeInfos.add(attr);
    if (isPersisted())
    {
      attr.setPersisted(true);
    }
  }

  public void removeAttribute(int index)
  {
    attributeInfos.remove(index);
  }

  public void addFindAllInParentMethod(DCMethod findAllInParentMethod)
  {
    if (findAllInParentMethod!=null)
    {
      boolean found = false;
      for (DCMethod method : findAllInParentMethods)
      {
        if (method==findAllInParentMethod)
        {
          found = true;
          break;
        }
      }
      if (!found)
      {
        this.findAllInParentMethods.add(findAllInParentMethod);        
      }
      findAllInParentMethod.setIsFindAllInParentMethod(true);
      findAllInParentMethod.setDataObject(this);
      setFindMethod(null);
      setFindAllMethod(null);
    }
  }

  public List<DCMethod> getFindAllInParentMethods()
  {
    return findAllInParentMethods;
  }

  public void addGetAsParentMethod(DCMethod getAsParentMethod)
  {
    if (getAsParentMethod!=null)
    {
      boolean found = false;
      for (DCMethod method : getAsParentMethods)
      {
        if (method==getAsParentMethod)
        {
          found = true;
          break;
        }
      }
      if (!found)
      {
        this.getAsParentMethods.add(getAsParentMethod);        
      }
      getAsParentMethod.setIsGetAsParentMethod(true);
      getAsParentMethod.setDataObject(this);
    }
  }

  public List<DCMethod> getGetAsParentMethods()
  {
    return getAsParentMethods;
  }

  public void cleanUpAttrNames()
  {
    HashMap<String,String> attrNames = new HashMap<String,String>();
    for (AttributeInfo attr : getAttributeDefs())
    {
      attrNames.put(attr.getAttrName(), attr.getAttrName());
    }
    if (attrNames.size()>1)
    {
      // this was to clean up ugly AuraPlayer attr names, but might be unwanted with other WS's
//      StringUtils.removeLeadingCharsIfSame(attrNames);    
//      StringUtils.removeTrailingNumber(attrNames);          
    }
    for (AttributeInfo attr : getAttributeDefs())
    {
      String newName = attrNames.get(attr.getAttrName());
      if (JboNameUtil.isJavaReservedWord(newName,true))
      {
        // append with "1"
        newName = newName+"1";
      }
      newName = StringUtils.startWithLowerCase(newName);
      attr.setAttrName(newName);
    }
  }

  public void setSortOrder(String sortOrder)
  {
    this.sortOrder = sortOrder;
  }

  public String getSortOrder()
  {
    return sortOrder;
  }

  public void setOrderBy(String orderBy)
  {
    this.orderBy = orderBy;
  }

  public String getOrderBy()
  {
    return orderBy;
  }

  public void setPayloadDateFormat(String payloadDateFormat)
  {
    this.payloadDateFormat = payloadDateFormat;
  }

  public String getPayloadDateFormat()
  {
    return payloadDateFormat;
  }

  public void setPayloadDateTimeFormat(String payloadDateTimeFormat)
  {
    this.payloadDateTimeFormat = payloadDateTimeFormat;
  }

  public String getPayloadDateTimeFormat()
  {
    return payloadDateTimeFormat;
  }

  public boolean equals(Object obj)
  {
    if (obj instanceof DataObjectInfo)
    {
      DataObjectInfo other = (DataObjectInfo) obj;
      return getName().equals(other.getName());
    }
    return false;
  }

  public void setXmlPayload(boolean xmlPayload)
  {
    this.xmlPayload = xmlPayload;
  }

  public boolean isXmlPayload()
  {
    return xmlPayload;
  }

  public void setPayloadRowElementName(String payloadRowElementName)
  {
    this.payloadRowElementName = payloadRowElementName;
  }

  public String getPayloadRowElementName()
  {
    return payloadRowElementName;
  }

  public void setPersisted(boolean persist)
  {
    if (persist!=this.persisted)
    {
      for(AttributeInfo attr : getAttributeDefs())
      {
        attr.setPersisted(persist);
      }
    }
    this.persisted = persist;
  }

  public boolean isPersisted()
  {
    return persisted;
  }
  
  public int getLevel()
  {
    int level = 0;
    DataObjectInfo parent = getParent();
    while (parent!=null)
    {
      level++;
      parent = parent.getParent();
    }
    return level;
  }

  public DataObjectInfo getRootDataObject()
  {
    DataObjectInfo root = this;
    while (root.getParent()!=null)
    {
      root = root.getParent();      
    }
    return root;
  }

}
