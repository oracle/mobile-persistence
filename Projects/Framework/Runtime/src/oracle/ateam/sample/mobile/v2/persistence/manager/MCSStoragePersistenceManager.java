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
import oracle.adfmf.json.JSONArray;
import oracle.adfmf.json.JSONException;
import oracle.adfmf.json.JSONObject;

import oracle.ateam.sample.mobile.exception.RestCallException;
import oracle.ateam.sample.mobile.mcs.storage.StorageObject;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.v2.persistence.db.BindParamInfo;
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
  /**
   * This method calls removeStorageObject
   */
  public void removeEntity(Entity entity, boolean doCommit)
  {
    removeStorageObject((StorageObject)entity);
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


  @Override
  /**
   * We need to add the collectionName to the payload row, otehrwise we cannot insert StorageObject in SQLite DB
   * because collectionName is part of the primary key.
   * We obtain the collection name from the links sectiond included in the row
   */
  protected <E extends Entity> E processPayloadElement(JSONObject row, Class entityClass,
                                                       List<BindParamInfo> parentBindParamInfos, E currentEntity)
    throws JSONException
  {
// This is where we need to extract the collectionName from, which is HR in this sample
//    "links": [
//            {
//              "rel": "canonical",
//              "href": "/mobile/platform/storage/collections/HR/objects/JobsList"
//            },
//            {
//              "rel": "self",
//              "href": "/mobile/platform/storage/collections/HR/objects/JobsList"
//            }
//          ]    
    JSONArray links = (JSONArray) row.get("links");
    if (links!=null && links.length()>0)
    {
      JSONObject link = (JSONObject) links.get(0);
      String href = link.getString("href");
      href = href.substring("/mobile/platform/storage/collections/".length());
      String collectionName = href.substring(0,href.indexOf("/"));
      row.put("collectionName", collectionName);
    }
    return super.processPayloadElement(row, entityClass, parentBindParamInfos, currentEntity);
  }

  @Override
  /**
   *  Returns true, override needed because createMethod not defined in persistence-mapping.xml
   */
  public boolean isCreateSupported(Class clazz)
  {
    return true;
  }

  @Override
  /**
   *  Returns true, override needed because removeMethod not defined in persistence-mapping.xml
   */
  public boolean isRemoveSupported(Class clazz)
  {
    return true;
  }

  @Override
  /**
   *  Returns true, override needed because updateMethod not defined in persistence-mapping.xml
   */
  public boolean isUpdateSupported(Class clazz)
  {
    return true;
  }

  @Override
  /**
   *  Returns true, override needed because mergeMethod not defined in persistence-mapping.xml
   */
  public boolean isMergeSupported(Class clazz)
  {
    return true;
  }


}
