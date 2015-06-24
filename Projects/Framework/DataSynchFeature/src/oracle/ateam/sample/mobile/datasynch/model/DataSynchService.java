/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 23-jun-2015   Steven Davelaar
 1.1           When removing data synch action in UI, it was not removed from DB
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.datasynch.model;

import java.util.List;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

import oracle.ateam.sample.mobile.v2.persistence.service.DataSynchAction;
import oracle.ateam.sample.mobile.v2.persistence.service.DataSynchManager;
import oracle.ateam.sample.mobile.v2.persistence.service.EntityCRUDService;
import oracle.ateam.sample.mobile.util.MessageUtils;

public class DataSynchService extends EntityCRUDService
{
  public DataSynchService()
  {
    super();
  }
  
  public DataSynchManager getCurrentDataSynchManager()
  {
    String entityClass = (String) AdfmfJavaUtilities.evaluateELExpression("#{applicationScope.dataSynchEntity}");
    if (entityClass==null)
    {
      MessageUtils.handleError("No data synch manager entity class set!");
    }
    DataSynchManager curManager = DataSynchManager.geDataSynchManager(entityClass);    
    return curManager;
  }

  protected Class getEntityClass()
  {
    return DataSynchAction.class;
  }

  protected String getEntityListName()
  {
    return "dataSynchActions";
  }
  
  public DataSynchAction[] getDataSynchActions()
  {
    DataSynchAction[] dataObjects = (DataSynchAction[]) getEntityList().toArray(new DataSynchAction[getEntityList().size()]);
    return dataObjects;
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
      removeEntity(dataSynchAction);
  }
}
