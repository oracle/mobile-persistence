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

import oracle.jbo.common.JboNameUtil;

import oracle.toplink.workbench.mappingsmodel.spi.db.ExternalColumn;

public class AttributeInfo
{
  private static HashMap<String, String> javaToDBTypeMapping = new HashMap<String, String>();
  private static HashMap<String, String> dbToJavaTypeMapping = new HashMap<String, String>();
  private boolean keyAttribute = false;
  private boolean required = false;
  private boolean persisted = false;
  private String javaTypeFullName;
  private String columnType;
  private String attrName;
  private String payloadName;
  private AttributeInfo parentReferenceAttribute;
  private DataObjectInfo parentDataObject;


  static
  {
    javaToDBTypeMapping.put("String", "VARCHAR");
    javaToDBTypeMapping.put("Integer", "NUMERIC");
    javaToDBTypeMapping.put("Long", "NUMERIC");
    javaToDBTypeMapping.put("Float", "NUMERIC");
    javaToDBTypeMapping.put("Double", "NUMERIC");
    javaToDBTypeMapping.put("BigDecimal", "NUMERIC");
    javaToDBTypeMapping.put("Blob", "BLOB");
    javaToDBTypeMapping.put("Clob", "TEXT");
    javaToDBTypeMapping.put("Date", "DATE");
    javaToDBTypeMapping.put("Time", "DATE");
    javaToDBTypeMapping.put("Timestamp", "DATE");
  }

  static
  {
    dbToJavaTypeMapping.put("CHAR", "java.lang.String");
    dbToJavaTypeMapping.put("NCHAR", "java.lang.String");
    dbToJavaTypeMapping.put("VARCHAR", "java.lang.String");
    dbToJavaTypeMapping.put("VARCHAR2", "java.lang.String");
    dbToJavaTypeMapping.put("NVARCHAR2", "java.lang.String");
    dbToJavaTypeMapping.put("INTEGER", "java.lang.Integer");
    dbToJavaTypeMapping.put("FLOAT", "java.lang.Float");
    dbToJavaTypeMapping.put("LONG", "java.lang.long");
    dbToJavaTypeMapping.put("BINARY_FLOAT", "java.lang.Double");
    dbToJavaTypeMapping.put("DOUBLE", "java.lang.Float");
    dbToJavaTypeMapping.put("BINARY_DOUBLE", "java.lang.Double");
    dbToJavaTypeMapping.put("BLOB", "java.sql.Blob");
    dbToJavaTypeMapping.put("CLOB", "java.sql.Clob");
    dbToJavaTypeMapping.put("DATE", "java.util.Date");
    dbToJavaTypeMapping.put("TIME", "java.util.Date");
    dbToJavaTypeMapping.put("TIMESTAMP", "java.util.Date");
  }

  /**
   * This constructor is used when using the "from web service data control" wizard
   * @param attrDef
   */
  public AttributeInfo(AttributeDefinition attrDef)
  {
    this.attrName = StringUtils.startWithLowerCase(StringUtils.toCamelCase(attrDef.getName()));
    this.payloadName = attrDef.getName();
    this.javaTypeFullName = attrDef.getJavaTypeString();
    this.javaTypeFullName = StringUtils.substitute(javaTypeFullName, "java.sql.Date", "java.util.Date");
    this.columnType = javaToDBTypeMapping.get(getJavaType());
    if (this.getColumnType() == null)
    {
      this.columnType = "VARCHAR";
    }
  }

  public AttributeInfo(String name, DataObjectInfo parentDataObject, AttributeInfo referenceAttribute)
  {
    this.attrName = StringUtils.startWithLowerCase(StringUtils.toCamelCase(name));
    this.javaTypeFullName = referenceAttribute.getJavaTypeFullName();
    this.columnType = referenceAttribute.getColumnType();
    this.parentDataObject = parentDataObject;
    this.parentReferenceAttribute = referenceAttribute;
  }

  public AttributeInfo(String name, String javaTypeFullName)
  {
    String attrNameWithUnderscores = StringUtils.substitute(name, ".", "_");
    this.attrName = StringUtils.startWithLowerCase(StringUtils.toCamelCase(attrNameWithUnderscores));
    this.payloadName = name;
    this.javaTypeFullName = javaTypeFullName;
    this.columnType = javaToDBTypeMapping.get(getJavaType());
    if (this.getColumnType() == null)
    {
      this.columnType = "VARCHAR";
    }
  }

  /**
   * This constructor is used when using the "from DB Tables" wizard
   * @param column
   */
  public AttributeInfo(ExternalColumn column)
  {
    this.attrName = StringUtils.startWithLowerCase(StringUtils.toCamelCase(column.getName()));
    this.payloadName = attrName;
    String type = column.getTypeName();
    int scale = column.getScale();
    this.javaTypeFullName = dbToJavaTypeMapping.get(type);
    if (javaTypeFullName == null)
    {
      if ("NUMBER".equals(type))
      {
        javaTypeFullName = scale > 0? "java.math.BigDecimal": "java.lang.Integer";
      }
      else
      {
        javaTypeFullName = "java.lang.String";
      }
    }
    // we don't use the original column type, might not be supported by SQLite
    this.columnType = javaToDBTypeMapping.get(getJavaType());
    if (this.getColumnType() == null)
    {
      this.columnType = "VARCHAR";
    }
    setKeyAttribute(column.isPrimaryKey());
    setRequired(!column.isNullable());
  }

  public String getImportType()
  {
    String packageName = getJavaTypeFullName().substring(0, getJavaTypeFullName().lastIndexOf("."));
    if ("java.lang".equals(packageName))
    {
      return null;
    }
    return getJavaTypeFullName();
  }

  public String getJavaTypeFullName()
  {
    return javaTypeFullName;
  }

  /**
   * Return java type without package name
   * @return
   */
  public String getJavaType()
  {
    int lastDotPos = javaTypeFullName.lastIndexOf(".");
    if (lastDotPos > -1)
    {
      return javaTypeFullName.substring(lastDotPos + 1);
    }
    return javaTypeFullName;
  }

  public void setParentDataObject(DataObjectInfo parentDataObject)
  {
    this.parentDataObject = parentDataObject;
  }

  public DataObjectInfo getParentDataObject()
  {
    return parentDataObject;
  }

  public String getColumnName()
  {
    String columnName = StringUtils.camelCaseToUpperCase(getAttrName());
    if (JboNameUtil.isSQLReservedWord(columnName) || "ORDER".equalsIgnoreCase(columnName))
    {
      columnName = columnName+"1";
    }  
    return columnName;
  }

  public String getColumnType()
  {
    return columnType;
  }

  public String getSetterMethodName()
  {
    return StringUtils.getSetterMethodName(getAttrName());
  }

  public String getGetterMethodName()
  {
    return StringUtils.getGetterMethodName(getAttrName());
  }

  public void setKeyAttribute(boolean keyAttribute)
  {
    this.keyAttribute = keyAttribute;
    if (keyAttribute)
    {
      setRequired(true);
    }
  }

  public boolean isKeyAttribute()
  {
    return keyAttribute;
  }

  public void setJavaTypeFullName(String javaTypeFullName)
  {
    this.javaTypeFullName = javaTypeFullName;
    this.columnType = javaToDBTypeMapping.get(getJavaType());
    if (this.getColumnType() == null)
    {
      this.columnType = "VARCHAR";
    }    
  }

  public void setColumnType(String columnType)
  {
    this.columnType = columnType;
  }

  public void setAttrName(String name)
  {
    this.attrName = name;
  }

  public String getAttrName()
  {
    return attrName;
  }

  public void setPayloadName(String payloadName)
  {
    this.payloadName = payloadName;
  }

  public String getPayloadName()
  {
    return payloadName;
  }

  public void setRequired(boolean required)
  {
    this.required = required;
  }

  public boolean isRequired()
  {
    return required;
  }

  public void setParentReferenceAttribute(AttributeInfo parentReferenceAttribute)
  {
    this.parentReferenceAttribute = parentReferenceAttribute;
  }

  public AttributeInfo getParentReferenceAttribute()
  {
    return parentReferenceAttribute;
  }

  public void setPersisted(boolean persisted)
  {
    this.persisted = persisted;
  }

  public boolean isPersisted()
  {
    return persisted;
  }
}
