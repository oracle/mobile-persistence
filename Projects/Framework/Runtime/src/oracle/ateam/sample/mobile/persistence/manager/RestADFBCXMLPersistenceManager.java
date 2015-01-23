/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 18-feb-2014   Steven Davelaar
 1.3           - Move most logic to new superclasses
 23-dec-2013   Steven Davelaar
 1.2           - connectionName now stored as member of persistence manager and passed into 
                 RestUtils.invokeRestService method
 19-nov-2013   Steven Davelaar
 1.1           - Refactored synchronizeWithLocalDb to support recursive call to
               process one-to-many mappings to store child rows in local DB
               - throw error when root element name does not match
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.manager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.util.KXmlUtil;
import oracle.adfmf.util.XmlAnyDefinition;

import oracle.ateam.sample.mobile.persistence.cache.EntityCache;
import oracle.ateam.sample.mobile.persistence.db.BindParamInfo;
import oracle.ateam.sample.mobile.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.persistence.metadata.AttributeMappingOneToMany;
import oracle.ateam.sample.mobile.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.persistence.metadata.Method;
import oracle.ateam.sample.mobile.persistence.metadata.MethodParameter;
import oracle.ateam.sample.mobile.persistence.metadata.ObjectPersistenceMapping;
import oracle.ateam.sample.mobile.persistence.model.Entity;
import oracle.ateam.sample.mobile.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;


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
