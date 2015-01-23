 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;


 /**
  * Interface that defines SOAP method header parameter or RESTful resource header parameter
  */
public interface MethodHeaderParameter
{
  public String getName();

  public String getValue();
}
