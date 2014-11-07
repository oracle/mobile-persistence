/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.util;

import com.sun.util.logging.Level;

import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

/**
 * Convenience class to easily add log statements to your custom classes.
 * To use this class, define a static field in your class as follows:
 * <pre>
 * private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(MyClass.class);
 * </pre>
 * Then you can add use this field to log messages at various levels.
 * 
 *  
 */
public class ADFMobileLogger
{

  Class logClass;
  
  private ADFMobileLogger(Class logClass)
  {
    this.logClass = logClass;
  }
  
  public static ADFMobileLogger createLogger(Class logClass)
  {
    return new ADFMobileLogger(logClass);
  }

  public boolean isLoggable(Level level)
  {
    return Utility.ApplicationLogger.isLoggable(level);
  }

  public void severe(String text)
  {
    Trace.log(Utility.ApplicationLogger, Level.SEVERE, logClass, logClass.getName(),text);    
  }

  public void warning(String text)
  {
    Trace.log(Utility.ApplicationLogger, Level.WARNING, logClass, logClass.getName(),text);    
  }

  public void info(String text)
  {
    Trace.log(Utility.ApplicationLogger, Level.INFO, logClass, logClass.getName(),text);    
  }

  public void config(String text)
  {
    Trace.log(Utility.ApplicationLogger, Level.CONFIG, logClass, logClass.getName(),text);    
  }

  public void fine(String text)
  {
    Trace.log(Utility.ApplicationLogger, Level.FINE, logClass, logClass.getName(),text);    
  }

  public void finer(String text)
  {
    Trace.log(Utility.ApplicationLogger, Level.FINER, logClass, logClass.getName(),text);    
  }

  public void finest(String text)
  {
    Trace.log(Utility.ApplicationLogger, Level.FINEST, logClass, logClass.getName(),text);    
  }
}
