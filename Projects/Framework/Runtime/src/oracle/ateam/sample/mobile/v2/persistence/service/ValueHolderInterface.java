 /*******************************************************************************
  Copyright © 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.service;
/**
 * Interface used for lazy loading (aka as Indirection) of 1:1 relations in a data object. 
 */
public interface ValueHolderInterface
{
  Object getValue();
  void setValue(Object value);
}
