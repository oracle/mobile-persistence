package oracle.ateam.sample.mobile.dt.model;

import oracle.binding.meta.ParameterDefinition;

public class DCMethodParameter
{

  public static final String LITERAL_VALUE = "LiteralValue";
  public static final String EL_EXPRESSION = "ELExpression";
  public static final String DATA_OBJECT_ATTRIBUTE = "DataObjectAttribute";
  public static final String SERIALIZED_DATA_OBJECT = "SerializedDataObject";
  public static final String SEARCH_VALUE = "SearchValue";

  private String name;
  private String valueProvider;                 
  private String dataObjectAttribute;                 
  private String value;         
  private String javaType;
  private boolean pathParam = false;
  
  public static String[] getValueProviderAllowableValues()
  {
    return new String[]{SERIALIZED_DATA_OBJECT,DATA_OBJECT_ATTRIBUTE,LITERAL_VALUE,EL_EXPRESSION,SEARCH_VALUE};
  }
  
  public DCMethodParameter(ParameterDefinition param)
  {
    super();
    this.name= param.getName();
    this.javaType = param.getJavaTypeString();      
  }

  public DCMethodParameter()
  {
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setValueProvider(String valueType)
  {
    this.valueProvider = valueType;
  }

  public String getValueProvider()
  {
    return valueProvider;
  }

  public void setDataObjectAttribute(String dataObjectAttribute)
  {
    this.dataObjectAttribute = dataObjectAttribute;
  }

  public String getDataObjectAttribute()
  {
    return dataObjectAttribute;
  }

  public void setValue(String literalValue)
  {
    this.value = literalValue;
  }

  public String getValue()
  {
    return value;
  }

  public void setJavaType(String javaType)
  {
    this.javaType = javaType;
  }

  public String getJavaType()
  {
    return javaType;
  }

  public boolean isLiteralValue()
  {
    return LITERAL_VALUE.equalsIgnoreCase(getValueProvider());
  }

  public boolean isELExpression()
  {
    return EL_EXPRESSION.equalsIgnoreCase(getValueProvider());
  }

  public boolean isDataObjectAttribute()
  {
    return DATA_OBJECT_ATTRIBUTE.equalsIgnoreCase(getValueProvider());
  }

  public boolean isSerializedDataObject()
  {
    return SERIALIZED_DATA_OBJECT.equalsIgnoreCase(getValueProvider());
  }

  public boolean isSearchValue()
  {
    return SEARCH_VALUE.equalsIgnoreCase(getValueProvider());
  }

  public void setPathParam(boolean pathParam)
  {
    this.pathParam = pathParam;
  }

  public boolean isPathParam()
  {
    return pathParam;
  }
}
