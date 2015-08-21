/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 21-aug-2015   Steven Davelaar
 1.1           Made instance variable transient to prevent recursion loop with serialization causing
               app startup time on ANdorid devices to become exceptionally long (>1 minute)
               Also commented out the line to store the instance on app scope, otherwise issue
               was still seen.
 28-may-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.util;

import java.io.UnsupportedEncodingException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.UUID;

import oracle.adf.model.datacontrols.device.DeviceManager;
import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adf.model.datacontrols.device.Location;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.JSONBeanSerializationHelper;


import oracle.ateam.sample.mobile.v2.persistence.manager.RestJSONPersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;

import sun.misc.BASE64Encoder;


public class MCSManager
{

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(MCSManager.class);
  private String sessionId;
  private String mobileBackendId;
  private String authHeader;
  private String userName;
  private String connectionName;
  private String anonymousKey;
  private transient static MCSManager instance;

  private MCSManager()
  {
    super();
    connectionName = PersistenceConfig.getPropertyValue("mcs.connection");
    anonymousKey = PersistenceConfig.getPropertyValue("mcs.anonymous-key");
    mobileBackendId = PersistenceConfig.getPropertyValue("mcs.mobile-backend-id");
  }

  public static MCSManager getInstance()
  {
    if (instance == null)
    {
      instance = new MCSManager();
//      AdfmfJavaUtilities.setELValue("#{applicationScope.ampa_mcs_manager}", instance);
    }
    return instance;
  }

  public void startSession()
  {
    sessionId = UUID.randomUUID().toString();
    sendEvent(true, "sessionStart", null);
  }

  public void endSession()
  {
    if (sessionId != null)
    {
      sendEvent(true, "sessionEnd", null);
    }
  }

  public Map<String, Object> getSessionContext()
  {
    //        {
    //            "name": "context",
    //            "type": "system",
    //            "timestamp": "2015-05-28T05:46:34.345Z",
    //            "properties": {
    //                "userName": "steven.davelaar",
    //                "model": "iPhone5,1",
    //                "longitude": "4.8638408",
    //                "latitude": "52.296804",
    //                "timezone": "-14400",
    //                "manufacturer": "Apple",
    //                "osName": "iPhone OS",
    //                "osVersion": "7.1",
    //                "osBuild": "13E28",
    //                "carrier": "AT&T"
    //            }
    //        }
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("name", "context");
    context.put("type", "system");
    context.put("timestamp", getCurrentDateTime());
    Map<String, Object> props = new HashMap<String, Object>();
    context.put("properties", props);
    String networkStatus = DeviceManagerFactory.getDeviceManager().getNetworkStatus();
    String model = DeviceManagerFactory.getDeviceManager().getModel(); // iPad Simulator x86_64
    String os = DeviceManagerFactory.getDeviceManager().getOs(); // iOS
    String version = DeviceManagerFactory.getDeviceManager().getVersion(); // 7.1
    String platform = DeviceManagerFactory.getDeviceManager().getPlatform(); // iPad Simulator
    String userName = getUserName();
    if (userName != null)
    {
      props.put("userName", userName);
    }
    props.put("model", model);
    props.put("timezone", getCurrentTimeZone());
    props.put("manufacturer", platform);
    props.put("osName", os);
    props.put("osVersion", version);
    props.put("carrier", networkStatus);
    Location loc = getLocation();
    if (loc != null)
    {
      // lat/lng must be sent as string, otherwise error returned!
      props.put("longitude", loc.getLongitude() + "");
      props.put("latitude", loc.getLatitude() + "");
    }
    return context;
  }

  public String login(String userName, String password, String mobileBackendId)
  {
    this.userName = userName;
    this.mobileBackendId = mobileBackendId;

    String userCredentials = userName + ":" + password;
    // postman auth header based John.Dunbar/Welcome#1: Basic Sm9obi5EdW5iYXI6V2VsY29tZSMx
    // for some strange reason the java.util.Base64, nor
    try
    {
      // oracle.adfmf.misc.Base64 nor java.util.Base64 does not encode correctly for some strange reason
      //      authHeader = "Basic " + Base64.encode(userCredentials.getBytes("UTF-8"));
      //      authHeader = "Basic " + Base64.getEncoder().encode(userCredentials.getBytes("UTF-8"));

      authHeader = "Basic " + new BASE64Encoder().encode(userCredentials.getBytes("UTF-8"));
      RestJSONPersistenceManager rpm = new RestJSONPersistenceManager();
      String result =
        rpm.invokeRestService(getConnectionName(), "GET", "/platform/users/login", null, getMCSHeaderParams(), 0, false);
      return result;
    }
    catch (UnsupportedEncodingException e)
    {
      // this should never happen
    }
    catch (Exception e)
    {
      authHeader = null;
      userName = null;
      throw e;
    }
    return null;
  }

  public String logout()
  {
    RestJSONPersistenceManager rpm = new RestJSONPersistenceManager();
    String result =
      rpm.invokeRestService(getConnectionName(), "GET", "/platform/users/logout", null, getMCSHeaderParams(), 0, false);
    authHeader = null;
    userName = null;
    return result;
  }

  public void sendEvent(boolean isSystemEvent, String name, Map<String, Object> properties)
  {
    if (!isAnalyticsEnabled())
    {
      return;
    }
    Thread t = new Thread(() ->
        {
          List<Map> events = new ArrayList<Map>();
          events.add(getSessionContext());
          Map<String, Object> event = new HashMap<String, Object>();
          events.add(event);
          event.put("name", name);
          event.put("type", (isSystemEvent? "system": "custom"));
          event.put("timestamp", getCurrentDateTime());
          event.put("sessionID", sessionId);
          if (properties != null)
          {
            event.put("properties", properties);
          }
          try
          {
            String payload = JSONBeanSerializationHelper.toJSON(events).toString();
            RestJSONPersistenceManager rpm = new RestJSONPersistenceManager();
            rpm.invokeRestService(getConnectionName(), "POST", "/platform/analytics/events", payload,
                                  getMCSHeaderParams(), 0, false);
          }
          catch (Exception e)
          {
            sLog.severe("Cannot create json payload: " + e.getLocalizedMessage());
          }

        });
    t.start();
  }

  public Map<String, String> getMCSHeaderParams()
  {
    Map<String, String> headerParams = new HashMap<String, String>();
    headerParams.put("Content-Type", "application/json");
    headerParams.put("Oracle-Mobile-BACKEND-ID", getMobileBackendId());
    String header = getAuthHeader();
    if (header==null)
    {
      header = getAnonymousHeader();
    }
    headerParams.put("Authorization", header);
    return headerParams;
  }

  public boolean isAnalyticsEnabled()
  {
    String enabled = PersistenceConfig.getPropertyValue("mcs.enable-analytics");
    return "true".equalsIgnoreCase(enabled);
  }

  public String getCurrentDateTime()
  {
    //    String pattern = "yyyy-MM-dd HH:mm:ss Z";
    String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    String sdate = sdf.format(now);
    return sdate;
  }

  public String getCurrentTimeZone()
  {
    //        String pattern = "zzzz";
    String pattern = "Z";
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    String sdate = sdf.format(now);
    return sdate;
  }

  protected Location getLocation()
  {
    Location location = null;
    DeviceManager dmgr = DeviceManagerFactory.getDeviceManager();
    //        if (dmgr.getOs().equalsIgnoreCase("ios")){
    try
    {
      // increase max age and disable high accuracy so position gets computed much faster
      // for example, in buildings with large wifi networks (like oracle offices)
      //           this.currentLocation = dmgr.getCurrentPosition(6000, true);
      location = dmgr.getCurrentPosition(600000, false);
    }
    catch (Exception e)
    {
      // cur pos not available, van happen in Android simulator
      sLog.severe("Cannot determine current location: " + e.getLocalizedMessage());
    }
    return location;
  }

  public void setSessionId(String sessionId)
  {
    this.sessionId = sessionId;
  }

  public String getSessionId()
  {
    return sessionId;
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
    return "Basic "+getAnonymousKey();
  }

  public void setUserName(String userName)
  {
    this.userName = userName;
  }

  public String getUserName()
  {
    return userName;
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
