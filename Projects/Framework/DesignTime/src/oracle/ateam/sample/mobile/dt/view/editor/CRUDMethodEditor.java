/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.editor;

import java.awt.Component;

import java.util.Iterator;
import java.util.List;

import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import oracle.ateam.sample.mobile.dt.model.DCMethod;

public class CRUDMethodEditor
  extends AbstractCellEditor implements TableCellEditor
{
  private JComboBox combo = new JComboBox();

  public CRUDMethodEditor(Map<String,DCMethod> methods)
  {
    super();
    combo.addItem("");
    Iterator<String> iterator = methods.keySet().iterator();
    while(iterator.hasNext())
    {
      combo.addItem(iterator.next());
    }
  }

  @Override
  public Object getCellEditorValue()
  {
    Object item = combo.getSelectedItem();
    return item;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
  {
    combo.setSelectedItem(value);   
    return combo;
  }
}
