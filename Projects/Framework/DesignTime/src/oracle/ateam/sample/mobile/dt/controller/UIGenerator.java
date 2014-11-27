/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 27-nov-2014   Steven Davelaar
 1.1           Fixed path of DataSynchFeature.jar (changed after renaming to AMPA)
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.controller;


import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.adfdt.controller.adfc.source.managedbean.ManagedBean;
import oracle.adfdt.controller.adfc.source.managedbean.ManagedBeanName;
import oracle.adfdt.controller.adfc.source.managedbean.ManagedBeanScope;
import oracle.adfdt.controller.adfc.source.model.ValueEntity;
import oracle.adfdt.controller.adfc.source.model.XmlConstants;
import oracle.adfdt.controller.adfc.source.navigator.AdfcConfigNode;
import oracle.adfdt.controller.adfc.source.single.AdfcSingleViewUtils;
import oracle.adfdt.controller.adfc.source.taskflow.TaskFlow;
import oracle.adfdt.model.objects.DataControl;

import oracle.adfdtinternal.controller.mobile.navigator.AdfcConfigNodeUtils;

import oracle.adfmf.common.ADFMobileConstants;
import oracle.adfmf.common.util.DeviceAccessUtils;
import oracle.adfmf.common.util.McAppUtils;
import oracle.adfmf.framework.dt.editor.FrameworkXmlEditorConstants;
import oracle.adfmf.framework.dt.editor.FrameworkXmlKeys;
import oracle.adfmf.framework.dt.editor.FrameworkXmlSourceNode;
import oracle.adfmf.framework.dt.editor.feature.FeatureXmlConstants;
import oracle.adfmf.framework.dt.editor.feature.FeatureXmlKeys;
import oracle.adfmf.framework.dt.editor.feature.FeatureXmlSourceNode;
import oracle.adfmf.framework.dt.ide.FeatureBuilderModel;

import oracle.ateam.sample.mobile.dt.model.TaskFlowModel;
import oracle.ateam.sample.mobile.dt.model.UIDataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.UIGeneratorModel;
import oracle.ateam.sample.mobile.dt.controller.velocity.VelocityInitializer;
import oracle.ateam.sample.mobile.dt.controller.velocity.VelocityTemplateProcessor;
import oracle.ateam.sample.mobile.dt.util.FileUtils;

import oracle.bali.xml.dom.position.DomPosition;
import oracle.bali.xml.dom.position.DomPositionFactory;
import oracle.bali.xml.gui.base.xmlComponent.XmlComponentModel;
import oracle.bali.xml.gui.jdev.JDevXmlContext;
import oracle.bali.xml.gui.jdev.xmlComponent.XmlPanelGui;
import oracle.bali.xml.model.AbstractModel;
import oracle.bali.xml.model.XmlCommitException;
import oracle.bali.xml.model.XmlContext;
import oracle.bali.xml.model.XmlModel;
import oracle.bali.xml.model.XmlView;
import oracle.bali.xml.model.task.FixedNameTransactionTask;

import oracle.ide.Context;
import oracle.ide.Ide;
import oracle.ide.model.Node;
import oracle.ide.model.NodeFactory;
import oracle.ide.model.Project;
import oracle.ide.net.URLFactory;
import oracle.ide.net.URLFileSystem;
import oracle.ide.net.URLPath;

import oracle.jdeveloper.library.ApplicationLibraryList;
import oracle.jdeveloper.library.JLibrary;
import oracle.jdeveloper.model.ApplicationLibraries;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class UIGenerator
{
  private static final String DATA_SYNCH_JAR_NAME = "DataSynchFeature.jar";
  private UIGeneratorModel model;
  private Project project;
  private GeneratorLogPage log = GeneratorLogPage.getPage("Mobile User Interface Generator");
  private VelocityTemplateProcessor processor;

  public UIGenerator(Project project, UIGeneratorModel model)
  {
    super();
    this.model = model;
    this.project = project;
  }

  public void run()
    throws IOException
  {
    log.initialize();
    log.info("ADF Mobile User Interface Generator started");

    String iconsZipFile = Ide.getOracleHomeDirectory()+"/jdev/extensions/oracle.ateam.mobile.persistence/icons/icons.zip";
    URL publicHtmlDir = McAppUtils.getProjectPublicHtmlDir(project);
    String targetDir = publicHtmlDir.getFile()+"images";
    FileUtils.extractZipFile(iconsZipFile, targetDir);

    VelocityInitializer vi = new VelocityInitializer();
    // MPS: Mobile Persistence Sample
    vi.initVelocity("MPS");
    processor = new VelocityTemplateProcessor(vi);
    PageDefGenerator pageDef = new PageDefGenerator(model, project, log);
    model.setPageDef(pageDef);
    for (UIDataObjectInfo doi: model.getDataControlVisitor().getRootAccessorBeans())
    {
      TaskFlowModel taskFlowModel = new TaskFlowModel(doi.getName());
      model.setTaskFlowModel(taskFlowModel);
      boolean created = createFeature(taskFlowModel.getName(), log);
      generatePages(doi, taskFlowModel);
      createTaskFlow(taskFlowModel, log, !created);        
    }

    addDataSynchFeatureReference();
    // need to add at end, therwise adfc-mobile-cinfog does not yet exist
    addGoToFeatureBean();
// Always generate UserCitextBean so we can use testUserName and testPassword managed properties
// before we have set up mobile security
//    if (model.getSecurityType()!=null)
//    {
      addUserContextBean();
//    }  
    // set iter bean

// No longer needed, is now a native feature in 12.1.3
//    DataControl dc = model.getDataControl().getRealDataControl();
//    String ateamBean = "oracle.ateam.sample.mobile.model.bean.StatefulIteratorBeanDcDefinition";
//    if (!ateamBean.equals(dc.getAttribute("ImplDef")))
//    {
//      dc.setAttribute("ImplDef", ateamBean);
//      log.info("ImplDef property of " + dc.getId() + " adapter data control set to " + ateamBean +
//               " to preserve current row across pages");
//    }
    addDataSynchJarIfNeeded();
// doesnt work yet
//    addNetworkStatusAccessPermission();
    log.info("ADF Mobile User Interface Generator finished succesfully");
  }

  private void addDataSynchJarIfNeeded()
  {
    ApplicationLibraries applicationLibraries = ApplicationLibraries.getInstance(project.getWorkspace());
    ApplicationLibraryList applicationLibraryList = applicationLibraries.getLibraryDefinitions();
    if (!applicationLibraryList.containsKey(DATA_SYNCH_JAR_NAME))
    {
//      JLibraryAdapter lib = new JLibraryAdapter(HashStructure.newInstance());
      JLibrary lib = applicationLibraryList.addLibrary(DATA_SYNCH_JAR_NAME);
      URLPath up = new URLPath();
      String directory = Ide.getOracleHomeDirectory();
      String urlpath = directory+"/jdev/extensions/oracle.ateam.mobile.persistence/"+DATA_SYNCH_JAR_NAME;
      up.add(URLFactory.newFileURL(urlpath));
      lib.setClassPath(up);
      lib.setName(DATA_SYNCH_JAR_NAME);    
      List<JLibrary> liblist = new ArrayList<JLibrary>();
      liblist.add(lib);
      applicationLibraries.setLibraryReferences(liblist);
      log.info("Added reusable feature jar "+DATA_SYNCH_JAR_NAME+" to application classpath");
    }
  }

  private void generatePages(UIDataObjectInfo doi, TaskFlowModel taskFlowModel)
  {
    model.setCurrentDataObject(doi);
    String folderName = taskFlowModel.getName();
    if (doi.isHasListPage() && !doi.isSamePage())
    {
      String pageId = doi.getName()+"List";
      String pagePath = "/"+folderName+"/"+pageId+".amx";
      taskFlowModel.addViewActivity(pageId,pagePath);
      generatePage(folderName, pageId + ".amx", "listPage.vm");        
    }
    if (doi.isHasFormPage() && (doi.isHasListPage() || !doi.isSamePage()))
    {
      String pageId = doi.getName();
      String pagePath = "/"+folderName+"/"+pageId+".amx";
      taskFlowModel.addViewActivity(pageId,pagePath);
      generatePage(folderName, pageId + ".amx", "formPage.vm");        
    }
    // recursive call for children
    for(UIDataObjectInfo child : doi.getChildren())
    {
      generatePages(child, taskFlowModel);
    }
  }

  private void generatePage(String folderName, String fileName, String template)
  {
    URL sourceURL = FileUtils.getWebURL(project, folderName, fileName);
    boolean exists = URLFileSystem.exists(sourceURL);
    if (!exists || model.isOverwritePages())
    {
      String verb = exists? " re-created ": " created ";
      String output = processor.processTemplate(model, template);
      if (output != null)
      {
        FileUtils.addFileToProject(sourceURL, output, null);
        log.info("Page " + fileName + verb + "in /public_html/" + folderName + " folder in " +
                 project.getShortLabel());
      }
      else
      {
        log.error("Could not create page " + fileName + ", error parsing "+template);
      }
    }
  }

    private void addNetworkStatusAccessPermission()
    {
//        <adfmf:deviceFeatureAccess>
//          <adfmf:deviceNetwork allowAccess="true" id="dn1"/>
//        </adfmf:deviceFeatureAccess>        
        FrameworkXmlSourceNode applicationXml = McAppUtils.findOrCreateApplicationXml(project.getWorkspace());
        JDevXmlContext jdevXmlContext = JDevXmlContext.getXmlContext(new Context(applicationXml));
        XmlView view = jdevXmlContext.getView(FrameworkXmlEditorConstants.APPLICATION_USAGE);
        XmlPanelGui panelGui = new XmlPanelGui(view);
        XmlComponentModel deviceFeatureAccessXmlModel =
          XmlComponentModel.createXmlComponentModel(FrameworkXmlKeys.XMLKEY_DEVICE_FEATURE_ACCESS, FrameworkXmlKeys.PANEL_ROOT_XMLKEY,
                                                    panelGui);
//        XmlComponentModel deviceNetworkXmlModel =
//          XmlComponentModel.createXmlComponentModel(FrameworkXmlKeys.XMLKEY_DEVICE_NETWORK_ACCESS, FrameworkXmlKeys.PANEL_ROOT_XMLKEY,
//                                                    panelGui);
//        deviceNetworkXmlModel.setParentXmlComponentModel(deviceFeatureAccessXmlModel);
//        XmlComponentModel deviceNetworkAllowAccessXmlModel =
//          XmlComponentModel.createXmlComponentModel(FrameworkXmlKeys.XMLKEY_DEVICE_NETWORK_ALLOW_ACCESS_ATTR, FrameworkXmlKeys.PANEL_ROOT_XMLKEY,
//                                                    panelGui);
//        deviceNetworkXmlModel.setParentXmlComponentModel(deviceNetworkAllowAccessXmlModel);
//        deviceNetworkAllowAccessXmlModel.updateModelValue("true");        

        org.w3c.dom.Node deviceFeatureAccessNode = deviceFeatureAccessXmlModel.getNode();
        final Document doc = view.getDocument();
        if (deviceFeatureAccessNode == null)
        {
//          final XmlComponentModel xmlComponentModel = getXmlComponentModel();
          final XmlComponentModel parentXmlComponentModel = deviceFeatureAccessXmlModel.getParentXmlComponentModel();
          org.w3c.dom.Node deviceAccessElemParentNode = null;

          if (parentXmlComponentModel != null)
          {
            deviceAccessElemParentNode = parentXmlComponentModel.getNode();
          }
          else
          {
            deviceAccessElemParentNode = doc.getFirstChild();
          }

          final DomPosition parentNodePosition =
            DomPositionFactory.createInsideOrAfterPosition((org.w3c.dom.Node) deviceAccessElemParentNode);
          final Element deviceFeatureAccessElement =
            doc.createElementNS(FeatureXmlConstants.NAMESPACE,
                                McAppUtils.getQualifiedName(FrameworkXmlEditorConstants.DEVICE_FEATURE_ACCESS_ELEMENT));

            try {
                deviceFeatureAccessNode = view.insertNode(deviceFeatureAccessElement, parentNodePosition);
            } catch (XmlCommitException e) {
            }

        }
//        DeviceAccessUtils.setDeviceFeatureAccess(deviceFeatureAccessNode, doc, FrameworkXmlEditorConstants.DEVICE_NETWORK_ELEMENT, FrameworkXmlEditorConstants.ACCESS_ATTR, "true");
        

    }    

  /**
   * Create feature in adfmf-feature.xml and add reference adfmf-application.xml.
   * Create the task flow used by the feature
   * @param doi
   */
  private boolean createFeature(String featureName, GeneratorLogPage log)
  {
    //  <adfmf:feature id="Departments" name="Departments">
    //    <adfmf:content id="Departments.1">
    //      <adfmf:amx file="Departments/departments-task-flow.xml#departments-task-flow">
    //        <adfmf:includes>
    //          <adfmf:include type="JavaScript" file="resources/js/lib.js" id="i1"/>
    //        </adfmf:includes>
    //      </adfmf:amx>
    //    </adfmf:content>
    //  </adfmf:feature>
    FeatureXmlSourceNode featureXml = McAppUtils.findOrCreateFeatureXml(project);
    Map<String, org.w3c.dom.Node> featureMap = getFeatureMap(featureXml.getURL());
    if (featureMap.containsKey(featureName))
    {
      log.warn("Feature " + featureName + " already exists");
      return false;
    }
    Context context = new Context(project.getWorkspace(), project);
    FeatureBuilderModel fmodel = new FeatureBuilderModel(context);
    fmodel.setAddFeatureRef(true);
    fmodel.setFeatureName(featureName);
    fmodel.setRaiseEditor(false);
    fmodel.setFileName(featureName);
//    fmodel.setDirectory(dirURL);
//    fmodel.setRelativeDirectory(featureName);
    fmodel.commitWizardState();
    
    // fdor some strange reason the featureBuilderModel creates a folder named after the feature suffixed
    // with null, so we remove that folder here
    URL publicHtmlDir = McAppUtils.getProjectPublicHtmlDir(project);
    URL dirURL = URLFactory.newDirURL(publicHtmlDir, featureName+"null");
    URLFileSystem.delete(dirURL);

    // refresh featureMap to get Node of newly added feature;
    featureMap = getFeatureMap(featureXml.getURL());

    JDevXmlContext jdevXmlContext = JDevXmlContext.getXmlContext(new Context(featureXml));
    XmlView view = jdevXmlContext.getView(FeatureXmlConstants.FEATURE_USAGE);
    XmlPanelGui panelGui = new XmlPanelGui(view);
    XmlComponentModel featureXmlModel =
      XmlComponentModel.createXmlComponentModel(FeatureXmlKeys.XMLKEY_FEATURE, FeatureXmlKeys.PANEL_ROOT_XMLKEY,
                                                panelGui);
    //      XmlKey navKey = ImmutableXmlKey.createElementKey("id",doi.getName());
    //      featureXmlModel.setNavigationXmlKey(navKey);
    // there should be a  more elegenat way to set the current feature node, but couldn't figure out
    // how to do this using nav key
    featureXmlModel.setNode(featureMap.get(featureName));

    if (model.isEnableSecurity())
    {
//In 2.0 release, the credential attribute has moved from adfmf-feature.xml to Connections.xml. Migration steps are listed below.
// -Remove the invalid attribute (credential) from adfmf-feature.xml
// - Enable Security on that feature by setting the securityEnabled attribute to true within the adfmf-feature.xml.
// - Add the authenticationMode in connections.xml for the associated connection. Ex: <authenticationMode value="remote"/>//      
// XmlComponentModel credentialsXmlModel =
//        XmlComponentModel.createXmlComponentModel(FeatureXmlKeys.XMLKEY_FEATURE_ATTR_CREDENTIALS, FeatureXmlKeys.PANEL_ROOT_XMLKEY,
//                                                  panelGui);
//      credentialsXmlModel.setParentXmlComponentModel(featureXmlModel);
//      credentialsXmlModel.updateModelValue(model.getSecurityType());      
   XmlComponentModel credentialsXmlModel =
        XmlComponentModel.createXmlComponentModel(FeatureXmlKeys.XMLKEY_FEATURE_ATTR_SECURITY_ENABLED, FeatureXmlKeys.PANEL_ROOT_XMLKEY,
                                                  panelGui);
      credentialsXmlModel.setParentXmlComponentModel(featureXmlModel);
      credentialsXmlModel.updateModelValue("true");      
    }

    XmlComponentModel contentXmlModel =
      XmlComponentModel.createXmlComponentModel(FeatureXmlKeys.XMLKEY_CONTENT, FeatureXmlKeys.PANEL_ROOT_XMLKEY,
                                                panelGui);
    contentXmlModel.setParentXmlComponentModel(featureXmlModel);
    XmlComponentModel amxXmlModel =
      XmlComponentModel.createXmlComponentModel(FeatureXmlKeys.XMLKEY_AMX_ATTR_FILE, FeatureXmlKeys.PANEL_ROOT_XMLKEY,
                                                panelGui);
    amxXmlModel.setParentXmlComponentModel(contentXmlModel);
    String taskFlowFile = featureName + "/" + featureName + "-task-flow.xml#" + featureName + "-task-flow";
    amxXmlModel.updateModelValue(taskFlowFile);

    log.info("New feature " + featureName + " added to adfmf-feature.xml in src/META-INF folder in " +
             project.getShortLabel());
    log.info("Reference to feature " + featureName + " added to adfmf-application.xml in .adf/META-INF folder in " +
             project.getWorkspace().getShortLabel());

    return true;
  }

  private void createTaskFlow(TaskFlowModel taskFlowModel, GeneratorLogPage log, boolean alreadyExists)
  {
    if (alreadyExists)
    {
      // TODO update existing tf!
      log.warn("Task flow " + taskFlowModel.getName() + "-task-flow.xml in public_html/" + taskFlowModel.getName() + " already exists, task flow NOT regenerated");
      return;  
    }
    Context context = new Context(project.getWorkspace(), project);
    context.setProperty(TaskFlowGenerator.TASK_FLOW_MODEL, taskFlowModel);
    TaskFlowGenerator tfmodel = new TaskFlowGenerator(context);
    tfmodel.commitWizardState();
    String verb = alreadyExists ? " updated " : " created ";
    log.info("Task flow " + taskFlowModel.getName() + "-task-flow.xml "+verb+"in public_html/" + taskFlowModel.getName() + " folder in " +
             project.getShortLabel());
  }

  public static Map<String, org.w3c.dom.Node> getFeatureMap(URL featureXmlUrl)
  {
    final Map<String, org.w3c.dom.Node> featureIdList = new HashMap<String, org.w3c.dom.Node>();
    Node node = null;
    if (URLFileSystem.exists(featureXmlUrl))
    {
      try
      {
        node = NodeFactory.findOrCreate(featureXmlUrl);
      }
      catch (Exception e)
      {
        node = null;
      }
    }
    if (node != null)
    {
      final Context ideContext = Context.newIdeContext(node);
      final XmlContext xmlContext = JDevXmlContext.getXmlContext(ideContext);
      final XmlModel model = xmlContext.getModel();

      model.acquireReadLock();

      try
      {
        final org.w3c.dom.Node rootNode = model.getDocument().getDocumentElement();
        final NodeList childNodes = rootNode.getChildNodes();
        if (childNodes != null)
        {
          for (int i = 0; i < childNodes.getLength(); ++i)
          {
            final org.w3c.dom.Node child = childNodes.item(i);
            if (child instanceof Element && FeatureXmlConstants.FEATURE_ELEMENT.equals(child.getLocalName()))
            {
              final String id = ((Element) child).getAttributeNS(null, FrameworkXmlEditorConstants.ID_ATTR);
              if (id != null && id.trim().length() > 0)
              {
                featureIdList.put(id, child);
              }
            }
          }
        }
      }
      finally
      {
        model.releaseReadLock();
      }
    }
    return featureIdList;
  }

  private void addGoToFeatureBean()
  {
    AdfcConfigNode node = AdfcConfigNodeUtils.findDefaultAdfcConfigNode(project);
    Context context = new Context(null, null, project, node);
    AbstractModel model = node.getXmlContext(context).getModel();
    new FixedNameTransactionTask("addfeatureBean")
    {
      protected void performTask(AbstractModel model)
        throws XmlCommitException
      {
        TaskFlow flow = AdfcSingleViewUtils.getSingleTaskFlow(model);
        //    <managed-bean id="__1">
        //      <managed-bean-name>GoToFeature</managed-bean-name>
        //      <managed-bean-class>oracle.ateam.sample.mobile.controller.bean.GoToFeatureBean</managed-bean-class>
        //      <managed-bean-scope>application</managed-bean-scope>
        //    </managed-bean>
        if (flow.getManagedBean("GoToFeature") == null)
        {
          ManagedBean bean = flow.createManagedBean();
          flow.addManagedBean(bean);
          ManagedBeanName beanName = bean.createName();
          beanName.setValue("GoToFeature");
          bean.setName(beanName);
          ValueEntity beanClass = bean.createManagedBeanClass();
          beanClass.setValue("oracle.ateam.sample.mobile.controller.bean.GoToFeatureBean");
          bean.setManagedBeanClass(beanClass);
          ManagedBeanScope beanScope = bean.createManagedBeanScope();
          beanScope.setValue("application");
          bean.setManagedBeanScope(beanScope);
          log.info("Added GoToFeature managed bean to adfc-mobile-config.xml");
        }
      }
    }.run(model);

    //    AbstractModel model = node.getModel();
  }

  private void addUserContextBean()
  {
    AdfcConfigNode node = AdfcConfigNodeUtils.findDefaultAdfcConfigNode(project);
    Context context = new Context(null, null, project, node);
    AbstractModel model = node.getXmlContext(context).getModel();
    new FixedNameTransactionTask("addUserContextBean")
    {
      protected void performTask(AbstractModel model)
        throws XmlCommitException
      {
        TaskFlow flow = AdfcSingleViewUtils.getSingleTaskFlow(model);
        if (flow.getManagedBean("UserContext") == null)
        {
          ManagedBean bean = flow.createManagedBean();
          flow.addManagedBean(bean);
          ManagedBeanName beanName = bean.createName();
          beanName.setValue("UserContext");
          bean.setName(beanName);
          ValueEntity beanClass = bean.createManagedBeanClass();
          beanClass.setValue("oracle.ateam.sample.mobile.controller.bean.UserContextBean");
          bean.setManagedBeanClass(beanClass);
          ManagedBeanScope beanScope = bean.createManagedBeanScope();
          beanScope.setValue("application");
          bean.setManagedBeanScope(beanScope);
          log.info("Added UserContext managed bean to adfc-mobile-config.xml");
        }
      }
    }.run(model);

    //    AbstractModel model = node.getModel();
  }

  private void addDataSynchFeatureReference()
  {
    // TODO add datasunchfeature to app xml
    //    <adfmf:featureReference id="oracle.ateam.sample.mobile.datasynch" showOnNavigationBar="false"
    //                            showOnSpringboard="false"/>
    // we cannot use MCAppUtils.addFeatureReference because we also need to set "show on nav bar" and
    // "show on spring board" to false
    final String featureId = "oracle.ateam.sample.mobile.datasynch";
    final URL appXmlUrl = McAppUtils.getApplicationXmlURL(project.getWorkspace());
    if (URLFileSystem.exists(appXmlUrl))
    {
      Node node = null;
      try
      {
        node = NodeFactory.findOrCreate(FrameworkXmlSourceNode.class, appXmlUrl);
        if (node != null)
        {
          final Context ideContext = Context.newIdeContext(node);
          final XmlContext xmlContext = JDevXmlContext.getXmlContext(ideContext);
          final XmlModel model = xmlContext.getModel();

          new FixedNameTransactionTask("Add feature reference id")
          {
            protected void performTask(AbstractModel model)
              throws XmlCommitException
            {
              final Document doc = model.getDocument();
              final Element root = doc.getDocumentElement();

              final NodeList featureRefs =
                root.getElementsByTagNameNS(FrameworkXmlEditorConstants.NAMESPACE, FrameworkXmlEditorConstants.FEATUREREF_ELEMENT);
              final int count = (featureRefs != null? featureRefs.getLength(): 0);
              for (int i = 0; i < count; ++i)
              {
                final Element element = (Element) featureRefs.item(i);
                final String id = element.getAttributeNS(null, FeatureXmlConstants.ID_ATTR);
                if (id != null && id.equals(featureId))
                {
                  return;
                }
              }
              // creates feature reference
              final Element featureRef =
                doc.createElementNS(FrameworkXmlEditorConstants.NAMESPACE, McAppUtils.getQualifiedName(FrameworkXmlEditorConstants.FEATUREREF_ELEMENT));
              featureRef.setAttributeNS(null, FrameworkXmlEditorConstants.ID_ATTR, featureId);
              featureRef.setAttributeNS(null, FrameworkXmlEditorConstants.SHOW_ON_NB_ATTR, "false");
              featureRef.setAttributeNS(null, FrameworkXmlEditorConstants.SHOW_ON_SB_ATTR, "false");
              model.insertNode(featureRef, DomPositionFactory.inside(root), false);
              log.info("Added reusable DataSync feature reference to adfmf-application.xml");
            }
          }.run(model);
        }
      }
      catch (Exception e)
      {
        log.error("Error adding DataSync feature reference to adfmf-application.xml: " + e.getMessage());
      }
    }
  }
}
