/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 25-nov-2013   Steven Davelaar
 1.1           getDataProvider now directly return underling pojo DC instance
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.controller.bean;

import oracle.adfmf.bindings.DataControl;
import oracle.adfmf.bindings.dbf.AmxBindingContext;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.framework.model.AdfELContext;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;


/**
 * Class that can be registered as managed bean to get easy access to the data control instance.
 * In the managed bean, you should define the managed property name to specify the name of the data control that should
 * be returned by the getInstance method of this bean.
 * You can then use EL expressions directly in AMX pages to call getter/setter method on your data control
 * like setCurrentEntityWithKey. This saves the burden of creating a managed bean just to invoke a data control
 * method using AdfmfUtilities.invokeDataControlMethod.
 *
 *
 */
public class DataControlBean
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(DataControlBean.class);
  private String name;

  /**
   * This dummy property MUST be defined as transient to avoid a call to getInstance method
   * by the ADF mobile change notification mechanism. This is what happens: when this bean is instantiated
   * for the first time, and added to applicationScope, the ApplicationScope class fires a propertyChange
   * event, this event calls all getter method on this bean so the content can be serialized to JSON and sent
   * to the client. Without this transient property the whole content of the data control is serialized to JSON
   * which might be a very expensive operation
   *
   */
  private transient Object instance;

  public DataControlBean()
  {
    super();
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  protected Object getDataControl(String name)
  {
    sLog.fine("Looking up data control "+name);
    AdfELContext adfELContext = AdfmfJavaUtilities.getAdfELContext();
    AmxBindingContext bc = (AmxBindingContext) adfELContext.evaluateVariable("data");
    DataControl  dc = (DataControl) bc.getDataControlById(name);
    if (dc!=null)
    {
      return dc.getDataProvider();
    }
    else
    {
      throw new AdfException("Data control "+name+" not found",AdfException.ERROR);
    }
  }

  public Object getInstance()
  {
    return getDataControl(getName());
  }

}
