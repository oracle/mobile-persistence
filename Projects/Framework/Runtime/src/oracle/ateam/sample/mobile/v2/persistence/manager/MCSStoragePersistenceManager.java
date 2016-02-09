/*******************************************************************************
 Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 09-feb-2016   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.microedition.io.HttpConnection;

import oracle.adfmf.dc.ws.rest.RestServiceAdapter;
import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.JSONBeanSerializationHelper;
import oracle.adfmf.framework.api.Model;
import oracle.adfmf.framework.exception.AdfException;

import oracle.adfmf.framework.internal.AdfmfJavaUtilitiesInternal;
import oracle.adfmf.json.JSONObject;

import oracle.ateam.sample.mobile.exception.RestCallException;
import oracle.ateam.sample.mobile.mcs.storage.StorageObject;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;

import oracle.ateam.sample.mobile.v2.persistence.model.Entity;

import sun.misc.BASE64Encoder;

/**
 * This class overwrites the standard entity CRUD methods that are called during data synchronization, and
 * implements the methods by calling the MCS storage specific methods in the superclass. This allows us to manipulate
 * MCS storage objets in offline mode and use the standard AMPA data synchronization mechanism later when we are online again.
 */
public class MCSStoragePersistenceManager
  extends MCSPersistenceManager
{
  public MCSStoragePersistenceManager()
  {
    super();
  }

  @Override
  /**
   * This method calls storeStorageObject
   */
  public void insertEntity(Entity entity, boolean doCommit)
  {
    storeStorageObject((StorageObject)entity);
  }

  @Override
  public void removeEntity(Entity entity, boolean doCommit)
  {
    super.removeEntity(entity, doCommit);
  }

  @Override
  /**
   * This method calls storeStorageObject
   */
  public void updateEntity(Entity entity, boolean doCommit)
  {
    storeStorageObject((StorageObject)entity);
  }

  @Override
  /**
   * This method calls storeStorageObject
   */
  public void mergeEntity(Entity entity, boolean doCommit)
  {
    storeStorageObject((StorageObject)entity);
  }

}
