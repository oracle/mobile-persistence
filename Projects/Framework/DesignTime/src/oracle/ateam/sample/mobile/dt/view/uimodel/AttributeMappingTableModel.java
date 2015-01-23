/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uimodel;

import java.util.List;

import java.util.Map;

import java.util.Set;

import javax.swing.table.AbstractTableModel;

import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.AccessorInfo.AttributeMapping;

public class AttributeMappingTableModel
  extends AbstractTableModel
{
  
  private String[] colHeaders = 
  { "Selected Parent Attribute(s)","Selected Child Attribute(s)"};
  private AccessorInfo childDataObject;
  private List<AccessorInfo.AttributeMapping> mappings;
  
  public AttributeMappingTableModel(AccessorInfo childDataObject)
  {
    this.childDataObject = childDataObject;
    this.mappings = childDataObject.getAttributeMappings();
  }

  @Override
  public int getRowCount()
  {
    return mappings.size();
  }

  @Override
  public int getColumnCount()
  {
    return colHeaders.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    AccessorInfo.AttributeMapping mapping = mappings.get(rowIndex);
    if (columnIndex==0)
    {
      return mapping.getParentAttr().getAttrName();
    }
    else if (columnIndex==1)
    {
      return mapping.getChildAttr().getAttrName();
    }
    return null;
  }

//  @Override
//  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
//  {
//    AccessorInfo.AttributeMapping mapping = mappings.get(rowIndex);
//    if (columnIndex==1)
//    {
//      mapping.getChildAttr().setAttrName((String) aValue);
//    }
//  }

  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return false;
//    AccessorInfo.AttributeMapping mapping = mappings.get(rowIndex);
//    return columnIndex==1 && mapping.getChildAttr().getParentReferenceAttribute()!=null;
  }

  public String getColumnName(int column)
  {
    return colHeaders[column];
  }

}
