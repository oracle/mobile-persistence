 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  29-dec-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
package oracle.ateam.sample.mobile.mcs.analytics;

import java.util.HashMap;
import java.util.Map;

import oracle.ateam.sample.mobile.util.DateUtils;

/**
 * Class that can be used to create MCS analytics custom events
 * You can create an instance of this class and call MCSPersistenceManager.addEvent passing the instance
 * as a parameter, or use the convenience method addCustomEvent on MCSPersistenceManager directly
 */
public class AnalyticsEvent
{
  
  private String name;
  private String type;
  private String timestamp;
  // protected so we can define it as transient in context event which cannot have sessionId
  protected String sessionID;
  // protected so we can define it as transient in system event which cannot have sessionId
  protected Map<String, Object> properties = new HashMap<String,Object>();

  /**
   * Constructor used for system event
   * Internal use only, to create system event, use MCSSystemEvent class
   * @param systemEvent
   * @param name
   * @param sessionId
   */
  protected AnalyticsEvent(boolean systemEvent,String name, String sessionId)
  {
    super();
    setType(systemEvent ? "system" : "custom");
    setName(name);
    setTimestamp(DateUtils.getCurrentDateTime());
    setSessionID(sessionId);
  }

  /**
   * Constructor to use for custom event. 
   * @param name
   * @param properties
   */
  public AnalyticsEvent(String name, String sessionId, Map<String,Object> properties)
  {
    super();
    setName(name);
    setType("custom");
    setTimestamp(DateUtils.getCurrentDateTime());
    setSessionID(sessionId);
    if (properties!=null)
    {
      setProperties(properties);      
    }
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getType()
  {
    return type;
  }

  public void setTimestamp(String timestamp)
  {
    this.timestamp = timestamp;
  }

  public String getTimestamp()
  {
    return timestamp;
  }

  public void setSessionID(String sessionId)
  {
    this.sessionID = sessionId;
  }

  public String getSessionID()
  {
    return sessionID;
  }

  public void setProperties(Map<String, Object> properties)
  {
    this.properties = properties;
  }

  public Map<String, Object> getProperties()
  {
    return properties;
  }

  public void addProperty(String key, Object value)
  {
    properties.put(key, value);
  }

}
