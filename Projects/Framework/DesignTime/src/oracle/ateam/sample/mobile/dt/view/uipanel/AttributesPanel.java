/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uipanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import oracle.adfdt.common.ui.editor.ClassPickerCellEditor;

import oracle.adfdt.common.ui.editor.ClassPickerPanel;

import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.view.uimodel.AttributeTableModel;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.view.editor.ClassPickerTextButtonCellEditor;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.bali.ewt.dialog.JEWTDialog;

import oracle.ide.Context;
import oracle.ide.Ide;
import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;

import oracle.javatools.ui.table.GenericTable;


public class AttributesPanel
  extends DefaultTraversablePanel implements ActionListener, ListSelectionListener
{
  private JScrollPane scrollPane =  new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  private GenericTable table = null;
  private BusinessObjectGeneratorModel model;
  
  MultiLineText instruction =
    new MultiLineText("Specify the persist, key and required attributes and modify name, Java type and column type as desired");
  private JLabel doiLabel = new JLabel("Data object");
  private JComboBox doilist = new JComboBox();
  private JButton addButton = new JButton("Add");
  private JButton removeButton = new JButton("Remove");
  private JLabel newAttrNameLabel = new JLabel("Name");
  private JTextField newAttrNameField = new JTextField();
  private JLabel newPayloadAttrNameLabel = new JLabel("Name in Payload");
  private JTextField newPayloadAttrNameField = new JTextField();
  transient Map<String, DataObjectInfo> dataObjectMap = new HashMap<String, DataObjectInfo>();
  private JEWTDialog newAttributeDialog = JEWTDialog.createDialog(this, "Add Attribute", JEWTDialog.BUTTON_DEFAULT);
  
  
  public AttributesPanel()
  {
    super();
    addButton.setToolTipText("Add a new attribute");
    removeButton.setToolTipText("Remove selected attribute");
    doilist.addActionListener(this);
    setLayout(new GridBagLayout());
    this.add(instruction,
             new GridBagConstraints(0, 0, 6, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(doiLabel,
        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 0, 0, 0), 0, 0));
    this.add(doilist,
        new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 5, 0, 0), 0, 0));
    this.add(addButton,
        new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 5, 0, 0), 0, 0));
    this.add(removeButton,
        new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 5, 0, 0), 0, 0));

    doilist.addActionListener(this);
    addButton.addActionListener(this);
    removeButton.addActionListener(this);
    removeButton.setEnabled(false);
    GridBagConstraints gbcScrollPane =
      new GridBagConstraints(0, 2, 6, 1, 1.0, 1.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH,
                             new Insets(5, 0, 5, 0), 0, 0);
    add(scrollPane, gbcScrollPane);
    buildNewAttributeDialog();
  }
  
  private void populateDataObjectList(BusinessObjectGeneratorModel model)
  {
    List<DataObjectInfo> dois = model.getSelectedDataObjects();
    List<String> dataObjectNames = new ArrayList<String>();
    dataObjectMap.clear();
    for (DataObjectInfo doi: dois)
    {
      dataObjectMap.put(doi.getClassName(), doi);
      dataObjectNames.add(doi.getClassName());
    }
    doilist.setModel(new DefaultComboBoxModel(dataObjectNames.toArray()));
  }

  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    populateDataObjectList(model);
    //    if (model.getDataControlName() != null)
    //    {
    //      doilist.setSelectedItem(model.getDataControlName());
    //    }
    if (doilist.getItemCount() > 0)
    {
      doilist.setSelectedItem(doilist.getItemAt(0));
      DataObjectInfo doi = dataObjectMap.get(doilist.getSelectedItem());
      createAttributesTable(doi);
    }
    // enable back -  next - finish
    tc.getWizardCallbacks().wizardEnableButtons(true, true, false);
  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {
    BusinessObjectGeneratorModel model =
        (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    if (tc.getDirection()==tc.FORWARD_TRAVERSAL)
    {
      for (DataObjectInfo doi : dataObjectMap.values())
      {
        if (doi.getKeyAttributes().size()==0)
        {
          throw new TraversalException("All data objects must have a primary key.");
        }
      }      
    }
    super.onExit(tc);
  }

  public void createAttributesTable(DataObjectInfo doi) {
      table = new GenericTable(new AttributeTableModel(doi));
      table.setColumnSelectorAvailable(false);
    table.getSelectionModel().addListSelectionListener(this);
    //To stop cell editing when user switches to another component without using tab/enter keys
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

//      TableCellRenderer checkBoxRenderer = new TableCellCheckBoxRenderer();
//      TableCellCheckBoxEditor checkBoxEditor = new TableCellCheckBoxEditor();

    TableColumn tc0 = table.getColumnModel().getColumn(0);
    tc0.setMinWidth(55);
    tc0.setMaxWidth(55);
    TableColumn tc1 = table.getColumnModel().getColumn(1);
    tc1.setMinWidth(40);
    tc1.setMaxWidth(40);
    TableColumn tc2 = table.getColumnModel().getColumn(2);
    tc2.setMinWidth(70);
    tc2.setMaxWidth(70);
    scrollPane.getViewport().setView(table);
    TableColumn tc4 = table.getColumnModel().getColumn(4);
    ClassPickerTextButtonCellEditor editor = new ClassPickerTextButtonCellEditor(new Context(null, Ide.getActiveProject()),this );
    tc4.setCellEditor(editor);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource()==doilist)
    {
      String dataObjectName = (String) doilist.getSelectedItem();
      createAttributesTable(dataObjectMap.get(dataObjectName));
    }  
    else if (e.getSource()==addButton)
    {
      if (model.isWebServiceDataControl())
      {
        // with web service data control, we do not launch a dialog, because there is now
        // new payload attribute to specify. A new attribute will always be an attribute that only
        // lives on the mobile device
        String dataObjectName = (String) doilist.getSelectedItem();
        DataObjectInfo dataObject = dataObjectMap.get(dataObjectName);
        dataObject.addAttribute(new AttributeInfo("newAttr","java.lang.String") );
        createAttributesTable(dataObjectMap.get(dataObjectName));
      }
      else
      {
        // with  REST service the return payload or sample payload might not include all attributes
        // as we are inspecting only the first "row" returned, so here we luanch a dialog where the
        // user can specify an additional payload attribute name. All other properties can then be filled
        // in in the table
        newAttrNameField.setText("newAttr");
        newPayloadAttrNameField.setText(null);
        boolean OK = newAttributeDialog.runDialog();      
        if (OK)
        {
          String dataObjectName = (String) doilist.getSelectedItem();
          DataObjectInfo dataObject = dataObjectMap.get(dataObjectName);
          String attrName = newAttrNameField.getText();
          String payloadAttrName = newPayloadAttrNameField.getText();
          payloadAttrName = "".equals(payloadAttrName) ? null : payloadAttrName;
          AttributeInfo ai = new AttributeInfo(attrName,"java.lang.String");
          ai.setPayloadName(payloadAttrName);
          dataObject.addAttribute(ai);
          createAttributesTable(dataObjectMap.get(dataObjectName));
        }
      }
    }
    else if (e.getSource()==removeButton)
    {
      int[] indices = table.getSelectedRows();
      // remopve in reverse order because otherwise we throw away the wrong attrs after the first has been removed
      String dataObjectName = (String) doilist.getSelectedItem();
      DataObjectInfo dataObject = dataObjectMap.get(dataObjectName);
      for (int i = indices.length-1; i > -1; i--)
      {
        dataObject.removeAttribute(indices[i]);     
      }
      createAttributesTable(dataObject);
    }
    removeButton.setEnabled(table.getSelectedRow()>-1);
  }

  private void buildNewAttributeDialog()
  {
    JPanel dialogPanel = new JPanel();
    newAttributeDialog.setContent(dialogPanel);
    newAttributeDialog.setPreferredSize(350, 150);
    newAttributeDialog.setResizable(true);
    newAttributeDialog.setModal(true);
    newAttributeDialog.setButtonMask((JEWTDialog.BUTTON_OK | JEWTDialog.BUTTON_CANCEL));
    newAttributeDialog.setDefaultButton(JEWTDialog.BUTTON_OK);
    GridBagLayout containerLayout = new GridBagLayout();
    dialogPanel.setLayout(containerLayout);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.insets = new Insets(0, 10, 5, 5);
    dialogPanel.add(newAttrNameLabel, gbc);
    gbc.gridy++;
    dialogPanel.add(newPayloadAttrNameLabel, gbc);

    gbc.gridy = 0;
    gbc.gridx = 1;
    gbc.weightx = 1.0f;
    gbc.insets = new Insets(0, 0, 5, 10);
    dialogPanel.add(newAttrNameField, gbc);
    gbc.gridy++;
    dialogPanel.add(newPayloadAttrNameField, gbc);
  }

  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    boolean removeEnable = table.getSelectedRow() > -1;
    removeButton.setEnabled(removeEnable);
  }
}
