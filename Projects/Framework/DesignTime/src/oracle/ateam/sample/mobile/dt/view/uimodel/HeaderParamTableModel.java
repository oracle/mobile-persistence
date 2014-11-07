package oracle.ateam.sample.mobile.dt.view.uimodel;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.HeaderParam;

public class HeaderParamTableModel
  extends AbstractTableModel
{
  
  String[] colHeaders = 
  { "Name","Value"};
  List<HeaderParam> headerParams;
  
  public HeaderParamTableModel(List<HeaderParam> headerParams)
  {
    this.headerParams = headerParams;
  }

  @Override
  public int getRowCount()
  {
    return headerParams.size();
  }

  @Override
  public int getColumnCount()
  {
    return colHeaders.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    HeaderParam param = headerParams.get(rowIndex);
    if (columnIndex==0)
    {
      return param.getName();
    }
    else if (columnIndex==1)
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
    return true;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    HeaderParam param = headerParams.get(rowIndex);
    if (columnIndex==0)
    {
      param.setName((String) aValue);
    }
    else if (columnIndex==1)
    {
      param.setValue((String) aValue);
    }
  }

  public String getColumnName(int column)
  {
    return colHeaders[column];
  }

  public List<HeaderParam> getHeaderParams()
  {
    return headerParams;
  }
}
