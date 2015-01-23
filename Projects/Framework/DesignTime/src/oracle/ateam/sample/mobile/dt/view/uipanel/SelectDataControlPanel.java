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

import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.swing.JComboBox;

import javax.swing.JLabel;

import javax.swing.JPanel;

import oracle.adfdt.model.DataControlManager;
import oracle.adfdt.model.datacontrols.JUDTAdapterDataControl;
import oracle.adfdt.model.objects.DataControl;

import oracle.ateam.sample.mobile.dt.controller.PersistenceMappingLoader;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;

public class SelectDataControlPanel
  extends DefaultTraversablePanel
{

  JLabel dclabel = new JLabel("Web Service Data Control");
  JComboBox dclist = new JComboBox();
  transient Map<String, DataControl> dataControlMap = new HashMap<String, DataControl>();

  public SelectDataControlPanel()
  {
    super();
    setLayout(new BorderLayout(0, 15));
    
    JPanel contentPanel = new JPanel();
    add(contentPanel, BorderLayout.NORTH);
    contentPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.insets = new Insets(0, 0, 5, 5);
    contentPanel.add(dclabel, gbc);
    //    gbc.gridy++;
    //    add(_lbName, gbc);
    //    gbc.gridy++;
    //    add(_lbAuthor, gbc);

    gbc.gridy = 0;
    gbc.gridx++;
    gbc.weightx = 1.0f;
    gbc.insets = new Insets(0, 0, 5, 0);

    contentPanel.add(dclist, gbc);
    gbc.gridy++;
    //    add(_tfName, gbc);
    //    gbc.gridy++;
    //    add(_tfAuthor, gbc);

    List<DataControl> dataControls = DataControlManager.getInstance().getAllDataControls();
    for (DataControl dc: dataControls)
    {
      if (dc.getRealDataControl() instanceof JUDTAdapterDataControl)
      {
        JUDTAdapterDataControl adc = (JUDTAdapterDataControl) dc.getRealDataControl();
        String implClass = adc.getDef().getClass().getName();
        if (implClass.endsWith("WSDefinition") || implClass.endsWith("RestURLDCDefinition")
            || implClass.endsWith("RestDCDefinition"))
        {
          // Only add web service data controls
          dataControlMap.put(dc.getId(), dc);
          dclist.addItem(dc.getId());
        }
      }
    }
  }

  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    BusinessObjectGeneratorModel model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    if (model.getDataControlName() != null)
    {
      dclist.setSelectedItem(model.getDataControlName());
    }
    else if (dclist.getItemCount() > 0)
    {
      dclist.setSelectedItem(dclist.getItemAt(0));
    }
    // enable back -  next - finish
    tc.getWizardCallbacks().wizardEnableButtons(true, dclist.getSelectedItem()!=null, false);
  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {
    BusinessObjectGeneratorModel model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    String newName = (String) dclist.getSelectedItem();
    if (newName == null)
    {
      throw new TraversalException("You need to select a data control");
    }
    else if (!newName.equals(model.getDataControlName()))
    {
      model.setDataControlName(newName);
      model.setDataControl(dataControlMap.get(model.getDataControlName()));
      model.setDataObjectInfos(null);
      model.setCurrentDataObject(null);
      model.setWebServiceDataControl(true);
    }
    super.onExit(tc);
  }
}
