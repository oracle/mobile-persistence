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

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.view.uimodel.DBDataObjectTableModel;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;

import oracle.javatools.ui.table.GenericTable;


public class DBDataObjectsPanel
  extends DefaultTraversablePanel
{
  JScrollPane scrollPane =
    new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  GenericTable table = null;
  JLabel instruction = new JLabel("Set data object class names");
  private BusinessObjectGeneratorModel model;

  public DBDataObjectsPanel()
  {
    super();
    setLayout(new GridBagLayout());
    //    add(new JLabel("Data Objects"),
    add(instruction,
        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                               new Insets(5, 0, 0, 0), 0, 0));
    GridBagConstraints gbcScrollPane =
      new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH,
                             new Insets(0, 0, 5, 0), 0, 0);
    add(scrollPane, gbcScrollPane);
  }

  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    createDataObjectsTable(model.getDataObjectInfos());
  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {
    super.onExit(tc);
  }

  public void createDataObjectsTable(List<DataObjectInfo> dataObjects)
  {
    table = new GenericTable(new DBDataObjectTableModel(dataObjects));
    table.setColumnSelectorAvailable(false);

    //To stop cell editing when user switches to another component without using tab/enter keys
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    scrollPane.getViewport().setView(table);
  }

}
