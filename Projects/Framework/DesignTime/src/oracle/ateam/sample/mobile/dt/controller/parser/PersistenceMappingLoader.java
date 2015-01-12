package oracle.ateam.sample.mobile.dt.controller.parser;

import java.io.ByteArrayInputStream;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.jaxb.MobileObjectPersistence;

public class PersistenceMappingLoader
{
  public PersistenceMappingLoader()
  {
    super();
  }

  public List<DataObjectInfo> run()
  {
    return new ArrayList<DataObjectInfo>();
  }

  private void loadFile()
  {
    String instancePath = MobileObjectPersistence.class.getPackage().getName();
    Unmarshaller u;
    try
    {
      JAXBContext jc = JAXBContext.newInstance(instancePath);
      u = jc.createUnmarshaller();
      // set validator which causes error to throw Exception which is probably caused by the fact the
      // application structure file is of an older version and needs to be migrated.
//      u.setValidating(false);
//      MobileObjectPersistence mobileObjectPersistence =
//        (MobileObjectPersistence) u.unmarshal(new ByteArrayInputStream(content.getBytes(node.getLoadEncoding())));
      //      Service service =
      //        (Service) u.unmarshal(new ByteArrayInputStream(content.getBytes()));

    }
    catch (Exception exc)
    {
      exc.printStackTrace();
    }
    // init complete continue validating
  }

}
