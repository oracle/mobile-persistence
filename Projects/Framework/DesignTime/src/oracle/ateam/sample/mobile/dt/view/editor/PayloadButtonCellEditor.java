/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.editor;

import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Iterator;
import java.util.List;

import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import oracle.ateam.sample.mobile.dt.model.DCMethod;

import oracle.bali.ewt.dialog.JEWTDialog;

public class PayloadButtonCellEditor
  extends AbstractCellEditor implements TableCellEditor, ActionListener
{
  private JButton button = new JButton("...");
  List<DCMethod> resources;
  DCMethod currentResource = null;
  JEWTDialog payloadDialog;
  JTextArea payloadField;

  public PayloadButtonCellEditor(List<DCMethod> resources, JEWTDialog payloadDialog, JTextArea payloadField)
  {
    super();
    this.resources = resources;
    button.addActionListener(this);
    this.payloadDialog = payloadDialog;
    this.payloadField = payloadField;
  }

  @Override
  public Object getCellEditorValue()
  {
    return null;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
  {
    currentResource = resources.get(row); 
    return button;
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent)
  {
    payloadField.setText(currentResource.getSamplePayload());
    boolean OK = payloadDialog.runDialog();
    if (OK)
    {
      currentResource.setSamplePayload(payloadField.getText());
    }
  }
}
