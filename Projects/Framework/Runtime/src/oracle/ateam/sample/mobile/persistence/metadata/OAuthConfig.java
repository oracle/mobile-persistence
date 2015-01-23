 /*******************************************************************************
  Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
  
  $revision_history$
  03-nov-2014   Jeevan Joseph
  1.0           initial creation
 ******************************************************************************/
package oracle.ateam.sample.mobile.persistence.metadata;

import java.util.HashMap;
import java.util.List;

import java.util.Map;

import oracle.adfmf.util.XmlAnyDefinition;

public class OAuthConfig
  extends XmlAnyDefinition
{

  private static final String CONFIG_NAME = "config-name";
  private static final String CONNECTION_NAME = "connection-name";
  private static final String TOKEN_URI = "token-uri";
  private static final String CLIENT_SECRET = "client-secret";
  private static final String GRANT_TYPE = "grant-type";
  private static final String CLIENT_ASSERTION_TYPE = "client-assertion-type";
  private static final String SCOPE = "scope";
  private HashMap parameterMap;


  public static OAuthConfig getInstance(String configName)
  {
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    OAuthConfig config = mapping.findOAuthConfig(configName);
    return config;
  }

  public OAuthConfig()
  {
    super();
  }

  public OAuthConfig(XmlAnyDefinition any)
  {
    super(any);
  }

  public String getConfigName()
  {
    return getChildDefinition(CONFIG_NAME).getText();
  }

  public String getConnectionName()
  {
    return getChildDefinition(CONNECTION_NAME).getText();
  }

  public String getTokenURI()
  {
    return getChildDefinition(TOKEN_URI).getText();

  }

  public String getClientSecret()
  {
    return getChildDefinition(CLIENT_SECRET).getText();

  }

  public String getGrantType()
  {
    return getChildDefinition(GRANT_TYPE).getText();

  }

  public String getClientAssertionType()
  {
    return getChildDefinition(CLIENT_ASSERTION_TYPE).getText();

  }

  public String getScope()
  {
    return getChildDefinition(SCOPE).getText();

  }

  /**
   *  Return name-value pairs for OAuth parameters
   */  
  public Map getParameterMapping()
  {
    if (parameterMap == null)
    {
      parameterMap = new HashMap();
      XmlAnyDefinition parameterContainer = this.getChildDefinition("parameters");
      List parameters = parameterContainer.getChildDefinitions("parameter");
      for (int i = 0; i < parameters.size(); i++)
      {
        XmlAnyDefinition descriptor = (XmlAnyDefinition) parameters.get(i);
        String paramName = (String) descriptor.getAttributeValue("name");
        if (paramName != null)
        {
          String paramValue = (String) descriptor.getAttributeValue("value");
          if (paramValue != null)
          {
            parameterMap.put(paramName, paramValue);

          }
          else
          {
            throw new IllegalArgumentException("Found pamameter " + paramName +
                                               " but value is null or not set. This is a mandatory attribute.");
          }
        }
        else
        {
          throw new IllegalArgumentException("Found parameter with empty or null name attribute . This is a mandatory attribute.");
        }
      }
    }
    return parameterMap;
  }


}
