 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
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

import oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.model.ChangeEventSupportable;
import oracle.ateam.sample.mobile.util.MessageUtils;
import oracle.ateam.sample.mobile.util.TaskExecutor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;


/**
 * Class that manages remote data synchronization actions for a specific EntityCRUDService.
 * If a remote persistence manager is configured in the EntityCRUDService instance, and the create, update or remove
 * operation on the remote persistence manager fails, for example because the device is offline or the remote server is not reachable,
 * or the specific REST service is not running or the REST service call returns an error, then this failed remote transaction is registered
 * as a pending data synchronization action in this manager.
 * By calling the synchronize method in this class, a new attempt is made to synchronize the pending data synch actions.
 * When the application is closed or abandoned, the pending data synch actions are persisted to a table in
 * the SQLite database.
 * This persistence is implemented in method saveSynchActionsToDB(). When a new instance of the DataSynchManager is created,
 * typically when the entity CRUD service class is instantiated, the persisted data synch actions will be loaded again.
 *
 */
public class DataSynchManager
  extends ChangeEventSupportable
{
  private transient EntityCRUDService crudService;
  private transient List dataSynchActions = new ArrayList();
  private transient boolean dataSynchRunning = false;

  // dummy attrs to prevent JSON serialization
  private transient List<DataSynchManager> registeredDataSynchManagers;

  public DataSynchManager(EntityCRUDService crudService)
  {
    super();
    this.crudService = crudService;
    //    loadSynchActionsFromFile();
    if (ClassMappingDescriptor.getInstance(crudService.getEntityClass()).isEnableOfflineTransactions())
    {
      loadSynchActionsFromDB();      
    }
    // register after loading actions from DB, loading might fail, and then we registered invalid
    // manager
    registerDataSynchManager();
  }

  public static List<DataSynchManager> getRegisteredDataSynchManagers()
  {
    List<DataSynchManager> syncManagers =
      (List<DataSynchManager>) AdfmfJavaUtilities.evaluateELExpression("#{applicationScope.dataSynchManagers}");
    return syncManagers;
  }

  public static DataSynchManager geDataSynchManager(String entityClass)
  {
    DataSynchManager curManager = null;
    List<DataSynchManager> managers = getRegisteredDataSynchManagers();
    for (DataSynchManager manager: managers)
    {
      if (entityClass.equals(manager.getCrudService().getEntityClass().getName()))
      {
        curManager = manager;
        break;
      }
    }
    return curManager;
  }

  protected void registerDataSynchManager()
  {
    List<DataSynchManager> syncManagers =
      (List<DataSynchManager>) AdfmfJavaUtilities.evaluateELExpression("#{applicationScope.dataSynchManagers}");
    if (syncManagers == null)
    {
      syncManagers = new ArrayList<DataSynchManager>();
      Map appscope = (Map) AdfmfJavaUtilities.evaluateELExpression("#{applicationScope}");
      appscope.put("dataSynchManagers", syncManagers);
    }
    syncManagers.add(this);
  }

  public void synchronize(boolean inBackground)
  {
    if (isDataSynchRunning() || getDataSynchActions().size() == 0)
    {
      return;
    }
    DataSynchPayload payload = new DataSynchPayload();
    payload.setDataSynchActions(getDataSynchActions());
    DataSynchronizer syncher = new DataSynchronizer(this, payload);
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

  protected void setCrudService(EntityCRUDService crudService)
  {
    this.crudService = crudService;
  }

  protected EntityCRUDService getCrudService()
  {
    return crudService;
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
    if (synchAction.getLastSynchError() != null && crudService.isOfflineTransactionsEnabled())
    {
      saveDataSynchAction(synchAction);
    }
  }

  /**
   * If a sync action for the same entity instance already exists, we merge the new sync action with
   * the existing one. This saves a server roundtrip and is needed when the existing sync action is a
   * create action where the server derives a new primary key. Then the second (update sync action)
   * will fail because the primary key has changed. If the existing sync action is a create action
   * and the new sync action is a remove, we remove both sync actions.
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

  protected DBPersistenceManager getDBPersistenceManager()
  {
    DBPersistenceManager pm = getCrudService().getLocalPersistenceManager();
    if (pm == null)
    {
      // use default pm
      pm = new DBPersistenceManager();
    }
    return pm;
  }

  protected void refreshDataSynchActionsList(List<DataSynchAction> oldList)
  {
    List<DataSynchAction> newList = getDataSynchActions();
    boolean oldHasDataSynchActions = oldList.size() > 0;
    if (getHasDataSynchActions() != oldHasDataSynchActions)
    {
      getPropertyChangeSupport().firePropertyChange("hasDataSynchActions", oldHasDataSynchActions,
                                                    getHasDataSynchActions());
    }
    getPropertyChangeSupport().firePropertyChange("dataSynchActions", oldList, newList);
    getProviderChangeSupport().fireProviderRefresh("dataSynchActions");
    if (AdfmfJavaUtilities.isBackgroundThread())
    {
      AdfmfJavaUtilities.flushDataChangeEvent();
    }
  }

  /**
   * This method fires property change events when value of property hasDataSynchActions has changed.
   * It also sets the current number of data sync actions in an applicationScope variable named
   * [entityListName]_DataSyncActionsCount. For example: employees_DataSyncActionsCount.
   * The entityListName is the value retirned by method getEntityListName() in the concrete subclass
   * of EntityCrudService.
   * This applicationScope variable can be used by the UI to
   * show a badge on a UI widget to show the current number of pending data sync actions.
   * @param oldValue
   */
  protected void refreshHasDataSynchActions(boolean oldValue)
  {
    String name = getCrudService().getEntityListName() + "_dataSyncActionsCount";
    AdfmfJavaUtilities.setELValue("#{applicationScope." + name + "}", new Integer(getDataSynchActions().size()));
    if (getHasDataSynchActions() != oldValue)
    {
      getPropertyChangeSupport().firePropertyChange("hasDataSynchActions", oldValue, getHasDataSynchActions());
    }
    if (AdfmfJavaUtilities.isBackgroundThread())
    {
      AdfmfJavaUtilities.flushDataChangeEvent();
    }
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

  //  protected String getFilePath()
  //  {
  //    String dir = AdfmfJavaUtilities.getDirectoryPathRoot(AdfmfJavaUtilities.ApplicationDirectory);
  //    String path = dir + "/" + crudService.getEntityClass() + "DataSynchActions.json";
  //    return path;
  //  }

  //  public void saveSynchActionsToFile()
  //  {
  //    if (getDataSynchActionList().size()==0)
  //    {
  //      return;
  //    }
  //    DataSynchPayload payload = new DataSynchPayload();
  //    payload.setDataSynchActions(getDataSynchActions());
  //    String json = toJSON(payload);
  //    PrintStream out = null;
  //    try
  //    {
  //      out = new PrintStream(new FileOutputStream(getFilePath()));
  //      out.print(json);
  //    }
  //    catch (FileNotFoundException e)
  //    {
  //      throw new AdfException("Cannot save synch actions to file: "+e.getLocalizedMessage(),AdfException.ERROR);
  //    }
  //    finally
  //    {
  //      if (out != null)
  //        out.close();
  //    }
  //  }

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

  public void loadSynchActionsFromDB()
  {
    DBPersistenceManager pm = getCrudService().getLocalPersistenceManager();
    List<String> attrNamesToSearch = new ArrayList<String>();
    attrNamesToSearch.add("serviceClass");
    this.dataSynchActions = pm.find(DataSynchAction.class, getCrudService().getClass().getName(), attrNamesToSearch);
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
   *  Returns true when there are pending data synch actions for this data synch manager.
   */
  public boolean getHasDataSynchActions()
  {
    return getDataSynchActions().size() > 0;
  }

}
