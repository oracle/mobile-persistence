/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uimodel;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;

public class DBDataObjectTableModel
  extends AbstractTableModel
{
  
  String[] colHeaders = 
//  { "Generate?","Web Service Accessor Path","Class Name","Key Attribute"};
  { "Class Name","Table Name"};
  List<DataObjectInfo> dataObjectInfos;
  
  public DBDataObjectTableModel(List<DataObjectInfo> dataObjectInfos)
  {
    this.dataObjectInfos = dataObjectInfos;
  }

  @Override
  public int getRowCount()
  {
    return dataObjectInfos.size();
  }

  @Override
  public int getColumnCount()
  {
    return colHeaders.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    DataObjectInfo doi = dataObjectInfos.get(rowIndex);
    if (columnIndex==0)
    {
      return doi.getClassName();
    }
    else if (columnIndex==1)
    {
      return doi.getAccessorPath();
    }
    return null;
  }

  public Class<?> getColumnClass(int columnIndex)
  {
    return String.class;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return columnIndex!=1;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    DataObjectInfo doi = dataObjectInfos.get(rowIndex);
    if (columnIndex==0)
    {
      doi.setClassName((String) aValue);
    }
  }

  public String getColumnName(int column)
  {
    return colHeaders[column];
  }

  public List<DataObjectInfo> getDataObjectInfos()
  {
    return dataObjectInfos;
  }
}
