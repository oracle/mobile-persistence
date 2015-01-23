 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  22-jan-2015   Steven Davelaar
  1.1           To prevent ConcurrentModificationException when looping over
                ations in DataSynchornizer and removing them once succesful,
                we copy over the sync actions to its own list
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that holds a collection of data sync actions that will be synchronized in one run
 * of the DataSynchronizer.
 */
public class DataSynchPayload
{
  private List<DataSynchAction> dataSynchActions = new ArrayList<DataSynchAction>();

  public DataSynchPayload()
  {
    super();
  }

  public void setDataSynchActions(List<DataSynchAction> dataSynchActions)
  {
// this would cause ConcurrentModificationException in Data Synchronizer!    
//    this.dataSynchActions = dataSynchActions;
    this.dataSynchActions.clear();
    this.dataSynchActions.addAll(dataSynchActions);
  }

  public List<DataSynchAction> getDataSynchActions()
  {
    return dataSynchActions;
  }
}
