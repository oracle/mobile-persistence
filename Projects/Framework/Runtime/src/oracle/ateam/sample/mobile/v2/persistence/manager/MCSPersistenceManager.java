/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 29-dec-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import java.net.URL;
import java.net.URLConnection;

import java.nio.file.Files;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.microedition.io.HttpConnection;

import oracle.adfmf.dc.ws.rest.RestServiceAdapter;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.JSONBeanSerializationHelper;
import oracle.adfmf.framework.api.Model;
import oracle.adfmf.framework.exception.AdfException;

import oracle.adfmf.json.JSONObject;

import oracle.ateam.sample.mobile.mcs.storage.StorageObject;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;

import sun.misc.BASE64Encoder;


public class MCSPersistenceManager
  extends RestJSONPersistenceManager
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(MCSPersistenceManager.class);
  private static final String LOGIN_URI = "/platform/users/login";
  private static final String LOGOUT_URI = "/platform/users/logout";
  private static final String ANALYTICS_EVENTS_URI = "/platform/analytics/events";
  private static final String STORAGE_COLLECTIONS_URI = "/platform/storage/collections/";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String ORACLE_MOBILE_BACKEND_ID = "Oracle-Mobile-Backend-Id";
  private static final String AUTHORIZATION = "Authorization";
  private String mobileBackendId;
  private String authHeader;
  private String connectionName;
  private String anonymousKey;

  public MCSPersistenceManager()
  {
    super();
    connectionName = PersistenceConfig.getPropertyValue("mcs.connection");
    anonymousKey = PersistenceConfig.getPropertyValue("mcs.anonymous-key");
    mobileBackendId = PersistenceConfig.getPropertyValue("mcs.mobile-backend-id");
  }

  public String login(String userName, String password)
  {
    String userCredentials = userName + ":" + password;
    try
    {
      // oracle.adfmf.misc.Base64 nor java.util.Base64 does not encode correctly for some strange reason
      //      authHeader = "Basic " + Base64.encode(userCredentials.getBytes("UTF-8"));
      //      authHeader = "Basic " + Base64.getEncoder().encode(userCredentials.getBytes("UTF-8"));
      setAuthHeader("Basic " + new BASE64Encoder().encode(userCredentials.getBytes("UTF-8")));
      String result = invokeRestService(getConnectionName(), "GET", LOGIN_URI, null, null, 0, false);
      return result;
    }
    catch (UnsupportedEncodingException e)
    {
      // this should never happen
    }
    catch (Exception e)
    {
      authHeader = null;
      throw e;
    }
    return null;
  }

  public String logout()
  {
    String result = invokeRestService(getConnectionName(), "GET", LOGOUT_URI, null, null, 0, false);
    setAuthHeader(null);
    return result;
  }

  public void sendAnalyticsEvents(String payload)
  {
    invokeRestService(getConnectionName(), "POST", ANALYTICS_EVENTS_URI, payload, null, 0, false);
  }

  /**
   * Call MCS Storage API /platform/storage/collections/{collection}/objects/{object} with HEAD
   * method to get object metadata. If the object already exists in local DB, we pass in the If-None-Match
   * header with the local Etag value so we can find out whether the local object is stil current.
   * If the REST call returns status code 304 - Not Modified, we know there is no need to fetch the object
   * content from MCS and the istLocalVersionIsCurrent flag will be set to true on the storageObject instance.
   * @param storageObject
   */
  public void getStorageObjectMetadata(StorageObject storageObject)
  {
    String uri = STORAGE_COLLECTIONS_URI + storageObject.getCollectionName() + "/objects/" + storageObject.getId();
    Map<String, String> headerParams = new HashMap<String, String>();
    if (storageObject.getETag() != null)
    {
      // we have retrieved the object before
      // Set If-None-Match header param so we know whether the object has changed
      headerParams.put("If-None-Match", storageObject.getETag());
    }
    // response is always null with HEAD call
    String response = invokeRestService(getConnectionName(), "HEAD", uri, null, headerParams, 0, false);
    if (getLastResponseStatus() == 304)
    {
      // 304 is Not Modified, this means local eTag is sams as remote, so
      // no need to retrieve the content of it has been retrieved before
      if (storageObject.getFilePath() != null)
      {
        storageObject.setLocalVersionIsCurrent(true);
      }
    }
    else
    {
      // populate the object metadata attrs with the response headers
      populateStorageObjectMetadata(storageObject, getLastResponseHeaders());
    }
  }

  /**
   * Populate storage object metadata with header parameters returned by HEAD call to storage object.
   * If persist flag is set to true in persistence-mapping.xml, we also save the object in local DB
   * @param storageObject
   * @param headers
   */
  public void populateStorageObjectMetadata(StorageObject storageObject, Map headers)
  {
    Object cl = headers.get("Content-Length");
    if (cl != null)
    {
      storageObject.setContentLength(new Long(cl.toString()));
    }
    storageObject.setContentType((String) headers.get(CONTENT_TYPE));
    storageObject.setCreatedBy((String) headers.get("Oracle-Mobile-Created-By"));
    storageObject.setCreatedOn((String) headers.get("Oracle-Mobile-Created-On"));
    storageObject.setModifiedBy((String) headers.get("Oracle-Mobile-Modified-By"));
    storageObject.setModifiedOn((String) headers.get("Oracle-Mobile-Modified-On"));
    storageObject.setETag((String) headers.get("ETag"));
    storageObject.setName((String) headers.get("Oracle-Mobile-Name"));
    if (ClassMappingDescriptor.getInstance(storageObject.getClass()).isPersisted())
    {
      getLocalPersistenceManager().mergeEntity(storageObject, true);
    }
  }

  /**
   * Call MCS Storage API /platform/storage/collections/{collection}/objects/{object} with GET
   * method to get object content and return the byte array response
   * @param storageObject
   */
  public byte[] getStorageObjectContent(StorageObject storageObject)
  {
    String uri = STORAGE_COLLECTIONS_URI + storageObject.getCollectionName() + "/objects/" + storageObject.getId();
    Map<String, String> headerParams = new HashMap<String, String>();
    // if the file content has been retrieved before, we pass in eTag header, so we don't download
    // it again while it is unchanged
    if (storageObject.getFilePath() != null)
    {
      // Set If-None-Match header param so we know whether the object has changed
      headerParams.put("If-None-Match", storageObject.getETag());
    }
    byte[] response = invokeByteArrayRestService(getConnectionName(), "GET", uri, null, headerParams, 0);
    if (getLastResponseStatus() == 304)
    {
      // 304 is Not Modified, this means local eTag is same as remote
      storageObject.setLocalVersionIsCurrent(true);
    }
    else
    {
      // populate the object metadata attrs with the response headers
      populateStorageObjectMetadata(storageObject, getLastResponseHeaders());
    }
    return response;
  }

  public Map<String, String> addMCSHeaderParamsIfNeeded(Map<String, String> headerParams)
  {
    if (headerParams == null)
    {
      headerParams = new HashMap<String, String>();
    }
    if (!headerParams.containsKey(CONTENT_TYPE))
    {
      headerParams.put(CONTENT_TYPE, "application/json");
    }
    if (!headerParams.containsKey(ORACLE_MOBILE_BACKEND_ID) && getMobileBackendId() != null)
    {
      headerParams.put(ORACLE_MOBILE_BACKEND_ID, getMobileBackendId());
    }
    if (!headerParams.containsKey(AUTHORIZATION))
    {
      String header = getAuthHeader() != null? getAuthHeader(): getAnonymousHeader();
      if (header != null)
      {
        headerParams.put(AUTHORIZATION, header);
      }
    }
    return headerParams;
  }


  public void setMobileBackendId(String mobileBackendId)
  {
    this.mobileBackendId = mobileBackendId;
  }

  public String getMobileBackendId()
  {
    return mobileBackendId;
  }

  public void setAuthHeader(String authHeader)
  {
    this.authHeader = authHeader;
  }

  public String getAuthHeader()
  {
    return authHeader;
  }

  public String getAnonymousHeader()
  {
    if (getAnonymousKey() != null)
    {
      return "Basic " + getAnonymousKey();
    }
    return null;
  }

  public void setConnectionName(String connectionName)
  {
    this.connectionName = connectionName;
  }

  public String getConnectionName()
  {
    return connectionName;
  }

  public void setAnonymousKey(String anonymousKey)
  {
    this.anonymousKey = anonymousKey;
  }

  public String getAnonymousKey()
  {
    return anonymousKey;
  }


  @Override
  /**
   * This methods adds MCS-specific headers if not yet set.
   */
  public String invokeRestService(String connectionName, String requestType, String requestUri, String payload,
                                  Map<String, String> headerParamMap, int retryLimit, boolean secured)
  {
    return super.invokeRestService(connectionName, requestType, requestUri, payload, addMCSHeaderParamsIfNeeded(headerParamMap), retryLimit,
                                   secured);
  }

  public void storeStorageObject(StorageObject storageObject)
  {
    try
    {

      RestServiceAdapter adp = Model.createRestServiceAdapter();
      adp.setConnectionName(getConnectionName());
      String requestEndPoint = adp.getConnectionEndPoint(getConnectionName());
      // Get the URI which is defined after the end point
      String requestURI =
        STORAGE_COLLECTIONS_URI + storageObject.getCollectionName() + "/objects/" + storageObject.getName();
      String request = requestEndPoint + requestURI;
      HashMap<String, String> httpHeadersValue = new HashMap<String, String>();
      httpHeadersValue.put("Oracle-Mobile-Name", storageObject.getName());
      httpHeadersValue.put("Content-Type", storageObject.getContentType());
      addMCSHeaderParamsIfNeeded(httpHeadersValue);
      // Get the connection
      HttpConnection connection = adp.getHttpConnection("PUT", request, httpHeadersValue);
      Path path = Paths.get(storageObject.getFilePath());
      byte[] data = Files.readAllBytes(path);
      //        FileInputStream fis = new FileInputStream(storageObject.getContent());
      OutputStream os = connection.openOutputStream();
      os.write(data);

      //        copyStream(fis, connection.openOutputStream());
      Integer code = connection.getResponseCode();
      //        String responseMsg = connection.getResponseMessage();
      // get the result
      // response format:
      //            {"id":"51e48d9a-0d9d-4627-b3e8-c2eaaa8886ae","name":"51e48d9a-0d9d-4627-b3e8-c2eaaa8886ae"
      //           ,"contentLength":45993,"contentType":"application/zip","eTag":"\"1\""
      //           ,"createdBy":"steven.davelaar","createdOn":"2015-05-18T21:11:54Z","modifiedBy":"steven.davelaar"
      //           ,"modifiedOn":"2015-05-18T21:11:54Z","links":[{"rel":"canonical"
      //           ,"href":"/mobile/platform/storage/collections/Services/objects/51e48d9a-0d9d-4627-b3e8-c2eaaa8886ae"}
      //    ,{"rel":"self","href":"/mobile/platform/storage/collections/Services/objects/51e48d9a-0d9d-4627-b3e8-c2eaaa8886ae"}]}
      String response = getResponse(adp.getInputStream(connection), false);
//      System.err.println(response);
      JSONObject responseObject = (JSONObject) JSONBeanSerializationHelper.fromJSON(JSONObject.class, response);
      super.processPayloadElement(responseObject, StorageObject.class, null, storageObject);
//      System.err.println(response);
    }
    catch (Exception e)
    {
      throw new AdfException(e, AdfException.ERROR);
    }
    finally
    {
    }
  }

  /**
   * Read the response message from HttpConnection inputStream when request was streamed
   * @param is
   * @return Response message
   */
  protected String getResponse(InputStream is, boolean gzipEncoded)
  {
    Reader reader = null;
    StringWriter writer = null;
    String charset = "UTF-8"; //
    InputStream response = null;
    try
    {

      if (gzipEncoded == true)
      {
        response = new GZIPInputStream(is);
      }
      else
      {
        response = is;
      }

      reader = new InputStreamReader(response, charset);
      writer = new StringWriter();

      char[] buffer = new char[10240];
      for (int length = 0; (length = reader.read(buffer)) > 0;)
      {
        writer.write(buffer, 0, length);
      }
    }
    catch (IOException e)
    {

    }
    finally
    {
      try
      {
        writer.close();
        reader.close();
      }
      catch (IOException e)
      {
        sLog.severe("Error closing MCS stream response: " + e.getLocalizedMessage());
      }
    }

    return writer.toString();
  }

}
