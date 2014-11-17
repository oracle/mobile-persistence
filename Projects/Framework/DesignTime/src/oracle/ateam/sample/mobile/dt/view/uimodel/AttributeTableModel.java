/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uimodel;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;

public class AttributeTableModel
  extends AbstractTableModel
{
  
  String[] colHeaders = 
  { "Persist?", "Key?","Required?","Name","Java Type","DB Column Type"};
  List<AttributeInfo> attributeInfos;
  private DataObjectInfo dataObjectInfo;
  
  public AttributeTableModel(DataObjectInfo dataObjectInfo)
  {
    this.attributeInfos = dataObjectInfo.getAttributeDefs();
    this.dataObjectInfo = dataObjectInfo;
  }

  @Override
  public int getRowCount()
  {
    return attributeInfos.size();
  }

  @Override
  public int getColumnCount()
  {
    return colHeaders.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    AttributeInfo attr = attributeInfos.get(rowIndex);
    if (columnIndex==0)
    {
      return attr.isPersisted(); 
    }
    else if (columnIndex==1)
    {
      return attr.isKeyAttribute();
    }
    else if (columnIndex==2)
    {
      return attr.isRequired();
    }
    else if (columnIndex==3)
    {
      return attr.getAttrName();
    }
    else if (columnIndex==4)
    {
      return attr.getJavaTypeFullName();
    }
    else if (columnIndex==5)
    {
      return attr.getColumnType();
    }
    return null;
  }

  public Class<?> getColumnClass(int columnIndex)
  {
    if (columnIndex<=2)
    {
      return Boolean.class;
    }
    return String.class;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return columnIndex!=0 || dataObjectInfo.isPersisted();
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    AttributeInfo attr = attributeInfos.get(rowIndex);
    if (columnIndex==0)
    {
      attr.setPersisted((Boolean) aValue);
    }
    else if (columnIndex==1)
    {
      attr.setKeyAttribute((Boolean) aValue);
    }
    else if (columnIndex==2)
    {
      attr.setRequired((Boolean) aValue);
    }
    else if (columnIndex==3)
    {
      attr.setAttrName((String) aValue);
    }
    else if (columnIndex==4)
    {
      attr.setJavaTypeFullName((String) aValue);
    }
    else if (columnIndex==5)
    {
      attr.setColumnType((String) aValue);
    }
  }

  public String getColumnName(int column)
  {
    return colHeaders[column];
  }

  public List<AttributeInfo> getAttributeInfos()
  {
    return attributeInfos;
  }
}
