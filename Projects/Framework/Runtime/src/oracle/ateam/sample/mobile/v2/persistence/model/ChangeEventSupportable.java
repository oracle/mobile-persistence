 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.model;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.java.beans.ProviderChangeListener;
import oracle.adfmf.java.beans.ProviderChangeSupport;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;


/**
 *  Abstract class that can be extended to support MAF data change events
 */
public abstract class ChangeEventSupportable
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(ChangeEventSupportable.class);

  protected transient ProviderChangeSupport providerChangeSupport = new ProviderChangeSupport(this);
  protected transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  public void addProviderChangeListener(ProviderChangeListener listener)
  {
    providerChangeSupport.addProviderChangeListener(listener);
  }

  public void removeProviderChangeListener(ProviderChangeListener listener)
  {
    providerChangeSupport.removeProviderChangeListener(listener);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  protected ProviderChangeSupport getProviderChangeSupport()
  {
    return providerChangeSupport;
  }

  protected PropertyChangeSupport getPropertyChangeSupport()
  {
    return propertyChangeSupport;
  }
}

