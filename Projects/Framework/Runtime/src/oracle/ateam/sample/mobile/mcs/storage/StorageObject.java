/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 25-mar-2016   Steven Davelaar
 1.1           - replaced method getContent with getContentStream
               - removed get/setStoredOnDevice
               - getFilePath returns null when file does not exist on file system
 29-dec-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.mcs.storage;

import java.io.File;
import java.io.InputStream;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

import oracle.ateam.sample.mobile.util.TaskExecutor;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;

/**
 * Representation of MCS storage object. This object holds the metadata of the storage object, as well as a reference to
 * file location on the mobile device. The get/setContentStream can be used to temporarily store
 * the (new) content of the file. If the contentStream is set, then calling StorageObjectService.saveStorageObjectToDevice
 * will stream the file to the device and save the storage object metadata in the SQLite DB.
 */
public class StorageObject
  extends Entity
{

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
  private transient InputStream contentStream;
  private transient Runnable downloadCallback;
  // dummy attrs to prevent serialization 
  private transient String downloadIfNeeded;
  private transient String downloadIfNeededInBackground;

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

  /**
   * Returns the file path where storage object file is stored on file system.
   * Note that when the path is set, but it is invalid (pointing to a non-existing file), this
   * method returns null. This is to handle iOS simulator testing, where the simulator path changes after
   * a redeployment of the app, but the dataase with storage object rows is preserved, causing an invalid
   * file path.
   * @return
   */
  public String getFilePath()
  {
    if (filePath!=null)
    {
      File file = new File(filePath);
      if (!file.exists())
      {
        // clear filePath
        filePath = null;
      }      
    }
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
      // we immediately set the local version current flag to true, because a data change event
      // to refresh UI might call this method again before the download is completed
      setLocalVersionIsCurrent(true);
      StorageObjectService sos = new StorageObjectService(true,true);
      sos.findStorageObjectInMCS(this);      
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
      // we immediately set the local version current flag to true, because a data change event
      // to refresh UI might call this method again before the download is completed
      setLocalVersionIsCurrent(true);
      StorageObjectService sos = new StorageObjectService(false,false);
      sos.findStorageObjectInMCS(this);
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


  public void setContentStream(InputStream contentStream)
  {
    this.contentStream = contentStream;
  }

  public InputStream getContentStream()
  {
    return contentStream;
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
