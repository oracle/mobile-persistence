/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.controller.parser;

import java.security.acl.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.List;

import java.util.Map;

import oracle.adf.model.adapter.dataformat.MethodDef;

import oracle.adf.model.adapter.dataformat.MethodReturnDef;

import oracle.adfdt.model.objects.DataControl;

import oracle.ateam.sample.mobile.dt.model.UIDataObjectInfo;

import oracle.binding.meta.AccessorDefinition;
import oracle.binding.meta.AttributeDefinition;
import oracle.binding.meta.DefinitionContainer;
import oracle.binding.meta.NamedDefinition;
import oracle.binding.meta.OperationDefinition;
import oracle.binding.meta.OperationReturnDefinition;
import oracle.binding.meta.ParameterDefinition;
import oracle.binding.meta.StructureDefinition;

public class DataControlUIDataObjectParser
{
  private DataControl dataControl;
  private StructureDefinition dataControlBean;
  private List<UIDataObjectInfo> accessorBeans = new ArrayList<UIDataObjectInfo>();
    
  public DataControlUIDataObjectParser(DataControl dataControl)
  {
    super();
    this.dataControl = dataControl;
    dataControlBean = dataControl.getRealDataControl().getDataControlDefinition().getStructure();
  }
  
  public void discoverBeanCollections()
  {
    discoverBeanCollections(dataControlBean, new ArrayList<StructureDefinition>(),null);
  }

  private boolean listContains(List<StructureDefinition> beansProcessed, StructureDefinition currentBean)
  {
    boolean found = false;
    for (StructureDefinition sd : beansProcessed)
    {
      if (sd.getFullName().equals(currentBean.getFullName()))
      {
        found = true;
        break;
      }
    }
    return found;
  }

  public void discoverBeanCollections(StructureDefinition currentBean, List<StructureDefinition> beansProcessed,
                                      UIDataObjectInfo parent)
  {
    if (listContains(beansProcessed,currentBean))
    {
      // can happen with recursive calls
      return;
    }
    beansProcessed.add(currentBean);
    Iterator accessors = currentBean.getAccessorDefinitions().iterator(); 
    while (accessors.hasNext())
    {
      Object aap = accessors.next();
      AccessorDefinition accessor = (AccessorDefinition) aap;
      StructureDefinition accBean = accessor.getStructure();    
      // only add bean when itself when it has attrobutes, otherwise it is
      //a "container" element
      boolean hasAttrs = accBean!=null && accBean.getAttributeDefinitions().iterator().hasNext();
      if (hasAttrs) {
          Iterator it = accBean.getAttributeDefinitions().iterator();
          AttributeDefinition attr = (AttributeDefinition) it.next();    
          if (attr.getName().equals("element") && !it.hasNext()) {
              // this is an untyped getList method that we should skip. 
              // Somehow in 12.1.3 this shows in the DC palette with one attribute
              // named "element". In 11.1.2.4 this list showed up properly with no attributes and was already skipped
              hasAttrs = false;
          }
      }
      boolean createDoi = hasAttrs && accessor.isCollection() && !"DataSynchAction".equals(accBean.getName());
      if (createDoi)
      {
        UIDataObjectInfo doi = new UIDataObjectInfo(accessor,accBean,parent);
        if (!accessorBeans.contains(doi))
        {
          accessorBeans.add(doi);
          // recursive call
          discoverBeanCollections(accBean,beansProcessed, doi);          
        }
      }  
    }

  }
  
  public List<UIDataObjectInfo> getAccessorBeans()
  {
    return accessorBeans;
  }

  public List<UIDataObjectInfo> getRootAccessorBeans()
  {
    List<UIDataObjectInfo> rootBeans = new ArrayList<UIDataObjectInfo>();
    for (UIDataObjectInfo bean : getAccessorBeans())
    {
      if (bean.getParent()==null)
      {
        rootBeans.add(bean);
      }
    }
    return rootBeans;
  }

}

