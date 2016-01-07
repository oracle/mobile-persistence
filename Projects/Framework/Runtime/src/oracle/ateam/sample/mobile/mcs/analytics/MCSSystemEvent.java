 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  29-dec-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
package oracle.ateam.sample.mobile.mcs.analytics;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;

/**
 * Class used to create MCS analytics system events like start/end session
 * You can create an instance of this class and call MCSPersistenceManager.addEvent passing the instance
 * as a parameter, or use the convenience methods startSession and endSession on MCSPersistenceManager directly
 */
public class MCSSystemEvent extends MCSAnalyticsEvent
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(MCSContextEvent.class);
  // define transient to prevent inclusion in MCS payload when serializing to JSON
  protected transient String properties;

  public MCSSystemEvent(String name, String sessionId)
  {
    super(true,name,sessionId);
  }

}
