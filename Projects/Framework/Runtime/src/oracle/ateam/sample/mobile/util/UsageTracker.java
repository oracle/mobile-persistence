/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 10-nov-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.util;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import oracle.adf.model.datacontrols.device.DeviceManager;
import oracle.adf.model.datacontrols.device.DeviceManagerFactory;
import oracle.adf.model.datacontrols.device.Location;

import oracle.adfmf.dc.ws.rest.RestServiceAdapter;
import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.JSONBeanSerializationHelper;

import oracle.ateam.sample.mobile.Version;
import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;

/**
 * Class used to track the mobile app usage. In mobile-persistence-config, two endpoints can be configured: one to register
 * the start of an application, and one to register when an error occurs while incoking some REST service.
 * A-Team uses this internally to track Oracle internal apps, but customers can use the same facility by simply
 * changing the endpoints in persistence config.
 * Tracking is switched on/off through propety tracking.enabled=true/false in mobile-persistence-config.
 */
public class UsageTracker
{

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(UsageTracker.class);
  private static Location location;
  private boolean locationInit = true;
  private final static String CONNECTION_END_POINT = "https://adc-ofm0187-gse.oracledemos.com/store";
  private final static String START_URI = "/applications/starts";
  private final static String ERROR_URI = "/applications/errors";

  public UsageTracker()
  {
    super();
  }

  protected boolean isOffline()
  {
    return OfflineUtils.isOffline();
  }

  protected String getCurrentDateTime()
  {
    String pattern = "yyyy-MM-dd HH:mm:ss Z";
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    String sdate = sdf.format(now);
    return sdate;
  }

  protected String getCurrentTimeZone()
  {
    String pattern = "zzzz";
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    String sdate = sdf.format(now);
    return sdate;
  }

  protected Location getLocation()
  {
    if (locationInit)
    {
      locationInit = false;
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
    }
    return location;
  }

  protected boolean isUseLocation()
  {
    String useLoc = PersistenceConfig.getPropertyValue("tracking.use.location");
    return "true".equalsIgnoreCase(useLoc);     
//    if (useLoc!=null)
//    {
//      return "true".equalsIgnoreCase(useLoc);     
//    }
//    return true;    
  }

  protected Map<String,Object> getBasePayload()
  {
    // device info
    String networkStatus = DeviceManagerFactory.getDeviceManager().getNetworkStatus();
    String model = DeviceManagerFactory.getDeviceManager().getModel(); // iPad Simulator x86_64
    String os = DeviceManagerFactory.getDeviceManager().getOs(); // iOS
    String version = DeviceManagerFactory.getDeviceManager().getVersion(); // 7.1
    String platform = DeviceManagerFactory.getDeviceManager().getPlatform(); // iPad Simulator

    // app info
    String appId = AdfmfContainerUtilities.getApplicationInformation().getId();
    String appVendor = AdfmfContainerUtilities.getApplicationInformation().getVendor();
    String appVersion = AdfmfContainerUtilities.getApplicationInformation().getVersion();

    Map<String,Object> payload = new HashMap<String,Object>();
    payload.put("appId", appId);
    payload.put("appVersion", appVersion);
    String appstoreAppId = PersistenceConfig.getPropertyValue("tracking.appstore.appid");
    if (appstoreAppId != null)
    {
      payload.put("appStoreAppId", appstoreAppId);
    }
    payload.put("os", os);
    payload.put("osVersion", version);
    payload.put("model", model);
    payload.put("network", networkStatus);
    payload.put("ampaVersion", Version.VERSION);
    payload.put("timeZone", getCurrentTimeZone());
    if (isUseLocation())
    {
      Location loc = getLocation();
      if (loc != null)
      {
        Map<String,String> locMap = new HashMap<String,String>();
        payload.put("location", locMap);
        locMap.put("lat", loc.getLatitude() + "");
        locMap.put("lng", loc.getLongitude() + "");
      }
    }
    return payload;
  }

  protected void sendMessage(String connectionEndPoint, String uri,Map payload)
  {
    try
    {
      String json = JSONBeanSerializationHelper.toJSON(payload).toString();
      RestServiceAdapter rsa = new NoConnRestServiceAdapterImpl(connectionEndPoint);
      rsa.clearRequestProperties();
      rsa.addRequestProperty("Content-Type", "application/json");
      rsa.setRequestType(RestServiceAdapter.REQUEST_TYPE_POST);
      rsa.setRequestURI(uri);
      rsa.send(json);
    }
    catch (Exception e)
    {
      sLog.severe("Error invoking tracking start resource: " + e.getLocalizedMessage());
    }

  }

  protected String getConnectionEndPoint()
  {
    String trackingConnection = PersistenceConfig.getPropertyValue("tracking.connection.endpoint");
    return trackingConnection!=null ? trackingConnection : CONNECTION_END_POINT;
  }

  protected String getStartUri()
  {
    String startUri = PersistenceConfig.getPropertyValue("tracking.start.uri");
    return startUri!=null ? startUri : START_URI;
  }

  protected String getErrorUri()
  {
    String errorUri = PersistenceConfig.getPropertyValue("tracking.error.uri");
    return errorUri!=null ? errorUri : ERROR_URI;
  }

  protected boolean isTrackingEnabled()
  {
    String trackingEnabled = PersistenceConfig.getPropertyValue("tracking.enabled");
    return "true".equalsIgnoreCase(trackingEnabled);
  }

  public void registerStartAppMessage()
  {
    if (!isTrackingEnabled())
    {
      return;
    }
    final String connectionEndPoint = getConnectionEndPoint();
    final String trackingStartUri = getStartUri();
    if (connectionEndPoint == null || trackingStartUri == null || isOffline())
    {
      return;
    }
    // send message in background thread so we don't delay app start-up
    Runnable runnable = new Runnable()
    {
      public void run()
      {
        Map payload = getBasePayload();
        payload.put("startTime", getCurrentDateTime());
        sendMessage(connectionEndPoint, trackingStartUri,payload);
      }
    };
    Thread thread = new Thread(runnable);
    thread.start();
  }


  public void registerErrorMessage(final String message)
  {
    if (!isTrackingEnabled())
    {
      return;
    }
    final String connectionEndPoint = getConnectionEndPoint();
    final String trackingErrorUri = getErrorUri();
    if (connectionEndPoint == null || trackingErrorUri == null || isOffline())
    {
      return;
    }
    // send message in background thread so we don't delay app start-up
    Runnable runnable = new Runnable()
    {
      public void run()
      {
        Map payload = getBasePayload();
        payload.put("errorTime", getCurrentDateTime());
        payload.put("errorMessage", message);
        sendMessage(connectionEndPoint, trackingErrorUri,payload);
      }
    };
    Thread thread = new Thread(runnable);
    thread.start();
  }

}
