/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 03-nov-2014   Steven Davelaar / Jeevan Joseph
 1.3           Added OAuth config
 25-may-2014   Steven Davelaar
 1.2           Added caching of descriptors
 19-dec-2013   Steven Davelaar
 1.1           Changed topNode in mapping xml to <mbile-object-persistence> so the file
               becomes editable in JDev
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.metadata;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import oracle.adfmf.util.KXmlUtil;
import oracle.adfmf.util.XmlAnyDefinition;


/**
 * Class that maps to the top-level node in the persistenceMapping XML file
 */
public class ObjectPersistenceMapping
  extends XmlAnyDefinition
{
  private static ObjectPersistenceMapping instance = null;
  private Map classMappingDescriptors = null;
  private Map OAuthConfigSet = null;

  public ObjectPersistenceMapping()
  {
    super();
  }

  public static ObjectPersistenceMapping getInstance()
  {
    if (instance == null)
    {
      InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(PersistenceConfig.getPersistenceMappingXML());
      ObjectPersistenceMapping topNode = null;
      try
      {
        // we use mobile-object-persistence as top node name so we can edit it inside JDev!
        topNode =
          (ObjectPersistenceMapping) KXmlUtil.loadFromXml(is, ObjectPersistenceMapping.class,
                                                          "mobile-object-persistence");
      }
      catch (Exception e)
      {
        // we used object-persistence in old versions, need to re-init input`stream as well, otherwise node not found
        is =
          Thread.currentThread().getContextClassLoader().getResourceAsStream(PersistenceConfig.getPersistenceMappingXML());
        topNode =
          (ObjectPersistenceMapping) KXmlUtil.loadFromXml(is, ObjectPersistenceMapping.class, "object-persistence");
      }
      instance = topNode;
    }
    return instance;
  }

  public String getName()
  {
    return getChildDefinition("name").getText();
  }

  /**
   * Returs map with fully-qualified class name as the key and associated ClassMappingDescriptoras instance as value
   * @return
   */
  public Map getClassMappingDescriptors()
  {
    if (classMappingDescriptors == null)
    {
      classMappingDescriptors = new HashMap();
      XmlAnyDefinition classDescriptorsContainer = this.getChildDefinition("class-mapping-descriptors");
      List descriptors = classDescriptorsContainer.getChildDefinitions("class-mapping-descriptor");
      for (int i = 0; i < descriptors.size(); i++)
      {
        XmlAnyDefinition descriptor = (XmlAnyDefinition) descriptors.get(i);
        ClassMappingDescriptor cmd = new ClassMappingDescriptor(descriptor);
        classMappingDescriptors.put(cmd.getClazz().getName(), cmd);
      }
    }
    return classMappingDescriptors;
  }

  /**
   * Return ClassMappingDescriptor for corresponding class name
   * @param className
   * @return
   */
  public ClassMappingDescriptor findClassMappingDescriptor(String className)
  {
    return (ClassMappingDescriptor) getClassMappingDescriptors().get(className);
  }

  /**
   * Return OAuth configurations as defined in persistenceMapping.xml
   * @return
   */
  public Map getOAuthConfigMap()
  {
    if (OAuthConfigSet == null)
    {
      OAuthConfigSet = new HashMap();
      XmlAnyDefinition configSet = this.getChildDefinition("oauth-config-set");
      List configDefs = configSet.getChildDefinitions("oauth-config");
      for (int i = 0; i < configDefs.size(); i++)
      {
        XmlAnyDefinition configDef = (XmlAnyDefinition) configDefs.get(i);
        OAuthConfig oauthConfig = new OAuthConfig(configDef);
        OAuthConfigSet.put(oauthConfig.getConfigName(), oauthConfig);
      }
    }
    return OAuthConfigSet;
  }

  /**
   * Return a specific OAuth configuration
   * @param configName
   * @return
   */
  public OAuthConfig findOAuthConfig(String configName)
  {
    return (OAuthConfig) getOAuthConfigMap().get(configName);
  }
}
