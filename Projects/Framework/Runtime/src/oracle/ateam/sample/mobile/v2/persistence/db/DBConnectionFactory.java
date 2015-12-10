 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
  
  $revision_history$
  10-dec-2015   Steven Davelaar
  1.4           Password encryption type prefix now added in PersistenceConfig.getDatabasePassword method
  25-nov-2015   Steven Davelaar
  1.3           Allow multiple threads to use same DB connection when WAL is enabled.
  19-nov-2015   Steven Davelaar
  1.2           Password needs to be prefixed with encryption type, otherwise non-default encrytpion
                types will not work (Thanks Tim Lam for reporting this)
  05-nov-2015   Steven Davelaar
  1.1           Allows multiple threads to use same connection, is no longer a problems since we are
                now using Write Ahead Logging, see DBPersistenceManager.initDB
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.db;


import SQLite.JDBCDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;

import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;


/**
 * Class that returns a Database Connection. The database name is read from
 * mobile-persistence-config.properties file that should be stored in the META-INF directory of the
 * ApplicationController project.
 * Since we are using Write Ahead Logging when creating the DB (see https://www.sqlite.org/wal.html) we
 * can have concurrent read operations, write operations will be executed sequentially by SQLite
 *
 */
public class DBConnectionFactory
{

  // This was the old way this worked befire we switched to WAL:
  //    * Since SQLite is a single-user database it can be accessed by only one DB connection
  //    * at a time. Since multiple background threads might want to access the database simultaneously, this class
  //    * ensures that only one connection is used for the main thread and all background threads. This is implemented by
  //    * a "wait": if a connection is requested, and the connection is still "in use" by another thread, it will wait for at
  //    * most 30 seconds to see whether the connection has been released in the meantime by the other thread.
  //    * After 30 seconds an error is thrown.
  //    * For this mechanism to work correctly, you always need to call releaseConnection() in the finally block of your
  //    * code when calling getConnection() in the try block.

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
        sLog.fine("Creating SQLite database connection using " + connStr);
        JDBCDataSource dataSource = new JDBCDataSource(connStr);
        // specifiy password if DB is/should be encrypted
        if (PersistenceConfig.encryptDatabase())
        {
          String pwString = PersistenceConfig.getDatabasePassword();
          conn = dataSource.getConnection(null, pwString);
        }
        else
        {
          conn = dataSource.getConnection();
        }
      }
      catch (SQLException e)
      {
        sLog.severe("Could not create SQLite database connection: " + e.getLocalizedMessage());
        // database not found
        throw new AdfException(e);
      }
      catch (Exception e)
      {
        throw e;
      }
    }
     // if we are  using Write Ahead Logging, then SQLite will take care of serailizing write calls, concurrent read calls are
     // supported with WAL, see https://www.sqlite.org/wal.html, otherwise, we allow only one thread to use the cnnection and only hand it out once
    // it has been released again
    if (!PersistenceConfig.useWAL())
    {
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
    }   
    return conn;
  }

  /**
   * No need to call this method when you are using Write Ahead Logging
   */
  public static void releaseConnection()
  {
    connectionInUse = false;
  }

  public static void closeConnectionIfNeeded()
  {
    if (conn != null)
    {
      try
      {
        sLog.info("Closing SQLite DB connection");
        conn.close();
        conn = null;
      }
      catch (SQLException e)
      {
        sLog.severe("Error closing connection " + e.getMessage());
      }
    }
  }


}
