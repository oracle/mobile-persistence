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

import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import javax.swing.JLabel;

import javax.swing.JPanel;

import javax.swing.JTextArea;

import javax.swing.border.Border;

import oracle.adfdt.model.DataControlManager;
import oracle.adfdt.model.datacontrols.JUDTAdapterDataControl;
import oracle.adfdt.model.objects.DataControl;

import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

import oracle.ateam.sample.mobile.dt.controller.parser.DataControlUIDataObjectParser;
import oracle.ateam.sample.mobile.dt.model.UIGeneratorModel;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ateam.sample.mobile.dt.view.wizard.UIGeneratorWizard;

import oracle.binding.meta.StructureDefinition;

import oracle.ide.Ide;
import oracle.ide.model.Project;
import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;

import oracle.ide.wizard.WizardCallbacks;

import oracle.javatools.parser.java.v2.model.JavaClass;

import oracle.jdeveloper.java.JavaManager;

public class SecurityWarningPanel
  extends DefaultTraversablePanel implements ActionListener
{

  MultiLineText instruction = 
    new MultiLineText("Using this wizard you can generate code that enables persistent data storage on a mobile device." +
    " While the data is stored in an encrypted SQLite database, you should be aware of the risk of data theft " +
    " when the device is lost or stolen. This wizard enables you to exclude individual data objects or specific data object attributes" +
    " from on-device storage to prevent theft of confidential data.");
  JLabel seclabel = new JLabel("I acknowledge the security risk");
  JCheckBox secCheckbox = new JCheckBox();
  private WizardCallbacks callbacks;

  public SecurityWarningPanel()
  {
    super();
    setLayout(new BorderLayout(0, 15));
    secCheckbox.addActionListener(this);
    secCheckbox.setSelected(false);
    JPanel contentPanel = new JPanel();
    add(contentPanel, BorderLayout.NORTH);
    contentPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 4;
    gbc.gridheight = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.insets = new Insets(0, 0, 15, 5);
    contentPanel.add(instruction, gbc);
    gbc.gridwidth = 1;
    gbc.gridy ++;
    gbc.insets = new Insets(0, 0, 5, 5);
    contentPanel.add(seclabel, gbc);

    gbc.gridy = 1;
    gbc.gridx++;
    gbc.weightx = 1.0f;
    gbc.insets = new Insets(0, 0, 5, 0);
    contentPanel.add(secCheckbox, gbc);

  }


  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    // enable back -  next - finish
    callbacks = tc.getWizardCallbacks();
    callbacks.wizardEnableButtons(true, secCheckbox.isSelected(), false);
  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {

    super.onExit(tc);
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent)
  {
    if (callbacks!=null)
    {
      callbacks.wizardEnableButtons(true, secCheckbox.isSelected(), false);      
    }
  }
}
