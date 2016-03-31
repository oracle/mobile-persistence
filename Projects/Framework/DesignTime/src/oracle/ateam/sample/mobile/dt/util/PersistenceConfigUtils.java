/*******************************************************************************
 Copyright (c) 20156, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 02-feb-2016   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.util;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Properties;

import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

public class PersistenceConfigUtils
{
  public static Properties loadPersistenceConfigProperties()
  
  {
    URL sourceURL = FileUtils.getSourceURL(ProjectUtils.getApplicationControllerProject(), "META-INF", "mobile-persistence-config.properties");
    InputStream is =FileUtils.getInputStream(sourceURL);
    if (is==null)
    {
      return null;
    }
    Properties prop = new Properties();
    try
    {
      prop.load(is);
      return prop;
    }
    catch (IOException e)
    {
    }
    return null;
  }

  public static void initializeMCSSettings(BusinessObjectGeneratorModel model)
  {
    Properties persistenceConfig = loadPersistenceConfigProperties();
    if (persistenceConfig!=null)
    {
      model.setConnectionName(persistenceConfig.getProperty("mcs.connection"));
      if (persistenceConfig.getProperty("mcs.connection")!=null)
      {
        model.setUseMCS(true);
      }
      model.setMcsBackendId(persistenceConfig.getProperty("mcs.mobile-backend-id"));
      model.setMcsAnonymousAccessKey(persistenceConfig.getProperty("mcs.anonymous-key"));        
    }        
  }
}
