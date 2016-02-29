/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 22-jun-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.service;

/**
 * Class that holds a collection of data sync actions that will be synchronized in one run
 * of the DataSynchronizer.
 * 
 * @deprecated Use the class with same name in oracle.ateam.sample.mobile.v2.persistence.* instead
 */
public class DataSynchPayload
{
  private DataSynchAction[] dataSynchActions;

  public DataSynchPayload()
  {
    super();
  }

  public void setDataSynchActions(DataSynchAction[] dataSynchActions)
  {
    this.dataSynchActions = dataSynchActions;
  }

  public DataSynchAction[] getDataSynchActions()
  {
    return dataSynchActions;
  }
}
