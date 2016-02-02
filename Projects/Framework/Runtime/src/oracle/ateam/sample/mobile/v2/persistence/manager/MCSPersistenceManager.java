/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 29-dec-2015   Steven Davelaar
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

import java.util.HashMap;
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

import oracle.ateam.sample.mobile.mcs.storage.StorageObject;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;

import sun.misc.BASE64Encoder;

/**
 * This class provides easy access to most of the platform REST API's available in Oracle Mobile Cloud Service. For custom API calls, it uses
 * the REST endpoints as defined in persistence-mapping.xml.
 * It injects the oracle-mobile-backend-id and (anonymous) Authorization header based on configuration in
 * mobile-persistence-config.properties. However, this injection will be overridden when MAF MCS login is configured, which
 * is the recommended approach.
 */
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
  private static final String AUTH_HEADER_EXPRESSION = "#{applicationScope.mcs_auth_header}";
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

  /**
   * This method allows you to basic authenticatication against MCS using the username and password that you pass in.
   * The oracle mobile backend id used to authenticate against is by taken from mobile-persistence-config.properties
   * The authorization header constructed out of the username and password is stored on applicationScope, and is injected
   * in any further MCS REST call through method addMCSHeaderParamsIfNeeded.
   * Note that it is preferred to use the standard MAF login functionality, rather than this custom login method. When using
   * the MAF login functionality, MAF itself will inject the Oracle mobile backend id and Authorization header on every
   * MCS REST call. The standard MAF login functionality also allows you to use OAuth insteaf of basic authentication, amd allows 
   * you to remember username and/or password, saving them encrypted in the keychain.
   * @param userName
   * @param password
   * @return
   */
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

  /**
   * Calls MCS logout URL. This method does NOT logout from MAF application. You can do this yourself by calling
   * AdfmfJavaUtilities.logout();
   * @return
   */
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

  /**
   * This method will add the following headers if they have not been included yet in header parameter map:
   * <ul>
   * <li>Content-Type: application/json</li>
   * <li>oracle-mobile-backend-id: value taken from mobile-persistence-config.properties</li>
   * <li>Authorization: value taken from method getAuthHeader, if this method returns null, value taken from getAnonymousHeader</li>
   * </ul>
   * Note that if you use standard MAF login against MCS, then MAF will override the oracle-mobile-backend-id and Authorization
   * header parameters with its own values based on the login configuration. This is convenient, because this means that as long as you are
   * not logged in, this class will inject the anonymous auth header from mobile-persistence-config.properies, and once you are logged
   * in, the MAF login header injection will win.
   * @param headerParams
   * @return
   */
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

  /**
   * Store the authorization header parameter value that is created by calling the login method 
   * in applicationScope under key mcs_auth_header.
   * 
   * @param authHeader
   */
  public void setAuthHeader(String authHeader)
  {
    AdfmfJavaUtilities.setELValue(AUTH_HEADER_EXPRESSION, authHeader);
  }

  /**
   * Return the authorization header parameter value that is stored 
   * in applicationScope under key mcs_auth_header.
   * 
   * @param authHeader
   */
  public String getAuthHeader()
  {
    return (String) AdfmfJavaUtilities.getELValue(AUTH_HEADER_EXPRESSION);
  }

  /**
   * Return the anonymous authorization header parameter value. By default, this value is constructed
   * from the anonymous access key defined in mobile-persistence-config.properties, prefixed
   * with "Basic ". Note that the anonymous access key can be changed by calling setAnonymousKey.
   * If the anonymous access key is not set, this method returns null.
   * @return
   */
  public String getAnonymousHeader()
  {
    if (getAnonymousKey() != null)
    {
      return "Basic " + getAnonymousKey();
    }
    return null;
  }

  /**
   * Change the default connection name as specified in mobile-persistence-config.properties
   * @param connectionName
   */
  public void setConnectionName(String connectionName)
  {
    this.connectionName = connectionName;
  }

  /**
   * Returns by default the MCS connection name defined in mobile-persistence-config.properties, unless
   * this value have been overridden by calling setConnectionName on the same instance of MCSPersistenceManager.
   * @return
   */
  public String getConnectionName()
  {
    return connectionName;
  }

  /**
   * Change the default anonymous accesss key as specified in mobile-persistence-config.properties
   * @param connectionName
   */
  public void setAnonymousKey(String anonymousKey)
  {
    this.anonymousKey = anonymousKey;
  }

  /**
   * Returns by default the MCS anonymous access key defined in mobile-persistence-config.properties, unless
   * this value have been overridden by calling setAnonymousKey on the same instance of MCSPersistenceManager.
   * @return
   */
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
