/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
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

import oracle.javatools.parser.java.v2.model.JavaClass;

import oracle.jdeveloper.java.JavaManager;

public class SelectUIDataControlPanel
  extends DefaultTraversablePanel
{

  MultiLineText instruction = new MultiLineText("Select the data control for which a feature will be generated. You can only select a bean data control with the bean class extending from oracle.ateam.sample.mobile.persistence.service.EntityCRUDService");
  JLabel dclabel = new JLabel("Data Control");
  JComboBox dclist = new JComboBox();
  JLabel seclabel = new JLabel("Enable Feature Security?");
  JCheckBox secCheckbox = new JCheckBox();
  transient Map<String, DataControl> dataControlMap = new HashMap<String, DataControl>();

  public SelectUIDataControlPanel()
  {
    super();
    setLayout(new BorderLayout(0, 15));
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
    contentPanel.add(dclabel, gbc);
    gbc.gridy++;
    contentPanel.add(seclabel, gbc);
    //    gbc.gridy++;
    //    add(_lbAuthor, gbc);

    gbc.gridy = 1;
    gbc.gridx++;
    gbc.weightx = 1.0f;
    gbc.insets = new Insets(0, 0, 5, 0);

    contentPanel.add(dclist, gbc);
    gbc.gridy++;
    contentPanel.add(secCheckbox, gbc);
//    gbc.gridy++;
    //    add(_tfAuthor, gbc);

    List<DataControl> dataControls = DataControlManager.getInstance().getAllDataControls();
    for (DataControl dc: dataControls)
    {
      if (dc.getRealDataControl() instanceof JUDTAdapterDataControl)
      {
        JUDTAdapterDataControl adc = (JUDTAdapterDataControl) dc.getRealDataControl();
        String implClass = adc.getDef().getClass().getName();
        if (implClass.endsWith("BeanDCDefinition") || implClass.endsWith("StatefulIteratorBeanDcDefinition"))
        {
          // check whether beanClass extends from EntityCrudService
          StructureDefinition beanDef = adc.getDataControlDefinition().getStructure();
          String beanClass = beanDef.getFullName();
          JavaClass serviceObject = getClass(Ide.getActiveProject(),beanClass);
          if (serviceObject!=null && serviceObject.getSuperclass()!=null 
            && serviceObject.getSuperclass().getName().endsWith("EntityCRUDService"))
          {
            dataControlMap.put(dc.getId(), dc);
            dclist.addItem(dc.getId());            
          }
        }
      }
    }
  }

  private JavaClass getClass(Project project, String className)
  {
    JavaManager jm = JavaManager.getJavaManager(project);
    JavaClass clazz = jm.getClass(className);
    return clazz;
  }

  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    UIGeneratorModel model = (UIGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    if (model.getDataControlName() != null)
    {
      dclist.setSelectedItem(model.getDataControlName());
    }
    else if (dclist.getItemCount() > 0)
    {
      dclist.setSelectedItem(dclist.getItemAt(0));
    }
    secCheckbox.setSelected(model.isEnableSecurity());
    // enable back -  next - finish
    tc.getWizardCallbacks().wizardEnableButtons(true, dclist.getSelectedItem()!=null, false);
  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {
    UIGeneratorModel model = (UIGeneratorModel) tc.get(UIGeneratorWizard.MODEL_KEY);
    String newName = (String) dclist.getSelectedItem();
    if (newName == null)
    {
      throw new TraversalException("You need to select a data control");
    }
    else if (!newName.equals(model.getDataControlName()))
    {
      model.setDataControlName(newName);
      model.setDataControl(dataControlMap.get(model.getDataControlName()));
      DataControlUIDataObjectParser visitor = new DataControlUIDataObjectParser(model.getDataControl());
      visitor.discoverBeanCollections();
      model.setDataControlVisitor(visitor);  
      model.setDataObjectInfos(visitor.getAccessorBeans());
      model.setCurrentDataObject(null);
    }
    model.setEnableSecurity(secCheckbox.isSelected());
    super.onExit(tc);
  }
}
