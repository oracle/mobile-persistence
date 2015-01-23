 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;


/**
 * Interface that defines SOAP method parameter or RESTful resource parameter
 */
public interface MethodParameter 
{
  public static final String LITERAL_VALUE = "LiteralValue";
  public static final String EL_EXPRESSION = "ELExpression";
  public static final String DATA_OBJECT_ATTRIBUTE = "DataObjectAttribute";
  public static final String SERIALIZED_DATA_OBJECT = "SerializedDataObject";
  public static final String SEARCH_VALUE = "SearchValue";

  public String getName();
  public String getValueProvider();
  public String getDataObjectAttribute();
  public String getValue();
  public String getJavaType();
  public Class getJavaTypeClass();
  public boolean isLiteralValue();
  public boolean isELExpression();
  public boolean isDataObjectAttribute();
  public boolean isSerializedDataObject();
  public boolean isSearchValue();
  public boolean isPathParam();
}
