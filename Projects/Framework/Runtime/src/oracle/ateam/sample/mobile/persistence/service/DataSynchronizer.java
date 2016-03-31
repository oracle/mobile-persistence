/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 01-jan-2015   Steven Davelaar
 1.2           Set data synch running flag to true at start of run method
 17-nov-2014   Steven Davelaar
 1.1           Call method dataSynchFinished directly on EntityCRUDService
 22-jun-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oracle.adfmf.framework.api.JSONBeanSerializationHelper;

import oracle.ateam.sample.mobile.persistence.manager.DBPersistenceManager;
import oracle.ateam.sample.mobile.persistence.manager.PersistenceManager;
import oracle.ateam.sample.mobile.persistence.manager.RemotePersistenceManager;
import oracle.ateam.sample.mobile.persistence.model.Entity;
import oracle.ateam.sample.mobile.util.MessageUtils;

/**
 * Class that performs data synchronization on a set of data data sync actions passed in.
 * The data synchronization is performed by calling the appropriate method on the remote 
 * persistence manager configured fot this entity in persistenceMapping.xml
 * 
 * @deprecated Use the class with same name in oracle.ateam.sample.mobile.v2.persistence.* instead
 */
public class DataSynchronizer
  implements Runnable
{
  private final DataSynchPayload payload;
  private final DataSynchManager synchManager;
  private final EntityCRUDService crudService;
  List failedSynchActions = new ArrayList();
  List succeededSynchActions = new ArrayList();

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
    DataSynchAction[] dataSynchActions = payload.getDataSynchActions();
    if (dataSynchActions != null)
    {
      RemotePersistenceManager remotePersistenceManager = crudService.getRemotePersistenceManager();
      for (int i = 0; i < dataSynchActions.length; i++)
      {
        DataSynchAction syncAction = dataSynchActions[i];
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

  public List getFailedSynchActions()
  {
    return failedSynchActions;
  }

  public List getSucceededSynchActions()
  {
    return succeededSynchActions;
  }
}
