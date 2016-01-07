package oracle.ateam.sample.mobile.util;

import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

public class OfflineUtils
{
  public static final String NETWORK_NOT_REACHABLE = "NotReachable"; // Indicates no network connectivity.
  private static boolean sForceOffline = false;

  public static void forceOffline(boolean force)
  {
    sForceOffline = force;
  }

  public static boolean isOffline()
  {
    if (sForceOffline)
    {
      return true;
    }
    String status = DeviceManagerFactory.getDeviceManager().getNetworkStatus();
    boolean offline = NETWORK_NOT_REACHABLE.equals(status) || "unknown".equals(status);
    return offline;
  }

  public static boolean isOnline()
  {
    return !isOffline();
  }

}
