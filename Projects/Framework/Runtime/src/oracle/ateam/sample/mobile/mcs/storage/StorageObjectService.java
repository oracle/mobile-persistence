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

import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.TaskExecutor;
import oracle.ateam.sample.mobile.v2.persistence.cache.EntityCache;
import oracle.ateam.sample.mobile.v2.persistence.manager.MCSPersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.service.EntityCRUDService;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;


/**
 *  Service class that provides local and remote operations against the storageObject data object. This allows
 *  you to work with MCS storage objecs in offline mode.
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
   * Inserts or updates a storageObject using the configured persistence managers.
   * The insert or update is determined by calling isNewEntity on the storageObject instance.
   * @param storageObject
   */
  public void saveStorageObject(StorageObject storageObject)
  {
    super.mergeEntity(storageObject);
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
   * Convenience method that casst remote persistence manage to MCSPersistenceManager
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
      storageObject = (StorageObject)getLocalPersistenceManager().findByKey(getEntityClass(), new Object[]{objectId});
    }
    if (storageObject==null)
    {
      storageObject = new StorageObject();
      storageObject.setId(objectId);    
      storageObject.setCollectionName(collection);
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
      });    
  }

  /**
   * Populate the storageObject with the content from MCS.
   * @param collection
   * @param objectId
   * @see getStorageObjectContentRemote
   */
  public StorageObject getStorageObjectContent(String collection, String objectId)
  {
    StorageObject storageObject = findOrCreateStorageObject(collection, objectId);
    getStorageObjectContent(storageObject);
    return storageObject;
  }
  
  /**
   * Populate the storageObject with the content from MCS.
   * Use this method if you have called getStorageObjectMetadata before and pass in
   * the object returned by getStorageObjectMetadata.
   * @param storageObject
   * @see getStorageObjectContentRemote
   */
  public void getStorageObjectContent(StorageObject storageObject)
  {
    // Do nothing if local file available and eTag was same when retrieving metadata
    /// in this case isLocalVersionIsCurrent is set to true when retrieving 
    // the object metadata in MCSPersistenceManager
    if (!storageObject.isLocalVersionIsCurrent()) 
    {
      getStorageObjectContentRemote(storageObject);
    }
  }

  /**
   * Retrieve the file content from MCS and save it to a file on the system and store the filePath in
   * the storageObject (see method storeObjectContent).
   * @param storageObject
   * @see storeObjectContent
   */
  public void getStorageObjectContentRemote(StorageObject storageObject)
  {
    if (isOffline())
    {
      sLog.fine("Cannot execute getStorageObjectContentRemote, no network connection");
      return;
    }
    TaskExecutor.getInstance().execute(isDoRemoteReadInBackground(), () ->
      {
        // auto synch any pending storage actions first, pass false for inBackground because
        // we want to proces pending actions before we do remote read
        synchronize(false);
        byte[] response = getMCSPersistenceManager().getStorageObjectContent(storageObject);
        // response is null when local file (based on value of eTag) is current version
        if (response!=null)
        {
          storeObjectContent(storageObject, response);          
        }
      });    
  }

  /**
   * Save byte response to a file on file system. The default directory for download is the value
   * of AdfmfJavaUtilities.ApplicationDirectory concatenated with /MCS/ and the name of the collection.
   * If the content type is application/zip, we unzip the file in the same directory after downloading to the file system.
   * 
   * @param storageObject
   * @param response
   */
  protected void storeObjectContent(StorageObject storageObject, byte[] response)  
  {
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
      fos.write(response);
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


