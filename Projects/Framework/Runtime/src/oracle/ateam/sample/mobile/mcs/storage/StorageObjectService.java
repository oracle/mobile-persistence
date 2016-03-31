/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 25-mar-2016   Steven Davelaar
 1.3           - Use MessageUtils.handleError instead of throwing exception in method findStorageObjectInMCS so
               exception is also shown wehn triggered from background thread.
               - Renamed method resetStorageObject to resetStorageObjectMetadata
               - renamed method findStorageObjectMetadataRemote to findStorageObjectMetadataInMCS
               - saveStorageObjectToFileSystem: check for byte array not null
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
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.CopyOption;
import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.Paths;

import java.nio.file.StandardCopyOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import oracle.adfmf.framework.exception.AdfException;

import oracle.ateam.sample.mobile.exception.RestCallException;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;
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
   * Stores the object on the device and in MCS
   * If remoteWriteInBackrgound flag is set to true, the storage in MCS happens
   * in background. If we are in offline mode, the store action will be saved as apending
   * sync action.
   * 
   * @param storageObject
   */
  public void saveStorageObject(StorageObject storageObject)
  {
    sLog.fine("Executing saveStorageObject");
    saveStorageObjectOnDevice(storageObject);
    saveStorageObjectInMCS(storageObject);
  }

  /**
   * Stores the object on the device: the metadata are stored
   * in SQLite DB, and the contentStream (when set) is written to the file system.
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
   * Retrieves all storageObject instances in the collection specified. Looks up the storage objects locally, as well 
   * as in MCS. It does NOT download the actual storage files from MCS, only the metadata. To retrieve the file itself,
   * you can call methods getDownloadIfNeeded or getDownloadIfNeededInBackground. These methods are named like a normal
   * getter method so you can also drag and drop them from the data control palette as an output text (both methods 
   * return an empty string) to force a download without writing Java code.
   * @param collection
   */
  public void findAllStorageObjectsInCollection(String collection)
  {
    sLog.fine("Executing findAllStorageObjectsInCollection");
    if (collection==null || "".equals(collection))
    {
      sLog.severe("Cannot execute findAllStorageObjectsInCollection, collection argument must be specified");
      return;
    }
    if (isPersisted())
    {
      Map<String,String> searchValues = new HashMap<String,String>();
      searchValues.put("collectionName", collection);
      setEntityList(getLocalPersistenceManager().find(getEntityClass(), searchValues));
    }
    doRemoteFind(collection);
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
   * Looks up a StorageObject metadata locally as well as in MCS. The storage file will NOT be downloaded.
   * @param collection
   * @param objectId
   * @return
   */
  public StorageObject findStorageObjectMetadata(String collection, String objectId)
  {
    sLog.fine("Executing findStorageObjectMetadata");
    if (collection==null || "".equals(collection) || objectId==null || "".equals(objectId))
    {
      sLog.severe("Cannot execute findStorageObjectMetadata, collection and objectId arguments must be specified");
      return null;
    }
    StorageObject storageObject = findOrCreateStorageObject(collection, objectId);
    findStorageObjectMetadataInMCS(storageObject);
    return storageObject;
  }
    
  /**
   * Looks up a StorageObject locally as well as in MCS. The storage file will be downloaded if the file has not
   * been downloaded before or when the version in MCS is newer (checked using ETag)
   * @param collection
   * @param objectId
   * @return
   */
  public StorageObject findStorageObject(String collection, String objectId)
  {
    sLog.fine("Executing findStorageObject");
    if (collection==null || "".equals(collection) || objectId==null || "".equals(objectId))
    {
      sLog.severe("Cannot execute findStorageObject, collection and objectId arguments must be specified");
      return null;
    }
    StorageObject storageObject = findOrCreateStorageObject(collection, objectId);
    findStorageObjectInMCS(storageObject);
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
    if (collection==null || "".equals(collection) || objectId==null || "".equals(objectId))
    {
      sLog.severe("Cannot execute findOrCreateStorageObject, collection and objectId arguments must be specified");
      return null;
    }
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

  /**
   * Get a storageObject metadata from MCS.
   * If remoteReadInBackground is set to true in persistence-mapping.xml (the default), the file will be retrieved
   * in a background thread.
   * @param collection
   * @param objectId
   * @see getStorageObjectContentRemote
   */
  public void findStorageObjectMetadataInMCS(StorageObject storageObject)
  {
    sLog.fine("Executing findStorageObjectMetadataInMCS");
    if (isOffline())
    {
      sLog.fine("Cannot execute findStorageObjectMetadataInMCS, no network connection");
      return;
    }
    TaskExecutor.getInstance().execute(isDoRemoteReadInBackground(), () ->
      {
        // auto synch any pending storage actions first, pass false for inBackground because
        // we want to proces pending actions before we do remote read
        synchronize(false);
        getMCSPersistenceManager().findStorageObjectMetadata(storageObject);
        getLocalPersistenceManager().mergeEntity(storageObject, true);
      });    
  }

  /**
   * Get a storageObject with the metadata and content from MCS.
   * If remoteReadInBackground is set to true in persistence-mapping.xml (the default), the file will be retrieved
   * in a background thread.
   * @param collection
   * @param objectId
   * @see getStorageObjectContentRemote
   */
  public StorageObject findStorageObjectInMCS(String collection, String objectId)
  {
    sLog.fine("Executing findStorageObjectInMCS");
    if (collection==null || "".equals(collection) || objectId==null || "".equals(objectId))
    {
      sLog.severe("Cannot execute findStorageObjectInMCS, collection and objectId arguments must be specified");
      return null;
    }
    StorageObject storageObject = findOrCreateStorageObject(collection, objectId);
    findStorageObjectInMCS(storageObject);
    return storageObject;
  }
  
  /**
   * Retrieve the file content and metadata from MCS and save it to a file on the system and store the filePath 
   * and metadata in the storageObject (see method storeObjectContent).
   * If remoteReadInBackground is set to true in persistence-mapping.xml (the default), the file will be retrieved
   * in a background thread.
   * If the device is offline this method will do nothing. 
   * @param storageObject
   * @see storeObjectContent
   */
  public void findStorageObjectInMCS(StorageObject storageObject)
  {
    sLog.fine("Executing findStorageObjectInMCS");
    if (isOffline())
    {
      sLog.fine("Cannot execute findStorageObjectInMCS, device is offline");
      return;
    }
    TaskExecutor.getInstance().execute(isDoRemoteReadInBackground(), () ->
      {
        // auto synch any pending storage actions first, pass false for inBackground because
        // we want to proces pending actions before we do remote read
        synchronize(false);
        getMCSPersistenceManager().findStorageObject(storageObject);
      });    
  }

  /**
   * Stream content of storage object to a file on file system and close the stream. 
   * If the contentStream is null, this method will do nothing.
   * The default target directory for streaming is the value of AdfmfJavaUtilities.ApplicationDirectory concatenated 
   * with /MCS/ and the name of the collection.
   * The storageObject name is used as file name. If the storageObject name is null, the storageObject id is used as file name. 
   * If streaming of the file is succesfull, the storageObject filePath is set to the fully qualified file system path.
   * If the content type is application/zip, we unzip the file in the same directory after downloading to the file system.
   * 
   * @param storageObject
   */
  public void saveStorageObjectToFileSystem(StorageObject storageObject)  
  {
    sLog.fine("Executing saveStorageObjectToFileSystem");
    InputStream contentStream = storageObject.getContentStream();
    if (contentStream==null)
    {
      sLog.fine("contentStream is null, file not saved to file system");
      return;
    }
    // set up file path based on directoryPath and storageObject name/id
    String fileName = storageObject.getName()!= null ? storageObject.getName() : storageObject.getId();
    String filePath = storageObject.getFilePath();
    String dir = storageObject.getDirectoryPath();
    File fileDir = new File(dir);
    if (!fileDir.exists())
    {
      fileDir.mkdirs();      
    }
    filePath = dir + File.separator + fileName;
    try
    {
      Files.copy(contentStream, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
      storageObject.setFilePath(filePath);
      sLog.info("Storage Object "+fileName+" succesfully saved to file system.");
      storageObject.setLocalVersionIsCurrent(true);
      File file = new File(filePath);
      if ("application/zip".equals(storageObject.getContentType()))
      {
        unzipFile(file, file.getParentFile(),false);
      }     
    }
    catch (IOException e)
    {
      sLog.info("Storage Object "+fileName+" NOT saved to file system: "+e.getLocalizedMessage());
    }
    finally
    {
        try
        {
          storageObject.setContentStream(null);
          contentStream.close();
        }
        catch (IOException e)
        {
        }
    }      

  }

  /**
   * Unzip file in specified directory. Delete zip file after unzipping if deleteZipFile arg is true
   * @param zipFile
   * @param unzipDir
   * @param deleteZipFile
   */
  public void unzipFile(File zipFile, File unzipDir, boolean deleteZipFile)
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
  public void resetStorageObjectMetadata(StorageObject storageObject)
  {
    super.resetEntity(storageObject);
  }

}


