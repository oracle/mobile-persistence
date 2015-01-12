package oracle.ateam.sample.mobile.dt.editor;

import java.net.URL;

import java.util.Iterator;
import java.util.logging.Level;

import oracle.adfmf.framework.dt.editor.FrameworkXmlEditorConstants;

import oracle.bali.xml.addin.XMLSourceNode;
import oracle.bali.xml.grammar.GrammarException;
import oracle.bali.xml.grammar.GrammarProvider;
import oracle.bali.xml.grammar.schema.SchemaGrammarProvider;
import oracle.bali.xml.gui.jdev.JDevXmlContext;

import oracle.ide.model.Element;

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
    System.err.println("********************* SDX *************");
    final GrammarProvider superGP = super.createGrammarProvider(context);
    final SchemaGrammarProvider sgp = new SchemaGrammarProvider(superGP);
    final URL url = getClass().getClassLoader().getResource("xsd/PersistenceMapping.xsd");
    System.err.println("XSD URL:  "+url);

    try
    {
      sgp.addSchema(url);
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
   @Override
   public Iterator<Element> getChildren()
   {
     return null;
   }

}
