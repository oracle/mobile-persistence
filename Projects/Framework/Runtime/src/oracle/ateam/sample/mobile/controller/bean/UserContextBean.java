/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 27-jan-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.controller.bean;

import java.util.List;

import oracle.adfmf.Constants;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.util.SecurityUtil;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.XmlAnyDefinition;

public class UserContextBean
{
  private transient String credentialStoreKey;
  private transient String testUserName;
  private transient String testPassword;
  
  // Dummy vars to prevent invocation getter methods during
  // JSON serialization. When omitted, app hangs when navigating
  // in online mode to data synch feature!
  private transient String userName;
  private transient String password;

  public UserContextBean()
  {
    super();
  }
    
  public String getUserName()
  {
    String realUserName = (String) AdfmfJavaUtilities.evaluateELExpression("#{securityContext.userName}");
    return (realUserName!=null && !"".equals(realUserName.trim())) ? realUserName : testUserName;
  }

  public String getPassword()
  {
    String realPassword = null;
    if (getCredentialStoreKey()!=null)
    {
// changed in 12.1.3
//        realPassword=  AdfmfJavaUtilitiesInternal.getPasswordCredential(getCredentialStoreKey());      
        realPassword=  SecurityUtil.getPassword(getCredentialStoreKey()).toString();      
    }
    return (realPassword!=null && !"".equals(realPassword.trim())) ? realPassword : testPassword;
  }
  
  protected String findCredentialStoreKey()
  {
  //    <Reference name="AuraPlauyerAuth" className="oracle.adf.model.connection.adfmf.LoginConnection" adfCredentialStoreKey="AuraPlauyerAuth"
  //      partial="false" manageInOracleEnterpriseManager="true" deployable="true" xmlns="">
    String key = null;
    List connections = Utility.getConnections().getChildDefinitions("Reference");
    for (int i = 0; i < connections.size(); i++)
    {
      XmlAnyDefinition connection = (XmlAnyDefinition) connections.get(i);
      if ("oracle.adf.model.connection.adfmf.LoginConnection".equals(connection.getAttributeValue("className")))
      {
        key = (String) connection.getAttributeValue(Constants.CREDENTIAL_STORE_KEY_ATTRIBUTE);
        break;
      }
    }
    return key;
  }

  public void setCredentialStoreKey(String credentialStoreKey)
  {
    this.credentialStoreKey = credentialStoreKey;
  }

  public String getCredentialStoreKey()
  {
    if (credentialStoreKey==null)
    {
      credentialStoreKey = findCredentialStoreKey();
    }
    return credentialStoreKey;
  }

  public void setTestUserName(String testUserName)
  {
    this.testUserName = testUserName;
  }

  public String getTestUserName()
  {
    return testUserName;
  }

  public void setTestPassword(String testPassword)
  {
    this.testPassword = testPassword;
  }

  public String getTestPassword()
  {
    return testPassword;
  }
}
