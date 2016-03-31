/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 05-apr-2014   Steven Davelaar
 1.3           Removed test connection
 06-jun-2013   Steven Davelaar
 1.1           Added method closeConnectionIfNeeded, set password when encryption is needed
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.db;


import SQLite.JDBCDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;

import oracle.ateam.sample.mobile.persistence.metadata.PersistenceConfig;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;


/**
 * Class that returns a Database Connection. The database name is read from
 * mobile-persistence-config.properties file that should be stored in the META-INF directory of the
 * ApplicationController project. Since SQLite is a single-user database it can be accessed by only one DB connection 
 * at a time. Since multiple background threads might want to access the database simultaneously, this class 
 * ensures that only one connection is used for the main thread and all background threads. This is implemented by 
 * a "wait": if a connection is requested, and the connection is still "in use" by another thread, it will wait for at
 * most 30 seconds to see whether the connection has been released in the meantime by the other thread. 
 * After 30 seconds an error is thrown.
 * For this mechanism to work correctly, you always need to call releaseConnection() in the finally block of your 
 * code when calling getConnection() in the try block.
 * 
 * @deprecated Use the class with same name in oracle.ateam.sample.mobile.v2.persistence.* instead
 */
public class DBConnectionFactory
{

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(DBConnectionFactory.class);
  protected static Connection conn = null;
  private static boolean connectionInUse = false;

  public static Connection getConnection()
    throws Exception
  {
    if (conn == null)
    {
      try
      {
        // create a database connection
        String dir = AdfmfJavaUtilities.getDirectoryPathRoot(AdfmfJavaUtilities.ApplicationDirectory);
        String connStr = "jdbc:sqlite:" + dir + "/" + PersistenceConfig.getDatabaseName();
        sLog.fine("Creating SQLite database connection using "+connStr);
        JDBCDataSource dataSource = new JDBCDataSource(connStr);
        // specifiy password if DB is/should be encrypted
        if (PersistenceConfig.encryptDatabase())
        {
          conn = dataSource.getConnection(null, PersistenceConfig.getDatabasePassword());
        }
        else
        {
          conn = dataSource.getConnection();          
        }
      }
      catch (SQLException e)
      {
        sLog.severe("Could not create SQLite database connection: "+e.getLocalizedMessage());
        // database not found
        throw new AdfException(e);
      }
      catch (Exception e)
      {
        throw e;
      }
    }
    long startTime = System.currentTimeMillis();
    while (connectionInUse)
    {
      // another thread is still using the connection, wait until released
      // with a maximum of 30 seconds
      if (System.currentTimeMillis()-startTime > 30000)
      {
        sLog.severe("Cannot acquire DB connection, another process might still be using the connection or connection is not properly released.");
        MessageUtils.handleError("Cannot acquire DB connection, another process might still be using the connection or connection is not properly released. Try again later.");
      }
    }
    connectionInUse = true;
    return conn;
  }

  public static void releaseConnection()
  {
    connectionInUse = false;
  }

  public static void closeConnectionIfNeeded()
  {
    if (conn!=null)
    {
      try
      {
        sLog.info("Closing SQLite DB connection");
        conn.close();
        conn=null;
      }
      catch (SQLException e)
      {
        sLog.severe("Error closing connection "+e.getMessage());
      }
    }
  }



}
