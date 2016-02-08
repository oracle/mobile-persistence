package oracle.ateam.sample.mobile.exception;

import java.util.Map;

import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;

public class RestCallException
  extends AdfException
{
  
  private String requestUri;
  private String requestMethod;
  private int responseStatus;
  Map responseHeaders;

  public RestCallException(Throwable throwable, String string)
  {
    super(throwable, string);
  }

  public RestCallException(Throwable throwable)
  {
    super(throwable);
  }

  public RestCallException(String string, String string1)
  {
    super(string, string1);
  }

  public RestCallException(String string, String string1, String string2, Object[] objects)
  {
    super(string, string1, string2, objects);
  }

  public RestCallException()
  {
    super();
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
