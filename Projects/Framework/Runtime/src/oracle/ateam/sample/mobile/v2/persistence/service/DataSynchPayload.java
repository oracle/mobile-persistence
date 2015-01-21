 /*******************************************************************************
  Copyright © 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.service;

import java.util.List;

/**
 * Class that holds a collection of data sync actions that will be synchronized in one run
 * of the DataSynchronizer.
 */
public class DataSynchPayload
{
  private List<DataSynchAction> dataSynchActions;

  public DataSynchPayload()
  {
    super();
  }

  public void setDataSynchActions(List<DataSynchAction> dataSynchActions)
  {
    this.dataSynchActions = dataSynchActions;
  }

  public List<DataSynchAction> getDataSynchActions()
  {
    return dataSynchActions;
  }
}
