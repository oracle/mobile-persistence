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

import oracle.ateam.sample.mobile.dt.controller.UIGenerator;
import oracle.ateam.sample.mobile.dt.model.UIGeneratorModel;
import oracle.ateam.sample.mobile.dt.view.uipanel.GeneratorSettingsPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.SelectUIDataControlPanel;

import oracle.ateam.sample.mobile.dt.view.uipanel.UIGeneratorSettingsPanel;

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


public class UIGeneratorWizard extends Wizard
{


  static public final String MODEL_KEY = "model";
  static final String STATE_WELCOME = "welcome";
  static final String STATE_OPTIONS = "options";
  static final String STATE_DATA_CONTROL = "dataControl";
  
  private String wizardTitle = "MAF User Interface Generator";

  public boolean runWizard(Component parent, final Project project)
  {

    FSMBuilder builder = new FSMBuilder();

    // Add all the pages to the finite state machine builder.
    ADD_PAGES_TO_BUILDER:
    {
      Step welcomeStep =
        WelcomePanel.newStep(wizardTitle, "Welcome to the "+wizardTitle+" Wizard",
                             "This wizard helps you to  generate a default mobile user interface consisting of features, AMX pages and associated page definition based on an EntityCRUDService bean data control. To enable this option, you must select a MAF project or a file within such a project in the Application Navigator.\n\n" +
          "Click Next to continue.", "Bla bla", null); // No help topic.
          welcomeStep.setStepLabel(wizardTitle);
          welcomeStep.setPageTitle(wizardTitle);
      builder.newStartState(welcomeStep, STATE_DATA_CONTROL);

      Step step = new Step("Select Data Control", SelectUIDataControlPanel.class, null);
      builder.newState(STATE_DATA_CONTROL, step, STATE_OPTIONS,false);

      step = new Step("Data Object UI Settings", UIGeneratorSettingsPanel.class, null);
      builder.newFinalState(STATE_OPTIONS, step);

    }

    try
    {
      FSM stateMachine = builder.getFSM();
      Namespace ns = new Namespace();
      UIGeneratorModel model = new UIGeneratorModel();
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
          UIGeneratorWizard.this.commit(applyEvent.getTraversableContext(), project);
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
      UIGeneratorModel model = (UIGeneratorModel) context.get(MODEL_KEY);
      UIGenerator generator = new UIGenerator(project,model);
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
