/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.controller;

import java.beans.PropertyVetoException;

import java.util.ArrayList;
import java.util.List;

import oracle.adf.model.binding.DCDefBase;

import oracle.adfdt.model.DataControlManager;
import oracle.adfdt.model.datacontrols.JUDTAdapterDataControl;
import oracle.adfdt.model.ide.managers.ApplicationManager;
import oracle.adfdt.model.ide.managers.PageDefinitionManager;
import oracle.adfdt.model.objects.AccessorIterator;
import oracle.adfdt.model.objects.Application;
import oracle.adfdt.model.objects.CtrlAction;
import oracle.adfdt.model.objects.CtrlHier;
import oracle.adfdt.model.objects.CtrlHierTypeBinding;
import oracle.adfdt.model.objects.CtrlMethodAction;
import oracle.adfdt.model.objects.CtrlValue;
import oracle.adfdt.model.objects.DataControl;
import oracle.adfdt.model.objects.IteratorBinding;
import oracle.adfdt.model.objects.PageDefinition;
import oracle.adfdt.model.objects.PageDefinitionUsage;
import oracle.adfdt.model.objects.Variable;
import oracle.adfdt.model.objects.VariablesIterator;

import oracle.ateam.sample.mobile.dt.model.UIDataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.UIGeneratorModel;
import oracle.ateam.sample.mobile.dt.util.StringUtils;

import oracle.binding.meta.StructureDefinition;

import oracle.ide.Context;
import oracle.ide.Ide;
import oracle.ide.model.Project;

import oracle.javatools.parser.java.v2.model.JavaClass;

import oracle.jbo.server.util.JboNamedData;
import oracle.jbo.uicli.mom.JUTags;


public class PageDefGenerator
{
  private UIGeneratorModel generatorModel;
  private UIDataObjectInfo dataObject;
  private Project project;
  private PageDefinition mContainer;
  private GeneratorLogPage log;
  private String dataControlName;
  private String rootIterName;

  public PageDefGenerator(UIGeneratorModel generatorModel, Project project, GeneratorLogPage log)
  {
    super();
    this.generatorModel = generatorModel;
    this.project = project;
    this.log = log;
    this.dataControlName = generatorModel.getDataControlName();
    this.rootIterName = dataControlName + "Iterator";
  }

  public void createListPageDef(String folderName)
    throws java.beans.PropertyVetoException
  {
    this.dataObject = generatorModel.getCurrentDataObject();
    String pageDefName = dataObject.getName() + "ListPageDef";
    String pagePath = "/" + folderName + "/" + dataObject.getName() + "List.amx";
    mContainer = createPageDefinitionIfNeeded(pageDefName, pagePath);
    log.info("Page definition " + pageDefName + " created");
  }

  public void createFormPageDef(String folderName)
    throws java.beans.PropertyVetoException
  {
    this.dataObject = generatorModel.getCurrentDataObject();
    String pageDefName = dataObject.getName() + "PageDef";
    String pagePath = "/" + folderName + "/" + dataObject.getName() + ".amx";
    mContainer = createPageDefinitionIfNeeded(pageDefName, pagePath);
    log.info("Page definition " + pageDefName + " created");
  }

//  public void createDataSynchBindings()
//    throws java.beans.PropertyVetoException
//  {
////    createDataSynchIterator(dataObject);
//    String attrName = "has"+dataObject.getRootDataObject().getName()+"DataSynchActions";
//    createValueBinding(attrName, dataControlName + "Iterator", attrName);
//  }

  public String createDeleteBinding(UIDataObjectInfo dataObject)
    throws java.beans.PropertyVetoException
  {
    String id = "Delete"+dataObject.getName();
    createActionBinding(id, dataObject.getAccessorName() + "Iterator", false, 30);
    return id;
  }

  public String createCreateBinding(UIDataObjectInfo dataObject)
    throws java.beans.PropertyVetoException
  {
    String id = "Create"+dataObject.getName();
    createActionBinding(id, dataObject.getAccessorName() + "Iterator", true, 41);
    return id;
  }

  public void createIteratorBindings(UIDataObjectInfo dataObject)
    throws PropertyVetoException
  {
    createRootIterator();
    List<UIDataObjectInfo> iteratorsNeeded = new ArrayList<UIDataObjectInfo>();
    UIDataObjectInfo parent = dataObject.getParent();
    while (parent!=null)
    {
      iteratorsNeeded.add(0,parent);
      parent = parent.getParent();
    }
    iteratorsNeeded.add(dataObject);
    String masterBinding = rootIterName;
    for(UIDataObjectInfo iterDataObject : iteratorsNeeded)
    {
      createAccessorIteratorBinding(iterDataObject, masterBinding);      
      masterBinding = iterDataObject.getAccessorName()+"Iterator";
    }
  }

  public void createAccessorIteratorBinding(UIDataObjectInfo dataObject, String masterIterBinding)
    throws java.beans.PropertyVetoException
  {
    //    <accessorIterator MasterBinding="DepartmentServiceIterator" Binds="departments" RangeSize="25"
    //                      DataControl="DepartmentService" BeanClass="oracle.demo.hrcrud.mobile.model.Department"
    //                      id="departmentsIterator"    />
    String iterName = dataObject.getAccessorName() + "Iterator";
    if (mContainer.findIteratorBinding(iterName) != null)
    {
      return;
    }
    AccessorIterator itb = (AccessorIterator) mContainer.createIteratorBinding(DCDefBase.PNAME_AccessorIterator);
    itb.setId(iterName);
    itb.setMasterIteratorBinding(masterIterBinding);
    itb.setAttribute("Binds", dataObject.getAccessorName());
    itb.setBeanClass(dataObject.getFullyQualifiedClassName());
    itb.setDataControlName(dataControlName);
    itb.setRangeSize(25);
    mContainer.addIterator(itb);
  }

  public void createRootIterator()
    throws java.beans.PropertyVetoException
  {
    //    <iterator Binds="root" RangeSize="25" DataControl="DepartmentService" id="DepartmentServiceIterator"/>
    if (mContainer.findIteratorBinding(rootIterName) != null)
    {
      return;
    }
    IteratorBinding rootIter = mContainer.createIteratorBinding(rootIterName);
    rootIter.setId(rootIterName);
    rootIter.setAttribute("Binds", "root");
    rootIter.setDataControlName(dataControlName);
    rootIter.setRangeSize(25);
    mContainer.addIterator(rootIter);
  }

  public PageDefinition createPageDefinitionIfNeeded(String pageDefName, String pagePath)
    throws java.beans.PropertyVetoException
  {
    String viewPackage = project.getProperty("defaultPackage");
    String pageDefPackageName = viewPackage + ".pageDefs";
    // this does not compile .. strange!
    Context context = new Context(project.getWorkspace(), project);
    Application dataBindings = ApplicationManager.findOrCreateApplication(context);

    String pageDefPath = pageDefPackageName + "." + pageDefName;
    PageDefinition container = PageDefinitionManager.findOrCreatePageDefinitionFromPath(context, pageDefPath);
    container.setPackageName(pageDefPackageName);

    String usageId = StringUtils.substitute(pageDefPath, ".", "_");
    PageDefinitionUsage pdusage = dataBindings.findPageDefinitionUsage(usageId);
    if (pdusage == null)
    {
      pdusage = dataBindings.createPageDefinitionUsage();
      pdusage.setId(usageId);
      pdusage.setPath(pageDefPath);
      pdusage.setBindingContainer(container);
      dataBindings.addPageDefinitionUsage(pdusage);
      dataBindings.addPageMapEntry(pagePath, usageId);
    }
    addDataControlUsage();
    return container;
  }

  public String createTreeBinding(UIDataObjectInfo dataObject)
    throws PropertyVetoException
  {
    //    <tree IterBinding="departmentsIterator" id="departments">
    //      <nodeDefinition DefName="oracle.demo.hrcrud.mobile.model.Department" Name="departments0">
    //        <AttrNames>
    //          <Item Value="departmentName"/>
    //          <Item Value="departmentId"/>
    //        </AttrNames>
    //      </nodeDefinition>
    //    </tree>
    String iterName = dataObject.getAccessorName() + "Iterator";
    List<String> attrs = new ArrayList<String>();
    addAttrIfNonEmpty(dataObject.getListAttribute1(), attrs);
    addAttrIfNonEmpty(dataObject.getListAttribute2(), attrs);
    addAttrIfNonEmpty(dataObject.getListAttribute3(), attrs);
    addAttrIfNonEmpty(dataObject.getListAttribute4(), attrs);
    String[] attrNames = attrs.toArray(new String[attrs.size()]);
    String id = dataObject.getAccessorName();
    if (mContainer.findControlBinding(id) == null)
    {
      CtrlHier tree = (CtrlHier) mContainer.createControlBinding("DCTree", null);
      tree.setId(id);
      tree.setIterBindingName(iterName);
      ArrayList<CtrlHierTypeBinding> nodeBindings = new ArrayList<CtrlHierTypeBinding>();

      CtrlHierTypeBinding treeNode =
        (CtrlHierTypeBinding) mContainer.createControlBinding(JUTags.PNAME_treeNodeDefinition, null);
      nodeBindings.add(treeNode);
      treeNode.setAttribute("Name", id + "0");
      //      treeNode.setDefClassName(dataObject.getFullyQualifiedClassName());
      treeNode.setAttribute("DefName", dataObject.getFullyQualifiedClassName());
      treeNode.setAttrNames(attrNames);
      tree.setTypeBindings(nodeBindings);
      mContainer.addControlBinding(tree);
    }
    return id;
  }

  private void addAttrIfNonEmpty(String attr, List<String> attrs)
  {
    if (attr!=null)
    {
      attrs.add(attr);      
    }
  }

  public void addDataControlUsage()
  {
    Context context = new Context(project.getWorkspace(), project);
    String defaultPackage = project.getProperty("defaultPackage");
    Application application = ApplicationManager.findOrCreateApplication(context, defaultPackage + ".DataBindings");
    ApplicationManager.findOrCreateDataControlInApplication(application, generatorModel.getDataControl());
    log.info("Added usage for " + generatorModel.getDataControlName() + " data control to DataBindings.cpx");
    
    // quick hack to generatee dc usages for all bean data controls
    List<DataControl> dataControls = DataControlManager.getInstance().getAllDataControls();
    for (DataControl dc: dataControls)
    {
      if (dc.getRealDataControl() instanceof JUDTAdapterDataControl)
      {
        JUDTAdapterDataControl adc = (JUDTAdapterDataControl) dc.getRealDataControl();
        String implClass = adc.getDef().getClass().getName();
        if (implClass.endsWith("BeanDCDefinition") || implClass.endsWith("StatefulIteratorBeanDcDefinition"))
        {
          // check whether beanClass extends from EntityCrudService
          ApplicationManager.findOrCreateDataControlInApplication(application, adc);
        }
      }
    }
  }

  public void createDataSynchIterator(UIDataObjectInfo uiDataObjectInfo)
    throws PropertyVetoException
  {
    //    <accessorIterator MasterBinding="DepartmentServiceIterator" Binds="dataSynchManager" RangeSize="25"
    //                      DataControl="DepartmentService"
    //                      BeanClass="oracle.ateam.sample.mobile.persistence.service.DataSynchManager"
    //                      id="dataSynchManagerIterator"/>
    String iterName = "dataSynchManagerIterator";
    if (mContainer.findIteratorBinding(iterName) != null)
    {
      return;
    }
    AccessorIterator itb = (AccessorIterator) mContainer.createIteratorBinding(DCDefBase.PNAME_AccessorIterator);
    itb.setId(iterName);
    itb.setMasterIteratorBinding(dataControlName + "Iterator");
    itb.setAttribute("Binds", "dataSynchManager");
    itb.setBeanClass("oracle.ateam.sample.mobile.persistence.service.DataSynchManager");
    itb.setDataControlName(dataControlName);
    itb.setRangeSize(25);
    mContainer.addIterator(itb);
  }

  public String createValueBinding(String id, String iterName, String attrName)
    throws PropertyVetoException
  {
    return createValueBinding(id,iterName, attrName, true);
  }

  public String createValueBinding(String id, String iterName, String attrName, boolean returnId)
    throws PropertyVetoException
  {
    if (mContainer.findControlBinding(id) != null)
    {
      return returnId ? id : "";
    }
    CtrlValue binding = (CtrlValue) mContainer.createControlBinding(DCDefBase.PNAME_TextField, null);
    binding.setId(id);
    binding.setIterBindingName(iterName);
    binding.addAttrName(attrName);
    mContainer.addControlBinding(binding);
    return returnId ? id : "";
  }

  public void createActionBinding(String id, String iterName, boolean updateModel, int action)
    throws PropertyVetoException
  {
    //    <action IterBinding="departmentsIterator" id="Create" RequiresUpdateModel="true" Action="createRow"/>
    if (mContainer.findControlBinding(id) != null)
    {
      return;
    }
    CtrlAction binding = (CtrlAction) mContainer.createControlBinding(DCDefBase.PNAME_Action, null);
    binding.setId(id);
    binding.setAction(action);
    binding.setRequiresUpdateModel(updateModel);
    binding.setIterBindingName(iterName);
    mContainer.addControlBinding(binding);
  }

  public void createQuickSearchBindings(String dataObjectName)
    throws PropertyVetoException
  {
    //    <variableIterator id="variables">
    //      <variable Type="java.lang.String" Name="find_searchValue" IsQueriable="false"/>
    //    </variableIterator>
    createVariable("find_searchValue", "java.lang.String", null);
    //    <methodAction id="find" RequiresUpdateModel="true" Action="invokeMethod" MethodName="find"
    //                  IsViewObjectMethod="false" DataControl="DepartmentService"
    //                  InstanceName="data.DepartmentService.dataProvider">
    //      <NamedData NDName="searchValue" NDType="java.lang.String" NDValue="${bindings.find_searchValue}"/>
    //    </methodAction>
    String methodName = "find"+dataObjectName;
    createMethodBinding(methodName, methodName, true,
                        createNamedDataList("searchValue", "java.lang.String", "${bindings.find_searchValue}"));

    //    <attributeValues IterBinding="variables" id="searchValue">
    //      <AttrNames>
    //        <Item Value="find_searchValue"/>
    //      </AttrNames>
    //    </attributeValues>
    createValueBinding("searchValue", "variables", "find_searchValue");
  }

  public String createSynchronizeBinding()
    throws PropertyVetoException
  {
    // if we set type to "boolean", it is still generated as java.lang.Boolean, so we changed the typ
    // on the synchronize method to java.lang.Boolean
//    String methodName = "synchronize"+dataObject.getRootDataObject().getName();
    String methodName = "synchronize";
    createMethodBinding(methodName, methodName, true,
                        createNamedDataList("inBackground", "java.lang.Boolean", "true"));
    return methodName;
  }

    public String createDoRemoteFindAllBinding(String dataObjectName)
      throws PropertyVetoException
    {
//      <methodAction id="doRemoteFindAll" RequiresUpdateModel="true" Action="invokeMethod" MethodName="doRemoteFindAll"
//                    IsViewObjectMethod="false" DataControl="DepartmentService"
//                    InstanceName="data.DepartmentService.dataProvider"/>
      String methodName ="findAll"+dataObjectName+"Remote";
      createMethodBinding(methodName, methodName, true,null);
      return methodName;
    }


  public String createMergeEntityBinding(UIDataObjectInfo dataObject)
    throws PropertyVetoException
  {
//    <NamedData NDName="entity" NDValue="#{bindings.departmentsIterator.currentRow.dataProvider}"
//               NDType="oracle.ateam.sample.mobile.persistence.model.Entity"/>
    // must pass in current row of top-level dataObject!
    UIDataObjectInfo topDataObject = dataObject;
    while (topDataObject.getParent()!=null)
    {
      topDataObject = topDataObject.getParent();
    }
    String methodName = "save"+topDataObject.getName();
    ArrayList<JboNamedData> params =  createNamedDataList("entity", topDataObject.getFullyQualifiedClassName()
              , "#{bindings."+topDataObject.getAccessorName()+"Iterator.currentRow.dataProvider}");
    createMethodBinding(methodName,methodName,true,params);
    return methodName;
  }

  public void createMethodBinding(String id, String methodName, boolean updateModel, ArrayList<JboNamedData> params)
    throws PropertyVetoException
  {
    if (mContainer.findControlBinding(id) != null)
    {
      return;
    }
    CtrlMethodAction binding = (CtrlMethodAction) mContainer.createControlBinding(DCDefBase.PNAME_MethodAction, null);
    binding.setId(id);
    binding.setAction(999);
    binding.setRequiresUpdateModel(updateModel);
    binding.setMethodName(methodName);
    binding.setDataControlName(dataControlName);
    binding.setInstanceName("data." + dataControlName + ".dataProvider");
    binding.setIsViewObjectMethod(false);
    binding.setParameters(params);
    mContainer.addControlBinding(binding);
  }

  /**
   * Convenience method that returns the list right away
   * Can be used when you have only one Named Data element
   */
  protected ArrayList<JboNamedData> createNamedDataList(String name, String type, String value)
  {
    ArrayList<JboNamedData> list = new ArrayList<JboNamedData>(1);
    list.add(createNamedData(name, type, value));
    return list;
  }

  protected JboNamedData createNamedData(String name, String type, String value)
  {
    JboNamedData nd = new JboNamedData();
    nd.setName(name);
    nd.setType(type);
    if (value != null && !"".equals(value))
    {
      nd.setValue(value);
    }
    return nd;
  }

  public void createVariable(String name, String type, String defaultValue)
  {
    VariablesIterator itb = (VariablesIterator) mContainer.findIteratorBinding(JUTags.variables);
    if (itb == null)
    {
      itb = (VariablesIterator) mContainer.createIteratorBinding(JUTags.PNAME_variableIterator);
      mContainer.addIterator(itb);
    }
    if (!mContainer.containsKey(name))
    {
      Variable var = mContainer.createVariable();
      var.setName(name);
      var.setType(type);
      if (defaultValue != null)
      {
        var.setDefaultValue(defaultValue);
      }
      mContainer.addVariable(var);
    }
  }

}
