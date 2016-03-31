/*******************************************************************************
 Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 10-jan-2016   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.exception;

import java.util.Map;

import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;

/**
 * Exception class used when a REST call fails. This class allows adding additional info to the exception about the Rest 
 * call being made.
 */
public class RestCallException
  extends AdfException
{
  
  private String requestUri;
  private String requestMethod;
  private int responseStatus;
  Map responseHeaders;
  private Exception wrappedException;

  public RestCallException(Exception exception)
  {
    super(exception);
    this.wrappedException = exception;
  }

  public Exception getWrappedException()
  {
    return wrappedException;
  }


  public void setRequestUri(String requestUri)
  {
    this.requestUri = requestUri;
  }

  public String getRequestUri()
  {
    return requestUri;
  }

  public void setRequestMethod(String requestMethod)
  {
    this.requestMethod = requestMethod;
  }

  public String getRequestMethod()
  {
    return requestMethod;
  }

  public void setResponseStatus(int responseStatus)
  {
    this.responseStatus = responseStatus;
  }

  public int getResponseStatus()
  {
    return responseStatus;
  }

  public void setResponseHeaders(Map responseHeaders)
  {
    this.responseHeaders = responseHeaders;
  }

  public Map getResponseHeaders()
  {
    return responseHeaders;
  }
}
