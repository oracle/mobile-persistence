/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 29-dec-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.mcs.storage;

import java.io.File;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.TaskExecutor;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;

/**
 * Representation of MCS storage object. Ths object holds the metadata of the storage obhject, as well as a reference to
 * file location on the mobile device. The getContent() method returns the content of the file as a byte array.
 * By default storage objects retrieved from MCS are stored on the device: the file itself on the file system, and the
 * metadata in the STORAGE_OBJECT table. To prevent local storage you can call setStoreOnDevice(false), and the file is
 * then only accessible by calling getContent().
 */
public class StorageObject
  extends Entity
{

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(StorageObjectService.class);

  private final static String APP_DIR =
    AdfmfJavaUtilities.getDirectoryPathRoot(AdfmfJavaUtilities.ApplicationDirectory);

  private String createdOn;
  private String eTag;
  private String id;
  private String modifiedOn;
  private String createdBy;
  private String modifiedBy;
  private String name;
  private String contentType;
  private Long contentLength;
  private String collectionName;
  private String filePath;
  private Boolean localVersionIsCurrent = false;
  private String directoryPath; 
  private Boolean storeOnDevice = true;
  private transient byte[] content;
  // dummy attrs to prevent serialization 
  private transient String downloadIfNeeded;
  private transient String downloadIfNeededInBackground;
  private Runnable downloadCallback;

  public String getCreatedOn()
  {
    return this.createdOn;
  }

  public void setCreatedOn(String createdOn)
  {
    this.createdOn = createdOn;
  }


  public void setETag(String eTag)
  {
    this.eTag = eTag;
  }

  public String getETag()
  {
    return eTag;
  }


  public void setFilePath(String filePath)
  {
    String oldFilePath = this.filePath;
    this.filePath = filePath;
    TaskExecutor.getInstance().executeUIRefreshTask(() ->
      {
        propertyChangeSupport.firePropertyChange("filePath", oldFilePath, filePath);
        AdfmfJavaUtilities.flushDataChangeEvent();   
      });
  }

  public String getFilePath()
  {
    return filePath;
  }

  /**
   * Dummy attribute that can be included in an AMX page to trigger download of the file in the background.
   * If isLocalVersionIsCurrent() retruns true no download attempt is made.
   * This method returns an empty string
   * @return
   */
  public String getDownloadIfNeededInBackground()
  {
    if (!isLocalVersionIsCurrent())
    {
      StorageObjectService sos = new StorageObjectService(true,true);
      sos.findStorageObjectInMCS(this,false);      
    }
    return "";
  }

  /**
   * Dummy attribute that can be included in an AMX page to trigger download of the file in foreground.
   * If isLocalVersionIsCurrent() retruns true no download attempt is made.
   * This method returns an empty string.
   * @return
   */
  public String getDownloadIfNeeded()
  {
    if (!isLocalVersionIsCurrent())
    {
      StorageObjectService sos = new StorageObjectService(false,false);
      sos.findStorageObjectInMCS(this,false);
    }
    return "";
  }

  public String getId()
  {
    return this.id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getModifiedOn()
  {
    return this.modifiedOn;
  }

  public void setModifiedOn(String modifiedOn)
  {
    this.modifiedOn = modifiedOn;
  }

  public String getCreatedBy()
  {
    return this.createdBy;
  }

  public void setCreatedBy(String createdBy)
  {
    this.createdBy = createdBy;
  }

  public String getModifiedBy()
  {
    return this.modifiedBy;
  }

  public void setModifiedBy(String modifiedBy)
  {
    this.modifiedBy = modifiedBy;
  }

  public String getName()
  {
    return this.name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getContentType()
  {
    return this.contentType;
  }

  public void setContentType(String contentType)
  {
    this.contentType = contentType;
  }

  public Long getContentLength()
  {
    return this.contentLength;
  }

  public void setContentLength(Long contentLength)
  {
    this.contentLength = contentLength;
  }

  public void setCollectionName(String collectionName)
  {
    this.collectionName = collectionName;
  }

  public String getCollectionName()
  {
    return collectionName;
  }


  public void setDirectoryPath(String directoryPath)
  {
    this.directoryPath = directoryPath;
  }

  /**
   * Returns the directory path for downloading the file.
   * Defaults to AdfmfJavaUtilities.ApplicationDirectory+"/MCS/"+collectionName
   * @return
   */
  public String getDirectoryPath()
  {
    if (directoryPath==null)
    {
      directoryPath = APP_DIR + File.separator + "MCS"  + File.separator + getCollectionName();
    }
    return directoryPath;
  }

  public void setLocalVersionIsCurrent(Boolean localFileIsCurrent)
  {
    this.localVersionIsCurrent = localFileIsCurrent;
  }

  public Boolean isLocalVersionIsCurrent()
  {
    return localVersionIsCurrent;
  }


  public void setStoreOnDevice(Boolean storeOnDevice)
  {
    this.storeOnDevice = storeOnDevice;
  }

  public Boolean isStoreOnDevice()
  {
    return storeOnDevice;
  }

  public Boolean getStoreOnDevice()
  {
    return storeOnDevice;
  }

  public void setContent(byte[] content)
  {
    this.content = content;
  }

  public byte[] getContent()
  {
    if (content==null && getFilePath()!=null)
    {
      Path path = Paths.get(getFilePath());
      try
      {
        content = Files.readAllBytes(path);
      }
      catch (IOException e)
      {
        sLog.severe("Error reading bytes from file "+getId()+": "+e.getLocalizedMessage());
      }
    }
    return content;
  }


  public void setDownloadCallback(Runnable downloadCallback)
  {
    this.downloadCallback = downloadCallback;
  }

  public Runnable getDownloadCallback()
  {
    return downloadCallback;
  }
}
