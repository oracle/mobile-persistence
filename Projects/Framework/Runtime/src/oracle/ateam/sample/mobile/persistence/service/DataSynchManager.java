/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 17-nov-2014   Steven Davelaar
 1.1           Moved method dataSynchFinished to EntityCRUDService for easy override
               and UI refresh
               DataSynchronizer now calls this method directly on the crudService
 22-jun-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.util.Map;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.JSONBeanSerializationHelper;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.util.Utility;

import oracle.ateam.sample.mobile.persistence.manager.DBPersistenceManager;
import oracle.ateam.sample.mobile.persistence.model.ChangeEventSupportable;
import oracle.ateam.sample.mobile.persistence.model.Entity;
import oracle.ateam.sample.mobile.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.util.MessageUtils;
import oracle.ateam.sample.mobile.util.TaskExecutor;


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
  private transient List dataSynchActionList = new ArrayList();
  private transient boolean dataSynchRunning = false;
  
  // dummy attrs to prevent JSON serialization
  private transient List registeredDataSynchManagers;
  private transient DataSynchAction[]  dataSynchActions;

  public DataSynchManager(EntityCRUDService crudService)
  {
    super();
    this.crudService = crudService;
//    loadSynchActionsFromFile();
    loadSynchActionsFromDB();
    // register after loading actions from DB, loading might fail, and then we registered invalid
    // manager
    registerDataSynchManager();
  }
  
  public static List getRegisteredDataSynchManagers()
  {
    List syncManagers = (List) AdfmfJavaUtilities.evaluateELExpression("#{applicationScope.dataSynchManagers}");
    return syncManagers;
  }

  public static DataSynchManager geDataSynchManager(String entityClass)
  {
    DataSynchManager curManager = null;
    List managers = getRegisteredDataSynchManagers();
    for (int i = 0; i < managers.size(); i++)
    {
      DataSynchManager manager = (DataSynchManager) managers.get(i);
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
    List syncManagers = (List) AdfmfJavaUtilities.evaluateELExpression("#{applicationScope.dataSynchManagers}");
    if (syncManagers==null)
    {
      syncManagers = new ArrayList();
      Map appscope = (Map) AdfmfJavaUtilities.evaluateELExpression("#{applicationScope}");
      appscope.put("dataSynchManagers",syncManagers);
    }
    syncManagers.add(this);
  }

  public void synchronize(boolean inBackground)
  {
    if (isDataSynchRunning() || getDataSynchActionList().size() == 0)
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
   * store it in SQLite DB. With this approach we prevent creating a data sync row and subsequent removal of that same row
   * in the normal scenario where we are online and the sync action is processed succesfully right away.
   * 
   * @param synchAction
   */
  protected void registerDataSynchAction(DataSynchAction synchAction)
  {
    DataSynchAction[] oldArray = getDataSynchActions();
    // set the id, first retrieve current max id
    int maxId= 0;
    for (int i = 0; i < oldArray.length; i++)
    {
      DataSynchAction action = oldArray[i];
      if (action.getId().intValue()>maxId)
      {
        maxId = action.getId().intValue();
      }
    }
    synchAction.setId(new Integer(maxId+1));
    dataSynchActionList.add(synchAction);
   // call to refresh list causes unpredictable behavior, not really needed here anyway
//    refreshDataSynchActionsList(oldArray);
    refreshHasDataSynchActions(oldArray.length>0);
    // we save to DB when last synch error is not null. 
    if (synchAction.getLastSynchError()!=null )
    {
      saveDataSynchAction(synchAction);
    }
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
    if (pm==null)
    {
      // use default pm
      pm = new DBPersistenceManager();
    }
    return pm;
  }

  protected void refreshDataSynchActionsList(DataSynchAction[] oldArray)
  {
    Object newArray = getDataSynchActions();
    boolean oldHasDataSynchActions = oldArray.length>0;
    if (getHasDataSynchActions()!=oldHasDataSynchActions)
    {
      getPropertyChangeSupport().firePropertyChange("hasDataSynchActions", oldHasDataSynchActions, getHasDataSynchActions());      
    }
    getPropertyChangeSupport().firePropertyChange("dataSynchActions", oldArray, newArray);
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
    String name = getCrudService().getEntityListName()+"_dataSyncActionsCount";
    AdfmfJavaUtilities.setELValue("#{applicationScope."+name+"}", new Integer(getDataSynchActionList().size()));
    if (getHasDataSynchActions()!=oldValue)
    {
      getPropertyChangeSupport().firePropertyChange("hasDataSynchActions", oldValue, getHasDataSynchActions());      
    }
    if (AdfmfJavaUtilities.isBackgroundThread())
    {
      AdfmfJavaUtilities.flushDataChangeEvent();
    }
  }

  protected List getDataSynchActionList()
  {
    return dataSynchActionList;
  }

  protected void setDataSynchRunning(boolean dataSynchRunning)
  {
    this.dataSynchRunning = dataSynchRunning;
  }

  public boolean isDataSynchRunning()
  {
    return dataSynchRunning;
  }

  public DataSynchAction[] getDataSynchActions()
  {
    DataSynchAction[] departments =
      (DataSynchAction[]) getDataSynchActionList().toArray(new DataSynchAction[getDataSynchActionList().size()]);
    return departments;
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
    getDataSynchActionList().remove(synchAction);
    if (synchAction.getLastSynchError()!=null)
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
    if (getDataSynchActionList().size()==0)
    {
      return;
    }
    DBPersistenceManager pm = getDBPersistenceManager(); 
    for (int i = 0; i < getDataSynchActionList().size(); i++)
    {
      DataSynchAction action = (DataSynchAction) getDataSynchActionList().get(i);
      pm.mergeEntity(action, true);
    }
  }

//  public void loadSynchActionsFromFile()
//  {
//    File file = new File(getFilePath());
//    if (!file.exists())
//    {
//      return;
//    }
//    final StringBuffer json = new StringBuffer(300000);
//    InputStream inputStream = Utility.getResourceAsStream(getFilePath());
//    final BufferedReader in;
//    try
//    {
//      // While this works for iPhone without explicitly mentioning an encoding, in Android we need to
//      // explicitly use the ISO-8859 or the US-ASCII encoding, otherwise the j.io.ISR will try to read the json
//      // file using UTF8 (which somehow fails with an exception).
//      in = new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"));
//    }
//    catch (UnsupportedEncodingException e)
//    {
//      e.printStackTrace();
//      throw new AdfException(e);
//    }
//    try
//    {
//      String line;
//      while ((line = in.readLine()) != null)
//      {
//        json.append(line);
//      }
//    }
//    catch (IOException e)
//    {
//      e.printStackTrace();
//      throw new AdfException(e);
//    }
//    finally
//    {
//      try
//      {
//        in.close();
//      }
//      catch (IOException e)
//      {
//        throw new AdfException(e);
//      }
//    }
//    DataSynchPayload payload = fromJSON(json.toString());
//// Doesn't work: creates immutable list!
//// List actions = Arrays.asList(payload.getDataSynchActions());
//// this.dataSynchActionList = actions;
//
//    DataSynchAction[] actions = payload.getDataSynchActions();
//    for (int i = 0; i < actions.length; i++)
//    {
//      this.dataSynchActionList.add(actions[i]);
//    }
//    // remove the file!
//    file.delete();
//  }

  public void loadSynchActionsFromDB()
  {
    DBPersistenceManager pm = (DBPersistenceManager) getCrudService().getLocalPersistenceManager();
    List attrNamesToSearch = new ArrayList();
    attrNamesToSearch.add("serviceClass");
    this.dataSynchActionList = pm.find(DataSynchAction.class, getCrudService().getClass().getName(), attrNamesToSearch);
    for (int i = 0; i < dataSynchActionList.size(); i++)
    {
      // create entity instance in synch action from json string
      // When adding the logic in createEntityFromJSONString directly in method
      // setEntityAsJSONString (which is eecuted during JSOn serialization of the data synch action, 
      // an outOfmemoryError is thrown for unclear reasons. When doing the create entity instance
      // action after the DataSynch actions are created, as below, it works fine
      DataSynchAction action = (DataSynchAction)dataSynchActionList.get(i);
      action.createEntityFromJSONString();
    }
    refreshHasDataSynchActions(false);
  }

  /**
   *  Returns true when there are pending data synch actions for this data synch manager.
   */
  public boolean getHasDataSynchActions()
  {
    return getDataSynchActionList().size()>0;
  }

}
