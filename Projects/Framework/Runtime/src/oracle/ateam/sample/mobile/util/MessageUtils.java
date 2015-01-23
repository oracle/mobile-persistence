/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 10-nov-2014   Steven Davelaar
 1.2           Register error with tracking service if set up
 26-sep-2013   Steven Davelaar
 1.1           Need to pass "info" instead of "INFO" returned by AdfException.INFO 
               when showing error in background thread
 07-jun-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.util;

import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;


/**
 * This class contains convenience methods for handling (unexpected) exceptions, and showing error
 * warning and information messages.
 * When handling a message, this class checks whether the current thread is a background hread or a main thread.
 * If it is the main thread an AdfException is thrown, which will be shown as a message popup in the user interface.
 * In case of a background thread, it makes no sense to throw an ADfException because it will not show in the UI. Instead,
 * the AdfmfContainerUtilities.invokeContainerJavaScriptFunction API is used to call the ADF Mobile JavaScript method
 * adf.mf.api.amx.addMessage which will render the message in the UI in the same way as is the case when throwing an ADFException
 * in the main thread.
 */
public class MessageUtils
{
  public static UsageTracker usageTracker = new UsageTracker();

  public static void handleError(AdfException ex)
  {
    handleMessage(ex.getSeverity(), ex.getMessage());
  }

  public static void handleError(String message)
  {
    handleMessage(AdfException.ERROR, message);
  }

  public static void handleError(Exception ex)
  {
    handleMessage(AdfException.ERROR, ex.getLocalizedMessage());
  }

  public static void handleMessage(String severity, String message)
  {
    // register error message if tracking service is set up
    if (AdfException.ERROR.equals(severity))
    {
      usageTracker.registerErrorMessage(message);      
    }

    if (AdfmfJavaUtilities.isBackgroundThread())
    {
      String jsSeverity = severity.equalsIgnoreCase("INFO") ? "info" : (severity.equalsIgnoreCase("WARNING") ? "warning" : "error");
      AdfmfContainerUtilities.invokeContainerJavaScriptFunction(AdfmfJavaUtilities.getFeatureId(),
        "adf.mf.api.amx.addMessage", new Object[] {jsSeverity, message });
      if (AdfException.ERROR.equals(severity))
      {
        // we still need to throw execption to stop background thread processing
        throw new AdfException(message,severity);        
      }
    }
    else
    {
      throw new AdfException(message,severity);
    }    
  }

}
