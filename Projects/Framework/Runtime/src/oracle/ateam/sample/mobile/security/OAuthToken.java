/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 03-nov-2014   Jeevan Joseph
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.security;

public class OAuthToken
{
  private String expires_in;
  private String token_type;
  private String oracle_tk_context;
  private String oracle_grant_type;
  private String access_token;

  public OAuthToken()
  {
    super();
  }

  public void setExpires_in(String expires_in)
  {
    this.expires_in = expires_in;
  }

  public String getExpires_in()
  {
    return expires_in;
  }

  public void setToken_type(String token_type)
  {
    this.token_type = token_type;
  }

  public String getToken_type()
  {
    return token_type;
  }

  public void setOracle_tk_context(String oracle_tk_context)
  {
    this.oracle_tk_context = oracle_tk_context;
  }

  public String getOracle_tk_context()
  {
    return oracle_tk_context;
  }

  public void setOracle_grant_type(String oracle_grant_type)
  {
    this.oracle_grant_type = oracle_grant_type;
  }

  public String getOracle_grant_type()
  {
    return oracle_grant_type;
  }

  public void setAccess_token(String access_token)
  {
    this.access_token = access_token;
  }

  public String getAccess_token()
  {
    return access_token;
  }
}
