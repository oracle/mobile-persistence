/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt;

import java.net.MalformedURLException;
import java.net.URL;

import oracle.ide.Context;
import oracle.ide.Ide;
import oracle.ide.controller.Controller;
import oracle.ide.controller.IdeAction;
import oracle.ide.extension.RegisteredByExtension;
import oracle.ide.help.HelpSystem;
import oracle.ide.webupdate.PostStartupHook;

@RegisteredByExtension("oracle.ateam.mobile.persistence")
public class MobileController
  implements Controller, PostStartupHook
{

  public static final int OPEN_DOC_COMMAND_ID = Ide.findCmdID("oracle.ateam.sample.mobile.OpenDoc");
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
    return true;
  }

  public boolean showDoc() 
  {
     URL url = null;;
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
    ideAction.setEnabled(true);
    return true;
  }

  @Override
  public void install()
  {
    showDoc();
  }

}
