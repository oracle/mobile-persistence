/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt;

import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import oracle.ateam.sample.mobile.dt.controller.PersistenceMappingLoader;
import oracle.ateam.sample.mobile.dt.util.FileUtils;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromRestWSWizard;
import oracle.ateam.sample.mobile.dt.view.wizard.EditPersistenceMappingWizard;

import oracle.ide.Context;
import oracle.ide.Ide;
import oracle.ide.controller.Controller;
import oracle.ide.controller.IdeAction;
import oracle.ide.extension.RegisteredByExtension;
import oracle.ide.help.HelpSystem;
import oracle.ide.webupdate.PostStartupHook;
import oracle.ide.wizard.WizardManager;

@RegisteredByExtension("oracle.ateam.mobile.persistence")
public class MobileController
  implements Controller, PostStartupHook
{

  public static final int OPEN_DOC_COMMAND_ID = Ide.findCmdID("oracle.ateam.sample.mobile.OpenDoc");
  public static final int EDIT_PERSISTENCE_MAPPING_COMMAND_ID = Ide.findCmdID("oracle.ateam.sample.mobile.editPersistenceMapping");
  public MobileController()
  {
    super();
  }

  @Override
  public boolean handleEvent(IdeAction ideAction, Context context)
  {
    if (ideAction.getCommandId()== OPEN_DOC_COMMAND_ID)
    {
      showDoc();      
    }
    else if (ideAction.getCommandId()== EDIT_PERSISTENCE_MAPPING_COMMAND_ID && ideAction.isEnabled())
    {

      WizardManager.getInstance().invokeWizard(new EditPersistenceMappingWizard(),context,null,null);
//      WizardManager.getInstance().invokeWizard(new BusinessObjectsFromRestWSWizard(),context,null,null);
    }
    return true;
  }

  public boolean showDoc() 
  {
     URL url = null;
     try
     {
       String path = Ide.getProductHomeDirectory()+"/extensions/oracle.ateam.mobile.persistence/doc/index.html";
       url = new URL("file:///"+path);
       HelpSystem.getHelpSystem().showHelp(url);
     }
     catch (MalformedURLException e)
     {
       System.out.println("URL Foutje!!!");
     }
     return true;
  }


  @Override
  public boolean update(IdeAction ideAction, Context context)
  {
    if (ideAction.getCommandId()==EDIT_PERSISTENCE_MAPPING_COMMAND_ID)
    {
      ideAction.setEnabled(false);        
      // only enable when persistance mapping file exists
      URL fileUrl = new PersistenceMappingLoader().getPersistenceMappingFileUrl();
      if (fileUrl!=null)
      {
        InputStream is =FileUtils.getInputStream(fileUrl);
        boolean enabled = is!=null;
        //      System.err.println("EDIT PM ENABLED: "+enabled);
        ideAction.setEnabled(enabled);        
      }
    }
    else
    {
      ideAction.setEnabled(true);      
    }
    return true;
  }

  @Override
  public void install()
  {
    showDoc();
  }

}
