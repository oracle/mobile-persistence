 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  29-dec-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
package oracle.ateam.sample.mobile.mcs.analytics;

import java.text.SimpleDateFormat;

import java.util.Date;

import oracle.adf.model.datacontrols.device.DeviceManager;
import oracle.adf.model.datacontrols.device.DeviceManagerFactory;
import oracle.adf.model.datacontrols.device.Location;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.DateUtils;

/**
 * Internal use only
 * Class used to create MCS analytics context event in MCSPersistenceManager
 */

public class ContextEvent extends AnalyticsEvent
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(ContextEvent.class);
  // define transient to prevent inclusion in MCS payload when serializing to JSON
  protected transient String sessionID;

  public ContextEvent()
  {
    super(true,"context",null);
    setContextProperties();
  }

  protected void setContextProperties()
  {
    String networkStatus = DeviceManagerFactory.getDeviceManager().getNetworkStatus();
    String model = DeviceManagerFactory.getDeviceManager().getModel(); // iPad Simulator x86_64
    String os = DeviceManagerFactory.getDeviceManager().getOs(); // iOS
    String version = DeviceManagerFactory.getDeviceManager().getVersion(); // 7.1
    String platform = DeviceManagerFactory.getDeviceManager().getPlatform(); // iPad Simulator
    String userName = (String) AdfmfJavaUtilities.evaluateELExpression("#{securityContext.userName}");
    if (userName != null)
    {
      properties.put("userName", userName);
    }
    properties.put("model", model);
    properties.put("timezone", DateUtils.getCurrentTimeZone());
    properties.put("manufacturer", platform);
    properties.put("osName", os);
    properties.put("osVersion", version);
    properties.put("carrier", networkStatus);
    Location loc = determineLocation();
    if (loc != null)
    {
      // lat/lng must be sent as string, otherwise error returned!
      properties.put("longitude", loc.getLongitude() + "");
      properties.put("latitude", loc.getLatitude() + "");
    }
  }

  protected Location determineLocation()
  {
    Location location = null;
    DeviceManager dmgr = DeviceManagerFactory.getDeviceManager();
    if (!dmgr.hasGeolocation())
    {
      return null;
    }
    try
    {
      // increase max age and disable high accuracy so position gets computed much faster
      // for example, in buildings with large wifi networks (like oracle offices)
      //           this.currentLocation = dmgr.getCurrentPosition(6000, true);
      // time out after 30 secs
      location = dmgr.getCurrentPosition(600000,30000, false);
      sLog.info("Current location: lat=" + location.getLatitude()+" long=" + location.getLongitude());
    }
    catch (Exception e)
    {
      // cur pos not available, van happen in Android simulator
      sLog.severe("Cannot determine current location: " + e.getLocalizedMessage());
    }
    return location;
  }
}
