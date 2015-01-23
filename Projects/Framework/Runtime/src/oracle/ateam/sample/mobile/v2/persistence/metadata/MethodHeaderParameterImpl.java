 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;


/**
 * Implementation of SOAP method header parameter or REST resource header parameter that can be instantiated programmatically
 */
public class MethodHeaderParameterImpl
  implements MethodHeaderParameter
{
  
  private String name;
  private String value;

  public MethodHeaderParameterImpl(String name)
  {
    this.name= name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }
}
