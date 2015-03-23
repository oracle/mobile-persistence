 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  24-mar-2015   Steven Davelaar
  1.1           If offline transactions are not supported, a failed transaction is
                not supported into DB but error is reported immediately to end user
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
 * persistence manager configured for this entity in persistence-mapping.xml
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

  /**
   * Performs the data sync action against the remote persistence manager. If the sync actions
   * throws an error, and offline transactions are enabled, the sync action is saved in the
   * database and will be re-executed later before new data synch actions will be performed.
   * If offline transactions are disabled (enableOfflineTransactions=false in persistence-mapping.xml)
   * then the error will be reported immediately to the end user (through a call to reportFailedTransaction
   * on EntityCRUDService instance).
   * At the end of the data synchronization run, a callback to method dataSyncFinished on the EntityCRUDService instance
   * will be performed with a list of succeeded and failed data sync actions. By overriding this callback method you
   * can execute custom logic after data synchronization has taken place.
   */
  public void run()
  {
    try
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
            if (crudService.isOfflineTransactionsEnabled())
            {
              synchManager.saveDataSynchAction(syncAction);            
            }
            else
            {
              // remove it from the list, we report the error and then the transaction is "lost".
              synchManager.removeDataSynchAction(syncAction);
              crudService.reportFailedTransaction(syncAction);
            }
            failedSynchActions.add(syncAction);            
          }
          
        }
      }
    }
    finally
    {
      synchManager.setDataSynchRunning(false);       
      crudService.dataSynchFinished(getSucceededSynchActions(), getFailedSynchActions());
    }
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
