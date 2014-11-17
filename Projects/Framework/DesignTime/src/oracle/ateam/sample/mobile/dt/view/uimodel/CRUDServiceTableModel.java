/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uimodel;

import java.util.List;

import java.util.Map;

import javax.swing.table.AbstractTableModel;

import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;

public class CRUDServiceTableModel
  extends AbstractTableModel
{
  
  String[] colHeaders = 
  {"Service","Create Method","Update Method","Merge Method","Delete Method"};

  List<DataObjectInfo> dataObjectInfos;
  Map<String, DCMethod> methods;
  
  public CRUDServiceTableModel(List<DataObjectInfo> dataObjectInfos, Map<String, DCMethod> methods)
  {
    this.dataObjectInfos = dataObjectInfos;
    this.methods = methods;
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
      return doi.getClassName()+"Service";
    }
    else if (columnIndex==1)
    {
      return doi.getCreateMethod()!=null ? doi.getCreateMethod().getName() : null;
    }
    else if (columnIndex==2)
    {
      return doi.getUpdateMethod()!=null ? doi.getUpdateMethod().getName() : null;
    }
    else if (columnIndex==3)
    {
      return doi.getMergeMethod()!=null ? doi.getMergeMethod().getName() : null;
    }
    else if (columnIndex==4)
    {
      return doi.getDeleteMethod()!=null ? doi.getDeleteMethod().getName() : null;
    }
    return null;
  }

  public Class<?> getColumnClass(int columnIndex)
  {
    return String.class;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return columnIndex!=0;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    String value = (String) (aValue!= null && aValue.equals("") ? null : aValue);
    DCMethod method = value!=null ? methods.get(value) : null;
    DataObjectInfo doi = dataObjectInfos.get(rowIndex);
    if (columnIndex==1)
    {
      doi.setCreateMethod(method);
    }
    else if (columnIndex==2)
    {
      doi.setUpdateMethod(method);
    }
    else if (columnIndex==3)
    {
      doi.setMergeMethod(method);
    }
    else if (columnIndex==4)
    {
      doi.setDeleteMethod(method);
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
