/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.service;

/**
 * Interface used for lazy loading (aka as Indirection) of 1:1 relations in a data object. 
 */
public interface ValueHolderInterface
{
  Object getValue();
  void setValue(Object value);
}
