/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 29-dec-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.mcs.analytics;

import java.io.UnsupportedEncodingException;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.UUID;

import java.util.Vector;

import oracle.adfmf.framework.api.JSONBeanSerializationHelper;

import oracle.adfmf.json.JSONArray;

import oracle.adfmf.json.JSONObject;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.OfflineUtils;
import oracle.ateam.sample.mobile.util.TaskExecutor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;

import oracle.ateam.sample.mobile.mcs.analytics.MCSAnalyticsEvent;

import oracle.ateam.sample.mobile.mcs.analytics.MCSContextEvent;

import oracle.ateam.sample.mobile.mcs.analytics.MCSSystemEvent;
import oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.manager.MCSPersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.service.DataSynchAction;

import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;

import sun.misc.BASE64Encoder;


public class MCSAnalyticsService
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(MCSAnalyticsService.class);
  private String sessionId;
  private List<MCSAnalyticsEvent> analyticsEvents = new ArrayList<MCSAnalyticsEvent>();
  private transient static MCSAnalyticsService instance;
  private boolean addingContextEvent = false;

  private MCSAnalyticsService()
  {
    super();
  }

  public static MCSAnalyticsService getInstance()
  {
    if (instance == null)
    {
      instance = new MCSAnalyticsService();
    }
    return instance;
  }

  public void startSession()
  {
    sessionId = UUID.randomUUID().toString();
    MCSAnalyticsEvent ss = new MCSSystemEvent("sessionStart", sessionId);
    addEvent(ss);
  }

  public void endSession()
  {
    if (sessionId != null)
    {
      MCSAnalyticsEvent se = new MCSSystemEvent("sessionEnd", sessionId);
      addEvent(se);
      sendAnalyticsEvents();
    }
  }

  public void sendAnalyticsEvents()
  {
    if (!isAnalyticsEnabled())
    {
      return;
    }
    TaskExecutor.getInstance().execute(true, () ->
      {
        // serialize the analytics payload. If we are online, send it, if we are offline, save it in DB
        // we use data synch actions table to store when offline
        String payload = null;
        try
        {
          if (analyticsEvents.size()>0)
          {
            Vector events = (Vector) JSONBeanSerializationHelper.toJSON(analyticsEvents);
            for (int i = 0; i < events.size(); i++)
            {
              JSONObject event = (JSONObject) events.get(i);
              event.remove(".type");
            }
            payload = events.toString();            
          }
        }
        catch (Exception e)
        {
          sLog.severe("Cannot create json payload: " + e.getLocalizedMessage());
          return;
        }
        // clear analyticsEvents list, so we will create new context event next time addEvent is called.
        analyticsEvents.clear();

        DBPersistenceManager pm = new DBPersistenceManager();
        String className = MCSAnalyticsEvent.class.toString();                              
        if (OfflineUtils.isOffline())
        {
           sLog.info("We are offline, save MCS analytics events to local DB");
           DataSynchAction action = new DataSynchAction(DataSynchAction.INSERT_ACTION,className,payload,MCSPersistenceManager.class.toString());
           EntityUtils.generatePrimaryKeyValue(action, 1);
           pm.insertEntity(action, true);
        }
        else
        {
          // first send any pending analytics events that were saved before when offline
          List<String> attrNamesToSearch = new ArrayList<String>();
          attrNamesToSearch.add("entityClass");
          List<DataSynchAction> dataSynchActions = pm.find(DataSynchAction.class, className, attrNamesToSearch);
          int count = dataSynchActions.size();
          if (count>0)
          {
            sLog.info("Sending MCS analytics events previously saved while offline");            
          }
          MCSPersistenceManager mpm = new MCSPersistenceManager();
          for (int i = 0; i < count; i++)
          {
            DataSynchAction action = dataSynchActions.get(i);
            mpm.sendAnalyticsEvents(action.getEntityAsJSONString());
            pm.removeEntity(action, true);
          } 
          // send the current list of events if any
          if (payload!=null)
          {
            sLog.info("Sending MCS analytics events");            
            mpm.sendAnalyticsEvents(payload);
          }
        }
      });
  }

  public boolean isAnalyticsEnabled()
  {
    String enabled = PersistenceConfig.getPropertyValue("mcs.enable-analytics");
    return "true".equalsIgnoreCase(enabled);
  }

  public void setSessionId(String sessionId)
  {
    this.sessionId = sessionId;
  }

  public String getSessionId()
  {
    return sessionId;
  }

  public void addCustomEvent(String name, Map<String,Object> properties)
  {
      MCSAnalyticsEvent event = new MCSAnalyticsEvent(name,sessionId,properties);
      addEvent(event);
  }

  public void addEvent(MCSAnalyticsEvent event)
  {
    // execute in background because when adding contxt event, the current location needs to be fetched
    // which can take a while. To prevent subsequent calls to addEvent to succeed before the context
    // event is added (which would cause invalid payload), we use TaskExecutor to execute the actions sequentially
    // in background thread.
    TaskExecutor.getInstance().execute(true, () ->
    {
      if (analyticsEvents.size() == 0 && !addingContextEvent)
      {
        // first add the context event
        addingContextEvent = true;
        analyticsEvents.add(new MCSContextEvent());
        addingContextEvent = false;
      }
      analyticsEvents.add(event);
    });                                       
  }
}
