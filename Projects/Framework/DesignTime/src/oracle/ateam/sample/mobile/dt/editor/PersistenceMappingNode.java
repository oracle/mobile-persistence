package oracle.ateam.sample.mobile.dt.editor;

import java.net.URL;

import java.util.Iterator;
import java.util.logging.Level;

import oracle.adfmf.framework.dt.editor.FrameworkXmlEditorConstants;

import oracle.ateam.sample.mobile.dt.model.jaxb.MobileObjectPersistence;

import oracle.bali.xml.addin.SchemaRegistryAddin;
import oracle.bali.xml.addin.XMLEditorAddin;
import oracle.bali.xml.addin.XMLSourceNode;
import oracle.bali.xml.grammar.GrammarException;
import oracle.bali.xml.grammar.GrammarProvider;
import oracle.bali.xml.grammar.schema.SchemaGrammarProvider;
import oracle.bali.xml.gui.jdev.JDevXmlContext;

import oracle.ide.model.Element;
import oracle.ide.xml.XMLRecognizer;

/**
 * PersistenceMappingNode class is derived from XMLSourceNode. The XMLSourceNode
 * class is provided by XMLEF. It maintains the DOM structure of the underlying
 * document persistence-mapping.xml. XMLSourceNode provides all of the basic XML
 * parsing and loading features. XMLSourceNode overrides the
 * createGrammerProvider method to specify the xsd file that is required by XMLEF.
 */
public class PersistenceMappingNode
  extends XMLSourceNode
{
  public PersistenceMappingNode(URL url)
  {
    super(url);
  }

  public PersistenceMappingNode()
  {
    super();
  }

  @Override
  public GrammarProvider createGrammarProvider(JDevXmlContext context)
  {
//    System.err.println("************* PersistenceMappingNode Class Registered **************");
    final GrammarProvider superGP = super.createGrammarProvider(context);
    final SchemaGrammarProvider sgp = new SchemaGrammarProvider(superGP);
    final URL url = Thread.currentThread().getContextClassLoader().getResource("xsd/persistenceMapping.xsd");

    try
    {
      // Need to close and re-open the mapping XML before code insight is available with code below.
      // So, as a work around we also register the schema in AmpaAddin class and then it works right away 
      sgp.addSchema(url);
//      SchemaRegistryAddin.registerSchema(url,".xml");
      XMLEditorAddin.register(PersistenceMappingNode.class,".xml",true,false);
//      XMLRecognizer.mapNamespaceElemToClass("http://www.oracle.com/ateam/mobile/persistenceMapping", "mobileObjectPersistence", PersistenceMappingNode.class);
    }
    catch (GrammarException e)
    {
      e.printStackTrace();
    }
    return sgp;
  }

  /**
    * Overrides getChildren() defined in oracle.ide.model.Node to avoid lint warning.
    * @return null
    */
//   @Override
//   public Iterator<Element> getChildren()
//   {
//     return null;
//   }

}
