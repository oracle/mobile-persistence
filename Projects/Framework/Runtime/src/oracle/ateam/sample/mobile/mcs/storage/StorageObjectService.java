/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 23-mar-2016   Steven Davelaar
 1.2           Use storageobject name as file name when downloading. Only use storageObject id if name is null
 07-mar-2016   Steven Davelaar
 1.1           saveStorageObjectToFileSystem now uses id instead of name to construct filepath!
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  /**
   * Constructor that allows you to override the default values for remoteReadInBackground and
   * remoteWriteInBackground as set in persistence-mapping.xml
   * @param remoteReadInBackground
   * @param remoteWriteInBackground
   */
  public StorageObjectService(boolean remoteReadInBackground, boolean remoteWriteInBackground)
  {
    super();
    setDoRemoteReadInBackground(remoteReadInBackground);
    setDoRemoteWriteInBackground(remoteWriteInBackground);
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
     * in the data control palette. 
     * 
     * Note that this method does NOT add the storageObject to the entity list because this method is 
     * automatically called by MAF framework when using the Create operation from the data control palette. 
     * MAF will add the entity to the list AFTER this method has been executed. 
     * 
     * You can use this method to set default values. 
     * If you want to refresh data in the UI based on the size of the entity list, then you cannot do this in this
     * method because the list is not updated yet (see above), instead override method entityAdded and add your 
     * logic there. The AMPA EntityList class ensures that this method is called after a storageObject has been added.
     * 
     * Do NOT drag and drop this method from the data control palette, use the Create operation 
     * instead to ensure that iterator binding and storageObject list stay in sync.
     * @param index
     * @param storageObject
     */
  public void addStorageObject(int index, StorageObject storageObject)
  {
    sLog.fine("Executing addStorageObject");
    addEntity(index, storageObject);
  }

  /**
   * Sets entity state to new, and if addToList argument is true, it adds the storage object to 
   * the list and fores change event to refresh the list in the UI
   * @param index
   * @param storageObject
   * @param addToList
   */
  public void addStorageObject(int index, StorageObject storageObject, boolean addToList)
  {
    sLog.fine("Executing addStorageObject");
    addEntity(index, storageObject, addToList);
  }

  /**
    * Removes the storage object file from the file system and removes the storage 
    * object from SQLite DB.
    * This method is automatically called when using the Delete operation on the storageObject collection
    * in the data control palette. It deletes the corresponding row from the database (if persisted) and 
    * calls the configured remove method on the remote persistence manager.
    * 
    * Note that this method does NOT remove the storageObject from the entity list because this method is 
    * automatically called by MAF framework when using the Delete operation from the data control palette. 
    * MAF will remove the entity from the list AFTER this method has been executed. 
    * If you want to directly remove a storage object without using the binding layer, then call the overloaded version
    * of this method with removeFromList argument set to true.
    * 
    * If you want to refresh data in the UI based on the size of the entity list, then you cannot do this in this
    * method because the list is not updated yet (see above), instead override method entityRemoved and add your 
    * logic there. The AMPA EntityList class ensures that this refresh method is called after a storageObject has been added.
    * 
    * Do NOT drag and drop this method from the data control palette, use the Delete operation 
    * instead to ensure that iterator binding and storageObject list stay in sync.
    * @param storageObject
    */
  public void removeStorageObject(StorageObject storageObject)
  {
    sLog.fine("Executing removeStorageObject");
    removeFromFileSystem(storageObject);
    removeEntity(storageObject);
  }

  /**
   * Removes the storageObject file from the file system and removes the storage 
   * object from SQLite DB
   * If removeFromList argument is true, it removes the storageObject from the
   * entity list and and fires change event to refresh the list in the UI
   * @param storageObject
   * @param removeFromList
   */
  public void removeStorageObject(StorageObject storageObject, boolean removeFromList)
  {
    sLog.fine("Executing removeStorageObject");
    removeFromFileSystem(storageObject);
    removeEntity(storageObject,removeFromList);
  }

  /**
   * Stores the object in MCS, and optionally on the device.
   * If the storeOnDevice flag of the storage object is true, the metadata are stored
   * in SQLite DB, and the content is written to the file system.
   * If remoteWriteInBackrgound flag is set to true, the storage in MCS happens
   * in background. If we are in offline mode, the store action will be saved as apending
   * sync action.
   * 
   * @param storageObject
   */
  public void saveStorageObject(StorageObject storageObject)
  {
    sLog.fine("Executing saveStorageObject");
    if (storageObject.isStoreOnDevice())
    {
      saveStorageObjectOnDevice(storageObject);
    }
    saveStorageObjectInMCS(storageObject);
  }

  /**
   * Stores the object on the device: the metadata are stored
   * in SQLite DB, and the content is written to the file system.
   * 
   * @param storageObject
   */
  public void saveStorageObjectOnDevice(StorageObject storageObject)
  {
    sLog.fine("Executing saveStorageObjectOnDevice");
    // first store on file syste so filePath is set on StorageObject and
    // also saved
    saveStorageObjectToFileSystem(storageObject);
    getLocalPersistenceManager().mergeEntity(storageObject, isAutoCommit());
    storageObject.setIsNewEntity(false);
  }
   
  /**
   * Remove the storage object file from the device file system
   * @param storageObject
   */
  public void removeFromFileSystem(StorageObject storageObject)
  {
    sLog.fine("Executing removeFromFileSystem");
    if (storageObject.getFilePath()!=null)
    {
      File file = new File(storageObject.getFilePath());
      if (file.exists())
      {
        file.delete();
        sLog.fine("File "+storageObject.getId()+" deleted from file system");
      }
    }    
  }

  /**
   * Stores the object in MCS. If we are offline, the store action will be registered
   * as a pending sync action and executed once the device is online again.
   * @param storageObject
   */
  public void saveStorageObjectInMCS(StorageObject storageObject)
  {
    sLog.fine("Executing saveStorageObjectInMCS");
    // We use UPDATE_ACTION, doesn't matter whether we use INSERT or UPDATE because we always
    // excecute the PUT method.
    writeEntityRemote(new DataSynchAction(DataSynchAction.UPDATE_ACTION, storageObject, this.getClass().getName()));
  }

  /**
   * Retrieves all storageObject instances in the collection specified.
   * @param collectionName
   */
  public void findAllStorageObjectsInCollection(String collectionName)
  {
    sLog.fine("Executing findAllStorageObjectsInCollection");
    if (isPersisted())
    {
      Map<String,String> searchValues = new HashMap<String,String>();
      searchValues.put("collectionName", collectionName);
      setEntityList(getLocalPersistenceManager().find(getEntityClass(), searchValues));
    }
    doRemoteFind(collectionName);
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
  public StorageObject findStorageObjectMetadata(String collection, String objectId)
  {
    sLog.fine("Executing findStorageObjectMetadata");
    StorageObject storageObject = findOrCreateStorageObject(collection, objectId);
    findStorageObjectMetadataRemote(storageObject);
    return storageObject;
  }
    
  /**
   * Looks up a StorageObject locally in the entity cache, and if it is not there, it queries
   * the SQLite DB. If storage object is not found, a new instance is returned with
   * the id and collectionName set the arguments passed into this method. This instance
   * is also added to the entity cache.
   * @param collection
   * @param objectId
   * @return
   */
  public StorageObject findOrCreateStorageObject(String collection, String objectId)
  {
    sLog.fine("Executing findOrCreateStorageObject");
    StorageObject storageObject = null;
    if (isPersisted())
    {
      storageObject = (StorageObject)getLocalPersistenceManager().findByKey(getEntityClass(), new Object[]{objectId,collection});
    }
    if (storageObject==null)
    {
      sLog.fine("Storage Object NOT found for collection "+collection+" and ID "+objectId);      
      storageObject = new StorageObject();
      storageObject.setId(objectId);    
      storageObject.setCollectionName(collection);
      storageObject.setIsNewEntity(true);
      EntityCache.getInstance().addEntity(storageObject);
    }
    else
    {
      sLog.fine("Storage Object found for collection "+collection+" and ID "+objectId);      
    }
    return storageObject;
  }

  public void findStorageObjectMetadataRemote(StorageObject storageObject)
  {
    sLog.fine("Executing findStorageObjectMetadataRemote");
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
        getMCSPersistenceManager().findStorageObjectMetadata(storageObject);
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
  public StorageObject findStorageObjectInMCS(String collection, String objectId, boolean throwNotFoundException)
  {
    sLog.fine("Executing findStorageObjectInMCS");
    StorageObject storageObject = findOrCreateStorageObject(collection, objectId);
    findStorageObjectInMCS(storageObject,throwNotFoundException);
    return storageObject;
  }
  
  /**
   * Retrieve the file content and metadata from MCS and save it to a file on the system and store the filePath 
   * and metadata in the storageObject (see method storeObjectContent).
   * If the device is offline, or the isLocalVersionIsCurrent flag on the storage object is true, then
   * this method will do nothing.
   * @param storageObject
   * @see storeObjectContent
   */
  public void findStorageObjectInMCS(StorageObject storageObject, boolean throwNotFoundException)
  {
    sLog.fine("Executing findStorageObjectInMCS");
    if (isOffline())
    {
      sLog.fine("Cannot execute findStorageObjectInMCS, device is offline");
      return;
    }
//    if (storageObject.isLocalVersionIsCurrent()) 
//    {
//      sLog.fine("Local version of storage object "+storageObject.getId()+" is already current");
//      return;
//    }
    TaskExecutor.getInstance().execute(isDoRemoteReadInBackground(), () ->
      {
        // auto synch any pending storage actions first, pass false for inBackground because
        // we want to proces pending actions before we do remote read
        synchronize(false);
        try
        {
          byte[] response = getMCSPersistenceManager().findStorageObject(storageObject);
          // response is null when local file (based on value of eTag) is current version
          if (response!=null)
          {
            if (storageObject.getDownloadCallback()!=null)
            {
              sLog.fine("Executing download callback for storage Object "+storageObject.getId());
              storageObject.getDownloadCallback().run();        
            }
            storageObject.setContent(response);
            if (storageObject.isStoreOnDevice())
            {
              saveStorageObjectOnDevice(storageObject);            
            }  
          }
        }
        catch (RestCallException e)
        {
           // check whether object is not found in MCS
           if (e.getResponseStatus()==404)
           {
             // remove the storage object from DB and file system  
             removeStorageObject(storageObject, true);
             if (throwNotFoundException)
             {
               throw e;                        
             }
           }
        }
      });    
  }

  /**
   * Save byte content of storage object to a file on file system. The default directory for download is the value
   * of AdfmfJavaUtilities.ApplicationDirectory concatenated with /MCS/ and the name of the collection.
   * The storageObject name is used as file name. If the storageObject name is null,
   * the storageObject id is used as file name. 
   * If the download is succesfull, the storageObject filePath is set to the fully qualified file system path.
   * If the content type is application/zip, we unzip the file in the same directory after downloading to the file system.
   * 
   * @param storageObject
   * @param response
   */
  public void saveStorageObjectToFileSystem(StorageObject storageObject)  
  {
    sLog.fine("Executing saveStorageObjectToFileSystem");
    byte[] content = storageObject.getContent();
    String dir = storageObject.getDirectoryPath();
    File fileDir = new File(dir);
    if (!fileDir.exists())
    {
      fileDir.mkdirs();      
    }
    String fileName = storageObject.getName()!= null ? storageObject.getName() : storageObject.getId();
    String filePath = dir + File.separator + fileName;
    File file = new File(filePath);
    OutputStream fos = null;
    try
    {
      fos = new FileOutputStream(file);
      fos.write(content);
      fos.flush();
      storageObject.setFilePath(filePath);
      sLog.info("Storage Object "+fileName+" succesfully saved to file system.");
    }
    catch (FileNotFoundException e)
    {
      sLog.info("Storage Object "+fileName+" NOT saved to file system: "+e.getLocalizedMessage());
    }
    catch (IOException e)
    {
      sLog.info("Storage Object "+fileName+" NOT saved to file system: "+e.getLocalizedMessage());
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
    sLog.fine("Executing unzipFile for "+zipFile.getName());
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
      sLog.info("Zip file "+zipFile.getName()+" succesfully unzipped to file system");
    }
    catch (IOException e)
    {
      sLog.info("Zip file "+zipFile.getName()+" NOT unzipped to file system: "+e.getLocalizedMessage());
      throw new AdfException(e);
    }
    finally
    {
      if (deleteZipFile)
      {
        zipFile.delete();        
        sLog.info("Zip file "+zipFile.getName()+" deleted from file system");
      }
    }
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

}


