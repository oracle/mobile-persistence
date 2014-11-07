package oracle.ateam.sample.mobile.dt.view.uipanel;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;

import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.db.SchemaObjectDescriptor;
import oracle.ide.db.panels.SelectDBObjectsPanel;
//import oracle.ideimpl.db.transfer.panels.TransferSelectionPanel;
import oracle.ide.panels.TraversableContext;

import oracle.ide.panels.TraversalException;

import oracle.javatools.db.DBException;
import oracle.javatools.db.DBObject;
import oracle.javatools.db.DBObjectProvider;
import oracle.javatools.db.SchemaObject;
import oracle.javatools.db.Table;
import oracle.javatools.db.View;

import oracle.toplink.workbench.addin.mappings.spi.db.JDeveloperTable;
import oracle.toplink.workbench.mappingsmodel.spi.db.ExternalColumn;
import oracle.toplink.workbench.mappingsmodel.spi.db.ExternalForeignKey;
import oracle.toplink.workbench.mappingsmodel.spi.db.ExternalForeignKeyColumnPair;

public class SelectDBTablesPanel
  extends SelectDBObjectsPanel // TransferSelectionPanel
{
  public SelectDBTablesPanel()
  {
    super();
  }

  protected DBObjectProvider getProvider(TraversableContext tc)
  {
    BusinessObjectGeneratorModel model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    return model.getDbProvider();
  }


  protected String[] getAvailableTypes()
  {
    //    return new String[]{Table.TYPE, View.TYPE};
    return new String[]
      { Table.TYPE };
  }

  protected void commit(TraversableContext traversableContext)
    throws TraversalException
  {
    super.commit(traversableContext);
    BusinessObjectGeneratorModel model = (BusinessObjectGeneratorModel) traversableContext.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    List<DataObjectInfo> dataObjectInfos = new ArrayList<DataObjectInfo>();
    SchemaObjectDescriptor[] objects = (SchemaObjectDescriptor[]) getSelectedObjects();
    ArrayList<JDeveloperTable> tables = new ArrayList<JDeveloperTable>();
    Map<String, DataObjectInfo> doiMap = new HashMap<String, DataObjectInfo>();
    for (SchemaObjectDescriptor object: objects)
    {
      try
      {
        SchemaObject descriptor = object.unwrapDescriptor(model.getDbProvider());
        if (descriptor instanceof Table)
        {
          Table table = (Table) descriptor;
          JDeveloperTable jtable = new JDeveloperTable(table);
          DataObjectInfo dataObjectInfo = new DataObjectInfo(jtable);
          dataObjectInfos.add(dataObjectInfo);
          doiMap.put(jtable.getName(), dataObjectInfo);
          tables.add(jtable);
        }
      }
      catch (DBException e)
      {
      }
      // create child accessors based on foreign keys if both parent and child table is in selection
    }
    for (JDeveloperTable table: tables)
    {
      for (ExternalForeignKey fk: table.getForeignKeys())
      {
        DataObjectInfo parentDataObject = doiMap.get(fk.getTargetTable().getName());
        if (parentDataObject != null)
        {
          DataObjectInfo childDataObject = doiMap.get(table.getName());
          // add child accessor with attr mappings
          AccessorInfo accessor = new AccessorInfo(parentDataObject, childDataObject);
          for (ExternalForeignKeyColumnPair columnPair: fk.getColumnPairs())
          {
            String childColumn = columnPair.getSourceColumn().getName();
            String parentColumn = columnPair.getTargetColumn().getName();
            AttributeInfo parentAttr = parentDataObject.getAttributeDefByColumnName(parentColumn);
            AttributeInfo childAttr = childDataObject.getAttributeDefByColumnName(childColumn);
            accessor.addAttributeMapping(parentAttr, childAttr);
          }
          parentDataObject.addChild(accessor);
        }
      }
    }
    model.setDataObjectInfos(dataObjectInfos);
  }

  protected void init(boolean b, TraversableContext traversableContext)
  {
    super.init(b, traversableContext);
  }

}
