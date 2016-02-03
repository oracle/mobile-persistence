/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import oracle.adfdt.model.objects.DataControl;

import oracle.adfmf.common.util.McAppUtils;

import oracle.ateam.sample.mobile.dt.controller.parser.DataControlDataObjectParser;

import oracle.ateam.sample.mobile.dt.model.jaxb.MobileObjectPersistence;

import oracle.ateam.sample.mobile.dt.util.FileUtils;

import oracle.ateam.sample.mobile.dt.util.PersistenceConfigUtils;
import oracle.ateam.sample.mobile.dt.util.ProjectUtils;

import oracle.ide.Ide;

import oracle.ide.model.Project;

import oracle.javatools.db.DBObjectProvider;

import oracle.jdeveloper.db.ConnectionInfo;

public class BusinessObjectGeneratorModel {

//  private static final String DEFAULT_PACKAGE_PROPERTY = "defaultPackage";

    private MobileObjectPersistence existingPersistenceMappingModel;

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
  private boolean enableUsageTracking = false;
  private boolean maf20Style = false;
  private String uriPrefix = "";
  // this should have same default value as Generator options panel.
  private Project generatorProject;
  private boolean useMCS = false;
  private String mcsBackendId;
  private String mcsAnonymousAccessKey;
  
  // editMode is true when running the Edit Persistence Mapping wizard
  private boolean editMode = false;

  public BusinessObjectGeneratorModel()
  {
    init();
  }

  public void setMaf20Style(boolean maf20Style)
  {
    this.maf20Style = maf20Style;
  }

  public boolean isMaf20Style()
  {
    return maf20Style;
  }

  public void setExistingPersistenceMappingModel(MobileObjectPersistence existingPersistenceMappingModel)
  {
    this.existingPersistenceMappingModel = existingPersistenceMappingModel;
  }

  public MobileObjectPersistence getExistingPersistenceMappingModel()
  {
    return existingPersistenceMappingModel;
  }

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

   private boolean oldPersistenceMappingFileExists()
   {
     Project appControllerProject = McAppUtils.getApplicationControllerProject(Ide.getActiveWorkspace()
                                                                                       , null);
     String fileName = "persistenceMapping.xml";
     URL sourceURL = FileUtils.getSourceURL(appControllerProject, "META-INF", fileName);
     String content = FileUtils.getStringFromInputStream(FileUtils.getInputStream(sourceURL));
     return content!=null;
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
    Map<String, DataObjectInfo> dataObjectMap = new HashMap<String, DataObjectInfo>();
    for (DataObjectInfo doi : getDataObjectInfos())
    {
      if (doi.isGenerate())
      {
        DataObjectInfo dataObjectWithSameClassName = dataObjectMap.get(doi.getClassName());
        if (dataObjectWithSameClassName!=null)
        {
          // DataObjectInfo with same name already exist, merged the two!
          // if one is an existing data object, then we merge the other one, if both are new
          // it doesn't matter'. If both are existing we do not merge (they must live in different
          // packages otherwise this would not be possible)
          if (doi.isExisting() && dataObjectWithSameClassName.isExisting())
          {
            dois.add(doi);          
          }
          else if (doi.isExisting())
          {
            // we need to remove the existing entry from the list, and add the new one, because the new
            // already exists in persistence-mapping.xml
            mergeDataObjects(doi,dataObjectWithSameClassName);            
            dois.remove(dataObjectWithSameClassName);
            dois.add(doi);
            dataObjectMap.put(doi.getClassName(),doi);
          }
          else
          {
            mergeDataObjects(dataObjectWithSameClassName,doi);                        
          }
        }
        else
        {
          dois.add(doi);          
          dataObjectMap.put(doi.getClassName(),doi);
        }
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

  private void mergeDataObjects(DataObjectInfo keep, DataObjectInfo merge)
  {
    for (AttributeInfo attr : merge.getAttributeDefs())
    {
      if (keep.getAttributeDef(attr.getAttrName())==null)
      {
        attr.setParentDataObject(keep);
        keep.addAttribute(attr);
      }
    }
    if (keep.getFindAllMethod()==null)
    {
      keep.setFindAllMethod(merge.getFindAllMethod());
    }
    if (keep.getFindMethod()==null)
    {
      keep.setFindMethod(merge.getFindMethod());
    }
    if (keep.getGetCanonicalMethod()==null)
    {
      keep.setGetCanonicalMethod(merge.getGetCanonicalMethod());
    }
    if (keep.getCreateMethod()==null)
    {
      keep.setCreateMethod(merge.getCreateMethod());
    }
    if (keep.getUpdateMethod()==null)
    {
      keep.setUpdateMethod(merge.getUpdateMethod());
    }
    if (keep.getMergeMethod()==null)
    {
      keep.setMergeMethod(merge.getMergeMethod());
    }
    if (keep.getDeleteMethod()==null)
    {
      keep.setDeleteMethod(merge.getDeleteMethod());
    }
    // move child accessors to "keep" data object"
    for (AccessorInfo accessor : merge.getAllChildren())
    {
      if (keep.findChildAccessor(accessor.getChildAccessorName())==null)
      {
        accessor.setParentDataObject(keep);
        keep.addChild(accessor);
      }
    }
    // loop over all other data objects, and check if they have a child accessor with the "merge"
    // data object as child. If so, move the pointer from "merge" to "keep" data object
    // NOTE: we should not loop over getSelectedDataObjectInfo, would cause infinite loop
    // as this method is called from getSelectedDataObjectInfos
    for (DataObjectInfo doi : this.getDataObjectInfos())
    {
      for (AccessorInfo accessor : doi.getAllChildren())
      {
        if (accessor.getChildDataObject()==merge)
        {
          accessor.setChildDataObject(keep);
          // if there is a child accessor method, we also need to add that "find-in=parent method to "keep" doi!
          if (accessor.getChildAccessorMethod()!=null)
          {
            keep.addFindAllInParentMethod(accessor.getChildAccessorMethod());
          }          
        }
      }
      // also check the parameterValueProvider objects for methods
      for(DCMethod method : doi.getAllMethods())
      {
        if (merge==method.getParameterValueProviderDataObject())
        {
          method.setParameterValueProviderDataObject(keep);
        }
      }
    }
  }


  public void setUriPrefix(String uriPrefix)
  {
    this.uriPrefix = uriPrefix;
  }

  /**
   * Returns the prefix that must be added to URI property of each CRUD method
   * @return
   */
  public String getUriPrefix()
  {
    return uriPrefix;
  }

  public void setGeneratorProject(Project generatorProject)
  {
    this.generatorProject = generatorProject;
  }

  public Project getGeneratorProject()
  {
    return generatorProject;
  }


  public void setUseMCS(boolean useMCS)
  {
    this.useMCS = useMCS;
  }

  public boolean isUseMCS()
  {
    return useMCS;
  }

  public void setMcsBackendId(String mcsBackendId)
  {
    this.mcsBackendId = mcsBackendId;
  }

  public String getMcsBackendId()
  {
    return mcsBackendId;
  }

  public void setMcsAnonymousAccessKey(String mcsAnonymousAccessKey)
  {
    this.mcsAnonymousAccessKey = mcsAnonymousAccessKey;
  }

  public String getMcsAnonymousAccessKey()
  {
    return mcsAnonymousAccessKey;
  }


  public void setEditMode(boolean editMode)
  {
    this.editMode = editMode;
  }

  /**
   * true when running edit persistence mapping wizard
   * @return
   */
  public boolean isEditMode()
  {
    return editMode;
  }

  private void init()
  {
    // set MCS settings from persistence config, so they are not lost when regenerating this file
    PersistenceConfigUtils.initializeMCSSettings(this);

    setMaf20Style(oldPersistenceMappingFileExists());

    Project appProject = ProjectUtils.getApplicationControllerProject();
    // we store Generator settings as project propagainst AppliationController prj, so we can default to values used
    // the last time    
    String generatorProjectName = appProject.getProperty(GENERATOR_PROJECT);
    Project generatorProject = ProjectUtils.getApplicationControllerProject();
    if (generatorProjectName!=null)
    {
      Map<String, Project> projects = ProjectUtils.getProjects();
      if (projects.containsKey(generatorProjectName))
      {
        generatorProject = projects.get(generatorProjectName);
      }
    }
    this.setGeneratorProject(generatorProject);  
    String defaultPackage = generatorProject.getProperty(DEFAULT_PACKAGE_PROPERTY);
    
    String dataObjectPackage = appProject.getProperty(DATA_OBJECT_PACKAGE);
    if (dataObjectPackage==null)
    {
      dataObjectPackage = defaultPackage+".model";
    }
    setPackageName(dataObjectPackage);  

    String servicePackage = appProject.getProperty(SERVICE_PACKAGE);
    if (servicePackage==null)
    {
      servicePackage = defaultPackage+".model.service";
    }
    setServicePackageName(servicePackage);  
    
    String overwriteDataObjects = appProject.getProperty(OVERWRITE_DATA_OBJECTS);
    // default to true, so only when false saved as prj prop then set to false    
    setOverwriteDataObjectClasses(!"false".equalsIgnoreCase(overwriteDataObjects));
    
    String overwriteServices = appProject.getProperty(OVERWRITE_SERVICES);
    // default to false, so only when true saved as prj prop then set to true    
    setOverwriteServiceObjectClasses("true".equalsIgnoreCase(overwriteServices));
  }

  private static final String DEFAULT_PACKAGE_PROPERTY = "defaultPackage";
  public static final String GENERATOR_PROJECT = "ampaGeneratorProject";
  public static final String DATA_OBJECT_PACKAGE = "ampaDataObjectPackage";
  public static final String SERVICE_PACKAGE = "ampaServicePackage";
  public static final String OVERWRITE_DATA_OBJECTS = "ampaOverwriteDataObjects";
  public static final String OVERWRITE_SERVICES = "ampaOverwriteServices";

}
