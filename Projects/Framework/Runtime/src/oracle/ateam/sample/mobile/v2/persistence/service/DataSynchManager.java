 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  14-feb-2016   Steven Davelaar
  1.5           - Changed implementation to handle all sync actions for all entities
                - added method dataSynchFinished
  01-feb-2016   Steven Davelaar
  1.4           Fixed issue in mergeWithExistingDataSyncActionIfNeeded with Update action
                followed by delete action
  04-sep-2015   Steven Davelaar
  1.3           Only load sync actions from DB when offline transactions is enabled
  14-aug-2015   Steven Davelaar
  1.2           Improved method mergeWithExistingDataSyncActionIfNeeded
  24-jun-2015   Steven Davelaar
  1.1           Added code to merge data sync actions if needed
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.Map;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.JSONBeanSerializationHelper;


import oracle.adfmf.util.Utility;

import oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.model.ChangeEventSupportable;
import oracle.ateam.sample.mobile.util.MessageUtils;
import oracle.ateam.sample.mobile.util.TaskExecutor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;


/**
 * Class that manages remote data synchronization actions 
 * If a remote persistence manager is configured in persistence-mapping.xml for the given entity, and the create, update or remove
 * operation on the remote persistence manager fails, for example because the device is offline or the remote server is not reachable,
 * or the specific REST service is not running or the REST service call returns an error, then this failed remote transaction is registered
 * as a pending data synchronization action in this manager.
 * By calling the synchronize method in this class, a new attempt is made to synchronize the pending data synch actions.
 * When the application is closed or abandoned, the pending data synch actions are persisted to a table in
 * the SQLite database.
 * This persistence is implemented in method saveSynchActionsToDB(). When a new instance of the DataSynchManager is created,
 * the persisted data synch actions will be loaded again.
 *
 */
public class DataSynchManager
  extends ChangeEventSupportable
{
  private transient List dataSynchActions = new ArrayList();
  private transient boolean dataSynchRunning = false;

  private transient static DataSynchManager sInstance;
  
  private EntityCRUDService callbackCrudService;
    
  /**
   * Create singleton instance of DataSynchManager.
   * If you want to use your own subclass of DataSynchManager to extend/override the default behavior, then
   * you can specify the fully qualified class name in mobile-persistence-config.properties using property
   * datasync.manager.class
   * @return
   */
  public static synchronized DataSynchManager getInstance()
  {
    if (sInstance==null)
    {
      String className = PersistenceConfig.getDataSynchManagerClass();
      if (className!=null)
      {
        sInstance = (DataSynchManager) EntityUtils.getClassInstance(className);     
      }
      else
      {
        sInstance = new DataSynchManager();        
      }
    }
    return sInstance;
  }

  /**
   *  Protected constructor, not private so subclases can be created to customize/extend the behavior
   */
  protected DataSynchManager()
  {
    super();
    loadSynchActionsFromDB();      
  }

  /**
   * Get instance of DataSynchronizer.
   */
  protected DataSynchronizer getDataSynchronizer(DataSynchPayload payload)
  {
    return new DataSynchronizer(this, payload);
  }

  /**
   * Perform data synchronization using the DataSynchronizer class
   * @param inBackground
   */
  public void synchronize(boolean inBackground)
  {
    synchronize(inBackground,null);
  }

  /**
   * Perform data synchronization using the DataSynchronizer class
   * @param inBackground
   * @param callbackCrudService the service to which the dataSynchFinished method call is propagated
   */
  public void synchronize(boolean inBackground, EntityCRUDService callbackCrudService)
  {
    if (isDataSynchRunning() || getDataSynchActions().size() == 0)
    {
      return;
    }
    this.callbackCrudService = callbackCrudService;
    DataSynchPayload payload = new DataSynchPayload();
    payload.setDataSynchActions(getDataSynchActions());
    DataSynchronizer syncher = getDataSynchronizer(payload);
    //   we now clear a synch action after succesfull processing in DataSynchronizer class
    //    clearDataSynchActions();
    TaskExecutor.getInstance().execute(inBackground, syncher);
  }
  
  protected String toJSON(DataSynchPayload payload)
  {
    String json = "";
    try
    {
      json = JSONBeanSerializationHelper.toJSON(payload).toString();
    }
    catch (Exception e)
    {
      MessageUtils.handleError(e);
    }
    return json;
  }

  protected DataSynchPayload fromJSON(String json)
  {
    try
    {
      return (DataSynchPayload) JSONBeanSerializationHelper.fromJSON(DataSynchPayload.class, json);
    }
    catch (Exception e)
    {
    }
    return null;
  }

  //  protected void clearDataSynchActions()
  //  {
  //    DataSynchAction[] oldArray = getDataSynchActions();
  //    dataSynchActionList.clear();
  //    // TODO remove from DB
  //    refreshDataSynchActionsList(oldArray);
  //  }

  /**
   * Register remote data sync action. If we are online and the sync action does not contain
   * a "last sync error" then we do not save the sync action in the local SQLITE db yet, because it will
   * be processed right away. If we are offline, or the data sync action contains a last sync error, then we also
   * store it in SQLite DB when offline transactions are supported. With this approach we prevent creating a data
   * sync row and subsequent removal of that same row in the normal scenario where we are online and the sync action
   * is processed succesfully right away.
   * @param synchAction
   */
  protected void registerDataSynchAction(DataSynchAction synchAction)
  {
    List<DataSynchAction> oldList = getDataSynchActions();
    // set the id, first retrieve current max id
    int maxId = 0;
    boolean isMerged = mergeWithExistingDataSyncActionIfNeeded(synchAction);
    if (isMerged)
    {
      return;
    }
    for (DataSynchAction action: oldList)
    {
      if (action.getId().intValue() > maxId)
      {
        maxId = action.getId().intValue();
      }
    }
    synchAction.setId(new Integer(maxId + 1));
    dataSynchActions.add(synchAction);
    // call to refresh list causes unpredictable behavior, not really needed here anyway
    //    refreshDataSynchActionsList(oldArray);
    refreshHasDataSynchActions(oldList.size() > 0);
    // we save to DB when last synch error is not null.
    boolean offlineTransactionsEnabled = ClassMappingDescriptor.getInstance(synchAction.getEntityClass()).isEnableOfflineTransactions();
    if (synchAction.getLastSynchError() != null && offlineTransactionsEnabled)
    {
      saveDataSynchAction(synchAction);
    }
  }

  /**
   * If a sync action for the same entity instance already exists, we merge the new sync action with
   * the existing one. This saves a server roundtrip and is needed when the existing sync action is a
   * create action where the server derives a new primary key. Then the second (update sync action)
   * would fail because the primary key has changed, By merging the create and update sync actions into one action,
   * we solve the problem of server-derived primary key change.
   * If the existing sync action is a create action and the new sync action is a remove, we remove both sync actions.
   * @param synchAction
   * @return returns true when the new sync has been merged and does not need its own registration
   */
  protected boolean mergeWithExistingDataSyncActionIfNeeded(DataSynchAction synchAction)
  {
    boolean merged = false;
    DataSynchAction actionToRemove = null;
    for (DataSynchAction action: getDataSynchActions())
    {
      if (action.getEntityClassString().equals(synchAction.getEntityClassString()))
      {
        Entity existing = action.getEntity();
        Entity newEntity = synchAction.getEntity();
        boolean same = EntityUtils.compareKeys(EntityUtils.getEntityKey(existing), EntityUtils.getEntityKey(newEntity));
        if (same)
        {
          if ((action.getAction().equals(DataSynchAction.INSERT_ACTION) ||
               action.getAction().equals(DataSynchAction.UPDATE_ACTION)) &&
              synchAction.getAction().equals(DataSynchAction.UPDATE_ACTION))
          {
            // merge the new update with existing insert/update
            // we use all attr values of new instance, except for isNewEntit
            newEntity.setIsNewEntity(existing.getIsNewEntity());
            action.setEntity(newEntity);
            saveDataSynchAction(action);
            merged = true;
            break;
          }
          else if (action.getAction().equals(DataSynchAction.INSERT_ACTION) &&
                   synchAction.getAction().equals(DataSynchAction.REMOVE_ACTION))
          {
            // entity created and deleted again, we remove the create data sync action and ignore
            // the delete
            actionToRemove = action;
            merged = true;
            break;
          }
          else if (action.getAction().equals(DataSynchAction.UPDATE_ACTION) &&
                   synchAction.getAction().equals(DataSynchAction.REMOVE_ACTION))
          {
            // entity updated and deleted, we remove the update data sync action and return false for merged
            // so new remove data sync acton will be saved in calling method registerDataSynchAction
            actionToRemove = action;
            merged = false;
            break;
          }
        }
      }
    }
    if (actionToRemove != null)
    {
      getDataSynchActions().remove(actionToRemove);
      getDBPersistenceManager().removeEntity(actionToRemove, true);
    }
    return merged;
  }

  /**
   * Save data synch action to local DB. We do a merge action because the row might already exist
   * if the sync action has failed multiple times.
   * @param synchAction
   */
  protected void saveDataSynchAction(DataSynchAction synchAction)
  {
    DBPersistenceManager pm = getDBPersistenceManager();
    pm.mergeEntity(synchAction, true);
    synchAction.setIsNewEntity(false);
  }

  /**
   * Return instance of DBPersistenceManager that is used to read and write data sync actions to SQLite DB.
   * @return
   */
  protected DBPersistenceManager getDBPersistenceManager()
  {
    return new DBPersistenceManager();
  }

  /**
   * Send data change events so UI can refresh based on current number of pending data sync action
   * @param oldList
   */
  protected void refreshDataSynchActionsList(List<DataSynchAction> oldList)
  {
    List<DataSynchAction> newList = getDataSynchActions();
    boolean oldHasDataSynchActions = oldList.size() > 0;
    refreshHasDataSynchActions(oldHasDataSynchActions);

    TaskExecutor.getInstance().executeUIRefreshTask(() -> 
    {
      getPropertyChangeSupport().firePropertyChange("dataSynchActions", oldList, newList);
      getProviderChangeSupport().fireProviderRefresh("dataSynchActions");
      AdfmfJavaUtilities.flushDataChangeEvent();                        
    });

//    if (getHasDataSynchActions() != oldHasDataSynchActions)
//    {
//      getPropertyChangeSupport().firePropertyChange("hasDataSynchActions", oldHasDataSynchActions,
//                                                    getHasDataSynchActions());
//    }
//    getPropertyChangeSupport().firePropertyChange("dataSynchActions", oldList, newList);
//    getProviderChangeSupport().fireProviderRefresh("dataSynchActions");
//    TaskExecutor.flushDataChangeEvent();
  }

  /**
   * This method fires property change events when value of property hasDataSynchActions has changed.
   * It also sets the current number of data sync actions in an applicationScope variable named
   * ampa_DataSyncActionsCount, and it sets the boolean applicationScope variable named ampa_hasDataSyncActions
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
      AdfmfJavaUtilities.setELValue("#{applicationScope.ampa_hasDataSyncActions}", getHasDataSynchActions());
      if (getHasDataSynchActions() != oldValue)
      {
        getPropertyChangeSupport().firePropertyChange("hasDataSynchActions", oldValue, getHasDataSynchActions());
      }
      AdfmfJavaUtilities.flushDataChangeEvent();
    });
  }


  protected void setDataSynchRunning(boolean dataSynchRunning)
  {
    this.dataSynchRunning = dataSynchRunning;
  }

  public boolean isDataSynchRunning()
  {
    return dataSynchRunning;
  }

  public List<DataSynchAction> getDataSynchActions()
  {
    return dataSynchActions;
  }


  /**
   * Remove data synch action from the list and when last synch error is set, also remove from
   * the local SQLite DB. This method is called from reusable DataSynch Feature and from DataSynchronizer
   * when sync action is processed succesfully.
   * @param synchAction
   */

  public void removeDataSynchAction(DataSynchAction synchAction)
  {
    //   MessageUtils.handleMessage(AdfException.INFO, "sync sction removed: "+synchAction.getId());
    getDataSynchActions().remove(synchAction);
    if (synchAction.getLastSynchError() != null)
    {
      DBPersistenceManager pm = getDBPersistenceManager();
      pm.removeEntity(synchAction, true);
    }
    refreshHasDataSynchActions(true);
  }

  /**
   * Write all sync actions to SYNCH_ACTION DB table
   */
  public void saveSynchActionsToDB()
  {
    if (getDataSynchActions().size() == 0)
    {
      return;
    }
    DBPersistenceManager pm = getDBPersistenceManager();
    for (DataSynchAction action: getDataSynchActions())
    {
      pm.mergeEntity(action, true);
    }
  }

  /**
   * Read all sync actions from SYNCH_ACTION DB table
   */
  public void loadSynchActionsFromDB()
  {
    DBPersistenceManager pm = getDBPersistenceManager();
    this.dataSynchActions = pm.findAll(DataSynchAction.class);
    for (int i = 0; i < dataSynchActions.size(); i++)
    {
      // create entity instance in synch action from json string
      // When adding the logic in createEntityFromJSONString directly in method
      // setEntityAsJSONString (which is eecuted during JSOn serialization of the data synch action,
      // an outOfmemoryError is thrown for unclear reasons. When doing the create entity instance
      // action after the DataSynch actions are created, as below, it works fine
      DataSynchAction action = (DataSynchAction) dataSynchActions.get(i);
      action.createEntityFromJSONString();
    }
    refreshHasDataSynchActions(false);
  }

  /**
   *  Returns true when there are pending data synch actions 
   */
  public boolean getHasDataSynchActions()
  {
    return getDataSynchActions().size() > 0;
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
    if (callbackCrudService!=null)
    {
      callbackCrudService.dataSynchFinished(succeededDataSynchActions, failedDataSynchActions);
    }
    //    int ok = succeededDataSynchActions.size();
    //    int fails = failedDataSynchActions.size();
    //    int total = ok + fails;
    //    MessageUtils.handleMessage(AdfException.INFO,
    //                               total + " data synch actions completed. Successful: " + ok + ", Failed: " + fails);
  }
  
  /**
   * This method is called by the framework under following conditions
   * - user performs an action that triggers a transaction against a remote persistence manager
   * - offline transactions are disabled (enableOfflineTransactions=false in persistence-mapping.xml)
   * - the device is offline or the remote service call fails.
   * This method will take the error message recorded against the data sync action and present it to the
   * end user.
   * @param dataSynchAction
   */
  protected void reportFailedTransaction(DataSynchAction dataSynchAction)
  {
    MessageUtils.handleError(dataSynchAction.getLastSynchError());
  }
  
}
