/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uipanel;

import java.awt.BorderLayout;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.JTextField;

import oracle.adf.model.adapter.DTContext;
import oracle.adf.model.connection.rest.RestConnection;
import oracle.adf.model.connection.url.URLConnection;
import oracle.adf.model.connection.url.URLConnectionProxy;

import oracle.adfdtinternal.model.adapter.webservice.wizard.rest.RestConnectionPanel;

import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.model.UIDataObjectInfo;
import oracle.ateam.sample.mobile.dt.util.FileUtils;
import oracle.ateam.sample.mobile.dt.util.PersistenceConfigUtils;
import oracle.ateam.sample.mobile.dt.util.ProjectUtils;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;
import oracle.ide.wizard.WizardCallbacks;


public class SelectURLConnectionPanel   
  extends DefaultTraversablePanel implements ActionListener
{

  private RestConnectionPanel connUI = new RestConnectionPanel(true);
  private WizardCallbacks callbacks;

  private JLabel mcsLabel = new JLabel("MCS Connection");
  private JCheckBox mcsField = new JCheckBox();
  private JLabel backendIdLabel = new JLabel("MCS Mobile Backend ID");
  private JTextField backendIdField = new JTextField();
  private JLabel anonymousLabel = new JLabel("MCS Anonymous Access Key");
  private JTextField anonymousField = new JTextField();
  
  public SelectURLConnectionPanel()
  {
    super();
    mcsField.addActionListener(this);

    this.setLayout(new BorderLayout(0, 15));
    //    this.setDefaultTitle(m_resourceManager.getString("JMIG_TGT_RES_0002"));
//    this.add(connUI, BorderLayout.NORTH);

    JPanel contentPanel = new JPanel();
    add(contentPanel, BorderLayout.NORTH);

    GridBagLayout containerLayout = new GridBagLayout();
    contentPanel.setLayout(containerLayout);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 6;
    gbc.gridheight = 1;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.insets = new Insets(0, 0, 20, 5);
    contentPanel.add(connUI, gbc);

    gbc.insets = new Insets(0, 0, 5, 5);
    gbc.gridwidth = 1;
    gbc.gridy++;
    contentPanel.add(mcsLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(mcsField, gbc);
    gbc.weightx = 0;

    gbc.insets = new Insets(0, 0, 5, 5);
    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(backendIdLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(backendIdField, gbc);
    gbc.weightx = 0;

    gbc.insets = new Insets(0, 0, 5, 5);
    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(anonymousLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(anonymousField, gbc);
    gbc.weightx = 0;
  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {
    String connection = connUI.getSrcConnection();
    if (connection == null)
    {
      throw new TraversalException("You need to select a REST Service Connection.");
    }
    BusinessObjectGeneratorModel model =
      (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    model.setConnectionName(connection);
    // There is a bug in RestConnectionPanel getSourceURI(). it is returning the value from the label "Source URI:"
//    String uri = connUI.getSourceURI();
    String uri = getConnectionUri(connection);
    
    if (uri!=null && mcsField.isSelected() && !uri.endsWith("/mobile")) {
        throw new TraversalException("MCS REST Service Connection URI should end with /mobile");
    }
    else if (uri!=null && uri.endsWith("/")) {
        throw new TraversalException("REST Service Connection URI should not end with a slash");
    }
    model.setConnectionUri(uri);
    model.setUseMCS(mcsField.isSelected());
    model.setMcsBackendId(backendIdField.getText());
    model.setMcsAnonymousAccessKey(anonymousField.getText());
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
    
    mcsField.setSelected(model.isUseMCS());    
    backendIdField.setText(model.getMcsBackendId());
    anonymousField.setText(model.getMcsAnonymousAccessKey());
    backendIdField.setEnabled(mcsField.isSelected());
    anonymousField.setEnabled(mcsField.isSelected());
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource()==mcsField)
    {
      backendIdField.setEnabled(mcsField.isSelected());
      anonymousField.setEnabled(mcsField.isSelected());
    }
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
