/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 26-jan-2015   Steven Davelaar
 1.1           Shut down task executor thread pool in deactive method
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.lifecycle;

import oracle.adfmf.application.LifeCycleListener;

import oracle.ateam.sample.mobile.v2.persistence.db.DBConnectionFactory;
import oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MCSManager;
import oracle.ateam.sample.mobile.util.TaskExecutor;
import oracle.ateam.sample.mobile.util.UsageTracker;


/**
 * This class can be used as Lifecycle listener or can be extended by your own life cycle listener.
 * In the start method it will check whether a local database exists on your mobile device, and if it doesn't
 * it will create the database.
 * The check for the database is done by checking for the existence of the database file.
 * The name of the SQL DDL file to execute is found in in mobile-persistence-config.properties.
 * The name of the database is also defined in this properties file.
 * This properties file should be located in the META-INF folder of your ApplicationController project.
 * Here is an example of the entries that must be present
 * in the property file to make this InitDBLifeCycleListener class work correctly:
 * <pre>
 * db.name=HR.db
 * persistence.mapping.xml=META-INF/tlMap.xml
 * ddl.script=META-INF/hr.sql
 * </pre>
 *
 *
 */
public class InitDBLifeCycleListener
  implements LifeCycleListener
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(InitDBLifeCycleListener.class);

  public InitDBLifeCycleListener()
  {
  }

  /**
   * Initializes the on-device database if needed.
   */
  public void start()
  {
    sLog.fine("Excuting InitDBLifeCycleListener.start method");
    new UsageTracker().registerStartAppMessage();
    DBPersistenceManager pm= new DBPersistenceManager();
    pm.initDBIfNeeded();
  }

  /**
   * No action performed here.
   */
  public void stop()
  {
    // Add code here...
  }

  /**
   * No action performed here.
   */
  public void activate()
  {
    MCSManager.getInstance().startSession();
  }

  /**
   * This method closes the DB connection if needed, and shuts down the single thread pool
   * used to execute background tasks.
   */
  public void deactivate()
  {
    MCSManager.getInstance().endSession();
    DBConnectionFactory.closeConnectionIfNeeded();
    TaskExecutor.shutDown();
  }

}
