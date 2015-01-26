/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
24-jan-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.util;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;

/**
 * This class is used to execute tasks (runnables), either in foreground or in background.
 * The added value comes when running tasks in the background, this class ensures that all background tasks
 * are executed in sequence of submission on a single thread. When making multiple remote REST calls, the sequence might
 * be important as result of the first call might ne needed by the next call. Furthermore, the local SQLite database is a
 * single user database. If you have multiple simulatneous REST calls running in the background, and they all want to store 
 * the data returned in the local database, you will most likely get errors about nested transactions not being allowed.
 * Finally, this class sets an application scope boolean flag to indicate whether there are background tasks running.
 * This flag can be used to show some sort of spinning wheel icon in the UI, or simply some text message like "Loading data".
 * The expression to evaluate this flag is #{applicationScope.ampa_bgtask_running}
 */
public class TaskExecutor
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(TaskExecutor.class);

  private static TaskExecutor instance;
  private ThreadPoolExecutor executor = null;

  private boolean running = false;

  private Future<?> lasttFuture = null;

  public TaskExecutor()
  {
    super();
  }

  public static synchronized TaskExecutor getInstance()
  {
    if (instance==null)
    {
      sLog.fine("Creating new instance of TaskExecutor");
      instance = new TaskExecutor();
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
      executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }
  }
  
  protected void setRunning(boolean running)
  {
    AdfmfJavaUtilities.setELValue("#{applicationScope.ampa_bgtask_running}", running);
    AdfmfJavaUtilities.flushDataChangeEvent();
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
            sLog.fine("Number of background tasks in Queue in TaskExecutor: "+size);
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
          AdfmfJavaUtilities.flushDataChangeEvent();
          sLog.fine("No more background tasks running in TaskExecutor: "+size);
          ThreadPoolExecutor executorDone = executor;
          // clear executor member so any new tasks submitted will get new thread pool
          executor = null;
          // shut down thread pool so we don't have idle thread running
          executorDone.shutdown();
        };
      Thread t = new Thread(r2);
      t.start();
    }
  }

  /**
   * Execute a task. If the task is to be executed in the background, a single thread
   * will be used for all submitted tasks, ensuring they are executed in the sequence of submission.
   * To check whether there are any background tasks running, you can use the boolean expression 
   * #{applicationScope.ampa_bgtask_running}.
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

  public boolean isRunning()
  {
    return running;
  }

}
