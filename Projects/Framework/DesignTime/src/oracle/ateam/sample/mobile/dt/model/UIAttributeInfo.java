/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.model;

import java.util.HashMap;

import oracle.ateam.sample.mobile.dt.util.StringUtils;

import oracle.binding.meta.AttributeDefinition;
import oracle.javatools.db.Column;
import oracle.javatools.db.datatypes.DataTypeAttribute;

import oracle.toplink.workbench.mappingsmodel.spi.db.ExternalColumn;

public class UIAttributeInfo
{
  private String attrName;
  private String javaType;
  private boolean displayInForm = true;
  private UIDataObjectInfo dataObject;
  private AttributeDefinition attrDef;
  
  public UIAttributeInfo(UIDataObjectInfo dataObject,AttributeDefinition attrDef)
  {
    this.attrName = attrDef.getName();
    this.javaType = attrDef.getJavaTypeString();
    if ("isNewEntity".equals(attrName) || "key".equals(attrName))
    {
      setDisplayInForm(false);
    }
    this.dataObject = dataObject;
    this.attrDef = attrDef;
  }

  public String getReadOnlyExpression()
  {
    if (attrDef.isReadOnly())
    {
      return "true";
    }
    else if (!dataObject.isCreate() && !dataObject.isUpdate())
    {
      return "true";
    }
    else if (dataObject.isCreate() && !dataObject.isUpdate() || attrDef.isKey())
    {
      return "#{!bindings.isNewEntity.inputValue}";
    }
    return "false";
  }

  public String getAttrName()
  {
    return attrName;
  }

  public void setJavaType(String javaType)
  {
    this.javaType = javaType;
  }

  public String getJavaType()
  {
    return javaType;
  }
  
  public boolean isNumber()
  {
    return javaType.endsWith("Integer") || javaType.endsWith("Double") || javaType.endsWith("Float") 
           || javaType.endsWith("BigDecimal") || javaType.endsWith("Long") || javaType.endsWith("Number");
  }

  public boolean isDate()
  {
    return javaType.endsWith("Date") || javaType.endsWith("Timestamp");
  }

  public void setDisplayInForm(boolean displayInForm)
  {
    this.displayInForm = displayInForm;
  }

  public boolean isDisplayInForm()
  {
    return displayInForm;
  }

  public void setDataObject(UIDataObjectInfo dataObject)
  {
    this.dataObject = dataObject;
  }

  public UIDataObjectInfo getDataObject()
  {
    return dataObject;
  }
}
