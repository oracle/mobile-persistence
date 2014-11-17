/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uipanel;

import java.awt.BorderLayout;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import oracle.adf.model.adapter.DTContext;
import oracle.adf.model.connection.rest.RestConnection;
import oracle.adf.model.connection.url.URLConnection;
import oracle.adf.model.connection.url.URLConnectionProxy;

import oracle.adfdtinternal.model.adapter.webservice.wizard.rest.RestConnectionPanel;

import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;
import oracle.ide.wizard.WizardCallbacks;


public class SelectURLConnectionPanel
  extends DefaultTraversablePanel
{

  private RestConnectionPanel connUI = new RestConnectionPanel(true);
  private WizardCallbacks callbacks;

  public SelectURLConnectionPanel()
  {
    super();
    this.setLayout(new BorderLayout(0, 15));
    //    this.setDefaultTitle(m_resourceManager.getString("JMIG_TGT_RES_0002"));
    //    connUI.setConnectionPrompt(getInstructions());
    this.add(connUI, BorderLayout.NORTH);
  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {
    String connection = connUI.getSrcConnection();
    if (connection == null)
    {
      throw new TraversalException("You need to select a URL connection.");
    }
    BusinessObjectGeneratorModel model =
      (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    model.setConnectionName(connection);
    // There is a bug in RestConnectionPanel getSourceURI(). it is returning the value from the label "Source URI:"
//    String uri = connUI.getSourceURI();
    String uri = getConnectionUri(connection);
    model.setConnectionUri(uri);

    super.onExit(tc);
  }

  public void onEntry(TraversableContext traversableContext)
  {
    super.onEntry(traversableContext);
    BusinessObjectGeneratorModel model =
      (BusinessObjectGeneratorModel) traversableContext.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    connUI.updateSrcConnection(model.getConnectionName());
    // enable back -  next - finish
    callbacks = traversableContext.getWizardCallbacks();
    callbacks.wizardEnableButtons(true, true, false);
  }

  /**
   * Work around to get connection URI through looking up the connection in connections.xml
   * @param connectionName
   * @return
   * @throws TraversalException
   */
  private String getConnectionUri(String connectionName)
    throws TraversalException
  {
//    URLConnection urlConn = null;
    RestConnection urlConn = null;
    try
    {
      Context ctx = DTContext.getInstance().getConnectionContext();
//      urlConn = (URLConnection) ctx.lookup(connectionName);
      urlConn = (RestConnection) ctx.lookup(connectionName);
    }
    catch (NameNotFoundException ne)
    {
      // This piece of code causes error when building using ANT, while require-bundel entries
      // are idemtical to extensionsxml, very weird, comment it out for now, in tests it didn't need
      // this path
      //      compile:
      //          [javac] Compiling 57 source files to K:\adfemg\MobileAccelerator\svn\trunk\DesignTime\classes
      //          [javac] ..\RESTResourcesPanel.java:275:
      // cannot access oracle.adf.share.jndi.AdfJndiContext
      //          [javac] class file for oracle.adf.share.jndi.AdfJndiContext not found
      //          [javac]           Context connectionCtx = rcInstance.getConnectionContext();
      //      RescatContextRegistry registry = RescatContextRegistry.getInstance();
      //      RescatContext sourceCtx = registry.getResourcePaletteContext();
      //      if (sourceCtx != null)
      //      {
      //        try
      //        {
      //          RCInstance rcInstance = sourceCtx.getRcInstance();
      //          Context connectionCtx = rcInstance.getConnectionContext();
      //          urlConn = (URLConnection) connectionCtx.lookup(model.getConnectionName());
      //        }
      //        catch (NamingException e)
      //        {
      throw new TraversalException("Error naming URL: ");
      //        }
      //      }
    }
    catch (Exception e)
    {
      throw new TraversalException("Error alg accessing URL: " + e.getLocalizedMessage());
    }
    if (urlConn != null)
    {
      return urlConn.getURL().toString();
    }
    return null;
  }


}
