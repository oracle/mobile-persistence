 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  14-feb-2016   Steven Davelaar
  1.3           added property enable parallel rest calls.
  10-dec-2015   Steven Davelaar
  1.2           - Added support for storing passwiord in credential store using GeneratedPassword class
                To use this, add db.generate.password=true to persistence-config.properties
                - Added encryption type prefix to password
  19-nov-2015   Steven Davelaar
  1.1           Added db.use.WAL property
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.GeneratedPassword;

/**
 *  Helper class that returns the information defined in the mobile-persistence-config.properties file,
 *  that should be located in the META-INF directory of your ApplicationCPOntroller project.
 */
public class PersistenceConfig
{
  private static String CONFIG_FILE = "META-INF/mobile-persistence-config";
  private static ResourceBundle config =
    ResourceBundle.getBundle(CONFIG_FILE, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
  private static String PASSWORD_GEN_KEY = "_may_the_p0wer_0f_aMpA_be_with_y0u";
  private static String PASSWORD_KEY = "PASSWORD_KEY";

  public static String getDatabaseName()
  {
    return config.getString("db.name");
  }

  public static String getPersistenceMappingXML()
  {
    return config.getString("persistence.mapping.xml");
  }

  public static String getDDLScript()
  {
    return config.getString("ddl.script");
  }

  public static boolean encryptDatabase()
  {
    // allow unencrypted mode for developm,ent debugging purposes
    try
    {
      String encrypt = config.getString("db.encryption");
      return !"false".equals(encrypt);    
    }
    catch (MissingResourceException e)
    {
      // property not found, encrypt the db by default
      return true;
    }
//    return true;
  }

  public static boolean enableParallelRestCalls()
  {
    // allow unencrypted mode for developm,ent debugging purposes
    try
    {
      String enable = config.getString("enable.parallel.rest.calls");
      return "true".equals(enable);    
    }
    catch (MissingResourceException e)
    {
      // property not found, default to false for backwards compatibility
      return false;
    }
  }

  public static boolean useWAL()
  {
    // switch to set Write Ahead Logging
    try
    {
      String useWAL = config.getString("db.use.WAL");
      return "true".equalsIgnoreCase(useWAL);    
    }
    catch (MissingResourceException e)
    {
      // property not found, encrypt the db by default
      return false;
    }
  }
  
  /**
   * Returns value of property in persistence-config file. Returns null if property is not defined
   * @param propertyName
   * @return
   */
  public static String getPropertyValue(String propertyName)
  {
    try
    {
      String value = config.getString(propertyName);
      return value;
    }
    catch (MissingResourceException e)
    {
      return null;
    }
  }

  public static String getEncryptionType()
  {
    try
    {
      String type = config.getString("db.encryption.type");
      if (type == null || "".equals(type))
      {
        type = AdfmfJavaUtilities.AES128;
      }
      return type;
    }
    catch (MissingResourceException e)
    {
      return AdfmfJavaUtilities.AES128;
    }
  }

  public static String createDatabasePassword()
  {
    GeneratedPassword.setPassword(PASSWORD_KEY,PASSWORD_GEN_KEY);
    return getDatabasePassword();
  }

  public static String getDatabasePassword()
  {
    String password = null;
    char[] genPw = GeneratedPassword.getPassword(PASSWORD_KEY);
    if (genPw!=null)
    {
      password = new String(genPw);      
    }
    else
    {
      // this DB was created while using older version of AMPA, use old
      // hardcoded password
      password = "Znsk2rio8XKDFI";
    }
    return getEncryptionType()+":"+password;
  }

  public static String getDatabaseFilePath()
  {
    String dir = AdfmfJavaUtilities.getDirectoryPathRoot(AdfmfJavaUtilities.ApplicationDirectory);
    return dir + "/" + getDatabaseName();
  }

  /**
   * Return the value of property datasync.manager.class
   * @return
   */
  public static String getDataSynchManagerClass()
  {
    return getPropertyValue("datasync.manager.class");
  }
  
}
