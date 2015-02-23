package oracle.ateam.sample.mobile.dt;

import java.net.URL;

import oracle.ateam.sample.mobile.dt.editor.PersistenceMappingNode;

import oracle.bali.xml.addin.SchemaRegistryAddin;
import oracle.bali.xml.addin.XMLEditorAddin;

import oracle.ide.Addin;

public class AmpaAddin
  implements Addin
{
  public AmpaAddin()
  {
    super();
  }

  @Override
  public void initialize()
  {
    // schema reg is also done in PersistenceMappingNode class, but then ot doesn't woprk the first time the
    // mapping xml is opened. Need to close and re-open before code insight is available
    final URL url = Thread.currentThread().getContextClassLoader().getResource("xsd/persistenceMapping.xsd");
    SchemaRegistryAddin.registerSchema(url,".xml");
    XMLEditorAddin.register(PersistenceMappingNode.class,".xml",true,false);
  }
}
