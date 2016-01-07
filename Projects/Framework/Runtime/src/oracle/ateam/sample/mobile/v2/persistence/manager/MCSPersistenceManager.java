/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 29-dec-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.manager;

import java.io.UnsupportedEncodingException;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.ateam.sample.mobile.mcs.analytics.MCSAnalyticsEvent;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;

import sun.misc.BASE64Encoder;


public class MCSPersistenceManager extends RestJSONPersistenceManager
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(MCSPersistenceManager.class);
  private static final String LOGIN_URI = "/platform/users/login";
  private static final String LOGOUT_URI = "/platform/users/logout";
  private static final String ANALYTICS_EVENTS_URI = "/platform/analytics/events";
  private String mobileBackendId;
  private String authHeader;
  private String connectionName;
  private String anonymousKey;

  public MCSPersistenceManager()
  {
    super();
    connectionName = PersistenceConfig.getPropertyValue("mcs.connection");
    anonymousKey = PersistenceConfig.getPropertyValue("mcs.anonymous-key");
    mobileBackendId = PersistenceConfig.getPropertyValue("mcs.mobile-backend-id");
  }

  public String login(String userName, String password)
  {
    return login(userName,password, this.mobileBackendId);
  }

  public String login(String userName, String password, String mobileBackendId)
  {
    this.mobileBackendId = mobileBackendId;

    String userCredentials = userName + ":" + password;
    try
    {
      // oracle.adfmf.misc.Base64 nor java.util.Base64 does not encode correctly for some strange reason
      //      authHeader = "Basic " + Base64.encode(userCredentials.getBytes("UTF-8"));
      //      authHeader = "Basic " + Base64.getEncoder().encode(userCredentials.getBytes("UTF-8"));

      authHeader = "Basic " + new BASE64Encoder().encode(userCredentials.getBytes("UTF-8"));
      String result =
        invokeRestService(getConnectionName(), "GET", LOGIN_URI, null, getMCSHeaderParams(), 0, false);
      return result;
    }
    catch (UnsupportedEncodingException e)
    {
      // this should never happen
    }
    catch (Exception e)
    {
      authHeader = null;
      throw e;
    }
    return null;
  }

  public String logout()
  {
    RestJSONPersistenceManager rpm = new RestJSONPersistenceManager();
    String result =
      rpm.invokeRestService(getConnectionName(), "GET", LOGOUT_URI, null, getMCSHeaderParams(), 0, false);
    authHeader = null;
    return result;
  }

  public void sendAnalyticsEvents(String payload)
  {
    invokeRestService(getConnectionName(), "POST", ANALYTICS_EVENTS_URI, payload, getMCSHeaderParams(), 0,
                              false);
  }

  public Map<String, String> getMCSHeaderParams()
  {
    Map<String, String> headerParams = new HashMap<String, String>();
    headerParams.put("Content-Type", "application/json");
    headerParams.put("Oracle-Mobile-BACKEND-ID", getMobileBackendId());
    String header = getAuthHeader();
    if (header == null)
    {
      header = getAnonymousHeader();
    }
    headerParams.put("Authorization", header);
    return headerParams;
  }


  public void setMobileBackendId(String mobileBackendId)
  {
    this.mobileBackendId = mobileBackendId;
  }

  public String getMobileBackendId()
  {
    return mobileBackendId;
  }

  public void setAuthHeader(String authHeader)
  {
    this.authHeader = authHeader;
  }

  public String getAuthHeader()
  {
    return authHeader;
  }

  public String getAnonymousHeader()
  {
    return "Basic " + getAnonymousKey();
  }

  public void setConnectionName(String connectionName)
  {
    this.connectionName = connectionName;
  }

  public String getConnectionName()
  {
    return connectionName;
  }

  public void setAnonymousKey(String anonymousKey)
  {
    this.anonymousKey = anonymousKey;
  }

  public String getAnonymousKey()
  {
    return anonymousKey;
  }

}
