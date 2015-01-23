/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uipanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import oracle.adfdt.model.DataControlManager;
import oracle.adfdt.model.objects.DataControl;

import oracle.ateam.sample.mobile.dt.view.editor.AttributeEditor;
import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.view.uimodel.AttributeTableModel;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DCMethodParameter;
import oracle.ateam.sample.mobile.dt.controller.parser.DataControlDataObjectParser;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.view.uimodel.DataObjectTableModel;

import oracle.ateam.sample.mobile.dt.view.uimodel.MethodParamsTableModel;
import oracle.ateam.sample.mobile.dt.model.UIAttributeInfo;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;

import oracle.javatools.ui.table.GenericTable;

public class CRUDMethodParametersPanel
  extends DefaultTraversablePanel implements ActionListener, ListSelectionListener
{
  JScrollPane scrollPane =  new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  GenericTable table = null;
  private BusinessObjectGeneratorModel model;
  
  String dcInstruction = "Specify how the parameters of the CRUD methods must be populated";
  String restInstruction = "Specify how the parameters of the CRUD resources must be populated";
  JLabel instruction = new JLabel("Specify how the parameters of the CRUD methods must be populated");
  JLabel methodLabel = new JLabel("CRUD Method");
  JComboBox methodList = new JComboBox();
  JCheckBox sendSerializedDO = new JCheckBox("Send Serialized Data Object as Payload?");
  JPanel restSpecificPanel = new JPanel();
  JButton addButton = new JButton("Add");
  JButton removeButton = new JButton("Remove");
  transient Map<String, DCMethod> methodsMap = new HashMap<String, DCMethod>();
  
  public CRUDMethodParametersPanel()
  {
    super();
    methodList.addActionListener(this);
    setLayout(new GridBagLayout());
    add(buildHeaderPanel(), 
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, 
                           GridBagConstraints.NONE, 
                           new Insets(5, 0, 0, 0), 0, 0));
    GridBagConstraints gbcScrollPane = 
        new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, 
                               GridBagConstraints.LINE_START, 
                               GridBagConstraints.BOTH, 
                               new Insets(0, 0, 5, 0), 0, 0);
    add(scrollPane, gbcScrollPane);
  }
  
  private JPanel buildHeaderPanel()
  {
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 6;
    gbc.gridheight = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.NONE;

    gbc.insets = new Insets(0, 0, 5, 5);
    contentPanel.add(instruction, gbc);

    gbc.gridy++;
    gbc.gridwidth = 1;
    contentPanel.add(methodLabel, gbc);

    //    gbc.gridy++;
    //    add(_lbName, gbc);
    //    gbc.gridy++;
    //    add(_lbAuthor, gbc);

    gbc.gridx++;
    gbc.weightx = 1.0f;
    gbc.insets = new Insets(0, 0, 5, 5);
    methodList.addActionListener(this);
    contentPanel.add(methodList, gbc);
    gbc.gridx=0;
    gbc.gridy++;
    gbc.weightx = 0;
    restSpecificPanel.setLayout(new GridBagLayout());
    gbc.gridwidth = 3;
    contentPanel.add(restSpecificPanel,gbc);

    GridBagConstraints gbcRest = new GridBagConstraints();
    gbcRest.insets = new Insets(0, 0, 5, 5);
    gbcRest.gridx = 0;
    gbcRest.gridy = 0;
    gbcRest.gridwidth = 1;
    gbcRest.gridheight = 1;
    gbcRest.anchor = GridBagConstraints.WEST;
    gbcRest.fill = GridBagConstraints.NONE;

    restSpecificPanel.add(sendSerializedDO,gbcRest);
    sendSerializedDO.addActionListener(this);
    gbcRest.gridx++;
    addButton.addActionListener(this);
    restSpecificPanel.add(addButton,gbcRest);
    gbcRest.gridx++;
    removeButton.addActionListener(this);
    removeButton.setEnabled(false);
    restSpecificPanel.add(removeButton,gbcRest);
    return contentPanel;    
  }

  private void populateMethodList(BusinessObjectGeneratorModel model)
  {
    List<DataObjectInfo> dois = model.getSelectedDataObjects();
    List<String> methodNames = new ArrayList<String>();
    methodsMap.clear();
    for (DataObjectInfo doi: dois)
    {
      if (doi.getParent()==null)
      {
        addMethod(doi.getFindAllMethod(), methodsMap, methodNames);
        addMethod(doi.getFindMethod(), methodsMap, methodNames);
        for(DCMethod method : doi.getFindAllInParentMethods())
        {
          addMethod(method, methodsMap, methodNames);          
        }
        for(DCMethod method : doi.getGetAsParentMethods())
        {
          addMethod(method, methodsMap, methodNames);          
        }
        addMethod(doi.getGetCanonicalMethod(), methodsMap, methodNames);
        addMethod(doi.getCreateMethod(), methodsMap, methodNames);
        addMethod(doi.getUpdateMethod(), methodsMap, methodNames);
        addMethod(doi.getMergeMethod(), methodsMap, methodNames);
        addMethod(doi.getDeleteMethod(), methodsMap, methodNames);        
      }
    }
    methodList.setModel(new DefaultComboBoxModel(methodNames.toArray()));
    if (methodNames.size()==0)
    {
      addButton.setEnabled(false);
    }
  }

  private void addMethod(DCMethod dcMethod, Map<String, DCMethod> methodsMap, List<String> methodNames)
  {
    if (dcMethod==null)
    {
      return;
    }
    String methodName = null;
    if (dcMethod.getRequestType()!=null)
    {
      methodName = dcMethod.getRequestType()+" "+dcMethod.getName();
    }
    else
    {
      methodName = dcMethod.getName();        
    }
    if (dcMethod!=null && !methodsMap.containsKey(methodName))
    {
      methodsMap.put(methodName, dcMethod);
      methodNames.add(methodName);
    }
  }

  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    if (model.isRestfulWebService())
    {
      instruction.setText(restInstruction);
      tc.getWizardCallbacks().wizardUpdateTitle("CRUD Resource Parameters", Boolean.TRUE);
    }
    else
    {
      instruction.setText(dcInstruction);
      restSpecificPanel.setVisible(false);      
    }

    populateMethodList(model);
    //    if (model.getDataControlName() != null)
    //    {
    //      doilist.setSelectedItem(model.getDataControlName());
    //    }
    if (methodList.getItemCount() > 0)
    {
      methodList.setSelectedItem(methodList.getItemAt(0));
      DCMethod method = methodsMap.get(methodList.getSelectedItem());
      createParametersTable(method, model.isRestfulWebService());
    }
    // enable back -  next - finish
    tc.getWizardCallbacks().wizardEnableButtons(true, true, true);
  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {
    BusinessObjectGeneratorModel model =
        (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    super.onExit(tc);
  }

  public void createParametersTable(DCMethod method, boolean nameUpdateable) {
      table = new GenericTable(new MethodParamsTableModel(method.getParams(),nameUpdateable));
    table.getSelectionModel().addListSelectionListener(this);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setColumnSelectorAvailable(false);

    //To stop cell editing when user switches to another component without using tab/enter keys
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

//      TableCellRenderer checkBoxRenderer = new TableCellCheckBoxRenderer();
//      TableCellCheckBoxEditor checkBoxEditor = new TableCellCheckBoxEditor();

      TableColumn tc0 = table.getColumnModel().getColumn(0);
//    tc0.setMinWidth(30);
//    tc0.setMaxWidth(30);
    TableColumn tc1 = table.getColumnModel().getColumn(1);
    JComboBox valueProvider = new JComboBox();
    valueProvider.addItem("");
    for (String value: DCMethodParameter.getValueProviderAllowableValues())
    {
      valueProvider.addItem(value);      
    }
    tc1.setCellEditor(new DefaultCellEditor(valueProvider));
//    tc1.setMinWidth(120);
    TableColumn tc2 = table.getColumnModel().getColumn(2);
    tc2.setCellEditor(new AttributeEditor(method.getParameterValueProviderDataObject()));
//    tc2.setMinWidth(200);
    TableColumn tc3 = table.getColumnModel().getColumn(3);
//    tc3.setMinWidth(200);
    scrollPane.getViewport().setView(table);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource()== methodList)
    {
      String methodName = (String) methodList.getSelectedItem();
      DCMethod method = methodsMap.get(methodName);
      boolean enableSerializeCheckbox = !"GET".equals(method.getRequestType());
      sendSerializedDO.setEnabled(enableSerializeCheckbox);
      sendSerializedDO.setSelected(method.isSendSerializedDataObjectAsPayload());
      createParametersTable(method, model.isRestfulWebService());      
    }
    else if (e.getSource()== addButton)
    {
      String methodName = (String) methodList.getSelectedItem();
      DCMethod method = methodsMap.get(methodName);
      method.addParam(new DCMethodParameter());
      createParametersTable(method, true);
    }
    else if (e.getSource()== sendSerializedDO)
    {
      String methodName = (String) methodList.getSelectedItem();
      DCMethod method = methodsMap.get(methodName);
      method.setSendSerializedDataObjectAsPayload(sendSerializedDO.isSelected());
    }
    else if (e.getSource()== removeButton)
    {
      int paramIndex = table.getSelectedRow();
      if (paramIndex>-1)
      {
        String methodName = (String) methodList.getSelectedItem();
        DCMethod method = methodsMap.get(methodName);
        method.getParams().remove(paramIndex);
        createParametersTable(method, true);        
      }
    }
    removeButton.setEnabled(table.getSelectedRow()>-1);
  }

  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    boolean removeEnable = table.getSelectedRow() > -1;
    removeButton.setEnabled(removeEnable);
  }
}
