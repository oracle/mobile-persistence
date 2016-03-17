/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 17-mar-2016   Steven Davelaar
 1.4           Added method refreshDataSynchActions
 04-mar-2016   Steven Davelaar
 1.3           removed method refreshHasDataSyncActions, not needed, we now have two global
               expressions, #{applicationScope.ampa_hasDataSyncActions} and 
                #{applicationScope.ampa_dataSyncActionsCount} that can be used show info about
               pending sync actioins in UI
 15-feb-2016   Steven Davelaar
 1.2           data syncing is now global, no longer per entity type, moved this
               class from DayaStychFeatire jar to runtime jar, so user can easily
               create DataControl for it.
 23-jun-2015   Steven Davelaar
 1.1           When removing data synch action in UI, it was not removed from DB
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.service;

import java.util.ArrayList;
import java.util.List;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

import oracle.ateam.sample.mobile.util.TaskExecutor;

/**
 * This class can be used to create a bean data control to build your own UI to show pending sync actions.
 * This class is also used by the reusable DataSynch feature.
 */
public class DataSynchService extends EntityCRUDService<DataSynchAction>
{
  public DataSynchService()
  {
    super();
  }
  
  public DataSynchManager getDataSynchManager()
  {
    return DataSynchManager.getInstance();
  }

  protected Class getEntityClass()
  {
    return DataSynchAction.class;
  }

  protected String getEntityListName()
  {
    return "dataSynchActions";
  }
  
  public List<DataSynchAction> getDataSynchActions()
  {
    return getDataSynchManager().getDataSynchActions();
  }

  /**
   * Callback method called by DataSynchronizer when dataSychronization action is done. It contains a list of succeeded
   * and failed data sync actions. You can override this method to add custom logic after data synchronization has taken place.
   * For example, you could warn the end user that one or more transactions have failed and will be re-tried later, or you can
   * inform them that all pending data sync actions have been processed successfully.
   * @param succeededDataSynchActions
   * @param failedDataSynchActions
   */
  protected void dataSynchFinished(List<DataSynchAction> succeededDataSynchActions,
                                   List<DataSynchAction> failedDataSynchActions)
  {
    refreshEntityList(new ArrayList<DataSynchAction>());
    //    int ok = succeededDataSynchActions.size();
    //    int fails = failedDataSynchActions.size();
    //    int total = ok + fails;
    //    MessageUtils.handleMessage(AdfException.INFO,
    //                               total + " data synch actions completed. Successful: " + ok + ", Failed: " + fails);
  }

  /**
   * Refresh data sync actions list so UI gets updated with latest list.
   */
  public void refreshDataSynchActions()
  {
    refreshEntityList(new ArrayList<DataSynchAction>());    
  }

  /**
   * This method is automatically called when using the Delete operation on the dataSynchAction collection
   * in the data control palette. It removes the dataSynchAction instance passed in from the dataSynchAction list, deletes the
   * corresponding row from the database.
   * Do NOT drag and drop this method from the data control palette, use the Delete operation instead to ensure
   * that iterator binding and dataSynchAction list stay in sync.
   * @param department
   */
  public void removeDataSynchAction(DataSynchAction dataSynchAction) {
      getDataSynchManager().removeDataSynchAction(dataSynchAction);
  }


}
