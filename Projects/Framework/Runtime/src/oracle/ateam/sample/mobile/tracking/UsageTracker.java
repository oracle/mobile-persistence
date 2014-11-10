/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 10-nov-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.tracking;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adfmf.dc.ws.rest.RestServiceAdapter;
import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.JSONBeanSerializationHelper;
import oracle.adfmf.framework.api.Model;
import oracle.adfmf.framework.internal.AdfmfContainerUtilitiesInternal;

import oracle.ateam.sample.mobile.Version;
import oracle.ateam.sample.mobile.persistence.metadata.PersistenceConfig;

/**
 * Class used to track the mobiel app usage. In persistence-config, two endpoints can be configured: one to register
 * the start of an application, and one to register when an error occurs while incoking some REST service.
 * A-Team uses this internally to track Oracle internal apps, but customers can use the same facility by simply
 * changing the endpoints in persistence config.
 */
public class UsageTracker
{
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
    String pattern = "yyyy-MM-dd'T'HH:mm:ss";
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    String sdate = sdf.format(now);
    return sdate;
  }

  public void sendStartAppMessage()
  {
    String trackingConnection = PersistenceConfig.getPropertyValue("tracking.connection");
    String trackingStartUri = PersistenceConfig.getPropertyValue("tracking.start.uri");
    if (trackingConnection == null || trackingStartUri == null || isOffline())
    {
      return;
    }

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
    payload.put("deviceId", appId);
    payload.put("os", os);
    payload.put("model", model);
    payload.put("ampaVersion", Version.VERSION);
    payload.put("startTime", getCurrentDateTime());
    try
    {
      String json = JSONBeanSerializationHelper.toJSON(payload).toString();
      RestServiceAdapter rsa = Model.createRestServiceAdapter();
      rsa.clearRequestProperties();
      rsa.setConnectionName(trackingConnection);
      rsa.setRequestType(RestServiceAdapter.REQUEST_TYPE_POST);
      rsa.setRequestURI(trackingStartUri);
      rsa.send(json);
    }
    catch (Exception e)
    {
    }

    //    {
    //      "deviceId": "deviceId send from persistent framework",
    //      "location": {
    //          "lat": 51.0,
    //          "lng": -0.1
    //      },
    //      "accuracy": 1200.4,
    //      "message": "(optinal)",
    //      "starttime": "timestamp of the time app started",
    //      "os": "iOs,Adroid,rtc.."
    //    }


  }
}
