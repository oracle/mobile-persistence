/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.controller.velocity;

import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

import oracle.ide.Ide;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

public class VelocityInitializer
{

  private VelocityEngine velocityEngine;
  private String modelKey;

  public VelocityInitializer()
  {
    super();
  }

  public void initVelocity(String modelKey)
  {
    this.velocityEngine = new VelocityEngine();
    this.modelKey = modelKey;
    String templatesBaseDir = Ide.getOracleHomeDirectory()+"/jdev/extensions/oracle.ateam.mobile.persistence/templates";

    velocityEngine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, templatesBaseDir);
    velocityEngine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_CACHE, "true");
    velocityEngine.setProperty(VelocityEngine.VM_PERM_ALLOW_INLINE,"true");
    velocityEngine.setProperty(VelocityEngine.VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL, "true");

    velocityEngine.setProperty(VelocityEngine.VM_LIBRARY, "macros.vm");    
    // this causes velocitymacros to be reread on usage
     velocityEngine.setProperty(VelocityEngine.VM_LIBRARY_AUTORELOAD, true);
                                               
    velocityEngine.setProperty(VelocityEngine.PARSE_DIRECTIVE_MAXDEPTH, "50");

//    if (useJhsConsole)
//    {
//      velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new JhsVelocityLogger(logger));
//    }
//    else
//    {
//      int lastSlash = logFile.lastIndexOf("/");
//      Util.createPath(application.getTemplatesBaseDir() + logFile.substring(0, lastSlash));
//      velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG, new JhsVelocityLogger(logger));
//    }

    try
    {
      velocityEngine.init();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public Context createVelocityContext(Object model)
  {
    VelocityContext newVelocityContext = new VelocityContext();
    newVelocityContext.put(modelKey, model);
    return newVelocityContext;
  }

  public VelocityEngine getVelocityEngine()
  {
    return velocityEngine;
  }

}

