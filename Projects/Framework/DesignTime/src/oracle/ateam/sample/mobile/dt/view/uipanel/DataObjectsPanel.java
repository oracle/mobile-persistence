/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uipanel;

import com.sun.org.apache.xpath.internal.operations.String;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import oracle.ateam.sample.mobile.dt.controller.PersistenceMappingLoader;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.controller.parser.DataControlDataObjectParser;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.jaxb.MobileObjectPersistence;
import oracle.ateam.sample.mobile.dt.view.uimodel.DataObjectTableModel;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;

import oracle.javatools.ui.table.GenericTable;

import oracle.jbo.common.JboNameUtil;

import org.w3c.dom.Node;


public class DataObjectsPanel
  extends DefaultTraversablePanel
  implements ActionListener
{
  JScrollPane scrollPane =
    new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  MultiLineText instruction = new MultiLineText("Select data objects for generation and set class names. Data objects with the same class name will be merged into one data object. Check the 'Persist' checkbox to enable persistence of the data object to the on-device SQLite database for data access in offline mode.");
  GenericTable table = null;
  JCheckBox showCollectionAccessorsOnly = new JCheckBox("Show collection accessors only?");
  private JButton addButton = new JButton("Add");
  private DataControlDataObjectParser dataControlVisitor;
  private BusinessObjectGeneratorModel model;


  public DataObjectsPanel()
  {
    super();
    showCollectionAccessorsOnly.addActionListener(this);
    addButton.addActionListener(this);
    showCollectionAccessorsOnly.setSelected(true);
    addButton.setToolTipText("Add new data object");
    setLayout(new GridBagLayout());
    //    add(new JLabel("Data Objects"),
    this.add(instruction,
             new GridBagConstraints(0, 0, 6, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 0, 0), 0, 0));
    add(showCollectionAccessorsOnly,
        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 5, 5, 5), 0, 0));
    add(addButton,
        new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 5, 5, 5), 0, 0));
    GridBagConstraints gbcScrollPane =
      new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH,
                             new Insets(0, 0, 5, 0), 0, 0);
    //    JPanel tablePanel = new JPanel();
    //    tablePanel.add(scrollPane);
    //    add(tablePanel, gbcScrollPane);
    add(scrollPane, gbcScrollPane);
  }

  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    if (model.isWebServiceDataControl() && model.getDataObjectInfos() == null)
    {
      dataControlVisitor = new DataControlDataObjectParser(model.getDataControl());
      model.setDataControlVisitor(dataControlVisitor);
      dataControlVisitor.discoverBeanCollections();
      setUpDataObjects();
    }
    if (model.isRestfulWebService())
    {
      showCollectionAccessorsOnly.setVisible(false);
    }
    createDataObjectsTable(model.getDataObjectInfos());
    // enable back -  next - finish
    tc.getWizardCallbacks().wizardEnableButtons(true, true, false);
  }

  private void setUpDataObjects()
  {
    List<DataObjectInfo> dataObjects =
      showCollectionAccessorsOnly.isSelected()? dataControlVisitor.getAllCollectionAccessorBeans():
      dataControlVisitor.getAllAccessorBeans();
    model.setDataObjectInfos(dataObjects);

    PersistenceMappingLoader loader = new PersistenceMappingLoader();
    MobileObjectPersistence jaxbModel = loader.loadJaxbModel();
    model.setExistingPersistenceMappingModel(jaxbModel);
    if (jaxbModel!=null)
    {
      Collection<DataObjectInfo> existingDataObjects =  loader.run(jaxbModel);
      model.getDataObjectInfos().addAll(existingDataObjects);      
    }

  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {
    if (tc.getDirection()==tc.FORWARD_TRAVERSAL && model.getSelectedDataObjects().size()==0)
    {
      throw new TraversalException("You need to select at least one data object for generation");
    }
    BusinessObjectGeneratorModel model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    if (model.isRestfulWebService())
    {
      // we need to set the payload element name on the findAllMethod of selected root data objects
      // we can't do that while discovering data objects, because complex web services like AuraPlayer might have sibling
      // data objects (AuraPlayer has the messages element) where we really need only one
      for(DataObjectInfo doi : model.getSelectedDataObjects())
      {
        // check class name is valid Java name
        if (!JboNameUtil.isNameValid(doi.getClassName()))
        {
          throw new TraversalException(doi.getClassName()+ " is not a valid Java class name.");
        }
        if (doi.getParent()==null && doi.getFindAllMethod()!=null)
        {
          // We ONLY do this for new methods, we should preserve the values manually set in persistence-mapping
          if (!doi.getFindAllMethod().isExisting() && !doi.getFindAllMethod().isRamlCreated())
          {
            doi.getFindAllMethod().setPayloadElementName(doi.getPayloadListElementName());            
            doi.getFindAllMethod().setPayloadRowElementName(doi.getPayloadRowElementName());            
          } 
        }
        else if (doi.getFindAllMethod()!=null && !doi.getFindAllMethod().isExisting() && !doi.getFindAllMethod().isRamlCreated())
        {
          // clear find-all method that might be set for child data objects UNLESS it is an "existing" method
          doi.setFindAllMethod(null);            
        }
      }
    }
    super.onExit(tc);
  }

  public void createDataObjectsTable(List<DataObjectInfo> dataObjects)
  {
    table = new GenericTable(new DataObjectTableModel(dataObjects));
    table.setColumnSelectorAvailable(false);

    //To stop cell editing when user switches to another component without using tab/enter keys
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    //      TableCellRenderer checkBoxRenderer = new TableCellCheckBoxRenderer();
    //      TableCellCheckBoxEditor checkBoxEditor = new TableCellCheckBoxEditor();
    TableColumn tc0 = table.getColumnModel().getColumn(0);
    tc0.setMinWidth(55);
    tc0.setMaxWidth(55);
    TableColumn tc1 = table.getColumnModel().getColumn(1);
    tc1.setMinWidth(55);
    tc1.setMaxWidth(55);
    scrollPane.getViewport().setView(table);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource()==showCollectionAccessorsOnly)
    {
      setUpDataObjects();
      createDataObjectsTable(model.getDataObjectInfos());      
    }
    else if (e.getSource()==addButton)
    {
      DataObjectInfo doi = new DataObjectInfo("NewDataObject","Unknown");
      model.getDataObjectInfos().add(doi);
      createDataObjectsTable(model.getDataObjectInfos());      
    }
  }

}
