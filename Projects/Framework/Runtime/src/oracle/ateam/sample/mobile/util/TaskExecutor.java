/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 10-mar-2016   Steven Davelaar
 1.3           Added getSequentialInstance method, renamed isDBInstance to updateUI and switched true/false meaning
 14-feb-2016   Steven Davelaar
  1.2           Added support for obtaining an instance that supports parallel task execution
 14-feb-2015   Steven Davelaar
  1.1           changed implementation to have one instance per feature. This is needed because flushing of data change 
                events doesn't work in any feature except for the initial feature that created an instnce of this class
 24-jan-2015   Steven Davelaar
  1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.MafExecutorService;

import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;

/**
 * This class is used to execute tasks (runnables), either in foreground or in background.
 * Depending on the type of instance obtained and parameters passed in, the background tasks are executed sequentially
 * or in parallel.
 * When calling the getInstance() method without arguments, the value of property enable.parallel.rest.calls in
 * mobile-persistence.config.properties  determines the type of instance returned. If set to true, the instance
 * uses a multiThreadPool enabling parallel executing of multiple tasks. If set to false, a single thread
 * pool is used which means all tasks are executed sequentially. To explicitly obtain a specific instance type, call the
 * getInstance() method with the boolean seiqential argument.
 *
 * When making multiple remote REST calls, the sequence migh be important as result of the first call might be needed by the next call.
 * Furthermore, actions against the local SQLite database might require sequential execution, for example to prevent
 * multiple threads writing at same time, which can cause errors about nesting transactions not being allowed.
 *
 * Finally, this class sets an application scope boolean flag to indicate whether
 * there are background tasks running. This flag can be used to show some sort of spinning wheel icon in the UI, or
 * simply some text message like "Loading data". The expression to evaluate this flag is #{applicationScope.ampa_bgtask_running}
 * When ontaning a DBInstance, this flag is not set.
 */
public class TaskExecutor
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(TaskExecutor.class);

  private ThreadPoolExecutor executor = null;

  private boolean running = false;

  private Future<?> lasttFuture = null;

  private static Map<String, TaskExecutor> instanceMap = new HashMap<String, TaskExecutor>();
  
  private boolean sequential;
  // set/update boolean EL expression that background task is runnning so UI can show spinning wheel
  private boolean updateUI = true;

  public TaskExecutor(boolean sequential)
  {
    super();
    this.sequential = sequential;
  }

  /**
   * Returns an instance based on the current feature. We cannot share the instance across features because
   * flushing of data change events would not work in any feature except for the feature that creates the instance.
   * 
   * The property enable.parallel.rest.calls in mobile-persistence.config.properties  determines
   * whether the instance uses a multiThreadPool enabling parallel executing of multiple tasks, or a single thread
   * pool which means all tasks are executed sequentially.
   * 
   * @return
   */
  public static synchronized TaskExecutor getInstance()
  {
    return getInstance(!PersistenceConfig.enableParallelRestCalls());
  }

  /**
   * Returns an instance based on the current feature. We cannot share the instance across features because
   * flushing of data change events would not work in any feature except for the feature that creates the instance.
   * @sequential when true, a single-threaded TaskExecutor instance is returned which means all tasks are executed 
   * sequentially. Otherwise, a multi-threaded TaskExecutor is returned that processes the tasks in parallel.
   * @return
   */
  public static synchronized TaskExecutor getInstance(boolean sequential)
  {
    String featureId = AdfmfJavaUtilities.getFeatureId();
    if (featureId==null)
    {
      // we are in application start-up phase
      featureId = "_appStartup";
    }
    TaskExecutor instance = instanceMap.get(featureId);
    if (instance == null)
    {
      sLog.fine("Creating new instance of TaskExecutor for feature " + featureId);
      instance = new TaskExecutor(sequential);
      instanceMap.put(featureId, instance);
    }
    return instance;
  }

  /**
   * Returns the instance used to execute DB SQL statements. This instance always executes 
   * tasks sequentially using a singlethread pool.
   * 
   * @return
   */
  public static synchronized TaskExecutor getDBInstance()
  {
    String instanceId = "DB";
    TaskExecutor instance = instanceMap.get(instanceId);
    if (instance == null)
    {
      sLog.fine("Creating new instance of TaskExecutor for " + instanceId);
      instance = new TaskExecutor(true);
      instance.updateUI = false;
      instanceMap.put(instanceId, instance);
    }
    return instance;
  }

  /**
   * Returns an instance based on the key that always executes tasks sequentially using a singlethread pool.
   * This instance does NOT set/update the #{applicationScope.ampa_bgtask_running} expression value
   * @return
   */
  public static synchronized TaskExecutor getSequentialInstance(String key)
  {
    String instanceId = key;
    TaskExecutor instance = instanceMap.get(instanceId);
    if (instance == null)
    {
      sLog.fine("Creating new instance of TaskExecutor for " + instanceId);
      instance = new TaskExecutor(true);
      instance.updateUI = false;
      instanceMap.put(instanceId, instance);
    }
    return instance;
  }

  protected synchronized void initIfNeeded()
  {
    if (executor == null)
    {
      sLog.fine("Creating new single thread pool in TaskExecutor");
      // we cannot use this shortcut to create the single-thread pool because we need access to the size
      //    ExecutorService executor = Executors.newSingleThreadExecutor();
      // Nor can we cast to ThreadPoolExecutor because it is returned inside a wrapper class:
      //      public static ExecutorService newSingleThreadExecutor() {
      //        return new FinalizableDelegatedExecutorService
      //        (new ThreadPoolExecutor(1, 1,
      //         0L, TimeUnit.MILLISECONDS,
      //         new LinkedBlockingQueue<Runnable>()));
      //       }
      if (this.sequential)
      {
        executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());        
      }
      else
      {
        executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                             60L, TimeUnit.SECONDS,
                                              new SynchronousQueue<Runnable>());
      }
    }
  }

  /**
   * This method toggles the boolean expression #{applicationScope.ampa_bgtask_running} between true and false
   * depending in whether there are still task being executed in the instance.
   * @param running
   */
  protected void setRunning(boolean running)
  {
    executeUIRefreshTask(() ->
     {
       if (updateUI)
       {
         AdfmfJavaUtilities.setELValue("#{applicationScope.ampa_bgtask_running}", running);      
       }
       AdfmfJavaUtilities.flushDataChangeEvent();       
     });
  }

  protected synchronized void updateStatus()
  {
    if (!running)
    {
      Runnable r2 = () ->
        {
          int size = executor.getQueue().size();
          if (size > 0 || !lasttFuture.isDone())
          {
            setRunning(true);
          }
          while (size > 0 || !lasttFuture.isDone())
          {
            sLog.finest("Number of background tasks in Queue in TaskExecutor: " + size);
            try
            {
              Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
            }
            size = executor.getQueue().size();
          }
          setRunning(false);
//          AdfmfJavaUtilities.flushDataChangeEvent();
          sLog.finest("No more background tasks running in TaskExecutor: " + size);
        };
      Thread t = new Thread(r2);
      t.start();
    }
  }

  /**
   * Execute a task. If the task is to be executed in the background and the instance is using a singleThread pool
   * than all tasks are executed in the sequence of submission.
   * To check whether there are any background tasks running in this thread pool, you can use 
   * the boolean expression #{applicationScope.ampa_bgtask_running}.
   * @param inBackground
   * @param task
   */
  public void execute(boolean inBackground, Runnable task)
  {
    if (inBackground)
    {
      initIfNeeded();
      lasttFuture = executor.submit(task);
      updateStatus();        
    }
    else
    {
      task.run();
    }
  }

  public void executeUIRefreshTask(Runnable task)
  {
    if (AdfmfJavaUtilities.isBackgroundThread())
    {
      // bug 22666682, fixed in MAF 2.3, can cause app to hang when using MafExecutorService
      // So, we use switch in mobile-persistence-config.properties to be abe to not use it for now
      if (PersistenceConfig.useMafExecutorService())
      {
        // execute inside MafExecutorService
         MafExecutorService.execute(task);        
      }
      else
      {
        task.run();        
      }
    }
    else 
    {
      // just execute the task
      task.run();
    }
  }

  public boolean isRunning()
  {
    return running;
  }

  /**
   *  Shuts down the thread pool gracefully for all task executor instances.
   */
  public static void shutDown()
  {
    for (TaskExecutor instance: instanceMap.values())
    {
      if (instance.executor != null)
      {
        instance.executor.shutdown();
        instance.executor = null;
      }
    }
  }
  
}
