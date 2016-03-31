/*******************************************************************************
 Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 
 09-feb-2016   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.controller.bean;

import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;

/**
 * This class returns the network connectivity status.
 * It also supports a "forceOffline" mode, by calling the forceOffline or setForceOffline methods.
 * It can be defined as a managed bean to allow easy access to online/offline status, and to force/unforce offline mode.
 * The forceOffline flag is saved as static variable, so it doesn;t matter which instance of this bean is used to
 * change the value.
 */
public class ConnectivityBean
{
  public static final String NETWORK_NOT_REACHABLE = "NotReachable"; // Indicates no network connectivity.
  // we define this propert a static, so it doesn't matter which instance is sued to change value
  private static boolean forceOffline = false;
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


  public void setForceOffline(boolean forceOffline)
  {
    boolean oldForceOffline = this.forceOffline;
    boolean oldOffline = isOffline();
    this.forceOffline = forceOffline;
    propertyChangeSupport.firePropertyChange("forceOffline", oldForceOffline, forceOffline);
    propertyChangeSupport.firePropertyChange("offline", oldOffline, isOffline());
    propertyChangeSupport.firePropertyChange("online", !oldOffline, isOnline());
  }

  public boolean isForceOffline()
  {
    return forceOffline;
  }

  public boolean isOffline()
  {
    if (forceOffline)
    {
      return true;
    }
    String status = getNetworkStatus();
    boolean offline = NETWORK_NOT_REACHABLE.equals(status) || "unknown".equals(status);
    return offline;
  }

  public boolean isOnline()
  {
    return !isOffline();
  }
  
  public String getNetworkStatus()
  {
    return DeviceManagerFactory.getDeviceManager().getNetworkStatus();    
  }

  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    propertyChangeSupport.addPropertyChangeListener(l);
  }

  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    propertyChangeSupport.removePropertyChangeListener(l);
  }
}
