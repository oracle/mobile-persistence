/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.controller.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.adf.model.adapter.dataformat.AccessorDef;
import oracle.adf.model.adapter.dataformat.MethodDef;
import oracle.adf.model.adapter.dataformat.MethodReturnDef;

import oracle.adfdt.model.objects.DataControl;

import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;

import oracle.binding.meta.AccessorDefinition;
import oracle.binding.meta.DefinitionContainer;
import oracle.binding.meta.OperationDefinition;
import oracle.binding.meta.OperationReturnDefinition;
import oracle.binding.meta.StructureDefinition;

public class DataControlDataObjectParser
{
  private DataControl dataControl;
  private StructureDefinition dataControlBean;
  private List<DataObjectInfo> accessorBeans = new ArrayList<DataObjectInfo>();
  private List<DataObjectInfo> methodAccessorBeans = new ArrayList<DataObjectInfo>();
  private Map<String, DCMethod> crudMethods; 
    
  public DataControlDataObjectParser(DataControl dataControl)
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
                                      DataObjectInfo parent)
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
      // if it is a top-level accessor, it must be a collection, otherwise we ignore it,
      // with this check we skip the ADF BC SDO accessors suffixed with _parameters
      // HMMM, but this also skips tableArray-item from AuroPlayer WS ...
//      if (level> 1 || accessor.isCollection())
//      {
        StructureDefinition accBean = accessor.getStructure();    
        // only add bean when itself when it has attrobutes, otherwise it is
        //a "container" element
        boolean hasAttrs = accBean.getAttributeDefinitions().iterator().hasNext();
        boolean createDoi = hasAttrs;
      DataObjectInfo newParent = parent;
        if (createDoi)
        {
        DataObjectInfo doi = new DataObjectInfo(accessor,accBean,accessor.isCollection(),parent);
          newParent = doi;
          accessorBeans.add(doi);
        }  
        // recursive call
        discoverBeanCollections(accBean,beansProcessed, newParent);
//      }
//      else
//      {
//        System.err.println(accessor.getName()+" is skipped: Top-level accessor that is NOT A COLLECTION!!");
//      }
    }

    DefinitionContainer  methodAccessors = (DefinitionContainer) currentBean.getOperationDefinitions();
    // although this is a typed list, we get a CCE on some entries , very weird
    //    for (MethodDef method : methodAccessors)
    //    for (int i = 0; i < methodAccessors.size(); i++)
    Iterator methods = methodAccessors.iterator();
    while (methods.hasNext())
    {
    //      Object entry = methodAccessors.get(i);
      Object entry = methods.next();
      if (entry instanceof OperationDefinition)
      {
        OperationDefinition method = (OperationDefinition) entry;
        OperationReturnDefinition returnType = (OperationReturnDefinition) method.getOperationReturnType();
    //        if (returnType!=null && returnType.isCollection() && !returnType.isScalarCollection() && !usages.containsKey(method.getName()) ) //&& accessor.isCollectionType())
//        if (returnType!=null && returnType.isAccessor() ) //&& accessor.isCollectionType())
        if (returnType!=null ) // && returnType.isAccessor() ) //&& accessor.isCollectionType())
        {
    //          StructureDefinition accBean = (StructureDefinition) returnType.getDefinitionParent();
          MethodDef methodDef = (MethodDef) returnType.getDefinitionParent();   
          MethodReturnDef methodreturnDef = (MethodReturnDef) methodDef.getOperationReturnType();
//          if (methodreturnDef!=null && methodreturnDef.isCollection() && !methodreturnDef.isScalarCollection() ) //&& accessor.isCollectionType())
          if (methodreturnDef!=null ) // && methodreturnDef.isCollection() && !methodreturnDef.isScalarCollection() ) //&& accessor.isCollectionType())
          {
            StructureDefinition accBean = methodreturnDef.getStructure();    
            if (accBean==null)
            {
              continue;
            }
            if (!accBean.getAttributeDefinitions().iterator().hasNext() && accBean.getAccessorDefinitions().iterator().hasNext())
            {
              // bean has no attrs
              // Return type is wrapped in "Result" accessor, then get the bean of the accessor
              AccessorDefinition accessor = (AccessorDefinition) accBean.getAccessorDefinitions().iterator().next();
              accBean = accessor.getStructure();                
            }
            boolean hasAttrs = accBean.getAttributeDefinitions().iterator().hasNext();
            boolean createDoi = hasAttrs;
            DataObjectInfo newParent = parent;
            if (createDoi)
            {
              boolean isCollection = methodreturnDef.isCollection();
              // above statement no longer works in 12.1.3, we need to get the accessor def from the struct to get
              // true returned if it is really a collectiond.
              // update 06-jun-14: In latest 12.1.3 build, the above code works again as expected!
//              if ( methodreturnDef.getStructure()!=null && methodreturnDef.getStructure().getAccessorDefinitions()!=null)
//              {
//                Iterator it = methodreturnDef.getStructure().getAccessorDefinitions().iterator();
//                if (it.hasNext())
//                {
//                  AccessorDef accdef = (AccessorDef) it.next();   
//                  isCollection = accdef.isCollection();
//                }
//              }
              DataObjectInfo doi = new DataObjectInfo(methodDef,accBean,isCollection,parent);
              newParent = doi;
              methodAccessorBeans.add(doi);              
              // recursive call
            }
            // only increase level when current bean is added as DataObject
            discoverBeanCollections(accBean,beansProcessed,newParent);
          }
        }        
      }
    }    
  }
  

  public Map<String, DCMethod> getCRUDMethods()
  {
    if (crudMethods==null)
    {
      crudMethods = discoverCRUDMethods();
    }
    return crudMethods;
  }

  public Map<String, DCMethod> discoverCRUDMethods()
  {
    Map<String, DCMethod> methods = new HashMap<String, DCMethod>();
    DefinitionContainer  methodAccessors = (DefinitionContainer) dataControlBean.getOperationDefinitions();
    Iterator methodsIter = methodAccessors.iterator();
    while (methodsIter.hasNext())
    {
      Object entry = methodsIter.next();
      if (entry instanceof OperationDefinition)
      {
        OperationDefinition method = (OperationDefinition) entry;
        methods.put(method.getName(),new DCMethod(method));          
      }
    }  
    DefinitionContainer  accessors = (DefinitionContainer) dataControlBean.getAccessorDefinitions();
    Iterator accessorIter = accessors.iterator();
    while (accessorIter.hasNext())
    {
      Object entry = accessorIter.next();
      if (entry instanceof AccessorDefinition)
      {
        AccessorDefinition accessor = (AccessorDefinition) entry;
        methods.put(accessor.getName(),new DCMethod(accessor));          
      }
    }  
    return methods;
  }

  public List<DataObjectInfo> getAccessorBeans()
  {
    return accessorBeans;
  }

  public List<DataObjectInfo> getMethodAccessorBeans()
  {
    return methodAccessorBeans;
  }
  
  public List<DataObjectInfo> getAllAccessorBeans()
  {
    List<DataObjectInfo> allBeans = new ArrayList<DataObjectInfo>();
    allBeans.addAll(getAccessorBeans());
    allBeans.addAll(getMethodAccessorBeans());
    return allBeans;
  }

  public List<DataObjectInfo> getAllCollectionAccessorBeans()
  {
    List<DataObjectInfo> colBeans = new ArrayList<DataObjectInfo>();
    for (DataObjectInfo bean : getAllAccessorBeans())
    {
      boolean add = bean.isCollection();
      if (add)
      {
        // only add if all parents are also collections
        DataObjectInfo parent = bean.getParent();
        while (parent!=null)
        {
          if (!parent.isCollection())
          {
            add = false;
            break;
          }
          parent = parent.getParent();
        }
      }
      if (add)
      {
        colBeans.add(bean);
      }
    }
    return colBeans;
  }

}

