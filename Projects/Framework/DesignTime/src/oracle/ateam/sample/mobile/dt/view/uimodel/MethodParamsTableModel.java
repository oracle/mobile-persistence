/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uimodel;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import oracle.ateam.sample.mobile.dt.model.DCMethodParameter;
import oracle.ateam.sample.mobile.dt.util.StringUtils;

public class MethodParamsTableModel
  extends AbstractTableModel
{
  
  private String[] colHeaders = 
  { "Name","Value Provider","Data Object Attribute","Value"};
  private List<DCMethodParameter> params;
  private boolean nameUpdateable;
  
  public MethodParamsTableModel(List<DCMethodParameter> params, boolean nameUpdateable)
  {
    this.params = params;
    this.nameUpdateable = nameUpdateable;
  }

  @Override
  public int getRowCount()
  {
    return params.size();
  }

  @Override
  public int getColumnCount()
  {
    return colHeaders.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    DCMethodParameter param = params.get(rowIndex);
    if (columnIndex==0)
    {
      return param.getName();
    }
    else if (columnIndex==1)
    {
      return param.getValueProvider();
    }
    else if (columnIndex==2)
    {
      return param.getDataObjectAttribute();
    }
    else if (columnIndex==3)
    {
      return param.getValue();
    }
    return null;
  }

  public Class<?> getColumnClass(int columnIndex)
  {
    return String.class;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    DCMethodParameter param = params.get(rowIndex);
    if (columnIndex==0)
    {
      return nameUpdateable;
    }
    if (columnIndex==1)
    {
      return true;
    }
    if (columnIndex==2)
    {
      return param.isDataObjectAttribute();
    }
    if (columnIndex==3)
    {
      return param.isLiteralValue() || param.isELExpression();
    }
    return columnIndex!=0;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    String value = StringUtils.isEmpty((String)aValue) ? null : aValue.toString().trim();
    DCMethodParameter param = params.get(rowIndex);
    if (columnIndex==0)
    {
      param.setName(value);
    }
    else if (columnIndex==1)
    {
      param.setValueProvider(value);
      if (param.getValueProvider()!=null && param.getValueProvider()!=value)
      {
        param.setDataObjectAttribute(null);
        param.setValue(null);
      }
    }
    else if (columnIndex==2)
    {
      param.setDataObjectAttribute(value);
    }
    else if (columnIndex==3)
    {
      param.setValue(value);
    }
  }

  public String getColumnName(int column)
  {
    return colHeaders[column];
  }

}
