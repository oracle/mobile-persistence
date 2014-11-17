/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.model;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import oracle.adfdt.model.objects.DataControl;

import oracle.ateam.sample.mobile.dt.controller.parser.DataControlDataObjectParser;

import oracle.ide.Ide;

import oracle.javatools.db.DBObjectProvider;

import oracle.jdeveloper.db.ConnectionInfo;

public class BusinessObjectGeneratorModel {

//  private static final String DEFAULT_PACKAGE_PROPERTY = "defaultPackage";

    private String dataControlName;
    private DataControl dataControl;
    private int dataControlIndex;
    private String packageName ;
    private String servicePackageName;
    private String className;
    private List<DataObjectInfo> dataObjectInfos;
    private DataObjectInfo currentDataObject;
    private String sqlFileContent = "";
    private List<String> existingDescriptorMappings = new ArrayList<String>();
  private String connectionName;
  private String connectionUri;
    private DBObjectProvider dbProvider;
    private DataControlDataObjectParser dataControlVisitor;
    private String logTitle;
  private boolean overwriteDataObjectClasses = true;
  private boolean overwriteServiceObjectClasses = false;
  private boolean enableUsageTracking = true;

  public void setEnableUsageTracking(boolean enableUsageTracking)
  {
    this.enableUsageTracking = enableUsageTracking;
  }

  public boolean isEnableUsageTracking()
  {
    return enableUsageTracking;
  }

  public void setOverwriteServiceObjectClasses(boolean overwriteServiceObjectClasses)
  {
    this.overwriteServiceObjectClasses = overwriteServiceObjectClasses;
  }

  public boolean isOverwriteServiceObjectClasses()
  {
    return overwriteServiceObjectClasses;
  }
  private boolean webServiceDataControl = false;
    private boolean restfulWebService = false;
  private List<DCMethod> restResources = new ArrayList<DCMethod>();
  private List<HeaderParam> headerParams = new ArrayList<HeaderParam>();

   public BusinessObjectGeneratorModel(String defaultPackage)
   {
  //   String defaultPackage = Ide.getActiveProject() project.getProperty(DEFAULT_PACKAGE_PROPERTY);
     setPackageName(defaultPackage+".model");  
     setServicePackageName(defaultPackage+".model.service");  
   }

  public void setDataControlName(String dataControlName)
  {
    this.dataControlName = dataControlName;
  }
  
  public String getWorkspaceName()
  {
    String name = Ide.getActiveWorkspace().getShortLabel();
    // strip off ".jws"
    return name.substring(0, (name.length()-4));
  }

  public String getDataControlName()
  {
    return dataControlName;
  }

  public void setDataControlIndex(int dataControlIndex)
  {
    this.dataControlIndex = dataControlIndex;
  }

  public int getDataControlIndex()
  {
    return dataControlIndex;
  }

  public void setDataObjectInfos(List<DataObjectInfo> dataObjectInfos)
  {
    this.dataObjectInfos = dataObjectInfos;
  }

  public List<DataObjectInfo> getDataObjectInfos()
  {
    return dataObjectInfos;
  }

  public List<DataObjectInfo> getSelectedDataObjects()
  {
    List<DataObjectInfo> dois = new ArrayList<DataObjectInfo>();
    for (DataObjectInfo doi : getDataObjectInfos())
    {
      if (doi.isGenerate())
      {
        dois.add(doi);
      }
    }
    return dois;
  }


  public void setDataControl(DataControl dataControl)
  {
    this.dataControl = dataControl;
  }

  public DataControl getDataControl()
  {
    return dataControl;
  }

  public void setPackageName(String packageName)
  {
    this.packageName = packageName;
  }

  public String getPackageName()
  {
    return packageName;
  }

  public void setClassName(String className)
  {
    this.className = className;
  }

  public String getClassName()
  {
    return className;
  }

  public void setCurrentDataObject(DataObjectInfo currentDataObject)
  {
    this.currentDataObject = currentDataObject;
  }

  public DataObjectInfo getCurrentDataObject()
  {
    return currentDataObject;
  }

  public void setSqlFileContent(String sqlFileContent)
  {
    this.sqlFileContent = sqlFileContent;
  }

  public String getSqlFileContent()
  {
    return sqlFileContent;
  }

  public void setExistingDescriptorMappings(List<String> existingDescriptorMappings)
  {
    this.existingDescriptorMappings = existingDescriptorMappings;
  }

  public List<String> getExistingDescriptorMappings()
  {
    return existingDescriptorMappings;
  }

  public void setServicePackageName(String servicePackageName)
  {
    this.servicePackageName = servicePackageName;
  }

  public String getServicePackageName()
  {
    return servicePackageName;
  }

  public void setConnectionName(String connectionName)
  {
    this.connectionName = connectionName;
  }

  public String getConnectionName()
  {
    return connectionName;
  }

  public void setDbProvider(DBObjectProvider dbProvider)
  {
    this.dbProvider = dbProvider;
  }

  public DBObjectProvider getDbProvider()
  {
    return dbProvider;
  }

  public void setDataControlVisitor(DataControlDataObjectParser dataControlVisitor)
  {
    this.dataControlVisitor = dataControlVisitor;
  }

  public DataControlDataObjectParser getDataControlVisitor()
  {
    return dataControlVisitor;
  }

  public void setLogTitle(String logTitle)
  {
    this.logTitle = logTitle;
  }

  public String getLogTitle()
  {
    return logTitle;
  }

  public void setOverwriteDataObjectClasses(boolean overwriteJavaFiles)
  {
    this.overwriteDataObjectClasses = overwriteJavaFiles;
  }

  public boolean isOverwriteDataObjectClasses()
  {
    return overwriteDataObjectClasses;
  }

  public void setWebServiceDataControl(boolean webServiceDataControl)
  {
    this.webServiceDataControl = webServiceDataControl;
  }

  public boolean isWebServiceDataControl()
  {
    return webServiceDataControl;
  }

  public void setRestfulWebService(boolean restfulWebService)
  {
    this.restfulWebService = restfulWebService;
  }

  public boolean isRestfulWebService()
  {
    return restfulWebService;
  }

  public void setRestResources(List<DCMethod> restResources)
  {
    this.restResources = restResources;
  }

  public List<DCMethod> getRestResources()
  {
    return restResources;
  }

  public void setConnectionUri(String connectionUri)
  {
    this.connectionUri = connectionUri;
  }

  public String getConnectionUri()
  {
    return connectionUri;
  }

  public void setHeaderParams(List<HeaderParam> headerParams)
  {
    this.headerParams = headerParams;
  }

  public List<HeaderParam> getHeaderParams()
  {
    return headerParams;
  }
}
