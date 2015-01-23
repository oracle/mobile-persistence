/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
  package oracle.ateam.sample.mobile.dt.controller;

import java.net.URL;

import oracle.adfdt.controller.adfc.source.activity.ActivityType;
import oracle.adfdt.controller.adfc.source.controlflow.ControlFlowCase;
import oracle.adfdt.controller.adfc.source.controlflow.ControlFlowRule;
import oracle.adfdt.controller.adfc.source.controlflow.FromOutcome;
import oracle.adfdt.controller.adfc.source.controlflow.XxxActivityId;
import oracle.adfdt.controller.adfc.source.model.XmlConstants;
import oracle.adfdt.controller.adfc.source.navigator.AdfcConfigNode;
import oracle.adfdt.controller.adfc.source.taskflow.BoundedTaskFlow;
import oracle.adfdt.controller.adfc.source.taskflow.UnboundedTaskFlow;
import oracle.adfdt.controller.adfc.source.view.Page;
import oracle.adfdt.controller.adfc.source.view.View;
import oracle.adfdt.controller.common.source.navigator.ExtendedNodeListenerManager;
import oracle.adfdt.controller.common.util.XmlUtils;

import oracle.adfdtinternal.controller.mobile.addin.ExtensionResources;
import oracle.adfdtinternal.controller.mobile.behavior.AdfMobileTaskFlowDTBehavior;
import oracle.adfdtinternal.controller.mobile.interactions.AdfMobileDTInteractions;
import oracle.adfdtinternal.controller.mobile.navigator.AdfcConfigNodeUtils;

import oracle.ateam.sample.mobile.dt.model.TaskFlowModel;

import oracle.bali.xml.addin.XMLSourceNode;
import oracle.bali.xml.dom.XmlDeclarationInfo;
import oracle.bali.xml.model.AbstractModel;
import oracle.bali.xml.model.XmlCommitException;
import oracle.bali.xml.model.XmlContext;
import oracle.bali.xml.model.task.FixedNameTransactionTask;

import oracle.ide.Context;
import oracle.ide.model.Node;
import oracle.ide.model.Project;
import oracle.ide.net.URLFactory;
import oracle.ide.net.URLFileSystem;
import oracle.ide.panels.TraversalException;
import oracle.ide.util.IdeUtil;

import oracle.jdeveloper.builder.SourceModel;
import oracle.jdeveloper.builder.file.FileBuilderModel;

import org.w3c.dom.Element;


// inspired by AdfMobileBoundedTaskFlowWizard may be extend TaskFlowWizardModel  instead???
public class TaskFlowGenerator
  extends FileBuilderModel
{
  public static final String TASK_FLOW_MODEL = "taskFlowModel";
  private TaskFlowModel taskFlowModel;
  private Node taskFlowNode;

  public TaskFlowGenerator(Context context)
  {
    super(context, FILENAME_SUFFIX);

    mContext = context;
  }


  @Override
  public String getBuilderType()
  {
    return BUILDER_TYPE;
  }

  @Override
  public String getFileType()
  {
    return FILE_TYPE;
  }

  /**
   * Retrieves the task-flow file name and path to use for the new task-flow file to be created.
   * @param ctx context
   */
  @Override
  protected void initializeFromContext(final Context ctx)
  {
    this.taskFlowModel = (TaskFlowModel) ctx.getProperty(TASK_FLOW_MODEL);    
    super.initializeFromContext(ctx);

    Project project = getProject();

    URL defaultTaskFlowLocation = AdfMobileTaskFlowDTBehavior.getInstance().getDefaultTaskFlowLocation(project);
    String loc = defaultTaskFlowLocation.toString()+taskFlowModel.getName()+"/";
    defaultTaskFlowLocation = URLFactory.newURL(loc);
    setDirectory(defaultTaskFlowLocation);
//    URL directoryUrl = getDirectory();
//    if (!URLFileSystem.isBaseURLFor(defaultTaskFlowLocation, directoryUrl))
//    {
//      setDirectory(defaultTaskFlowLocation);
//    }
    getSourceModel().generateDefaultFileName(taskFlowModel.getName()+DEFAULT_FILENAME_BASE);
  }

  /**
   * Validates that the new file URL is relative to the project's html root.
   *
   * @param newFileUrl           The new file URL.
   * @return                     The validated new file URL.
   * @throws TraversalException  If the new file URL is not relative to the project's html root.
   */
  @Override
  public URL validateNewFileURL(URL newFileUrl)
    throws TraversalException
  {
    // Delegate to the superclass.
    newFileUrl = super.validateNewFileURL(newFileUrl);

    return newFileUrl;
  }

  @Override
  protected SourceModel createSourceModel()
  {
    return new FileSourceModel()
    {
      public Class getNodeType()
      {
        return AdfcConfigNode.class;
      }

      public int getDefaultNameStartIndex()
      {
        return -1;
      }

      public void generateDefaultFileName(String baseName)
      {
        super.generateDefaultFileName(baseName);
      }

      public String getFileName()
      {
        String fileName = super.getFileName();
        return fileName;
      }
    };
  }

  @Override
  protected String getBaseName()
  {
    String projectName = URLFileSystem.getName(getProject().getURL());
//    return projectName + DEFAULT_FILENAME_BASE;
    return taskFlowModel.getName() + DEFAULT_FILENAME_BASE;
  }

// This was a test to generate TF without extendung FileBuilderModel. Works but does not add
// file directl;y to project, and does not open TF diagram wich is nice during demos
//  public void run()
//  {
//    String folderName = ((TaskFlowModel) mContext.getProperty(TASK_FLOW_MODEL)).getName();    
//    URL tfurl = FileUtils.getWebURL(mContext.getProject(), folderName,folderName+DEFAULT_FILENAME_BASE+".xml");
//    try
//    {
//      Node tdNode = NodeFactory.findOrCreate(AdfcConfigNode.class, tfurl);
//      buildFile(tdNode);
//    }
//    catch (IllegalAccessException e)
//    {
//    }
//    catch (InstantiationException e)
//    {
//    }
//  }

  @Override
  protected boolean buildFile(Node ideNode)
  {
    if (super.buildFile(ideNode))
    {
      setTaskFlowNode(ideNode);
      Context localContext = new Context(mContext);
      localContext.setNode(ideNode);
      XmlContext xmlContext = XMLSourceNode.getXmlContext(localContext);
      AbstractModel xmlModel = xmlContext.getModel();

      final String taskFlowId = URLFileSystem.getName(ideNode.getURL());
      // Add xml declaration at the top of the file.
      xmlModel.getDomModel().setXmlDeclarationInfo(new XmlDeclarationInfo(XmlDeclarationInfo.VERSION_1_0,
                                                                          IdeUtil.getIdeIanaEncoding(), null));
      new FixedNameTransactionTask(TRANSACTION_NAME)
      {
        protected void performTask(AbstractModel model)
          throws XmlCommitException
        {
          Element unboundedTaskFlowElement =
            XmlUtils.createElement(model, XmlConstants.ADFC_MOBILE_CONFIG, XmlConstants.NAMESPACE);

          XmlUtils.addChildElement(model, null, unboundedTaskFlowElement);

          UnboundedTaskFlow unboundedTaskFlow = new UnboundedTaskFlow(model, unboundedTaskFlowElement);
          unboundedTaskFlow.setNamespace();
          unboundedTaskFlow.setVersion();
          BoundedTaskFlow boundedTaskFlow = unboundedTaskFlow.createTaskFlowDefinition();
          boundedTaskFlow.setId(taskFlowId);
          unboundedTaskFlow.addTaskFlowDefinition(boundedTaskFlow);

          TaskFlowModel taskFlowModel = (TaskFlowModel) mContext.getProperty(TASK_FLOW_MODEL);
          boolean defaultActivitySet = false;
          for (TaskFlowModel.ViewActivity viewActivity : taskFlowModel.getViewActivities())
          {
            View view = (View) boundedTaskFlow.createActivity(ActivityType.VIEW);
            view.setId(viewActivity.getId());
            Page page = view.createPage();
            page.setValue(viewActivity.getPage());
            view.setPage(page);
            boundedTaskFlow.addActivity(view);
            if (!defaultActivitySet)
            {
              defaultActivitySet = true;
              XxxActivityId activityId = boundedTaskFlow.createDefaultActivity();
              activityId.setValue(view.getId());
              boundedTaskFlow.setDefaultActivity(activityId);
            }
          }

          for (TaskFlowModel.ControlFlowRule rule : taskFlowModel.getControlFlowRules())
          {
            ControlFlowRule navRule = boundedTaskFlow.createControlFlowRule();
            boundedTaskFlow.addControlFlowRule(navRule);
            XxxActivityId fromActivityId = navRule.createFromActivityId();
            fromActivityId.setValue(rule.getFromId());
            navRule.setFromActivityId(fromActivityId);
            ControlFlowCase controlFlowCase = navRule.createControlFlowCase();
            navRule.addControlFlowCase(controlFlowCase);
            FromOutcome fromOutcome = controlFlowCase.createFromOutcome();
            controlFlowCase.setFromOutcome(fromOutcome);
            fromOutcome.setValue(rule.getOutcome());
            XxxActivityId toActivityId = controlFlowCase.createToActivityId();
            toActivityId.setValue(rule.getToId());
            controlFlowCase.setToActivityId(toActivityId);
          }
        }
      }.run(xmlModel);

      AdfcConfigNodeUtils.findOrCreateDefaultAdfcConfigNode(mContext.getProject());

      AdfMobileDTInteractions adfMobileDTInteractions = AdfMobileDTInteractions.getInstance();
      adfMobileDTInteractions.taskFlowCreated(localContext, ideNode.getURL(), taskFlowId);

      mContext.setProperty(NEW_NODE_KEY, ideNode);

      // Notify any extended node listeners that a new AdfcConfigNode is created.
      ExtendedNodeListenerManager.fireNodeCreated(getProject(), ideNode);

      return true;
    }

    return false;
  }

  public static final String NEW_NODE_KEY = "new-adfc-config-node"; // NOTRANS
  private static final String TRANSACTION_NAME = "createADFMobileTaskFlow";
  //         AdfcSourceArb.getString(AdfcSourceArb.TRANSACTION_NAME_INITIALIZE_BOUNDED_TASK_FLOW);

  private static final String BUILDER_TYPE = "ADF Mobile Task Flow"; // NOTRANS

  private static final String DEFAULT_FILENAME_BASE = "-task-flow"; // NOTRANS

  private static final String FILENAME_SUFFIX = ".xml"; // NOTRANS

  private static final String FILE_TYPE = ExtensionResources.get(ExtensionResources.WIZARD_FILE_TYPE);

  private Context mContext;

  public void setTaskFlowNode(Node taskFlowNode)
  {
    this.taskFlowNode = taskFlowNode;
  }

  public Node getTaskFlowNode()
  {
    return taskFlowNode;
  }
}
