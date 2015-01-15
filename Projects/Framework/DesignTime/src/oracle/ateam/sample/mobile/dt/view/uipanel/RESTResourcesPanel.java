/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import oracle.ateam.sample.mobile.dt.controller.PersistenceMappingLoader;
import oracle.ateam.sample.mobile.dt.controller.parser.RESTResourceDataObjectParser;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DCMethodParameter;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.HeaderParam;
import oracle.ateam.sample.mobile.dt.model.jaxb.MobileObjectPersistence;
import oracle.ateam.sample.mobile.dt.view.editor.PayloadButtonCellEditor;
import oracle.ateam.sample.mobile.dt.view.uimodel.HeaderParamTableModel;
import oracle.ateam.sample.mobile.dt.view.uimodel.RestResourcesTableModel;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.bali.ewt.dialog.JEWTDialog;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;

import oracle.javatools.ui.table.GenericTable;

public class RESTResourcesPanel
  extends DefaultTraversablePanel
  implements ActionListener, ListSelectionListener
{
  JScrollPane scrollPane =
    new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  MultiLineText instruction =
    new MultiLineText("Specify RESTful resources. The return payload will be parsed for candidate data objects. You can use both query parameters with sample values and path parameters. A path parameter name needs to be enclosed in curly brackets. A dialog will be presented to enter sample values for the path parameters. If you specify a sample payload, the REST resource return payload will not be used.");
  GenericTable table = null;
  JScrollPane headerScrollPane =
    new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  GenericTable headerTable = null;
  JButton addResourceButton = new JButton("Add");
  JButton removeResourceButton = new JButton("Remove");
  JButton setHeadersButton = new JButton("Set Headers");
  JButton addHeaderParamButton = new JButton("Add");
  JButton removeHeaderParamButton = new JButton("Remove");
  JCheckBox flattenNestedObjects = new JCheckBox("Flatten Nested Objects?");
  private BusinessObjectGeneratorModel model;
  JEWTDialog paramValuesDialog = null;
  JEWTDialog headersDialog = null;
  JEWTDialog payloadDialog = null;
  JTextArea payloadField = new JTextArea();
  private Map<String,String> pathParams = new HashMap<String,String>();

  public RESTResourcesPanel()
  {
    super();
    addResourceButton.addActionListener(this);
    removeResourceButton.addActionListener(this);
    removeResourceButton.setEnabled(false);
    addHeaderParamButton.addActionListener(this);
    removeHeaderParamButton.addActionListener(this);
    removeHeaderParamButton.setEnabled(false);
    setHeadersButton.addActionListener(this);
    flattenNestedObjects.setSelected(false);
    addResourceButton.setToolTipText("Add REST resource");
    removeResourceButton.setToolTipText("Remove selected REST resource");
    setHeadersButton.setToolTipText("Specify HTTP request headers");
    flattenNestedObjects.setToolTipText("Include attributes of nested JSON objects in parent data object?");
    setLayout(new GridBagLayout());
    //    add(new JLabel("Data Objects"),
    this.add(instruction,
             new GridBagConstraints(0, 0, 6, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 0, 0), 0, 0));
//    add(instruction,
//        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
//                               new Insets(5, 0, 0, 0), 0, 0));
    add(addResourceButton,
        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 0, 0, 0), 0, 0));
    add(removeResourceButton,
        new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 5, 0, 0), 0, 0));
    add(setHeadersButton,
        new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 5, 0, 0), 0, 0));
    add(flattenNestedObjects,
        new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 5, 0, 0), 0, 0));
    GridBagConstraints gbcScrollPane =
      new GridBagConstraints(0, 2, 6, 1, 1.0, 1.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH,
                             new Insets(5, 0, 5, 0), 0, 0);
    //    JPanel tablePanel = new JPanel();
    //    tablePanel.add(scrollPane);
    //    add(tablePanel, gbcScrollPane);
    add(scrollPane, gbcScrollPane);
    buildHeadersDialog();
    buildPayloadDialog();
  }

  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    createRestResourcesTable(model.getRestResources());
    createHeaderParamsTable(model.getHeaderParams());
    if (tc.getDirection() == tc.FORWARD_TRAVERSAL)
    {
      // clear uri params
      pathParams.clear();
    }
    // enable back -  next - finish
    tc.getWizardCallbacks().wizardEnableButtons(true, true, false);
  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {
    BusinessObjectGeneratorModel model =
      (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    if (tc.getDirection() == tc.FORWARD_TRAVERSAL)
    {
      fillPathParameterMap(model);
      if (pathParams.size()>0)
      {
        buildAndRunParamValuesDialog(model);
      }
      discoverDataObjects(model);
    }
    super.onExit(tc);
  }

  private void fillPathParameterMap(BusinessObjectGeneratorModel model)
  {
    List<DCMethod> resources = model.getRestResources();
    // first clear old params that might not be needed anymore
    pathParams.clear();
    for (DCMethod resource: resources)
    {
      for (DCMethodParameter param : resource.getParams())
      {
        if (param.isPathParam() && !pathParams.containsKey(param.getName()))
        {
          pathParams.put(param.getName(), "");
        }
      }
    }    
  }

  private void buildAndRunParamValuesDialog(final BusinessObjectGeneratorModel model)
  {
    paramValuesDialog = JEWTDialog.createDialog(this, "Enter URI Parameter Values", JEWTDialog.BUTTON_DEFAULT);
    JPanel paramValuesPanel = new JPanel();
    paramValuesDialog.setContent(paramValuesPanel);
    int height = 100 +( pathParams.entrySet().size() * 30);
    paramValuesDialog.setPreferredSize(300, height);
    paramValuesDialog.setResizable(true);
    paramValuesDialog.setModal(true);
    paramValuesDialog.setButtonMask((JEWTDialog.BUTTON_OK | JEWTDialog.BUTTON_CANCEL));
    paramValuesDialog.setDefaultButton(JEWTDialog.BUTTON_OK);
    GridBagLayout containerLayout = new GridBagLayout();
    paramValuesPanel.setLayout(containerLayout);
    Map<String,JTextField> paramFields = new HashMap<String,JTextField>();

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.insets = new Insets(0, 10, 5, 5);
    Iterator<String> paramNames = pathParams.keySet().iterator();
    while (paramNames.hasNext())
    {
      gbc.weightx = 0;
      String paramName = paramNames.next();
      paramValuesPanel.add(new JLabel(paramName), gbc);
      gbc.gridx++;
      gbc.weightx = 1.0f;
      JTextField field = new JTextField();
      field.setText(pathParams.get(paramName));
      paramFields.put(paramName, field);
      paramValuesPanel.add(field, gbc);
      gbc.gridx--;
      gbc.gridy++;
    }
    boolean OK = paramValuesDialog.runDialog();
    if (OK)
    {
      paramNames = pathParams.keySet().iterator();
      while (paramNames.hasNext())
      {
        String paramName = paramNames.next();
        pathParams.put(paramName, paramFields.get(paramName).getText());
      }  
    }  
  }

  private void buildHeadersDialog()
  {
    headersDialog = JEWTDialog.createDialog(this, "Set Request Header Parameters", JEWTDialog.BUTTON_DEFAULT);
    JPanel headersPanel = new JPanel();
    headersDialog.setContent(headersPanel);
    headersDialog.setPreferredSize(500, 300);
    headersDialog.setResizable(true);
    headersDialog.setModal(true);
    headersDialog.setButtonMask((JEWTDialog.BUTTON_OK | JEWTDialog.BUTTON_CANCEL));
    headersDialog.setDefaultButton(JEWTDialog.BUTTON_OK);
    GridBagLayout containerLayout = new GridBagLayout();
    headersPanel.setLayout(containerLayout);
    headersPanel.add(addHeaderParamButton,
        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 0, 0, 0), 0, 0));
    headersPanel.add(removeHeaderParamButton,
        new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 5, 0, 0), 0, 0));
    GridBagConstraints gbcScrollPane =
      new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH,
                             new Insets(0, 0, 5, 0), 0, 0);
    headersPanel.add(headerScrollPane, gbcScrollPane);    
  }

  private void buildPayloadDialog()
  {
    payloadDialog = JEWTDialog.createDialog(this, "Sample Return Payload", JEWTDialog.BUTTON_DEFAULT);
    JPanel headersPanel = new JPanel();
    payloadDialog.setContent(headersPanel);
    payloadDialog.setPreferredSize(500, 450);
    payloadDialog.setResizable(true);
    payloadDialog.setModal(true);
    payloadDialog.setButtonMask((JEWTDialog.BUTTON_OK | JEWTDialog.BUTTON_CANCEL));
    payloadDialog.setDefaultButton(JEWTDialog.BUTTON_OK);
    GridBagLayout containerLayout = new GridBagLayout();
    headersPanel.setLayout(containerLayout);
    payloadField.setAutoscrolls(true);
    payloadField.setLineWrap(true);
    JScrollPane sp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    headersPanel.add(sp, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.LINE_START
                    , GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));    
    sp.setViewportView(payloadField);
  }

  private void discoverDataObjects(BusinessObjectGeneratorModel model)
    throws TraversalException
  {
    List<DCMethod> resources = model.getRestResources();
    String connectionUri = model.getConnectionUri();
    String connectionName = model.getConnectionName();
    List<HeaderParam> headerParams = model.getHeaderParams();
    RESTResourceDataObjectParser dataObjectParser =
      new RESTResourceDataObjectParser(resources, connectionName, connectionUri, headerParams, pathParams,flattenNestedObjects.isSelected());
    model.setDataObjectInfos(dataObjectParser.run());
    PersistenceMappingLoader loader = new PersistenceMappingLoader();
    MobileObjectPersistence jaxbModel = loader.loadJaxbModel();
    model.setExistingPersistenceMappingModel(jaxbModel);
    if (jaxbModel!=null)
    {
      Collection<DataObjectInfo> existingDataObjects =  loader.run(jaxbModel);
      model.getDataObjectInfos().addAll(existingDataObjects);      
    }
    model.setRestfulWebService(true);
  }

  public void createRestResourcesTable(List<DCMethod> resources)
  {
    table = new GenericTable(new RestResourcesTableModel(resources));
    table.getSelectionModel().addListSelectionListener(this);
    table.setColumnSelectorAvailable(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
   //To stop cell editing when user switches to another component without using tab/enter keys
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    scrollPane.getViewport().setView(table);
    TableColumn tc1 = table.getColumnModel().getColumn(1);
    JComboBox requestType = new JComboBox();
    requestType.addItem("GET");
    requestType.addItem("POST");
    requestType.addItem("PUT");
    requestType.addItem("PATCH");
    requestType.addItem("DELETE");
    tc1.setCellEditor(new DefaultCellEditor(requestType));
    tc1.setMaxWidth(140);
    tc1.setWidth(140);
    TableColumn tc2 = table.getColumnModel().getColumn(2);
    tc2.setCellEditor(new PayloadButtonCellEditor(resources,payloadDialog, payloadField));
    tc2.setMaxWidth(60);
    tc2.setWidth(60);
  }

  public void createHeaderParamsTable(List<HeaderParam> params)
  {
    headerTable = new GenericTable(new HeaderParamTableModel(params));
    headerTable.getSelectionModel().addListSelectionListener(this);
    headerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    headerTable.setColumnSelectorAvailable(false);
    //To stop cell editing when user switches to another component without using tab/enter keys
    headerTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    headerScrollPane.getViewport().setView(headerTable);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource()==addResourceButton)
    {
      DCMethod method = new DCMethod(model.getConnectionName());
      method.setRequestType("GET");
      model.getRestResources().add(method);
      createRestResourcesTable(model.getRestResources());      
    }
    else if (e.getSource()==removeResourceButton)
    {
      int index = table.getSelectedRow();
      if (index>-1)
      {
        model.getRestResources().remove(index);        
      }
      createRestResourcesTable(model.getRestResources());      
    }
    else if (e.getSource()==addHeaderParamButton)
    {
      model.getHeaderParams().add(new HeaderParam());
      createHeaderParamsTable(model.getHeaderParams());      
    }
    else if (e.getSource()==removeHeaderParamButton)
    {
      int index = headerTable.getSelectedRow();
      if (index>-1)
      {
        model.getHeaderParams().remove(index);
        createHeaderParamsTable(model.getHeaderParams());              
      }
    }
    else if (e.getSource()==setHeadersButton)
    {
      boolean OK = headersDialog.runDialog();      
    }
    removeResourceButton.setEnabled(table.getSelectedRow()>-1);
    removeHeaderParamButton.setEnabled(headerTable.getSelectedRow()>-1);
  }

  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    if (e.getSource()==table.getSelectionModel())
    {
      boolean removeEnable = table.getSelectedRow() > -1;
      removeResourceButton.setEnabled(removeEnable);      
    }
    else if (e.getSource()==headerTable.getSelectionModel())
    {
      boolean removeEnable = headerTable.getSelectedRow() > -1;
      removeHeaderParamButton.setEnabled(removeEnable);      
    }
  }
}
