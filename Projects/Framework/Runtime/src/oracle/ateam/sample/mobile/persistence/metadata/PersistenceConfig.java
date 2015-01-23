/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.metadata;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

/**
 *  Helper class that returns the information defined in the mobile-persistence-config.properties file,
 *  that should be located in the META-INF directory of your ApplicationCPOntroller project.
 */
public class PersistenceConfig
{
  private static String CONFIG_FILE = "META-INF/mobile-persistence-config";
  private static ResourceBundle config =
    ResourceBundle.getBundle(CONFIG_FILE, Locale.getDefault(), Thread.currentThread().getContextClassLoader());

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

  public static String getDatabasePassword()
  {
    // TODO use new password generation utlity when it is availabe in MAF 2.0
    return "Znsk2rio8XKDFI";
  }

  public static String getDatabaseFilePath()
  {
    String dir = AdfmfJavaUtilities.getDirectoryPathRoot(AdfmfJavaUtilities.ApplicationDirectory);
    return dir + "/" + getDatabaseName();
  }


}
