/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.model;

import java.util.ArrayList;
import java.util.List;

import oracle.ateam.sample.mobile.dt.util.StringUtils;

/**
 *  Class that specifies the rationship between a parent and child data object.
 *  A data object info can have a list of child accessors, resulting in one-to-many mappings
 *  generated in persistenceMapping.xml and a child accessor list method.
 *  A data object can also have a list of parent accessors, resulting in one-to-one mappings
 *  generated in persistenceMapping.xml and a parent accessor object method
 */
public class AccessorInfo
{
  private DataObjectInfo childDataObject;
  private DataObjectInfo parentDataObject;
  private List<AttributeMapping> attributeMappings = new ArrayList<AttributeMapping>();
  private String childAccessorName;
  private String childAccessorPayloadName;

  private String parentAccessorName;
  private DCMethod childAccessorMethod;
  private DCMethod parentAccessorMethod;

  public AccessorInfo(DataObjectInfo parentDataObject, DataObjectInfo childDataObject)
  {
    this(parentDataObject,childDataObject,true);
  }

  public AccessorInfo(DataObjectInfo parentDataObject, DataObjectInfo childDataObject, boolean isChildAccesor)
  {
    this.parentDataObject = parentDataObject;
    this.childDataObject = childDataObject;
    // set default value named after child class
    if (isChildAccesor)
    {
      setChildAccessorName(childDataObject.getClassVariableName());
      setChildAccessorPayloadName(childDataObject.getName());      
    }
  }

  public void setChildAccessorPayloadName(String childAccessorPayloadName)
  {
    this.childAccessorPayloadName = childAccessorPayloadName;
  }

  public String getChildAccessorPayloadName()
  {
    return childAccessorPayloadName;
  }
  public void setParentAccessorMethod(DCMethod parentAccessorMethod)
  {
    this.parentAccessorMethod = parentAccessorMethod;
  }

  public DCMethod getParentAccessorMethod()
  {
    return parentAccessorMethod;
  }

  public void setParentAccessorName(String parentAccessorName)
  {
    this.parentAccessorName = parentAccessorName;
  }

  public String getParentAccessorName()
  {
    return parentAccessorName;
  }

  public void setChildAccessorName(String accessorName)
  {
    this.childAccessorName = accessorName;
  }

  public String getChildAccessorName()
  {
    return childAccessorName;
  }


  public void addAttributeMapping(AttributeInfo parentAttr, AttributeInfo childAttr)
  {
    AttributeMapping mapping = new AttributeMapping(parentAttr,childAttr);
    attributeMappings.add(mapping);
  }

  public List<AttributeMapping> getAttributeMappings()
  {
    return attributeMappings;
  }
  
  public boolean isHasAttributeMappings()
  {
    return getAttributeMappings().size()>0;
  }

  public void setChildDataObject(DataObjectInfo childDataObject)
  {
    this.childDataObject = childDataObject;
  }

  public DataObjectInfo getChildDataObject()
  {
    return childDataObject;
  }

  public String getChildAccessorListName()
  {
    return getChildAccessorName()+"List";
  }

  public String getChildAccessorGetterMethodName()
  {
    return StringUtils.getGetterMethodName(getChildAccessorName());
  }

  public String getChildAccessorSetterMethodName()
  {
    return StringUtils.getSetterMethodName(getChildAccessorName());
  }

  public String getParentAccessorGetterMethodName()
  {
    return StringUtils.getGetterMethodName(getParentAccessorName());
  }

  public String getParentAccessorSetterMethodName()
  {
    return StringUtils.getSetterMethodName(getParentAccessorName());
  }

  public String getChildAccessorListGetterMethodName()
  {
    return StringUtils.getGetterMethodName(getChildAccessorListName());
  }

  public String getChildAccessorListSetterMethodName()
  {
    return StringUtils.getSetterMethodName(getChildAccessorListName());
  }

  public void setParentDataObject(DataObjectInfo parentDataObject)
  {
    this.parentDataObject = parentDataObject;
  }

  public DataObjectInfo getParentDataObject()
  {
    return parentDataObject;
  }

  public void setChildAccessorMethod(DCMethod methodAccessor)
  {
    this.childAccessorMethod = methodAccessor;
  }

  public DCMethod getChildAccessorMethod()
  {
    return childAccessorMethod;
  }

  public boolean isMethodAccessor()
  {
    return childAccessorMethod!=null;
  }
  
  public String getAccessorDisplayName()
  {
    String parentSuffix = getChildAccessorName()!=null ? "."+getChildAccessorName() : "" ;
    String childSuffix = getParentAccessorName()!=null ? "."+getParentAccessorName() : "" ;
    return parentDataObject.getClassName()+parentSuffix + " -> " + childDataObject.getClassName()+childSuffix;    
  }

  public class AttributeMapping
  {
    AttributeInfo parentAttr;
    AttributeInfo childAttr;
    AttributeMapping(AttributeInfo parentAttr, AttributeInfo childAttr)
    {
      this.parentAttr = parentAttr;
      this.childAttr = childAttr;
    }

    public void setParentAttr(AttributeInfo parentAttr)
    {
      this.parentAttr = parentAttr;
    }

    public AttributeInfo getParentAttr()
    {
      return parentAttr;
    }

    public String getParentAttrGetterMethod()
    {
      return StringUtils.getGetterMethodName(parentAttr.getAttrName());
    }

    public String getParentAttrSetterMethod()
    {
      return StringUtils.getSetterMethodName(parentAttr.getAttrName());
    }

    public String getChildAttrGetterMethod()
    {
      return StringUtils.getGetterMethodName(childAttr.getAttrName());
    }

    public String getChildAttrSetterMethod()
    {
      return StringUtils.getSetterMethodName(childAttr.getAttrName());
    }

    public void setChildAttr(AttributeInfo childAttr)
    {
      this.childAttr = childAttr;
    }

    public AttributeInfo getChildAttr()
    {
      return childAttr;
    }
  }
}
