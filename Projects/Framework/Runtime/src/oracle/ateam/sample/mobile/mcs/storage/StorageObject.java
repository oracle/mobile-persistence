package oracle.ateam.sample.mobile.mcs.storage;

import java.io.File;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

import oracle.ateam.sample.mobile.v2.persistence.model.Entity;


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
  private boolean localVersionIsCurrent = false;
  private String directoryPath; 
  private File content;

  public String getCreatedOn()
  {
    return this.createdOn;
  }

  public void setCreatedOn(String createdOn)
  {
    this.createdOn = createdOn;
  }

  public String getETag()
  {
    return this.eTag;
  }

  public void setETag(String eTag)
  {
    this.eTag = eTag;
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

  public void setFilePath(String filePath)
  {
    this.filePath = filePath;
  }

  public String getFilePath()
  {
    return filePath;
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


  public void setContent(File content)
  {
    this.content = content;
  }

  public File getContent()
  {
    if (content==null && filePath!=null)
    {
      content = new File(filePath);
    }
    return content;
  }

  public void setLocalVersionIsCurrent(boolean localFileIsCurrent)
  {
    this.localVersionIsCurrent = localFileIsCurrent;
  }

  public boolean isLocalVersionIsCurrent()
  {
    return localVersionIsCurrent;
  }
}
