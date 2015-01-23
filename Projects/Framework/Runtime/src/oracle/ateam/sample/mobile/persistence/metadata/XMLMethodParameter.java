/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 15-sep-2014   Steven Davelaar
 1.1           Use Utility.loadClass rather than Class.forName, the latter can cause app
               to hang when used in feature lifecycle listener activate method
 02-apr-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.metadata;

import oracle.adfmf.util.Utility;
import oracle.adfmf.util.XmlAnyDefinition;

/**
 * Implementation of SOAP method parameter or REST resource parameter that reads values from persistenceMapping.xml
 */
public class XMLMethodParameter extends XmlAnyDefinition implements MethodParameter
{

  public XMLMethodParameter(XmlAnyDefinition xmlAnyDefinition)
  {
    super(xmlAnyDefinition);
  }

  public String getName()
  {
    return (String) getAttributeValue("name");
  }

  public String getValueProvider()
  {
    return (String) getAttributeValue("valueProvider");
  }

  public String getDataObjectAttribute()
  {
    return (String) getAttributeValue("dataObjectAttribute");
  }

  public String getValue()
  {
    return (String) getAttributeValue("value");
  }

  public String getJavaType()
  {
    return (String) getAttributeValue("javaType");
  }

  public Class getJavaTypeClass()
  {
    String type = getJavaType();
    if (type==null)
    {
      type = "java.lang.String";
    }
    try
    {
      return Utility.loadClass(type);
    }
    catch (ClassNotFoundException e)
    {
    }
    return String.class;
  }

  public boolean isLiteralValue()
  {
    return MethodParameter.LITERAL_VALUE.equalsIgnoreCase(getValueProvider());
  }

  public boolean isELExpression()
  {
    return MethodParameter.EL_EXPRESSION.equalsIgnoreCase(getValueProvider());
  }

  public boolean isDataObjectAttribute()
  {
    return MethodParameter.DATA_OBJECT_ATTRIBUTE.equalsIgnoreCase(getValueProvider());
  }

  public boolean isSerializedDataObject()
  {
    return MethodParameter.SERIALIZED_DATA_OBJECT.equalsIgnoreCase(getValueProvider());
  }

  public boolean isSearchValue()
  {
    return MethodParameter.SEARCH_VALUE.equalsIgnoreCase(getValueProvider());
  }

  public boolean isPathParam()
  {
    
    String value = (String) getAttributeValue("pathParam");
    if (value==null || "".equals(value))
    {
      // use old attr name for backwards compatibility
      value = (String) getAttributeValue("uriParam");
    }
    return "true".equals(value);
  }
  
}
