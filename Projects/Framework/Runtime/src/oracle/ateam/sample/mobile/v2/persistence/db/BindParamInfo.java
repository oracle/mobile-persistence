 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
  
  $revision_history$
  19-mar-2015   Steven Davelaar / Puja Subramanyam
  1.1           Fix in convertDateValueIfNeeded to keep time component 
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.db;

import java.math.BigDecimal;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import java.util.HashMap;
import java.util.Map;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;


/**
 * Class that holds information needed to set up a bind parameter in a SQL statement when accessing local database.
 * It contains the mapping between an entity attribute and the underlying table column, and can hold the actual value.
 * Optionally, it also contains SQL operator information when used to construct SQL where clause.
 * This class is also used to create an entity from a web service payload. When parsing the web service payload a list
 * of bindParamInfos is created which is then used to create an entity instance and to insert a row in the SQLite database.
 */
public class BindParamInfo
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(BindParamInfo.class);

  private static Map<Class,Integer> typeMapping = new HashMap<Class,Integer>();  

  static
  {
    // Mapping based on http://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html#1034737
    typeMapping.put(String.class, new Integer(Types.CHAR));
    typeMapping.put(BigDecimal.class, new Integer(Types.NUMERIC));
    typeMapping.put(Boolean.class, new Integer(Types.CHAR));
    typeMapping.put(Integer.class, new Integer(Types.INTEGER));
    typeMapping.put(Long.class, new Integer(Types.BIGINT));
    typeMapping.put(Float.class, new Integer(Types.REAL));
    typeMapping.put(Double.class, new Integer(Types.DOUBLE));
    typeMapping.put(Byte[].class, new Integer(Types.BINARY));
    typeMapping.put(Date.class, new Integer(Types.DATE));
    typeMapping.put(java.util.Date.class, new Integer(Types.DATE));
    typeMapping.put(Time.class, new Integer(Types.TIME));
    typeMapping.put(Timestamp.class, new Integer(Types.TIMESTAMP));
    typeMapping.put(Clob.class, new Integer(Types.CLOB));
    typeMapping.put(Blob.class, new Integer(Types.BLOB));
  }

  private String attributeName;
  private String tableName;
  private String columnName;
  private Class javaType;
  private int sqlType;
  private Object value;
  private boolean primaryKey = false;
  private String operator = "=";
  private boolean caseInsensitive = false;

  public void setAttributeName(String attributeName)
  {
    this.attributeName = attributeName;
  }

  public String getAttributeName()
  {
    return attributeName;
  }

  public void setColumnName(String columnName)
  {
    this.columnName = columnName;
  }

  public String getColumnName()
  {
    return columnName;
  }

  public void setSqlType(int sqlType)
  {
    this.sqlType = sqlType;
  }

  public int getSqlType()
  {
    return sqlType;
  }

  public void setValue(Object value)
  {
    this.value = convertDateValueIfNeeded(value);
  }

  public Object getValue()
  {
    return value;
  }

  public static int getSqlType(Class javaType)
  {
    Integer type = (Integer) typeMapping.get(javaType);
    return type.intValue();
  }

  public void setJavaType(Class javaType)
  {
    this.javaType = javaType;
    if (typeMapping.get(javaType)!=null)
    {
      setSqlType(getSqlType(javaType));      
    }
  }

  public Class getJavaType()
  {
    return javaType;
  }

  public void setTableName(String tableName)
  {
    this.tableName = tableName;
  }

  public String getTableName()
  {
    return tableName;
  }

  public Object convertDateValueIfNeeded(Object value)
  {
    if (value!=null && value instanceof java.util.Date)
    {
      java.util.Date utilDateValue = (java.util.Date) value;
      Timestamp sqlDate = new Timestamp(utilDateValue.getTime());
      value = sqlDate;
    }
    return value;
  }

  public void setPrimaryKey(boolean primaryKey)
  {
    this.primaryKey = primaryKey;
  }

  public boolean isPrimaryKey()
  {
    return primaryKey;
  }

  public void setOperator(String operator)
  {
    this.operator = operator;
  }

  public String getOperator()
  {
    return operator;
  }

  public void setCaseInsensitive(boolean caseInsensitive)
  {
    this.caseInsensitive = caseInsensitive;
  }

  public boolean isCaseInsensitive()
  {
    return caseInsensitive;
  }
}
