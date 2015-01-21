 /*******************************************************************************
  Copyright © 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oracle.ateam.sample.mobile.v2.persistence.manager.RemotePersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;

/**
 * Class that performs data synchronization on a set of data data sync actions passed in.
 * The data synchronization is performed by calling the appropriate method on the remote 
 * persistence manager configured fot this entity in persistenceMapping.xml
 */
public class DataSynchronizer
  implements Runnable
{
  private final DataSynchPayload payload;
  private final DataSynchManager synchManager;
  private final EntityCRUDService crudService;
  List<DataSynchAction> failedSynchActions = new ArrayList<DataSynchAction>();
  List<DataSynchAction> succeededSynchActions = new ArrayList<DataSynchAction>();

  public DataSynchronizer(DataSynchManager synchManager,DataSynchPayload payload)
  {
    super();
    this.synchManager = synchManager;
    this.crudService = synchManager.getCrudService();    
    this.payload = payload;
  }

  public void run()
  {
    synchManager.setDataSynchRunning(true); 
    List<DataSynchAction> dataSynchActions = payload.getDataSynchActions();
    if (dataSynchActions != null)
    {
      RemotePersistenceManager remotePersistenceManager = crudService.getRemotePersistenceManager();
      for (DataSynchAction syncAction : dataSynchActions)
      {
        String action = syncAction.getAction();
        Entity entity = syncAction.getEntity();
        try
        {
          if (DataSynchAction.INSERT_ACTION.equals(action))
          {
            remotePersistenceManager.insertEntity(entity, crudService.isAutoCommit());
          }
          else if (DataSynchAction.UPDATE_ACTION.equals(action))
          {
            remotePersistenceManager.updateEntity(entity, crudService.isAutoCommit());
          }              
          else if (DataSynchAction.REMOVE_ACTION.equals(action))
          {
            remotePersistenceManager.removeEntity(entity, crudService.isAutoCommit());
          }              
          else if (DataSynchAction.CUSTOM_ACTION.equals(action))
          {
            remotePersistenceManager.invokeCustomMethod(entity, syncAction.getCustomMethodName());
          }              
          succeededSynchActions.add(syncAction);       
          // remove succesful sync action from list
          synchManager.removeDataSynchAction(syncAction);
        }
        catch (Exception e)
        {
          syncAction.setLastSynchAttempt(new Date());
          syncAction.setLastSynchError(e.getLocalizedMessage());
          synchManager.saveDataSynchAction(syncAction);
          failedSynchActions.add(syncAction);            
        }
        
      }
    }
    synchManager.setDataSynchRunning(false); 
    crudService.dataSynchFinished(getSucceededSynchActions(), getFailedSynchActions());
  }

  public List<DataSynchAction> getFailedSynchActions()
  {
    return failedSynchActions;
  }

  public List<DataSynchAction> getSucceededSynchActions()
  {
    return succeededSynchActions;
  }
}
