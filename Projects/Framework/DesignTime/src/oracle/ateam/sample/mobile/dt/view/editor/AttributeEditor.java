/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.editor;

import java.awt.Component;

import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;

public class AttributeEditor
  extends AbstractCellEditor implements TableCellEditor
{
  private JComboBox combo = new JComboBox();

  public AttributeEditor(DataObjectInfo dataObject)
  {
    super();
    for(AttributeInfo attr : dataObject.getAttributeDefs())
    {
      combo.addItem(attr.getAttrName());
    }
  }

  @Override
  public Object getCellEditorValue()
  {
    return combo!=null ? combo.getSelectedItem() : null;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
  {
//    combo = new JComboBox();
//    DCMethod method = methods.get(row);
//    for (String attr : method.getDataObject().getAttributes())
//    {
//      combo.addItem(attr);
//    }
    return combo;
  }
}
