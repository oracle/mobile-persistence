 /*******************************************************************************
  Copyright © 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import oracle.adfmf.util.XmlAnyDefinition;

public class OAuthConfig
  extends XmlAnyDefinition
{

  private static final String CONFIG_NAME = "configName";
  private static final String CONNECTION_NAME = "connectionName";
  private static final String TOKEN_URI = "tokenUri";
  private static final String CLIENT_SECRET = "clientSecret";
  private static final String GRANT_TYPE = "grantType";
  private static final String CLIENT_ASSERTION_TYPE = "clientAssertionType";
  private static final String SCOPE = "scope";
  private HashMap<String,String> parameterMap;


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
    return getAttributeStringValue(CONFIG_NAME);
  }

  public String getConnectionName()
  {
    return getAttributeStringValue(CONNECTION_NAME);
  }

  public String getTokenURI()
  {
    return getAttributeStringValue(TOKEN_URI);
  }

  public String getClientSecret()
  {
    return getAttributeStringValue(CLIENT_SECRET);

  }

  public String getGrantType()
  {
    return getAttributeStringValue(GRANT_TYPE);
  }

  public String getClientAssertionType()
  {
    return getAttributeStringValue(CLIENT_ASSERTION_TYPE);
  }

  public String getScope()
  {
    return getAttributeStringValue(SCOPE);
  }

  /**
   *  Return name-value pairs for OAuth parameters
   */  
  public Map<String,String> getParameterMapping()
  {
    if (parameterMap == null)
    {
      parameterMap = new HashMap<String,String>();
      List<XmlAnyDefinition> parameters = this.getChildDefinitions("parameter");
      for (XmlAnyDefinition parameter : parameters)
      {
        String paramName = parameter.getAttributeStringValue("name");
        if (paramName != null)
        {
          String paramValue = parameter.getAttributeStringValue("value");
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
