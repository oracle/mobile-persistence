 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;
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
    return getAttributeStringValue("name");
  }

  public String getValueProvider()
  {
    return getAttributeStringValue("valueProvider");
  }

  public String getDataObjectAttribute()
  {
    return getAttributeStringValue("dataObjectAttribute");
  }

  public String getValue()
  {
    return getAttributeStringValue("value");
  }

  public String getJavaType()
  {
    return getAttributeStringValue("javaType");
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
    return getAttributeBooleanValue("pathParam",false);
  }
}
