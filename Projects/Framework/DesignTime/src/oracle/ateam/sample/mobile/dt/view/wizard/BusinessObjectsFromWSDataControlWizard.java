package oracle.ateam.sample.mobile.dt.view.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import java.io.IOException;

import oracle.ateam.sample.mobile.dt.controller.BusinessObjectGenerator;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.view.uipanel.AttributesPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.CRUDMethodParametersPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.CRUDMethodsPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.CRUDMethodsRESTPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.DataObjectsPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.GeneratorSettingsPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.ParentChildAccessorsPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.SecurityWarningPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.SelectDataControlPanel;

import oracle.bali.ewt.wizard.WizardDialog;

import oracle.ide.Ide;
import oracle.ide.dialogs.WizardLauncher;
import oracle.ide.model.Project;
import oracle.ide.panels.ApplyEvent;
import oracle.ide.panels.CommitListener;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;
import oracle.ide.util.Namespace;
import oracle.ide.wizard.FSM;
import oracle.ide.wizard.FSMBuilder;
import oracle.ide.wizard.FSMInvalidException;
import oracle.ide.wizard.FSMWizard;
import oracle.ide.wizard.Step;
import oracle.ide.wizard.WelcomePanel;
import oracle.ide.wizard.Wizard;

import oracle.javatools.dialogs.DialogUtil;


public class BusinessObjectsFromWSDataControlWizard extends Wizard
{

  private static final String DEFAULT_PACKAGE_PROPERTY = "defaultPackage";

  public static final String MODEL_KEY = "model";
  static final String STATE_WELCOME = "welcome";
  static final String STATE_SECURITY_WARNING = "secWarning";
  static final String STATE_SELECT_DC = "selectDc";
  static final String STATE_DATA_OBJECTS = "dataObjects";
  static final String STATE_ATTRIBUTES = "attributes";
  static final String STATE_PERSISTENCE_MAPPING= "pmapping";
  static final String STATE_CRUD_METHOD= "crudMethods";
  static final String STATE_CRUD_METHOD_PARAMS = "crudMethodParams";
  static final String STATE_OPTIONS = "options";

//  private String wizardTitle = "Create Mobile Web Service Proxy and Persistence Provider";
  private String wizardTitle = "MAF Business Objects From Web Service Data Control";
  private String logTitle = wizardTitle+ " Generator";
  
  public boolean runWizard(Component parent, final Project project)
  {

    FSMBuilder builder = new FSMBuilder();

    // Add all the pages to the finite state machine builder.
    ADD_PAGES_TO_BUILDER:
    {
      Step welcomeStep =
        WelcomePanel.newStep(wizardTitle, "Welcome to the "+wizardTitle+" Wizard",
         "This wizard helps you to generate Java data objects and service objects that can be used to read and write data from a web service, as well as from the on-device SQLite database, allowing you to use your mobile application in off-line mode. A SQL DDL script to auto-create the on-device SQLite database is also generated. To build the MAF user interface, you can create a data control from the generated service objects. To enable this option, you must select a MAF project or a file within such a project in the Application Navigator."                    
//                             "This wizard helps you to create 'data object' Java classes that can act as a proxy to a remote web service and " +
//        " 'service object' Java classes that use the proxy to make a request to and retrieve a response from the web service. The service object " +
//        " class also takes care of storing the data in  the on-device SQLite database allowing the mobile application to work in off-line mode as well." 
          +"\n\nClick Next to continue.", "Bla bla", null); // No help topic.
          welcomeStep.setStepLabel(wizardTitle);
          welcomeStep.setPageTitle(wizardTitle);
        builder.newStartState(welcomeStep, STATE_SECURITY_WARNING);

        Step step = new Step("Security Warning", SecurityWarningPanel.class, null);
        builder.newState(STATE_SECURITY_WARNING, step, STATE_SELECT_DC,false);

      step = new Step("Select Data Control", SelectDataControlPanel.class, null);
      builder.newState(STATE_SELECT_DC, step, STATE_DATA_OBJECTS,false);

      step = new Step("Select Data Objects", DataObjectsPanel.class, null);
      builder.newState(STATE_DATA_OBJECTS, step, STATE_ATTRIBUTES,false);

      step = new Step("Data Object Attributes", AttributesPanel.class, null);
      builder.newState(STATE_ATTRIBUTES, step, STATE_PERSISTENCE_MAPPING,false);

      step = new Step("Parent-Child Accessors", ParentChildAccessorsPanel.class, null);
      builder.newState(STATE_PERSISTENCE_MAPPING, step, STATE_CRUD_METHOD,false);

      step = new Step("CRUD Methods", CRUDMethodsPanel.class, null);
      builder.newState(STATE_CRUD_METHOD, step, STATE_CRUD_METHOD_PARAMS,false);

      step = new Step("CRUD Method Parameters", CRUDMethodParametersPanel.class, null);
      builder.newState(STATE_CRUD_METHOD_PARAMS, step, STATE_OPTIONS,false);

      step = new Step("Generator Settings", GeneratorSettingsPanel.class, null);
      builder.newFinalState(STATE_OPTIONS, step);

    }

    try
    {
      FSM stateMachine = builder.getFSM();
      Namespace ns = new Namespace();
      String defaultPackage = Ide.getActiveProject().getProperty(DEFAULT_PACKAGE_PROPERTY);
      BusinessObjectGeneratorModel model = new BusinessObjectGeneratorModel(defaultPackage);
      model.setLogTitle(wizardTitle);
      ns.put(MODEL_KEY, model);

      FSMWizard wizard = new FSMWizard(stateMachine, ns);

      // Initialize the wizard's style.
      INITIALIZE_WIZARD:
      {
        wizard.setWizardTitle(wizardTitle);
        wizard.setWelcomePageAdded(true);
        wizard.setFinishPageAdded(true);
        wizard.setShowStepNumber(true);
        //                ImageIcon imageicon =
        //                    new ImageIcon(getClass().getResource("genericwiz.gif"));
        //                wizard.updateImage(imageicon.getImage());
      }

      // Create a wizard dialog with the correct parent dialog or frame.
      WizardDialog wd;
      CREATE_WIZARD_DIALOG:
      {
        Dialog dialog = DialogUtil.getAncestorDialog(parent);
        if (parent != null)
        {
          wd = wizard.getDialog(dialog);
        }
        else
        {
          Frame f = DialogUtil.getAncestorFrame(parent);
          wd = wizard.getDialog(f);
        }
      }


      wizard.addCommitListener(new CommitListener()
      {
        public void checkCommit(ApplyEvent applyEvent)
        {
        }

        public void commit(ApplyEvent applyEvent)
          throws TraversalException
        {
          BusinessObjectsFromWSDataControlWizard.this.commit(applyEvent.getTraversableContext(), project);
        }

        public void rollback(ApplyEvent applyEvent)
        {
        }

        public void cancel(ApplyEvent applyEvent)
        {
        }
      });

      return WizardLauncher.runDialog(wd);

    }
    catch (FSMInvalidException ie)
    {
      ie.printStackTrace();
      return false;
    }

  }

  private void commit(TraversableContext context, Project project)
    throws TraversalException
  {
    try
    {
      BusinessObjectGeneratorModel model = (BusinessObjectGeneratorModel) context.get(MODEL_KEY);
      model.setLogTitle(logTitle);
      BusinessObjectGenerator generator = new BusinessObjectGenerator(project,model);
      generator.run();
    }
    catch (IOException ioe)
    {
      throw new TraversalException(ioe.getMessage());
    }
  }

  @Override
  public boolean isAvailable(oracle.ide.Context context)
  {
    return true;
  }

  @Override
  public String getShortLabel()
  {
    return wizardTitle;
  }

  @Override
  public boolean invoke(oracle.ide.Context context)
  {
    runWizard(Ide.getMainWindow(), context.getProject());
    return true;
  }
}
