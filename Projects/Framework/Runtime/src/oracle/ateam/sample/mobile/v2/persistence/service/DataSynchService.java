/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
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

  /**
   * This method fires property change events when value of property hasDataSynchActions has changed.
   * It also sets the current number of data sync actions in an applicationScope variable named
   * ampa_DataSyncActionsCount. 
   * This applicationScope variable can be used by the UI to show a badge on a UI widget to show the 
   * current number of pending data sync actions.
   * @param oldValue
   */
  protected void refreshHasDataSynchActions(boolean oldValue)
  {
  //    String name = getCrudService().getEntityListName() + "_dataSyncActionsCount";
    TaskExecutor.getInstance().executeUIRefreshTask(() -> 
    {
      AdfmfJavaUtilities.setELValue("#{applicationScope.ampa_dataSyncActionsCount}", new Integer(getDataSynchActions().size()));
      if (getHasDataSynchActions() != oldValue)
      {
        getPropertyChangeSupport().firePropertyChange("hasDataSynchActions", oldValue, getHasDataSynchActions());
      }
      AdfmfJavaUtilities.flushDataChangeEvent();
    });
  }

}
