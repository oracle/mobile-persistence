package oracle.ateam.sample.mobile.logging;

import oracle.ateam.sample.mobile.v2.persistence.model.Entity;

import java.util.Date;


public class WebServiceCall
  extends Entity
{

  private Integer id;
  private String connection;
  private String request;
  private String method;
  private String requestHeaders;
  private Long duration;
  private String requestPayload;
  private String responsePayload;
  private String errorMessage;
  private Date timestamp;


  public Integer getId()
  {
    return this.id;
  }

  public void setId(Integer id)
  {
    this.id = id;
  }

  public String getConnection()
  {
    return this.connection;
  }

  public void setConnection(String connection)
  {
    this.connection = connection;
  }

  public String getRequest()
  {
    return this.request;
  }

  public void setRequest(String request)
  {
    this.request = request;
  }

  public String getMethod()
  {
    return this.method;
  }

  public void setMethod(String method)
  {
    this.method = method;
  }

  public String getRequestHeaders()
  {
    return this.requestHeaders;
  }

  public void setRequestHeaders(String requestHeaders)
  {
    this.requestHeaders = requestHeaders;
  }

  public Long getDuration()
  {
    return this.duration;
  }

  public void setDuration(Long duration)
  {
    this.duration = duration;
  }

  public String getRequestPayload()
  {
    return this.requestPayload;
  }

  public void setRequestPayload(String requestPayload)
  {
    this.requestPayload = requestPayload;
  }

  public String getResponsePayload()
  {
    return this.responsePayload;
  }

  public void setResponsePayload(String responsePayload)
  {
    this.responsePayload = responsePayload;
  }

  public String getErrorMessage()
  {
    return this.errorMessage;
  }

  public void setErrorMessage(String errorMessage)
  {
    this.errorMessage = errorMessage;
  }

  public Date getTimestamp()
  {
    return this.timestamp;
  }

  public void setTimestamp(Date timestamp)
  {
    this.timestamp = timestamp;
  }


}
