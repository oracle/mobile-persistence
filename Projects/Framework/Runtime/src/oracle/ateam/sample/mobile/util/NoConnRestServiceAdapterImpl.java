/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 10-nov-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.util;

import oracle.adfmf.dc.ws.rest.RestServiceAdapterImpl;

/**
 * This class can be used to invoke a REST service without an entry in connections.xml.
 * The connection endpoint normally obtained from connections.xml can be set through the
 * constructor, or by invoking the setConnectionEndPoint method.
 */
public class NoConnRestServiceAdapterImpl
  extends RestServiceAdapterImpl
{

  private String connectionEndPoint;

  public NoConnRestServiceAdapterImpl()
  {
    super("", "", "");
  }

  public NoConnRestServiceAdapterImpl(String connectionEndPoint)
  {
    super("", "", "");
    this.connectionEndPoint = connectionEndPoint;
  }

  public String getConnectionEndPoint(String string)
  {
//    return super.getConnectionEndPoint(string);
    return connectionEndPoint;
  }

  public void setConnectionEndPoint(String connectionEndPoint)
  {
    this.connectionEndPoint = connectionEndPoint;
  }

}
