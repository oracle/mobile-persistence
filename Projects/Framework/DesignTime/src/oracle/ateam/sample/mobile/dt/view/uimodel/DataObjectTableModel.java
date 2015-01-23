/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uimodel;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;

public class DataObjectTableModel
  extends AbstractTableModel
{
  
  String[] colHeaders = 
//  { "Generate?","Web Service Accessor Path","Class Name","Key Attribute"};
  { "Select?","Persist?","Class Name","Access Path"};
  List<DataObjectInfo> dataObjectInfos;
  
  public DataObjectTableModel(List<DataObjectInfo> dataObjectInfos)
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
      return doi.isGenerate();
    }
    if (columnIndex==1)
    {
      return doi.isPersisted();
    }
    else if (columnIndex==2)
    {
      // prefix with spaces to get indentation representing master-detail relations
      String displayName = doi.getClassName();
      for (int i = 0; i < doi.getLevel(); i++)
      {
        displayName = "  "+displayName;
     }
      return displayName;
    }
    else if (columnIndex==3)
    {
      return doi.getAccessorPath();
    }
//    else if (columnIndex==3)
//    {
//      return doi.getKeyAttribute();
//    }
    return null;
  }

  public Class<?> getColumnClass(int columnIndex)
  {
    if (columnIndex<=1)
    {
      return Boolean.class;
    }
    return String.class;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return columnIndex!=3;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    DataObjectInfo doi = dataObjectInfos.get(rowIndex);
    if (columnIndex==0)
    {
      doi.setGenerate((Boolean) aValue);
    }
    else if (columnIndex==1)
    {
      doi.setPersisted((Boolean) aValue);
    }
    else if (columnIndex==2)
    {
      String name = aValue!=null ? ((String)aValue).trim() : null;
      doi.setClassName(name);
    }
//    else if (columnIndex==3)
//    {
//      doi.setKeyAttribute((String) aValue);
//    }
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
