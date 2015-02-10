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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.swing.ButtonGroup;
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
import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.view.uimodel.DataObjectTableModel;

import oracle.ateam.sample.mobile.dt.view.uimodel.MethodParamsTableModel;
import oracle.ateam.sample.mobile.dt.model.UIAttributeInfo;
import oracle.ateam.sample.mobile.dt.util.StringUtils;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.bali.ewt.dialog.JEWTDialog;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;

import oracle.javatools.icons.OracleIcons;
import oracle.javatools.ui.table.GenericTable;

public class CRUDMethodParametersPanel
  extends DefaultTraversablePanel implements ActionListener, ListSelectionListener, KeyListener
{
  JScrollPane scrollPane =  new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  GenericTable table = null;
  private BusinessObjectGeneratorModel model;
  
  String dcInstruction = "Specify how the parameters of the CRUD methods must be populated";
  String restInstruction = "Specify payload format and how resource parameters must be populated. Add new custom resources if needed.";
  MultiLineText instruction = new MultiLineText("Specify how the parameters of the CRUD methods must be populated");
  JLabel methodLabel = new JLabel("CRUD Method");
  JComboBox methodList = new JComboBox();
  JCheckBox sendSerializedDO = new JCheckBox("Send Serialized Data Object as Payload");

  private JLabel payloadListElementLabel = new JLabel("Payload List Element");
  private JTextField payloadListElementField = new JTextField();
  private JLabel payloadRowElementLabel = new JLabel("Payload Row Element");
  private JTextField payloadRowElementField = new JTextField();


  JPanel restSpecificPanel = new JPanel();
  JButton addButton = new JButton("Add");
  JButton removeButton = new JButton("Remove");
  transient Map<String, DCMethod> methodsMap = new HashMap<String, DCMethod>();
  JButton newResourceButton = new JButton(OracleIcons.getIcon(OracleIcons.ADD));
  
  // new resource dialog components
  transient Map<String, DataObjectInfo> dataObjectMap = new HashMap<String, DataObjectInfo>();
  JEWTDialog newResourceDialog = null;
  private JLabel dataObjectLabel = new JLabel("Data Object");
  private JComboBox dataObjectField = new JComboBox();
  private JLabel resourceNameLabel = new JLabel("Name");
  private JTextField resourceNameField = new JTextField();
  private JLabel resourceUriLabel = new JLabel("Resource");
  private JTextField resourceUriField = new JTextField();
  private JComboBox resourceRequestType = new JComboBox();
  
  public CRUDMethodParametersPanel()
  {
    super();
    methodList.addActionListener(this);
    dataObjectField.addActionListener(this);
    resourceNameField.addKeyListener(this);
    resourceNameLabel.setToolTipText("Name of the method that will be generated to invoke the custom resource.");
    resourceNameField.setToolTipText("Name of the method that will be generated to invoke the custom resource.");
    resourceUriField.addKeyListener(this);
    resourceUriLabel.setToolTipText("The REST URI of the resource. You can specify a sample query string to define query parameters.");
    resourceUriField.setToolTipText("The REST URI of the resource. You can specify a sample query string to define query parameters.");
    setLayout(new GridBagLayout());
    this.add(instruction,
             new GridBagConstraints(0, 0, 6, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 0, 0), 0, 0));

    add(buildHeaderPanel(), 
                new GridBagConstraints(0, 1, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, 
                           GridBagConstraints.NONE, 
                           new Insets(5, 0, 0, 0), 0, 0));
    GridBagConstraints gbcScrollPane = 
        new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0, 
                               GridBagConstraints.LINE_START, 
                               GridBagConstraints.BOTH, 
                               new Insets(0, 0, 0, 0), 0, 0);
    add(scrollPane, gbcScrollPane);
  }
  
  private JPanel buildHeaderPanel()
  {
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    contentPanel.add(methodLabel,
                  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                         new Insets(0, 0, 0, 0), 0, 0));
    contentPanel.add(methodList,
                  new GridBagConstraints(1, 0, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                         new Insets(0, 5, 5, 0), 0, 0));
    contentPanel.add(newResourceButton,
                  new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                         new Insets(0, 5, 5, 0), 0, 0));
    methodList.addActionListener(this);
    newResourceButton.setToolTipText("New Custom Resource");      


    gbc.gridwidth = 6;
    gbc.gridheight = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridx=0;
    gbc.gridy=1;
    gbc.weightx = 0;
    gbc.gridwidth = 3;

    restSpecificPanel.setLayout(new GridBagLayout());
    contentPanel.add(restSpecificPanel,
        new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                           new Insets(0, 0, 0, 0), 0, 0));

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

  private void buildNewResourceDialog(final BusinessObjectGeneratorModel model)
  {
    JPanel dialogPanel = new JPanel();
    newResourceDialog.setContent(dialogPanel);
    int width = 500;
    newResourceDialog.setPreferredSize(width, 200);
    newResourceDialog.setResizable(true);
    newResourceDialog.setModal(true);
    newResourceDialog.setButtonMask((JEWTDialog.BUTTON_OK | JEWTDialog.BUTTON_CANCEL));
    newResourceDialog.setDefaultButton(JEWTDialog.BUTTON_OK);
    newResourceButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        newResourceDialog.setTitle("New Custom Resource");
        resetResourceDialogFields();
        enableOrDisableResourceDialogOKButton();
        boolean OK = newResourceDialog.runDialog();
        if (OK)
        {
          DCMethod method = new DCMethod(resourceNameField.getText(),model.getConnectionName(),resourceUriField.getText()
                                         ,(String) resourceRequestType.getSelectedItem());
          DataObjectInfo dataObject = dataObjectMap.get(dataObjectField.getSelectedItem());
          dataObject.addCustomMethod(method);
          populateMethodList(model);
        }
      }

    });

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
    dialogPanel.add(dataObjectLabel, gbc);
    gbc.gridy++;
    dialogPanel.add(resourceNameLabel, gbc);
    gbc.gridy++;
    dialogPanel.add(resourceUriLabel, gbc);


    gbc.gridy = 0;
    gbc.gridx = 1;
    gbc.weightx = 1.0f;
    gbc.insets = new Insets(0, 0, 5, 10);

    dialogPanel.add(dataObjectField, gbc);
    gbc.gridy++;
    dialogPanel.add(resourceNameField, gbc);
    gbc.gridy++;
    dialogPanel.add(resourceUriField, gbc);

      List<String> types = new ArrayList<String>();
      types.add("GET");
      types.add("POST");
      types.add("PUT");
      types.add("PATCH");
      types.add("DELETE");
      gbc.gridx++;
      gbc.weightx = 0;
      resourceRequestType.setModel(new DefaultComboBoxModel(types.toArray()));
      resourceRequestType.setSelectedItem("GET");
      dialogPanel.add(resourceRequestType, gbc);            

  }

  private void populateDataObjectList()
  {
    List<DataObjectInfo> dois = model.getSelectedDataObjects();
    List<String> dataObjectNames = new ArrayList<String>();
    for (DataObjectInfo doi: dois)
    {
      dataObjectMap.put(doi.getClassName(), doi);
      dataObjectNames.add(doi.getClassName());
    }
    dataObjectField.setModel(new DefaultComboBoxModel(dataObjectNames.toArray()));
  }

  private void populateMethodList(BusinessObjectGeneratorModel model)
  {
    List<DataObjectInfo> dois = model.getSelectedDataObjects();
    List<String> methodNames = new ArrayList<String>();
    methodsMap.clear();
    for (DataObjectInfo doi: dois)
    {
      if (doi.isGenerateServiceClass())
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
        for(DCMethod method : doi.getCustomMethods())
        {
          addMethod(method, methodsMap, methodNames);          
        }
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
      methodLabel.setText("Resource");
      tc.getWizardCallbacks().wizardUpdateTitle("Resource Details", Boolean.TRUE);
    }
    else
    {
      instruction.setText(dcInstruction);
      restSpecificPanel.setVisible(false);      
      newResourceButton.setVisible(false);
    }

    if (newResourceDialog==null)
    {
      newResourceDialog = JEWTDialog.createDialog(this, "New Custom Resource", JEWTDialog.BUTTON_DEFAULT);
      buildNewResourceDialog(model);
    }
    populateDataObjectList();
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
    enableOrDisableResourceDialogOKButton();
  }

  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    boolean removeEnable = table.getSelectedRow() > -1;
    removeButton.setEnabled(removeEnable);
  }

  private void enableOrDisableResourceDialogOKButton()
  {
    boolean ok = StringUtils.isNotEmpty(resourceNameField.getText())
               && StringUtils.isNotEmpty(resourceUriField.getText());
    newResourceDialog.setOKButtonEnabled(ok);
  }

  private void resetResourceDialogFields()
  {
    dataObjectField.setSelectedIndex(-1);
    resourceNameField.setText(null);
    resourceUriField.setText(null);
  }

  @Override
  public void keyTyped(KeyEvent e)
  {
    enableOrDisableResourceDialogOKButton();
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
  }

  @Override
  public void keyReleased(KeyEvent e)
  {
  }
}
