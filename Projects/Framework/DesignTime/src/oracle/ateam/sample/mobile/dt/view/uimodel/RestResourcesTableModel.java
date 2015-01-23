/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uimodel;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import oracle.ateam.sample.mobile.dt.model.DCMethod;

public class RestResourcesTableModel
  extends AbstractTableModel
{
  
  String[] colHeaders = 
  { "Resource","Method","Payload"};
  List<DCMethod> resources;
  
  public RestResourcesTableModel(List<DCMethod> resources)
  {
    this.resources = resources;
  }

  @Override
  public int getRowCount()
  {
    return resources.size();
  }

  @Override
  public int getColumnCount()
  {
    return colHeaders.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    DCMethod method = resources.get(rowIndex);
    if (columnIndex==0)
    {
      return method.getFullUri();
    }
    else if (columnIndex==1)
    {
      return method.getRequestType();
    }
    return null;
  }

  public Class<?> getColumnClass(int columnIndex)
  {
    return String.class;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return true;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    DCMethod method = resources.get(rowIndex);
    if (columnIndex==0)
    {
      String value = (String) aValue;
      value  = value!=null ? value.trim() : null;
      method.setFullUri(value);
    }
    else if (columnIndex==1)
    {
      method.setRequestType((String) aValue);
    }
  }

  public String getColumnName(int column)
  {
    return colHeaders[column];
  }

}
