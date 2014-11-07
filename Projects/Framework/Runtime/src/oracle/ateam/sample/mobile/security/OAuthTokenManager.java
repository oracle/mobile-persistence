/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 03-nov-2014   Jeevan Joseph / Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.security;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.adfmf.dc.ws.rest.RestServiceAdapter;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.JSONBeanSerializationHelper;
import oracle.adfmf.framework.api.Model;
import oracle.adfmf.framework.exception.AdfException;

import oracle.ateam.sample.mobile.persistence.metadata.Method;
import oracle.ateam.sample.mobile.persistence.metadata.MethodHeaderParameterImpl;
import oracle.ateam.sample.mobile.persistence.metadata.MethodParameter;
import oracle.ateam.sample.mobile.persistence.metadata.OAuthConfig;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;

/**
 *  Helper class for implementing OAuth security
 */
public class OAuthTokenManager
{
  public OAuthTokenManager()
  {
    super();
  }

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(OAuthTokenManager.class);

  private final static String ERROR_AUTH_TOKEN_INVOCATION = "Failed to retrieve new token from auth server for user ";
  private final static String ERROR_AUTH_TOKEN_PARSE = "Failed to parse auth token JSON response \n";


  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String HEADER_CONTENT_TYPE = "Content-Type";

  private static final String HEADER_CONTENT_TYPE_DEFAULT_VALUE = "application/x-www-form-urlencoded; charset=utf-8";
  private static final String AUTH_KEYWORD_BASIC = "Basic";
  private static final String AUTH_KEYWORD_BEARER = "Bearer";
  private static final String HEADER_CLIENT_CREDS = "CLIENT_CREDS";
  private static final String GRANT_TYPE_PASSWORD = "password";

  protected OAuthToken getAccessToken(OAuthConfig oauthConfig)
  {
    OAuthToken authToken = null; //Apply cache algorithm here
    Map parameterMap = oauthConfig.getParameterMapping();
    StringBuffer tokenPostData = new StringBuffer();
    tokenPostData.append("grant_type=").append(oauthConfig.getGrantType()).append("&client_assertion_type=").append(oauthConfig.getClientAssertionType());

    // process OAuth parameters. Values can be EL expressions that need to be evaluated
    Iterator itr = parameterMap.keySet().iterator();
    while (itr.hasNext())
    {
      String paramName = (String) itr.next();
      String paramValueExp = (String) parameterMap.get(paramName);
      String paramValue = (String) AdfmfJavaUtilities.evaluateELExpression(paramValueExp);
      tokenPostData.append("&").append(paramName).append("=").append(paramValue);
    }
    String payload = tokenPostData.toString();
    String response = requestNewToken(oauthConfig, payload);
    try
    {
      authToken = (OAuthToken) JSONBeanSerializationHelper.fromJSON(OAuthToken.class, response);
    }
    catch (Exception e)
    {
      String message = ERROR_AUTH_TOKEN_PARSE + response;
      sLog.severe(message);
      throw new AdfException(e, AdfException.ERROR);
    }
    return authToken;
  }

  public List getOAuthHeaderParams(String oauthConfigName)
  {
    OAuthConfig oauthConfig = OAuthConfig.getInstance(oauthConfigName);
    List headerParams = new ArrayList();
    OAuthToken authToken = getAccessToken(oauthConfig);
    MethodHeaderParameterImpl authHeader = new MethodHeaderParameterImpl(HEADER_AUTHORIZATION);
    authHeader.setValue(AUTH_KEYWORD_BEARER + " " + authToken.getAccess_token());
    MethodHeaderParameterImpl clientCredsHeader = new MethodHeaderParameterImpl(HEADER_CLIENT_CREDS);
    clientCredsHeader.setValue(oauthConfig.getClientSecret());

    headerParams.add(authHeader);
    headerParams.add(clientCredsHeader);
    return headerParams;
  }

  protected String requestNewToken(OAuthConfig oauthConfig, String payload)
  {
    RestServiceAdapter restServiceAdapter = Model.createRestServiceAdapter();
    restServiceAdapter.clearRequestProperties();
    restServiceAdapter.setConnectionName(oauthConfig.getConnectionName());
    restServiceAdapter.setRequestType(RestServiceAdapter.REQUEST_TYPE_POST);
    restServiceAdapter.setRetryLimit(0);
    restServiceAdapter.addRequestProperty(HEADER_AUTHORIZATION,
                                          AUTH_KEYWORD_BASIC + " " + oauthConfig.getClientSecret());
    restServiceAdapter.addRequestProperty(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_DEFAULT_VALUE);
    restServiceAdapter.setRequestURI(oauthConfig.getTokenURI());

    String response = "";

    // Execute SEND and RECEIVE operation
    try
    {

      response = restServiceAdapter.send(payload);
    }
    catch (Exception e)
    {
      String message = ERROR_AUTH_TOKEN_INVOCATION + payload;
      sLog.severe(message);
      throw new AdfException(e, AdfException.ERROR);
    }

    return response;
  }
}
