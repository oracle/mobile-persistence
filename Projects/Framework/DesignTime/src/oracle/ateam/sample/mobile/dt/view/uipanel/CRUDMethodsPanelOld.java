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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import oracle.ateam.sample.mobile.dt.view.editor.CRUDMethodEditor;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.view.uimodel.CRUDServiceTableModel;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;

import oracle.javatools.ui.table.GenericTable;


public class CRUDMethodsPanelOld
  extends DefaultTraversablePanel
{

  private BusinessObjectGeneratorModel model;

  JLabel instruction = new JLabel("Select the web service CRUD methods you want to invoke from your service object");
  JScrollPane scrollPane =  new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  GenericTable table = null;

  public CRUDMethodsPanelOld()
  {
    // GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty
    //                  , int anchor, int fill, Insets insets, int ipadx, int ipady)
    // Insets(int top, int left, int bottom, int right)

    setLayout(new GridBagLayout());
    add(instruction, 
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

  private void populateTable()
  {
    Map<String,DCMethod> methods = model.getDataControlVisitor().getCRUDMethods();
    List<DataObjectInfo> services = new ArrayList<DataObjectInfo>();
    for (DataObjectInfo doi: model.getSelectedDataObjects())
    {
      if (doi.getParent() == null)
      {
        doi.initCrudMethodsIfNeeded(methods);
        services.add(doi);
      }
    }
    table = new GenericTable(new CRUDServiceTableModel(services,methods));
    table.setColumnSelectorAvailable(false);

    //To stop cell editing when user switches to another component without using tab/enter keys
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    // set up method list cell editor

    CRUDMethodEditor cRUDMethodEditor = new CRUDMethodEditor(methods);
    TableColumn tc1 = table.getColumnModel().getColumn(1);
    TableColumn tc2 = table.getColumnModel().getColumn(2);
    TableColumn tc3 = table.getColumnModel().getColumn(3);
    TableColumn tc4 = table.getColumnModel().getColumn(4);
    tc1.setCellEditor(new CRUDMethodEditor(methods));
    tc2.setCellEditor(new CRUDMethodEditor(methods));
    tc3.setCellEditor(new CRUDMethodEditor(methods));
    tc4.setCellEditor(new CRUDMethodEditor(methods));
    scrollPane.getViewport().setView(table);
  }

  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    populateTable();
  }

  public void onExit(TraversableContext tc) throws TraversalException
  {
    super.onExit(tc);
  }

}
