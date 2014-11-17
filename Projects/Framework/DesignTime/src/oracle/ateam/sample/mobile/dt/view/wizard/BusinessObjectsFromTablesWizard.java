/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import java.io.IOException;

import javax.swing.ImageIcon;

import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

import oracle.ateam.sample.mobile.dt.controller.BusinessObjectGenerator;

import oracle.ateam.sample.mobile.dt.view.uipanel.AttributesPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.DBDataObjectsPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.GeneratorSettingsPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.SelectDBConnectionPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.SelectDBTablesPanel;

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

public class BusinessObjectsFromTablesWizard extends Wizard
{

  private static final String DEFAULT_PACKAGE_PROPERTY = "defaultPackage";

  static final String MODEL_KEY = "model";
  static final String STATE_WELCOME = "welcome";
  static final String STATE_OPTIONS = "options";
  static final String STATE_DB_CONNECTION = "dbconn";
  static final String STATE_SELECT_TABLES = "selectTables";
  static final String STATE_DATA_OBJECTS = "dataObjects";
  static final String STATE_ATTRIBUTES = "attributes";
  
  private String wizardTitle = "MAF Business Objects From Tables";
  private String logTitle = wizardTitle+ " Generator";

  public boolean runWizard(Component parent, final Project project)
  {

    FSMBuilder builder = new FSMBuilder();

    // Add all the pages to the finite state machine builder.
    ADD_PAGES_TO_BUILDER:
    {
      Step welcomeStep =
        WelcomePanel.newStep(wizardTitle, "Welcome to the "+wizardTitle+" Wizard",
                             "This wizard helps you to create Java data objects and service objects that can be used to read and write data from the on-device SQLite database. A SQL DDL script to auto-create the on-device SQLite database is also generated. To build the MAF user interface, you can create a data control from the generated service objects. To enable this option, you must select a MAF project or a file within such a project in the Application Navigator.\n\n" +
          "Click Next to continue.", "Bla bla", null); // No help topic.
          welcomeStep.setStepLabel(wizardTitle);
          welcomeStep.setPageTitle(wizardTitle);
      builder.newStartState(welcomeStep, STATE_DB_CONNECTION);

      Step step = new Step("Select Database Connection", SelectDBConnectionPanel.class, null);
      builder.newState(STATE_DB_CONNECTION, step, STATE_SELECT_TABLES,false);

      step = new Step("Select Database Tables", SelectDBTablesPanel.class, null);
      builder.newState(STATE_SELECT_TABLES, step, STATE_DATA_OBJECTS,false);

      step = new Step("Data Objects", DBDataObjectsPanel.class, null);
      builder.newState(STATE_DATA_OBJECTS, step, STATE_ATTRIBUTES);

      step = new Step("Data Object Attributes", AttributesPanel.class, null);
      builder.newState(STATE_ATTRIBUTES, step, STATE_OPTIONS);

      step = new Step("Generator Settings", GeneratorSettingsPanel.class, null);
      builder.newFinalState(STATE_OPTIONS, step);

    }

    try
    {
      FSM stateMachine = builder.getFSM();
      Namespace ns = new Namespace();
      String defaultPackage = Ide.getActiveProject().getProperty(DEFAULT_PACKAGE_PROPERTY);
      BusinessObjectGeneratorModel model = new BusinessObjectGeneratorModel(defaultPackage);
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
          BusinessObjectsFromTablesWizard.this.commit(applyEvent.getTraversableContext(), project);
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
