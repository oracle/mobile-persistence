/*******************************************************************************
 Copyright: see readme.txt
 
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
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.JSONBeanSerializationHelper;
import oracle.adfmf.framework.api.Model;
import oracle.adfmf.framework.internal.AdfmfContainerUtilitiesInternal;

import oracle.ateam.sample.mobile.Version;
import oracle.ateam.sample.mobile.persistence.metadata.PersistenceConfig;
import oracle.ateam.sample.mobile.persistence.service.EntityCRUDService;

/**
 * Class used to track the mobiel app usage. In persistence-config, two endpoints can be configured: one to register
 * the start of an application, and one to register when an error occurs while incoking some REST service.
 * A-Team uses this internally to track Oracle internal apps, but customers can use the same facility by simply
 * changing the endpoints in persistence config.
 */
public class UsageTracker
{

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(UsageTracker.class);
  private static Location location;
  private boolean locationInit = true;

  public UsageTracker()
  {
    super();
  }

  protected boolean isOffline()
  {
    String networkStatus = DeviceManagerFactory.getDeviceManager().getNetworkStatus();
    boolean offline = "NotReachable".equals(networkStatus) || "unknown".equals(networkStatus);
    return offline;
  }

  protected String getCurrentDateTime()
  {
    String pattern = "yyyy-MM-dd HH:mm:ss Z";
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

  protected Map getBasePayload()
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

    Map payload = new HashMap();
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
    String useLoc = PersistenceConfig.getPropertyValue("tracking.use.location");
    if ("true".equalsIgnoreCase(useLoc))
    {
      Location loc = getLocation();
      if (loc != null)
      {
        Map locMap = new HashMap();
        payload.put("location", locMap);
        locMap.put("lat", loc.getLatitude() + "");
        locMap.put("lng", loc.getLongitude() + "");
      }
    }
    return payload;
  }

  protected void sendMessage(String connection, String uri,Map payload)
  {
    try
    {
      String json = JSONBeanSerializationHelper.toJSON(payload).toString();
      RestServiceAdapter rsa = Model.createRestServiceAdapter();
      rsa.clearRequestProperties();
      rsa.addRequestProperty("Content-Type", "application/json");
      rsa.setConnectionName(connection);
      rsa.setRequestType(RestServiceAdapter.REQUEST_TYPE_POST);
      rsa.setRequestURI(uri);
      rsa.send(json);
    }
    catch (Exception e)
    {
      sLog.severe("Error invoking tracking start resource: " + e.getLocalizedMessage());
    }

  }

  public void registerStartAppMessage()
  {
    final String trackingConnection = PersistenceConfig.getPropertyValue("tracking.connection");
    final String trackingStartUri = PersistenceConfig.getPropertyValue("tracking.start.uri");
    if (trackingConnection == null || trackingStartUri == null || isOffline())
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
        sendMessage(trackingConnection, trackingStartUri,payload);
      }
    };
    Thread thread = new Thread(runnable);
    thread.start();
  }

  public void registerErrorMessage(final String message)
  {
    final String trackingConnection = PersistenceConfig.getPropertyValue("tracking.connection");
    final String trackingErrorUri = PersistenceConfig.getPropertyValue("tracking.error.uri");
    if (trackingConnection == null || trackingErrorUri == null || isOffline())
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
        sendMessage(trackingConnection, trackingErrorUri,payload);
      }
    };
    Thread thread = new Thread(runnable);
    thread.start();
  }

}
