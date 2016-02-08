/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 29-dec-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.mcs.storage;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.List;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import oracle.adfmf.framework.exception.AdfException;

import oracle.ateam.sample.mobile.exception.RestCallException;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.TaskExecutor;
import oracle.ateam.sample.mobile.v2.persistence.cache.EntityCache;
import oracle.ateam.sample.mobile.v2.persistence.manager.MCSPersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.service.DataSynchAction;
import oracle.ateam.sample.mobile.v2.persistence.service.EntityCRUDService;


/**
 *  Service class that provides local and remote operations against the storageObject data object. This allows
 *  you to work with MCS storage objecs in both online and offline mode.
 *  Local operations store data in STORAGE_OBJECT table, remote operations call the MCS storage API.
 *
 *  You can customize and extend this behavior by overriding methods of the EntityCRUDService superclass, and/or
 *  creating a subclass of the local and remote persistence managers as configured in persistence-mapping.xml.
 */
public class StorageObjectService
  extends EntityCRUDService<StorageObject>
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(StorageObjectService.class);

  /**
   * Default constructor. 
   *
   * If you need programmatic access to the same instance of this class as used by the bean data control typically
   * created for this class, then you can use the following convenience method:
   *
   * StorageObjectService crudService = (StorageObjectService) EntityUtils.getEntityCRUDService(StorageObject.class);
   *
   */
  public StorageObjectService()
  {
  }

  protected Class getEntityClass()
  {
    return StorageObject.class;
  }

  protected String getEntityListName()
  {
    return "storageObjects";
  }

  public List<StorageObject> getStorageObjects()
  {
    return getEntityList();
  }

  /**
   * This method is automatically called when using the Create operation on the storageObject collection
   * in the data control palette. It gets a new storageObject instance as argument and adds this instance to the
   * storageObject list.
   * Do NOT drag and drop this method from the data control palette, use the Create operation instead to ensure
   * that iterator binding and storageObject list stay in sync.
   * @param index
   * @param storageObject
   */
  public void addStorageObject(int index, StorageObject storageObject)
  {
    addEntity(index, storageObject);
  }

  /**
   * This method is automatically called when using the Delete operation on the storageObject collection
   * in the data control palette. It removes the storageObject instance passed in from the storageObject list, deletes the
   * corresponding row from the database (if persisted) and calls the configured remove method on the remote
   * persistence manager.
   * Do NOT drag and drop this method from the data control palette, use the Delete operation instead to ensure
   * that iterator binding and storageObject list stay in sync.
   * @param storageObject
   */
  public void removeStorageObject(StorageObject storageObject)
  {
    removeEntity(storageObject);
  }

  /**
   * Stores the object in MCS, and optionally on the device.
   * If the storeOnDevice flag of the storage object is true, the metadata are stored
   * in SQLite DB, and the content is written to the file system.
   * If remoteWriteInBackrgound flag is set to true, the storage in MCS happens
   * in background.
   * 
   * @param storageObject
   */
  public void saveStorageObject(StorageObject storageObject)
  {
    if (storageObject.isStoreOnDevice())
    {
      saveStorageObjectOnDevice(storageObject);
    }
    TaskExecutor.getInstance().execute(isDoRemoteWriteInBackground(), () ->
    {
      getMCSPersistenceManager().storeStorageObject(storageObject);
    });
  }

  /**
   * Stores the object on the device: the metadata are stored
   * in SQLite DB, and the content is written to the file system.
   * 
   * @param storageObject
   */
  public void saveStorageObjectOnDevice(StorageObject storageObject)
  {
    
    // storeOnDevic flag need to be added to PM and sql ddl
      getLocalPersistenceManager().mergeEntity(storageObject, isAutoCommit());
      storageObject.setIsNewEntity(false);
      saveStorageObjectToFileSystem(storageObject);
  }

  /**
   * Stores the object in MCS. If we are offline, the store action will be registered
   * as a pending sync action and executed once the device is online again.
   * @param storageObject
   */
  public void saveStorageObjectInMCS(StorageObject storageObject)
  {
    writeEntityRemote(new DataSynchAction(DataSynchAction.UPDATE_ACTION, storageObject, this.getClass().getName()));
  }


  /**
   * Retrieves all storageObject instances in the collection specified
   * @param collectionName
   */
  public void findStorageObjectsInCollection(String collectionName)
  {
    super.find(collectionName);
  }
  
  /**
   * Convenience method that casts remote persistence manager to MCSPersistenceManager
   * @return MCSPersistenceManager instance
   */
  public MCSPersistenceManager getMCSPersistenceManager()
  {
    return (MCSPersistenceManager) getRemotePersistenceManager();
  }

  /**
   * Invokes the getCanonical method on the remote persistence manager if this has not happened yet
   * for this instance during this application session. The corresponding row in the local database is also updated if
   * the entity is persistable. This method uses the setting of remote-read-in-background property
   * in persistenceMapping.xml to determine whether the method is executed in the background.
   * While you can call this method from the user interface layer using the data control palette, it is easier and
   * cleaner to call this method from a getter method for one of the attributes that will be populated by the
   * getCanonical method call. Here is an example of the code you should add to such a getter method:
   *
   * if (!canonicalGetExecuted())
   * {
   *   StorageObjectService crudService = (StorageObjectService) EntityUtils.getEntityCRUDService(Department.class);
   *   crudService.getCanonicalStorageObject(this);
   * }
   *
   * If you specifed the getCanonical triggering attribute in the AMPA REST wizard, then the above code is already generated
   * for you.
   *
   * @param collectionName
   */
  public StorageObject getStorageObjectMetadata(String collection, String objectId)
  {
    StorageObject storageObject = findOrCreateStorageObject(collection, objectId);
    getStorageObjectMetadataRemote(storageObject);
    return storageObject;
  }
    
  /**
   * 
   * @param collection
   * @param objectId
   * @return
   */
  public StorageObject findOrCreateStorageObject(String collection, String objectId)
  {
    StorageObject storageObject = null;
    if (isPersisted())
    {
      storageObject = (StorageObject)getLocalPersistenceManager().findByKey(getEntityClass(), new Object[]{objectId,collection});
    }
    if (storageObject==null)
    {
      storageObject = new StorageObject();
      storageObject.setId(objectId);    
      storageObject.setCollectionName(collection);
      EntityCache.getInstance().addEntity(storageObject);
    }
    return storageObject;
  }

  public void getStorageObjectMetadataRemote(StorageObject storageObject)
  {
    if (isOffline())
    {
      sLog.fine("Cannot execute getStorageObjectMetadataRemote, no network connection");
      return;
    }
    TaskExecutor.getInstance().execute(isDoRemoteReadInBackground(), () ->
      {
        // auto synch any pending storage actions first, pass false for inBackground because
        // we want to proces pending actions before we do remote read
        synchronize(false);
        getMCSPersistenceManager().getStorageObjectMetadata(storageObject);
        if (storageObject.isStoreOnDevice())
        {
          getLocalPersistenceManager().mergeEntity(storageObject, true);
        }
      });    
  }

  /**
   * Get a storageObject with the metadata and content from MCS.
   * @param collection
   * @param objectId
   * @see getStorageObjectContentRemote
   */
  public StorageObject getStorageObjectFromMCS(String collection, String objectId, boolean throwNotFoundException)
  {
    StorageObject storageObject = findOrCreateStorageObject(collection, objectId);
    getStorageObjectFromMCS(storageObject,throwNotFoundException);
    return storageObject;
  }
  
  /**
   * Retrieve the file content from MCS and save it to a file on the system and store the filePath in
   * the storageObject (see method storeObjectContent).
   * If the device is offline, or the isLocalVersionIsCurrent flag on the storage object is true, then
   * this method will do nothing.
   * @param storageObject
   * @see storeObjectContent
   */
  public void getStorageObjectFromMCS(StorageObject storageObject, boolean throwNotFoundException)
  {
    if (isOffline())
    {
      sLog.fine("Cannot execute getStorageObjectFromMCS, device is offline");
      return;
    }
    if (storageObject.isLocalVersionIsCurrent()) 
    {
      sLog.fine("Local version of storage object "+storageObject.getId()+" is already current");
      return;
    }
    TaskExecutor.getInstance().execute(isDoRemoteReadInBackground(), () ->
      {
        // auto synch any pending storage actions first, pass false for inBackground because
        // we want to proces pending actions before we do remote read
        synchronize(false);
        try
        {
          byte[] response = getMCSPersistenceManager().getStorageObject(storageObject);
          // response is null when local file (based on value of eTag) is current version
          if (response!=null)
          {
            storageObject.setContent(response);
            if (storageObject.isStoreOnDevice())
            {
              saveStorageObjectOnDevice(storageObject);            
            }  
          }
        }
        catch (RestCallException e)
        {
           if (e.getResponseStatus()==404 && throwNotFoundException)
           {
             throw e;         
           }
        }
      });    
  }

  /**
   * Save byte content of storage object to a file on file system. The default directory for download is the value
   * of AdfmfJavaUtilities.ApplicationDirectory concatenated with /MCS/ and the name of the collection.
   * If the content type is application/zip, we unzip the file in the same directory after downloading to the file system.
   * 
   * @param storageObject
   * @param response
   */
  public void saveStorageObjectToFileSystem(StorageObject storageObject)  
  {
    byte[] content = storageObject.getContent();
    String dir = storageObject.getDirectoryPath();
    File fileDir = new File(dir);
    if (!fileDir.exists())
    {
      fileDir.mkdirs();      
    }
    String filePath = dir + File.separator + storageObject.getName();
    File file = new File(filePath);
    OutputStream fos = null;
    try
    {
      fos = new FileOutputStream(file);
      fos.write(content);
      fos.flush();
      storageObject.setFilePath(filePath);
    }
    catch (FileNotFoundException e)
    {
    }
    catch (IOException e)
    {
    }
    finally
    {
      if (fos!=null)
      {
        try
        {
          fos.close();
        }
        catch (IOException e)
        {
        }
      }
    }
    if ("application/zip".equals(storageObject.getContentType()))
    {
      unzipFile(file, file.getParentFile(),false);
    }     
  }

  /**
   * Unzip file in specified directory. Delete zip file after unzipping if deleteZipFile arg is true
   * @param zipFile
   * @param unzipDir
   * @param deleteZipFile
   */
  protected void unzipFile(File zipFile, File unzipDir, boolean deleteZipFile)
  {
    if (!unzipDir.exists())
    {
      unzipDir.mkdirs();      
    }
    byte[] buffer = new byte[2048];
    try
    {
      FileInputStream fInput = new FileInputStream(zipFile);
      ZipInputStream zipInput = new ZipInputStream(fInput);
      ZipEntry entry = zipInput.getNextEntry();
      while (entry != null)
      {
        String entryName = entry.getName();
        File file = new File(unzipDir.getPath() + File.separator + entryName);
        if (entry.isDirectory())
        {
          File newDir = new File(file.getAbsolutePath());
          if (!newDir.exists())
          {
            newDir.mkdirs();
          }
        }
        else
        {
          FileOutputStream fOutput = new FileOutputStream(file);
          int count = 0;
          while ((count = zipInput.read(buffer)) > 0)
          {
            fOutput.write(buffer, 0, count);
          }
          fOutput.close();
        }
        zipInput.closeEntry();
        entry = zipInput.getNextEntry();
      }
      zipInput.closeEntry();
      zipInput.close();
      fInput.close();
    }
    catch (IOException e)
    {
      throw new AdfException(e);
    }
    finally
    {
      if (deleteZipFile)
      {
        zipFile.delete();        
      }
    }
  }

  /**
   * Synchronizes all pending data sync actions using the remote persistence manager
   * @param inBackground
   */
  public void synchronizeStorageObject(Boolean inBackground)
  {
    super.synchronize(inBackground);
  }

  /**
   * Resets the values of the storageObject instance to the values as stored in the SQLite database. This method
   * will do nothing when the storageObject is not persisted to the database.
   * @param storageObject
   */
  public void resetStorageObject(StorageObject storageObject)
  {
    super.resetEntity(storageObject);
  }

  /**
   * Returns true when there are pending storageObject data sync actions. Returns false if there are no such actions.
   */
  public boolean getHasStorageObjectDataSynchActions()
  {
    return getDataSynchManager().getHasDataSynchActions();
  }


}


