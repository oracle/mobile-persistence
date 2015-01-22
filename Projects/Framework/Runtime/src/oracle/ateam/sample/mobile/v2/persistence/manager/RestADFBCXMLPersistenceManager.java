 /*******************************************************************************
  Copyright © 2015, Oracle and/or its affiliates. All rights reserved.
  
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.manager;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;


/**
 * Implementation of Persistence manager interface that provides basic CRUD operations using
 * the REST web services protocol against a remote server. The payload of the REST web service should
 * be in ADF BC XML format, the format that is created by using the readXml and writeXMl API's on
 * an ADF BC view object.
 */
public class RestADFBCXMLPersistenceManager extends RestXMLPersistenceManager
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(RestADFBCXMLPersistenceManager.class);

  public RestADFBCXMLPersistenceManager()
  {
  }

  protected String getStartTag(String elemName, boolean addBc4jRemoveAttr)
  {
    return "<" + elemName + (addBc4jRemoveAttr? " bc4j-action=\"remove\"":"")+ ">";
  }

}
