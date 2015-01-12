package oracle.ateam.sample.mobile.dt.controller.parser;

import java.io.ByteArrayInputStream;

import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import oracle.adfmf.common.util.McAppUtils;

import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.jaxb.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.dt.model.jaxb.MobileObjectPersistence;
import oracle.ateam.sample.mobile.dt.model.jaxb.ObjectFactory;
import oracle.ateam.sample.mobile.dt.util.FileUtils;

import oracle.ide.Ide;
import oracle.ide.model.Project;

public class PersistenceMappingLoader
{
  
  private List<DataObjectInfo> dataObjects  = new ArrayList<DataObjectInfo>();
  public PersistenceMappingLoader()
  {
    super();
  }

  public List<DataObjectInfo> run()
  {
//    MobileObjectPersistence mop = loadJaxbModel();
//    if (mop!=null)
//    {
//      for (ClassMappingDescriptor descriptor : mop.getClassMappingDescriptor())
//      {
//        addDataObject(descriptor);        
//      }
//    }
    return dataObjects;
  }

  public MobileObjectPersistence loadJaxbModel()
  {
    String fileName = "persistence-mapping.xml";
    Project appControllerProject = McAppUtils.getApplicationControllerProject(Ide.getActiveWorkspace()
                                                                                      , null);

    URL sourceURL = FileUtils.getSourceURL(appControllerProject, "META-INF", fileName);
    InputStream is =FileUtils.getInputStream(sourceURL);
    MobileObjectPersistence persistenceModel = null;
    if (is != null)
    {
      try
      {
        String instancePath = ObjectFactory.class.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(instancePath,ObjectFactory.class.getClassLoader());
        Unmarshaller u = jc.createUnmarshaller();
        // set validator which causes error to throw Exception which is probably caused by the fact the
        // application structure file is of an older version and needs to be migrated.
        u.setValidating(false);
        persistenceModel = (MobileObjectPersistence) u.unmarshal(is);
      }
      catch (Exception exc)
      {
        exc.printStackTrace();
      }
    }
    return persistenceModel;
  }


  private void addDataObject(ClassMappingDescriptor descriptor)
  {
    String className = descriptor.getClassName();
    String name = className.substring(className.lastIndexOf(".")+1);
    DataObjectInfo doi = new DataObjectInfo(name,null);
    doi.setClassName(className);
    doi.setPayloadDateFormat(descriptor.getDateFormat());
    doi.setPayloadDateTimeFormat(descriptor.getDateTimeFormat());
    doi.setPersisted(descriptor.isPersisted());
    doi.setGenerate(true);
    String manager = descriptor.getCrudServiceClass().getRemotePersistenceManager();
    doi.setXmlPayload(manager.toUpperCase().indexOf("JSON")==-1);
    // TODO also set sort order using attr names
    doi.setOrderBy(descriptor.getOrderBy());
  }
}
