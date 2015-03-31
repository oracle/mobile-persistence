 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  19-mar-2015   Steven Davelaar
  1.1           - Overloaded implementation of getCanonical: now possible to executed in background.
                - Added executeGetCanonical method to easily add behavior when executed in background.
                - Added call to EntityUtils.refreshCurrentEntity in refreshEntityList method
                to ensure UI is also refreshed correctly when child entities are shown in form layout 
                - Added support for enableOfflineTransactions flag: report remote transaction errors
                  immediately when offline transactions are disabled (using new method reportFailedTransaction).
                - Call EntityCRUDService.synchronize convenience method in various methods instead of directly calling
                getDataSynchManager.synchronize so it will be easier to customize sync behavior. 
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.util.Utility;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;
import oracle.ateam.sample.mobile.util.StringUtils;
import oracle.ateam.sample.mobile.util.TaskExecutor;
import oracle.ateam.sample.mobile.v2.persistence.cache.EntityCache;
import oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.manager.RemotePersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingDirect;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.model.ChangeEventSupportable;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;


/**
 * Abstract class that provides CRUD operations against two configurable persistence managers:
 * a local persistence manager and a remote persistence manager. Both persistence managers can be configured
 * per entity in the persistence-mapping.xml file.
 * When both a local and remote persistence manager are configured,
 * then when the findAll method is called for the first time, it wll synchronize the local database with the
 * remote data source. In persistenceMapping.xml, you can also specify whether the remote CRUD actions are performed
 * in the background so the user can continue to work with his mobile app, or whether he needs to wait until
 * the remote action has completed.
 *
 */
public abstract class EntityCRUDService<E extends Entity>
  extends ChangeEventSupportable
{
  public static final String NETWORK_NOT_REACHABLE = "NotReachable"; // Indicates no network connectivity.
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(EntityCRUDService.class);

  private transient DBPersistenceManager localPersistenceManager = new DBPersistenceManager();
  private transient RemotePersistenceManager remotePersistenceManager = null;
  private boolean autoCommit = true;
  //  private Entity currentEntity;
  private boolean doRemoteReadInBackground = true;
  private boolean doRemoteWriteInBackground = true;
  private int lastNewEntityIndex = -1;
  private boolean autoGeneratePrimaryKey = true;
  private boolean showWebServiceInvocationErrors = false;
  private boolean offlineTransactionsEnabled = true;


  private transient DataSynchManager dataSynchManager;

  /**
   * The current list of entities. All operations that filter, add or remove
   * entityList will change the content of this list.
   */
  private EntityList<E> entityList = new EntityList<E>(this);

  /**
   * Default constructor, initializes properties based on settings in persistenceMapping.xml and also initializes the 
   * DataSynchManager which loads any pending data synch actions from the database. 
   * If auto-query is set to true for this entity in the persistenceMapping.xml, the findALl method 
   * will be executed as well.
   */
  public EntityCRUDService()
  {
    super();
    initialize();
    // we call getDataSynchManager so any pending data synch actions are loaded from the DB
    // and processed just before the first read/write operation
    getDataSynchManager();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(getEntityClass());
    if (descriptor.isAutoQuery())
    {
      findAll();
    }
  }

    /** 
     * Constructor, initializes properties based on settings in persistenceMapping.xml except for
     * auto-query setting and also initializes the 
     * DataSynchManager which loads any pending data synch actions from the database. 
     * If auto-query parameter passed inis true, the findALl method 
     * will be executed as well. Use this constructor if you want to ignore the auto-query settng as specified
     * in the persistenceMapping.xml
     */
    public EntityCRUDService(boolean doAutoQuery)
    {
      super();
      initialize();
      // we call getDataSynchManager so any pending data synch actions are loaded from the DB
      // and processed just before the first read/write operation
      getDataSynchManager();
      if (doAutoQuery)
      {
        findAll();
      }
    }

  /**
   * This method returns the type of the entity you want to have CRUD opertaions on.
   * @return
   */
  protected abstract Class getEntityClass();

  /**
   * This method returns the name of the property used to return the array of entity instances.
   * This name is used to automatically send change notifcations when the list of entities has been
   * changed so your mobile UI will automatically refresh.
   * For example, if you have a method getDepartments, this method should return "departments"
   * @return
   */
  protected abstract String getEntityListName();

  /**
   * Populates the entity list with all entities found.
   * When a remote persistence manager is configured, and the findAllRemote method had not been executed before
   * the data is fetched remotely and the local DB is populated with the results.
   */
  protected void findAll()
  {
    if (getLocalPersistenceManager() != null)
    {
      setEntityList(executeLocalFindAll());
    }
    if ((!isRemoteFindAllExecuted() || localPersistenceManager == null) && remotePersistenceManager != null && isOnline())
    {
      // also do remote find 
      doRemoteFindAll();
    }
    setLastNewEntityIndex(-1);
  }

  /**
   * Executes findAll method against DB persistence manager.
   * Convenience method that you can override to change the query behavior,
   * for example by adding a default filter.
   * @return
   */
  protected List<E> executeLocalFindAll()
  {
    return getLocalPersistenceManager().findAll(getEntityClass());
  }

  /**
   * Executes findAll method against remote persistence manager.
   * Convenience method that you can override to change the query behavior or to load
   * other data objects before or after. Note that this method is executed in the
   * background if doRemoteReadInBackGround property for this service in
   * the persistenceMapping.xml is set to true
   * @return
   */
  protected List<E> executeRemoteFindAll()
  {
    return remotePersistenceManager.findAll(getEntityClass());
  }

  /**
   * Executes findAllInParent method against remote persistence manager.
   * Convenience method that you can override to change the query behavior or to load
   * other data objects before or after. Note that this method is executed in the
   * background if doRemoteReadInBackGround property for this service in
   * the persistenceMapping.xml is set to true
   * @return
   */
  protected List<E> executeRemoteFindAllInParent(Entity parent, String accessorAttribute)
  {
    return remotePersistenceManager.findAllInParent(getEntityClass(), parent, accessorAttribute);
  }

  /**
   * Executes find method against remote persistence manager.
   * Convenience method that you can override to change the query behavior or to load
   * other data objects before or after. Note that this method is executed in the
   * background if doRemoteReadInBackGround property for this service in
   * the persistenceMapping.xml is set to true
   * @return
   */
  protected List<E> executeRemoteFind(String searchValue)
  {
    return remotePersistenceManager.find(getEntityClass(), searchValue);
  }


  /**
   * Populates the entity list with all entities thet have an attribute value that matches the searchValue.
   */
  protected void find(String searchValue)
  {
    if (getLocalPersistenceManager() != null)
    {
      setEntityList(getLocalPersistenceManager().find(getEntityClass(), searchValue, getQuickSearchAttributeNames()));
      setLastNewEntityIndex(-1);
    }
    doRemoteFind(searchValue);
  }

  /**
   * Override this method to configure on which attributes the quick search (find method) will
   * be performed. By default, all String attributes are searchable.
   * @return
   */
  protected List<String> getQuickSearchAttributeNames()
  {
    return null;
  }

  /**
   * Finds an entity instance using the key value
   * @param key
   * @return
   */
  protected Entity findEntityByKey(Object key)
  {
    Object[] keyValues = new Object[]
    {
      key
    };
    return findEntityByKey(keyValues);
  }

  /**
   * Finds an entity instance using the key value. This methid will first check the cache, and if not
   * foudn a nd a local persistence manager is defined, it will lookup the entity in the SQLite DB
   * @param key
   * @return
   */
  protected Entity findEntityByKey(Object[] key)
  {
    if (getLocalPersistenceManager() != null)
    {
      return getLocalPersistenceManager().findByKey(getEntityClass(), key);
    }
    else
    {
      return EntityCache.getInstance().findByUID(getEntityClass(), key);
    }
  }

  /**
   * Inserts an entity using the configured persistence managers.
   * If the insert is succesfull, the entity is added to the entity list, the entity new state is set to false and
   * method refreshEntityList is called to send change notifcations.
   * @param entity
   */
  protected void insertEntity(Entity entity)
  {
    validateEntity(entity);
    if (localPersistenceManager != null)
    {
      localPersistenceManager.insertEntity(entity, isAutoCommit());
    }
    if (remotePersistenceManager != null && remotePersistenceManager.isCreateSupported(entity.getClass()))
    {
      writeEntityRemote(new DataSynchAction(DataSynchAction.INSERT_ACTION, entity, this.getClass().getName()));
    }
    entity.setIsNewEntity(false);
    //  no longer add to list, this is already done wehn addEntity method is called through create operation
    //  addEntityToList(0,entity);
  }

  /**
   * Insert, update or remove an entity, or perform a custom action using the remote persistence manager.
   * If CRUD or custom action fails because the device is offline or the service call fails for some reason,  
   * and offline transactions are enabled, the data sync action will be saved for later execution and the end 
   * user will not see an error message: the next time the end user tries to perform a remote data action, 
   * these pending data sync actions will be replayed first.
   * If offline transactions are disabled, the error message will be displayed immediately to the end user 
   * (by calling method reportFailedTransaction), and the transaction is "lost", it will not be saved nor retried later.
   * @param dataSynchAction the sync action
   */
  protected void writeEntityRemote(DataSynchAction dataSynchAction)
  {
    // no need to throw error, data synch actions will be registered and processed next time
    //    if (getDataSynchManager().isDataSynchRunning())
    //    {
    //      throw new AdfException("Data sync currently running, try again later.",AdfException.ERROR);
    //    }
    dataSynchAction.setIsNewEntity(true);
    dataSynchAction.setLastSynchAttempt(new Date());
    dataSynchAction.setLastSynchError(isOnline()?
                                      (getDataSynchManager().isDataSynchRunning()?
                                       "Previous data synchronization still in progress": null): "Device is offline");
    if (dataSynchAction.getLastSynchError()!=null && !isOfflineTransactionsEnabled())
    {
      reportFailedTransaction(dataSynchAction);
    }
    else
    {
      getDataSynchManager().registerDataSynchAction(dataSynchAction);
      if (isOnline())
      {
        synchronize(isDoRemoteWriteInBackground());
      }      
    }
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

  /**
   * Callback method called by DataSynchronizer when dataSychronization action is done. It contains a list of succeeded
   * and failed data sync actions. You can override this method to add custom logic after data synchronization has taken place.
   * For example, you could warn the end user that one or more transactions have failed and will be re-tried later, or you can
   * inform them that all pending data sync actions have been processed successfully.
   * @param succeededDataSynchActions
   * @param failedDataSynchActions
   */
  protected void dataSynchFinished(List<DataSynchAction> succeededDataSynchActions, List<DataSynchAction> failedDataSynchActions)
  {
    refreshEntityList(getEntityList());
    if (isDoRemoteWriteInBackground())
    {
      AdfmfJavaUtilities.flushDataChangeEvent();
    }
  //    int ok = succeededDataSynchActions.size();
  //    int fails = failedDataSynchActions.size();
  //    int total = ok + fails;
  //    MessageUtils.handleMessage(AdfException.INFO,
  //                               total + " data synch actions completed. Successful: " + ok + ", Failed: " + fails);
  }

  /**
   * Returns the value for which the current max key value needs to be
   * incremented to generate a new primary key value for a new row. Returns 1.
   * Override this method if you want a different increment.
   * @return
   */
  protected int getKeyValueIncrement()
  {
    return 1;
  }

  /**
   * Adds new entity and sets entity status to new. Note that the entity is ALREADY added to the entity list
   * because this method is (indirectly) called through overridden add method in EntityList
   * If autogeneratePrimaryKey is true, the key value is set based on the current max
   * key value in the underlying table and the value returned by getKeyValueIncrement
   * @param index
   * @param entity
   */
  protected void addEntity(int index, E entity)
  {
    setLastNewEntityIndex(index);
    entity.setIsNewEntity(true);
    if (isAutoGeneratePrimaryKey())
    {
      generatePrimaryKeyValue(entity);
      EntityCache.getInstance().addEntity(entity);
    }
  }

  protected void generatePrimaryKeyValue(Entity entity)
  {
    EntityUtils.generatePrimaryKeyValue(getLocalPersistenceManager(), entity, getKeyValueIncrement());
  }

  /**
   * Updates an entity using the configured persistence managers.
   * @param entity
   */
  protected void updateEntity(Entity entity)
  {
    validateEntity(entity);
    if (localPersistenceManager != null)
    {
      localPersistenceManager.updateEntity(entity, isAutoCommit());
    }
    if (remotePersistenceManager != null && remotePersistenceManager.isUpdateSupported(entity.getClass()))
    {
      writeEntityRemote(new DataSynchAction(DataSynchAction.UPDATE_ACTION, entity,this.getClass().getName()));
    }
  }

  /**
   * Performs a custom action using the configured remote persistence manager.
   * @param entity
   */
  protected void invokeCustomMethod(Entity entity, String methodName)
  {
    if (remotePersistenceManager != null)
    {
      DataSynchAction action = new DataSynchAction(DataSynchAction.CUSTOM_ACTION, entity, this.getClass().getName());
      action.setCustomMethodName(methodName);
      writeEntityRemote(action);
    }
  }

  /**
   * Inserts or updates an entity using the configured persistence managers.
   * The insert or update is determined by calling isNewEntity on the entity instance.
   * @param entity
   */
  protected void mergeEntity(Entity entity)
  {
    boolean isNew = entity.getIsNewEntity();
    // we could have called mergeEntity on persistence manager, but
    // on insert we want to reuse the refresh code in insertEntity method
    //    persistenceManager.mergeEntity(entity, isAutoCommit());
    if (isNew)
    {
      insertEntity(entity);
    }
    else
    {
      updateEntity(entity);
    }
  }

  /**
   * Removes an entity using the configured local and remote persistence managers.
   * Note that this method assumes that the entity is ALREADY removed from the entity list
   * because this method is typically (indirectly) called through overridden remove method in EntityList
   * which fires when using the standard Remove operation in the data control palette. 
   * @param entity
   */
  protected void removeEntity(Entity entity)
  {
    if (!entity.getIsNewEntity())
    {
      if (localPersistenceManager != null)
      {
        localPersistenceManager.removeEntity(entity, isAutoCommit());
        EntityCache.getInstance().removeEntity(entity);
      }
      if (remotePersistenceManager != null && remotePersistenceManager.isRemoveSupported(entity.getClass()))
      {
        writeEntityRemote(new DataSynchAction(DataSynchAction.REMOVE_ACTION, entity,this.getClass().getName()));
      }
    }
  }

  /**
   * Send change notifications when the content of the entity list changes
   * @param oldEntityList
   */
  protected void refreshEntityList(List<E> oldEntityList)
  {
    getPropertyChangeSupport().firePropertyChange(getEntityListName(), oldEntityList, getEntityList());
    getProviderChangeSupport().fireProviderRefresh(getEntityListName());
    // the above two statements do NOT refresh the UI when the UI displays a form layout instead of
    // a list view. 
    EntityUtils.refreshCurrentEntity(getEntityListName(),getEntityList(),getProviderChangeSupport());
    if (AdfmfJavaUtilities.isBackgroundThread())
    {
      AdfmfJavaUtilities.flushDataChangeEvent();
    }
  }


  /**
   * Set autoCommit flag. When set to true, the local DBPersistenceManager will issue a commit after each
   * INSERT, UPDATE or DELEET statement.
   * @param autoCommit
   */
  protected void setAutoCommit(boolean autoCommit)
  {
    this.autoCommit = autoCommit;
  }

  /**
   * Returns value of autoCommit flag. When set to true, the local DBPersistenceManager will issue a commit after each
   * INSERT, UPDATE or DELEET statement.
   * @param autoCommit
   */
  protected boolean isAutoCommit()
  {
    return autoCommit;
  }

  /**
   * Executes commit statement on the configured persistence managers.
   */
  protected void commit()
  {
    if (localPersistenceManager != null)
    {
      localPersistenceManager.commmit();
    }
    if (remotePersistenceManager != null && isOnline())
    {
      TaskExecutor.getInstance().execute(isDoRemoteWriteInBackground()
          , () -> {
                    remotePersistenceManager.commmit();
                  });    
    }
  }

  /**
   * Executes rollback statement on the configured persistence managers.
   * Also clears the entity cache for this entity type so that when a new query is executed,
   * no cached objects with stale data will be used.
   */
  protected void rollback()
  {
    if (localPersistenceManager != null)
    {
      localPersistenceManager.rollback();
    }
    if (remotePersistenceManager != null && isOnline())
    {
      TaskExecutor.getInstance().execute(isDoRemoteWriteInBackground()
          , () -> {
                    remotePersistenceManager.rollback();
                  });    
    }
    EntityCache.getInstance().clear(getEntityClass());
  }

  /**
   * Return the current list of entities. Method is protected to prevent it from showing
   * up in data control palette. You can override in yourt cncrete service and cast it to the actual type
   * @return
   */
  protected List<E> getEntityList()
  {
    return entityList;
  }

  protected boolean getEntityListIsEmpty()
  {
    return entityList.size() == 0;
  }

  protected void setLocalPersistenceManager(DBPersistenceManager localPersistenceManager)
  {
    this.localPersistenceManager = localPersistenceManager;
  }

  public DBPersistenceManager getLocalPersistenceManager()
  {
    return localPersistenceManager;
  }

  protected void setRemotePersistenceManager(RemotePersistenceManager remotePersistenceManager)
  {
    this.remotePersistenceManager = remotePersistenceManager;
    // also set local PM neede for various operations when entity is persistable
    remotePersistenceManager.setLocalPersistenceManager(localPersistenceManager);
  }

  protected RemotePersistenceManager getRemotePersistenceManager()
  {
    return remotePersistenceManager;
  }

  /**
   * Changes the current entity list. Calls refreshEntityList to ensure change notifcations
   * are sent to the UI.
   * @param entityList
   */
  protected void setEntityList(List<E> entityList)
  {
    List<E> oldEntityList = getEntityList();
    // we need to use EntityList subclass to ensure add/removeEntity methods are called when using Create/Delete operations from DC
    this.entityList = new EntityList<E>(this);
    this.entityList.addAll(entityList);
    refreshEntityList(oldEntityList);
  }

  /**
   *  Calls findAll on the remote persistence manager. Calling this method will effectively
   *  synchronize the local database table that is mapped to this entity with the remote data source.
   *  You can configure whether this action is performed in the background by calling setDoRemoteReadInBackground
   */
  protected void doRemoteFindAll()
  {
    if (getRemotePersistenceManager() == null)
    {
      sLog.fine("Cannot execute doRemoteFindAll, no remote persistence manager configured");
      return;
    }
    else if (!getRemotePersistenceManager().isFindAllSupported(getEntityClass()))
    {
      sLog.fine("Cannot execute doRemoteFindAll, no findAll method defined for entity " + getEntityClass().getName());
      return;
    }
    else if (isOffline())
    {
      sLog.fine("Cannot execute doRemoteFindAll, no network connection");
      return;
    }
    setRemoteFindAllExecuted(true);
    TaskExecutor.getInstance().execute(isDoRemoteReadInBackground()
        , () -> {
                  // auto synch any pending actions first, pass false for inBackground because
                  // we want to proces pending actions before we do remote read
                  synchronize(false);
                  List<E> entities = executeRemoteFindAll();
                  if (entities != null)
                  {
                    // when an error occurs (for example server not available, the method returns null
                    setEntityList(entities);
                  }
                });    
  }

  /**
   *  Calls findAllInParent on the remote persistence manager.
   *  You can configure whether this action is performed in the background by calling setDoRemoteReadInBackground
   */
  protected void doRemoteFindAllInParent(final Entity parent, final String accessorAttribute)
  {
    if (getRemotePersistenceManager() == null)
    {
      sLog.fine("Cannot execute doRemoteFindAllInParent, no remote persistence manager configured");
      return;
    }
    else if (!getRemotePersistenceManager().isFindAllInParentSupported(getEntityClass(), accessorAttribute))
    {
      sLog.fine("Cannot execute doRemoteFindAllInParent, no findAllInParent method defined for entity " +
                getEntityClass().getName());
      return;
    }
    else if (isOffline())
    {
      sLog.fine("Cannot execute doRemoteFindAllInParent, no network connection");
      return;
    }
    TaskExecutor.getInstance().execute(isDoRemoteReadInBackground()
        , () -> {
                  // auto synch any pending actions first, pass false for inBackground because
                  // we want to proces pending actions before we do remote read
                  synchronize(false);
                  List<E> entities = executeRemoteFindAllInParent(parent, accessorAttribute);
                  if (entities != null)
                  {
                    // when an error occurs (for example server not available, the method returns null
                    setEntityList(entities);
                  }
                });    

  }

  /**
   *  Calls findAll on the remote persistence manager. Calling this method will effectively
   *  synchronize the local database table that is mapped to this entity with the remote data source.
   *  You can configure whether this action is performed in the background by calling setDoRemoteReadInBackground
   */
  protected void doRemoteFind(final String searchValue)
  {
    if (getRemotePersistenceManager() == null)
    {
      sLog.fine("Cannot execute doRemoteFind, no remote persistence manager configured");
      return;
    }
    else if (!getRemotePersistenceManager().isFindSupported(getEntityClass()))
    {
      sLog.fine("Cannot execute doRemoteFind, no find method defined for entity " + getEntityClass().getName());
      return;
    }
    else if (isOffline())
    {
      sLog.fine("Cannot execute doRemoteFind, no network connection");
      return;
    }
    TaskExecutor.getInstance().execute(isDoRemoteReadInBackground()
        , () -> {
                  // auto synch any pending actions first, pass false for inBackground because
                  // we are already running in background thread
                  synchronize(false);
                  List<E> entities = executeRemoteFind(searchValue);
                  if (entities != null)
                  {
                    // when an error occurs (for example server not available, the method returns null
                    setEntityList(entities);
                  }
                });    
  }

  protected void setDoRemoteReadInBackground(boolean doRemoteReadInBackground)
  {
    this.doRemoteReadInBackground = doRemoteReadInBackground;
  }

  protected boolean isDoRemoteReadInBackground()
  {
    return doRemoteReadInBackground;
  }

  protected void setDoRemoteWriteInBackground(boolean doRemoteWriteInBackground)
  {
    this.doRemoteWriteInBackground = doRemoteWriteInBackground;
  }

  protected boolean isDoRemoteWriteInBackground()
  {
    return doRemoteWriteInBackground;
  }

  protected void setLastNewEntityIndex(int lastNewEntityIndex)
  {
    this.lastNewEntityIndex = lastNewEntityIndex;
  }

  protected int getLastNewEntityIndex()
  {
    return lastNewEntityIndex;
  }

  protected boolean isOffline()
  {
    //    String status = (String) AdfmfJavaUtilities.evaluateELExpression("#{deviceScope.hardware.networkStatus}");
    String status = DeviceManagerFactory.getDeviceManager().getNetworkStatus();
    boolean offline = NETWORK_NOT_REACHABLE.equals(status) || "unknown".equals(status);
    //    boolean offline2 = NETWORK_NOT_REACHABLE.equals(status);
    //    if (offline!=offline2)
    //    {
    //      MessageUtils.handleError("Not same, EL value: "+offline2);
    //    }
    return offline;
  }

  protected boolean isOnline()
  {
    return !isOffline();
  }

  /**
   * Returns the dataSynchManager for this CRUD service. 
   * @return
   */
  protected DataSynchManager getDataSynchManager()
  {
    if (dataSynchManager == null)
    {
      dataSynchManager = new DataSynchManager(this);
    }
    return dataSynchManager;
  }
  
  protected void setAutoGeneratePrimaryKey(boolean autoGeneratePrimaryKey)
  {
    this.autoGeneratePrimaryKey = autoGeneratePrimaryKey;
  }

  protected boolean isAutoGeneratePrimaryKey()
  {
    return autoGeneratePrimaryKey;
  }

  /**
   * This method validates the entity.
   * It checks for required attributes. Override this method to add additional custom validations.
   * You can call entity.getIsNewEntity() to determine whether the entity will ne inserted or updated.
   * When using a validationGroup and validationBehavior in the amx page, all empty fields are set to
   * an empty string, this method first changes these attrbute values to null as it should.
   * @param entity
   */
  protected void validateEntity(Entity entity)
  {
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entity.getClass());
    List mappings = descriptor.getAttributeMappingsDirect();
    List<String> attrNames = new ArrayList<String>();
    for (int i = 0; i < mappings.size(); i++)
    {
      AttributeMappingDirect mapping = (AttributeMappingDirect) mappings.get(i);
      Object attributeValue = entity.getAttributeValue(mapping.getAttributeName());
      if ("".equals(attributeValue))
      {
        attributeValue = null;
        entity.setAttributeValue(mapping.getAttributeName(), null);
      }
      if (mapping.isRequired() && attributeValue == null)
      {
        attrNames.add(mapping.getAttributeName());
      }
    }
    if (attrNames.size() > 0)
    {
      String listToString = StringUtils.listToString(attrNames, ", ");
      String verb = attrNames.size() == 1? " is ": " are ";
      MessageUtils.handleError(listToString + verb + " required");
    }
  }

  /**
   * Process any pending data synch actions
   * @param inBackground
   */
  protected void synchronize(Boolean inBackground)
  {
    getDataSynchManager().synchronize(inBackground.booleanValue());
  }

  protected String getRemoteFindAllExecutedExpression()
  {
    String entityName = StringUtils.substitute(getEntityClass().getName(), ".", "_");
    return "#{applicationScope."+entityName+"RemoteFindAllExecuted}";
  }

  /** 
   * This method stores the executed value in an applicationScoped variable named
   * [entityClassName]RemoteFindAllExecuted. It is not stored in a member varuable of this
   * service class because the remoteFindAll might already be executed in the context of another 
   * feature which has its own class loader and its own instance of this service class
   */
  protected void setRemoteFindAllExecuted(boolean executed)
  {
    AdfmfJavaUtilities.setELValue(getRemoteFindAllExecutedExpression(), Boolean.valueOf(executed));
  }

  /**
   * This method is called to determine whether a remoteFindAll needs to be executed when the findAll method
   * is called. This method inspects a boolean applicationScoped property as returned by method  
   * getRemoteFindAllExecutedExpression().
   * @return
   */
  protected boolean isRemoteFindAllExecuted()
  {
      Boolean value = (Boolean) AdfmfJavaUtilities.evaluateELExpression(getRemoteFindAllExecutedExpression());
    return value!=null ? value.booleanValue() : false;
  }

  /**
   * If the entity is new, we remove the entity from the entity list. If it is an existing entity, we
   * refresh the attribute values with the values as stored in the corresponding row in the local database
   * @param entity
   */
  protected void resetEntity(Entity entity)
  {
    if (entity.getIsNewEntity())
    {
      getEntityList().remove(entity);
    }
    else if (getLocalPersistenceManager() != null)
    {
      getLocalPersistenceManager().resetEntity(entity);
    }
  }

  /**
   * Invokes the getCanonical method on the remote persistence manager if this has not happened yet
   * for this instance during this application session. The corresponding row in the local database is also updated if
   * the entity is persistable. The method is executed in foreground.
   * @param entity
   */
  protected void getCanonical(Entity entity)
  {
    getCanonical(entity,false);
  }
  
  /**
   * Invokes the getCanonical method on the remote persistence manager if this has not happened yet
   * for this instance during this application session. The corresponding row in the local database is also updated if
   * the entity is persistable. The method is executed in background when param executeInBackground is set to true.
   * The UI will be refreshed correctly when background call is finished.
   * @param entity
   * @param executeInBackground
   */
  protected void getCanonical(Entity entity, boolean executeInBackground)
  {
    if (isOnline() && getRemotePersistenceManager() != null && !entity.canonicalGetExecuted())
    {      
      // immediately set flag to false, so we can call this method from some get Attribute method without
      // causing endless loop
      entity.setCanonicalGetExecuted(true);

      TaskExecutor.getInstance().execute(executeInBackground
          , () -> {
                    executeGetCanonical(entity);                
                    if (executeInBackground)
                    {
                      AdfmfJavaUtilities.flushDataChangeEvent();
                    }
                  });    
    }
  }

  /**
   * Executes getCanonical method against remote persistence manager.
   * Convenience method that you can override to perform additional actions before or after. 
   * Note that this method is executed in the background if getCanonical is called with executeInBackground set to true.
   * @return
   */
  protected void executeGetCanonical(Entity entity)
  {
    getRemotePersistenceManager().getCanonical(entity);
    if (getLocalPersistenceManager() != null && ClassMappingDescriptor.getInstance(getEntityClass()).isPersisted())
    {
      getLocalPersistenceManager().mergeEntity(entity, true);
    }
    EntityUtils.refreshCurrentEntity(getEntityListName(),getEntityList(),getProviderChangeSupport());                          
  }


  /**
   * Initialize this service instance based on properties specified in crud-service-class element
   * in persistenceMapping XML file:
   * <ul>
   * <li>local-persistence-manager</li>
   * <li>remote-persistence-manager</li>
   * <li>remote-read-in-background</li>
   * <li>remote-write-in-background</li>
   * <li>auto-increment-primary-key</li>
   * <li>show-web-service-invocation-errors</li>
   * </ul>
   */
  protected void initialize()
  {
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(getEntityClass());
    String className = descriptor.getLocalPersistenceManagerClassName();
    Object pm = getClassInstance(className);
    if (pm instanceof DBPersistenceManager)
    {
      setLocalPersistenceManager((DBPersistenceManager) pm);
    }
    else if (pm != null)
    {
      throw new AdfException("Local persistence manager class " + className + ": should extend DBPersisenceManager",
                             AdfException.ERROR);

    }
    className = descriptor.getRemotePersistenceManagerClassName();
    if (className!=null && !"".equals(className))
    {
      pm = getClassInstance(className);
      if (pm instanceof RemotePersistenceManager)
      {
        RemotePersistenceManager rpm = (RemotePersistenceManager) pm;
        setRemotePersistenceManager(rpm);
      }
      else if (pm != null)
      {
        throw new AdfException("Remote persistence manager class " + className +
                               ": should implement RemotePersistenceManager interface", AdfException.ERROR);
      }      
    }
    setDoRemoteReadInBackground(descriptor.isRemoteReadInBackground());
    setDoRemoteWriteInBackground(descriptor.isRemoteWriteInBackground());
    setAutoGeneratePrimaryKey(descriptor.isAutoIncrementPrimaryKey());
    setDoRemoteReadInBackground(descriptor.isRemoteReadInBackground());
    setShowWebServiceInvocationErrors(descriptor.isShowWebServiceInvocationErrors());
    setOfflineTransactionsEnabled(descriptor.isEnableOfflineTransactions());
  }

  protected Object getClassInstance(String className)
  {
    Object instance = null;
    if (className == null || "".equals(className))
    {
      return null;
    }
    try
    {
      Class clazz = Utility.loadClass(className);
//      Class clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
      instance = clazz.newInstance();
    }
    catch (InstantiationException e)
    {
      throw new AdfException("Error creating instance for class" + className + ": " + e.getLocalizedMessage(),
                             AdfException.ERROR);
    }
    catch (IllegalAccessException e)
    {
      throw new AdfException("Error creating instance for class" + className + ": " + e.getLocalizedMessage(),
                             AdfException.ERROR);
    }
    catch (ClassNotFoundException e)
    {
      throw new AdfException("Error creating instance for class" + className + ": " + e.getLocalizedMessage(),
                             AdfException.ERROR);
    }
    return instance;
  }

  protected void setShowWebServiceInvocationErrors(boolean showWebServiceInvocationErrors)
  {
    this.showWebServiceInvocationErrors = showWebServiceInvocationErrors;
  }

  protected boolean isShowWebServiceInvocationErrors()
  {
    return showWebServiceInvocationErrors;
  }

  protected void setOfflineTransactionsEnabled(boolean offlineTransactionsEnabled)
  {
    this.offlineTransactionsEnabled = offlineTransactionsEnabled;
  }

  protected boolean isOfflineTransactionsEnabled()
  {
    return offlineTransactionsEnabled;
  }

}
