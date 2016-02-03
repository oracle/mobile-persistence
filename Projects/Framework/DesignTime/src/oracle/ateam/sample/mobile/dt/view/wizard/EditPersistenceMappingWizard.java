/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;

import java.util.List;

import oracle.ateam.sample.mobile.dt.controller.BusinessObjectGenerator;
import oracle.ateam.sample.mobile.dt.controller.PersistenceMappingLoader;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.jaxb.MobileObjectPersistence;
import oracle.ateam.sample.mobile.dt.util.PersistenceConfigUtils;
import oracle.ateam.sample.mobile.dt.util.ProjectUtils;
import oracle.ateam.sample.mobile.dt.view.uipanel.AttributesPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.CRUDMethodParametersPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.CRUDMethodsRESTPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.DataObjectsPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.GeneratorSettingsPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.ParentChildAccessorsPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.RESTResourcesPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.RuntimeOptionsPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.SecurityWarningPanel;
import oracle.ateam.sample.mobile.dt.view.uipanel.SelectURLConnectionPanel;

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


public class EditPersistenceMappingWizard extends Wizard
{


  public static final String MODEL_KEY = "model";
  static final String STATE_DATA_OBJECTS = "dataObjects";
  static final String STATE_ATTRIBUTES = "attributes";
  static final String STATE_PERSISTENCE_MAPPING= "pmapping";
  static final String STATE_CRUD_METHOD= "crudMethods";
  static final String STATE_CRUD_METHOD_PARAMS = "crudMethodParams";
  static final String STATE_RUNTIME_OPTIONS = "runtimeOptions";
  static final String STATE_OPTIONS = "options";

//  private String wizardTitle = "Create Mobile Web Service Proxy and Persistence Provider";
  private String wizardTitle = "Edit Persistence Mapping";
  private String logTitle = wizardTitle+ " Generator";
  
  public boolean runWizard(Component parent, final Project project)
  {

    FSMBuilder builder = new FSMBuilder();

    // Add all the pages to the finite state machine builder.
    ADD_PAGES_TO_BUILDER:
    {

      Step step = new Step("Data Objects", DataObjectsPanel.class, null);
      builder.newStartState(step, STATE_ATTRIBUTES);

      step = new Step("Data Object Attributes", AttributesPanel.class, null);
      builder.newState(STATE_ATTRIBUTES, step, STATE_PERSISTENCE_MAPPING,false);

      step = new Step("Parent-Child Accessors", ParentChildAccessorsPanel.class, null);
      builder.newState(STATE_PERSISTENCE_MAPPING, step, STATE_CRUD_METHOD,false);

      step = new Step("CRUD Resources", CRUDMethodsRESTPanel.class, null);
      builder.newState(STATE_CRUD_METHOD, step, STATE_CRUD_METHOD_PARAMS,false);

      step = new Step("Resource Details", CRUDMethodParametersPanel.class, null);
      builder.newState(STATE_CRUD_METHOD_PARAMS, step, STATE_RUNTIME_OPTIONS,false);

      step = new Step("Runtime Options", RuntimeOptionsPanel.class, null);
      builder.newState(STATE_RUNTIME_OPTIONS, step, STATE_OPTIONS,false);

      step = new Step("Generator Settings", GeneratorSettingsPanel.class, null);
      builder.newFinalState(STATE_OPTIONS, step);

    }

    try
    {
      FSM stateMachine = builder.getFSM();
      Namespace ns = new Namespace();
      BusinessObjectGeneratorModel model = new BusinessObjectGeneratorModel();
      model.setLogTitle(wizardTitle);
      model.setEditMode(true);

      PersistenceMappingLoader loader = new PersistenceMappingLoader();
      MobileObjectPersistence jaxbModel = loader.loadJaxbModel();
      model.setExistingPersistenceMappingModel(jaxbModel);
      if (jaxbModel!=null)
      {
        Collection<DataObjectInfo> existingDataObjects =loader.run(jaxbModel);
        model.setDataObjectInfos(new ArrayList<DataObjectInfo>(existingDataObjects));      
      }
      model.setRestfulWebService(true);
      
      // if connectionName not yet set based on MCS settings, then set connectionName based on first one found
      if (model.getConnectionName()!=null)
      {
        String connectionName = null;
        for (DataObjectInfo doi : model.getDataObjectInfos())
         {
          for (DCMethod method : doi.getAllMethods())
          {
            if (method.getConnectionName()!=null)
            {
              connectionName = method.getConnectionName();
              break;
            }
          }
          if (connectionName!=null)
          {
            break;
          }
         }
         model.setConnectionName(connectionName);        
      }
      ns.put(MODEL_KEY, model);

      FSMWizard wizard = new FSMWizard(stateMachine, ns);

      // Initialize the wizard's style.
      INITIALIZE_WIZARD:
      {
        wizard.setWizardTitle(wizardTitle);
        wizard.setWelcomePageAdded(false);
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
          EditPersistenceMappingWizard.this.commit(applyEvent.getTraversableContext(), project);
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
      BusinessObjectGenerator generator = new BusinessObjectGenerator(model);
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
