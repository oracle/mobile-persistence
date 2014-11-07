package oracle.ateam.sample.mobile.dt.view.uipanel;

import java.awt.BorderLayout;


import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;


import oracle.ide.wizard.WizardCallbacks;

import oracle.javatools.db.DBException;
import oracle.javatools.db.DBObjectProvider;
import oracle.javatools.db.DBObjectProviderFactory;
import oracle.javatools.db.Database;

// import oracle.jdeveloper.db.ConnectionInfo;
//import oracle.jdeveloper.db.adapter.DatabaseProvider;
import oracle.jdeveloper.db.panels.AppConnectionPanelUI;

public class SelectDBConnectionPanel
  extends DefaultTraversablePanel implements ItemListener
{
  private  AppConnectionPanelUI connUI = new AppConnectionPanelUI(true,true);
  private WizardCallbacks callbacks;
  
  public SelectDBConnectionPanel()
  {
    super();
    this.setLayout(new BorderLayout(0, 15));
//    this.setDefaultTitle(m_resourceManager.getString("JMIG_TGT_RES_0002"));     
//    connUI.setConnectionPrompt(getInstructions());
    this.add(connUI , BorderLayout.NORTH);
  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {
    try
    {
      DBObjectProvider provider = DBObjectProviderFactory.findOrCreateProvider(Database.PROVIDER_TYPE,
          connUI.getConnectionName());
      BusinessObjectGeneratorModel model =
          (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
       model.setConnectionName(connUI.getConnectionName());
       model.setDbProvider(provider);
    }
    catch (DBException e)
    {
    }
      
    super.onExit(tc);
  }

  public void onEntry(TraversableContext traversableContext)
  {
    super.onEntry(traversableContext);
    BusinessObjectGeneratorModel model =
        (BusinessObjectGeneratorModel) traversableContext.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    connUI.setConnectionName(model.getConnectionName());
    connUI.addComboListener(this);
    // enable back -  next - finish
    callbacks = traversableContext.getWizardCallbacks();
    callbacks.wizardEnableButtons(true, connUI.getConnectionName()!=null, false);
  }

  @Override
  public void itemStateChanged(ItemEvent e)
  {
    callbacks.wizardEnableButtons(true, true, false);
  }
}
