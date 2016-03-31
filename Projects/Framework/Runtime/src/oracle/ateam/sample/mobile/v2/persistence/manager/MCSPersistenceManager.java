/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

 $revision_history$
 25-mar-2016   Steven Davelaar
 1.2           - Implemented streaming for both retrieving and uploading files from/to MCS
               - Use new RestServiceAdapter and  RestServiceAdapterFactory class
 07-mar-2016   Steven Davelaar
 1.1           - Add collectionName to response payload when storing object in MCS, so we can insert/update
               it in DB, collectionName if part of primary key 
               - Added rest call logging in storeStorageObject
 29-dec-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.manager;

import java.io.File;
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

import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.el.ValueExpression;

import javax.microedition.io.HttpConnection;

import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.JSONBeanSerializationHelper;
import oracle.adfmf.framework.api.Model;
import oracle.adfmf.framework.exception.AdfException;

import oracle.adfmf.framework.internal.AdfmfJavaUtilitiesInternal;
import oracle.adfmf.json.JSONObject;

import oracle.ateam.sample.mobile.exception.RestCallException;
import oracle.ateam.sample.mobile.mcs.storage.StorageObject;
import oracle.ateam.sample.mobile.mcs.storage.StorageObjectService;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;

import oracle.ateam.sample.mobile.v2.persistence.model.Entity;

import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;

import oracle.maf.api.dc.ws.rest.RestServiceAdapter;

import oracle.maf.api.dc.ws.rest.RestServiceAdapterFactory;

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
  private static final String USER_URI = "/platform/users";
  private static final String ANALYTICS_EVENTS_URI = "/platform/analytics/events";
  private static final String REGISTER_DEVICE_URI = "/platform/devices/register";
  private static final String DEREGISTER_DEVICE_URI = "/platform/devices/deregister";
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
    init();    
  }

  /**
   * Initialize connectionName, mobileBackendId and anonymousAccessKey from
   * mobile-persistence-config.properties. EL expressions are allowed for these
   * values.
   */
  protected void init()
  {
    sLog.fine("Executing init");
    // values might be EL expression
    String connectionNameConfig = PersistenceConfig.getPropertyValue("mcs.connection");
    if (connectionNameConfig!=null)
    {
      connectionName = AdfmfJavaUtilities.evaluateELExpression(connectionNameConfig).toString();      
      sLog.fine("MCS default connectionName="+connectionName);
    }
    String anonymousKeyConfig = PersistenceConfig.getPropertyValue("mcs.anonymous-key");
    if (anonymousKeyConfig!=null)
    {
      anonymousKey = AdfmfJavaUtilities.evaluateELExpression(anonymousKeyConfig).toString();      
      sLog.fine("MCS default anonymousKey="+anonymousKey);
    }
    String mobileBackendIdConfig = PersistenceConfig.getPropertyValue("mcs.mobile-backend-id");
    if (mobileBackendIdConfig!=null)
    {
      mobileBackendId = AdfmfJavaUtilities.evaluateELExpression(mobileBackendIdConfig).toString();      
      sLog.fine("MCS default mobileBackendId="+mobileBackendId);
    }
  }

  /**
   * This method allows you to basic authenticatication against MCS using the username and password that you pass in.
   * The oracle mobile backend id used to authenticate against is by taken from mobile-persistence-config.properties.
   * The authorization header constructed out of the username and password is stored on applicationScope, and is injected
   * in any further MCS REST call through method addMCSHeaderParamsIfNeeded.
   * Note that it is preferred to use the standard MAF login functionality, rather than this custom login method. When using
   * the MAF login functionality, MAF itself will inject the Oracle mobile backend id and Authorization header on every
   * MCS REST call. This will override the mobile backend-id and anonymous access key defined in 
   * mobile-persistence-config.properties.
   * The standard MAF login functionality also allows you to use OAuth insteaf of basic authentication, amd allows 
   * you to remember username and/or password, saving them encrypted in the keychain.
   * @param userName
   * @param password
   * @return
   */
  public String login(String userName, String password)
  {
    sLog.fine("Executing login");
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
    sLog.fine("Executing logout");
    String result = invokeRestService(getConnectionName(), "GET", LOGOUT_URI, null, null, 0, false);
    setAuthHeader(null);
    return result;
  }

  public void sendAnalyticsEvents(String payload)
  {
    sLog.fine("Executing sendAnalyticsEvents");
    invokeRestService(getConnectionName(), "POST", ANALYTICS_EVENTS_URI, payload, null, 0, false);
  }

  /**
   * Call MCS Storage API /platform/storage/collections/{collection}/objects/{object} with HEAD
   * method to get object metadata. If the object already exists in local DB, we pass in the If-None-Match
   * header with the local Etag value so we can find out whether the local object is stil current.
   * If the REST call returns status code 304 - Not Modified, we know there is no need to fetch the object
   * content from MCS.
   * @param storageObject
   */
  public void findStorageObjectMetadata(StorageObject storageObject)
  {
    sLog.fine("Executing findStorageObjectMetadata");
    String uri = STORAGE_COLLECTIONS_URI + storageObject.getCollectionName() + "/objects/" + storageObject.getId();
    Map<String, String> headerParams = new HashMap<String, String>();
    if (storageObject.getETag() != null)
    {
      // we have retrieved the object before
      // Set If-None-Match header param so we know whether the object has changed
      headerParams.put("If-None-Match", storageObject.getETag());
    }
    // response is always null with HEAD call
    try
    {
      String response = invokeRestService(getConnectionName(), "HEAD", uri, null, headerParams, 0, false);
      if (getLastResponseStatus() == 304)
      {
        // 304 is Not Modified, this means local eTag is sams as remote, so
        // no need to retrieve the content of it has been retrieved before
      //      if (storageObject.getFilePath() != null)
      //      {
      //        storageObject.setLocalVersionIsCurrent(true);
      //      }
      }
      else
      {
        // populate the object metadata attrs with the response headers
        populateStorageObjectMetadata(storageObject, getLastResponseHeaders(),true);
      }
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(ClassMappingDescriptor.getInstance(StorageObject.class),e,false);                
    }
  }

  /**
   * Populate storage object metadata with header parameters returned by HEAD call to storage object.
   * The storage object is not saved to local DB here. 
   * @param storageObject
   * @param headers
   * @param saveToDB
   */
  public void populateStorageObjectMetadata(StorageObject storageObject, Map headers, boolean saveToDB)
  {
    sLog.fine("Executing populateStorageObjectMetadata");
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
    // Fire Data change events to refresh UI
    EntityUtils.refreshEntity(storageObject);
    if (saveToDB)
    {
      getLocalPersistenceManager().mergeEntity(storageObject, true);
      storageObject.setIsNewEntity(false);
    }
  }

  /**
   * Call MCS Storage API /platform/storage/collections/{collection}/objects/{object} with GET
   * method. The object metadata return in header params is saved into the storage object passed into
   * this method, the actual content of the storage object is streamed to the file system.
   * 
   * @param storageObject
   */
  public void findStorageObject(StorageObject storageObject)
  {
    sLog.fine("Executing findStorageObject");    
    // if the file content has been retrieved before, we pass in eTag header, so we don't download
    // it again while it is unchanged
    boolean checkIsUpdated = storageObject.getETag() != null &&  storageObject.getFilePath()!=null;
    if (checkIsUpdated)
    {
      // make HEAD call to get metadata and to check whether we need to downlaod the file itself
      // again
        findStorageObjectMetadata(storageObject);
        if (getLastResponseStatus() == 304)
        {
          // 304 is Not Modified, this means local eTag is same as remote
          sLog.fine("Storage Object "+storageObject.getId()+" not modified since last download, E-Tags still match");
          return;
        }
    }
    // stream storage file from MCS to file system
    String requestURI =
      STORAGE_COLLECTIONS_URI + storageObject.getCollectionName() + "/objects/" + storageObject.getId();
    HashMap<String, String> httpHeadersValue = new HashMap<String, String>();
    addMCSHeaderParamsIfNeeded(httpHeadersValue);
    RestServiceAdapter adp = RestServiceAdapterFactory.newFactory().createRestServiceAdapter();
    adp.setConnectionName(getConnectionName());
    adp.setRequestMethod("GET");
    String requestEndPoint = adp.getConnectionEndPoint(getConnectionName());
    String request = requestEndPoint + requestURI;
    // Get the URI which is defined after the end point
    // Get the connection
    HttpConnection connection = null;
    StorageObjectService sos = (StorageObjectService) EntityUtils.getEntityCRUDService(StorageObject.class);
    long startTime = System.currentTimeMillis();
    try
    {
      connection = adp.getHttpConnection("GET", request, httpHeadersValue);
      InputStream is = adp.getInputStream(connection);      
      // set the input stream on the storage object and save metadata and stream to file system
      boolean gzip = "gzip".equalsIgnoreCase(adp.getResponseHeaders().get("Content-Encoding"));
      // check whether respons eis Gzip, if so change InputStream to GZIPInputStream
      is = gzip ? new GZIPInputStream(is) : is;
      storageObject.setContentStream(is);
      // if we checked ETag with HEAD call by calling findStorageObjectMetadata, the metdata are already
      // set on storageObject
      if (!checkIsUpdated)
      {
        populateStorageObjectMetadata(storageObject,adp.getResponseHeaders(),false);        
      }
      sos.saveStorageObjectOnDevice(storageObject);

      if (storageObject.getDownloadCallback()!=null)
      {
        sLog.fine("Executing download callback for storage Object "+storageObject.getId());
        storageObject.getDownloadCallback().run();        
      }
        
      logRestCall(connectionName,"GET",requestURI,httpHeadersValue.toString(),null,"byte[]",startTime,null);
//      sLog.fine("Storage Object "+storageObject.getId()+" downloaded succesfully");
    }
    catch (Exception e)
    {
      logRestCall(connectionName,"GET",requestURI,httpHeadersValue.toString(),null,"byte[]",startTime,null);
      String error = e.getCause() != null? e.getCause().getLocalizedMessage() : e.getLocalizedMessage();
      String message = "Error invoking REST GET service " + requestURI + " : " + error;
      sLog.severe(message);
      if (adp.getResponseStatus()==404)
      {
        // also remove local storage object if exists
        sos.removeStorageObject(storageObject, true);
      }
      handleWebServiceInvocationError(ClassMappingDescriptor.getInstance(StorageObject.class),e,false);                
    }
    finally 
    {
      if (connection!=null)
      {
        try
        {
          connection.close();
        }
        catch (IOException e)
        {
          sLog.severe("Error closing Http connection: "+e.getLocalizedMessage());
        }
      }
    }
  }

  @Override
  /**
   * This methods adds MCS-specific headers if not yet set.
   * If the connectionName is null because it is not specified in persistence-mapping.xml, we will use the MCS connection
   * name as specified in mobile-persistence-config.properties
   */
  public byte[] invokeByteArrayRestService(String connectionName, String requestType, String requestUri, String payload,
                                           Map<String, String> headerParamMap, int retryLimit)
  {
    String connName = connectionName!=null ?  connectionName : getConnectionName();
    return super.invokeByteArrayRestService(connName, requestType, requestUri, payload, addMCSHeaderParamsIfNeeded(headerParamMap),
                                            retryLimit);
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
   * 
   * When you specify (one of) the above headers as part of a method in persistence-mapping.xml,
   * then these values are already included in the header parameter map and this methiod will NOT override them.
   * 
   * @param headerParams
   * @return
   */
  public Map<String, String> addMCSHeaderParamsIfNeeded(Map<String, String> headerParams)
  {
    sLog.fine("Executing addMCSHeaderParamsIfNeeded");
    if (headerParams == null)
    {
      headerParams = new HashMap<String, String>();
    }
    // param names are not case sensitive, so we must check for existence in case insensitive way
    List<String> paramNamesUpper = new ArrayList<String>();
    Iterator<String> paramNames = headerParams.keySet().iterator();
    while (paramNames.hasNext())
    {
      paramNamesUpper.add(paramNames.next().toUpperCase());
    }
    if (!paramNamesUpper.contains(CONTENT_TYPE.toUpperCase()))
    {
      headerParams.put(CONTENT_TYPE, "application/json");
    }
    if (!paramNamesUpper.contains(ORACLE_MOBILE_BACKEND_ID.toUpperCase()) && getMobileBackendId() != null)
    {
      headerParams.put(ORACLE_MOBILE_BACKEND_ID, getMobileBackendId());
    }
    if (!paramNamesUpper.contains(AUTHORIZATION.toUpperCase()))
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
   * with "Basic " if needed. When using an EL expression in mobile-persistence-config.properties, the 
   * value typically already starts with "Basic".
   * Note that the anonymous access key can be changed by calling setAnonymousKey.
   * If the anonymous access key is not set, this method returns null.
   * @return
   */
  public String getAnonymousHeader()
  {
    if (getAnonymousKey() != null)
    {
      if (getAnonymousKey().toUpperCase().startsWith("BASIC "))
      {
        return getAnonymousKey();                
      }
      else
      {
        return "Basic " + getAnonymousKey();        
      }
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
   * If the connectionName is null because it is not specified in persistence-mapping.xml, we will use the MCS connection
   * name as specified in mobile-persistence-config.properties
   */
  public String invokeRestService(String connectionName, String requestType, String requestUri, String payload,
                                  Map<String, String> headerParamMap, int retryLimit, boolean secured)
  {
    String connName = connectionName!=null ?  connectionName : getConnectionName();
    return super.invokeRestService(connName, requestType, requestUri, payload, addMCSHeaderParamsIfNeeded(headerParamMap), retryLimit,
                                   secured);
  }

  /**
   * Creates or updates a storage object in an MCS collection. The ID of the storage object is used
   * as the object identifier, so, when an object with the same ID already exists in this collection, it will be updated
   * and the ETag value of the object will be incremented with 1, the new ETag value returned by MCS is stored in the
   * SQLIte DB together with the other metadata returned by MCS.
   * If the filePath is not set on the storageObject, this method will do nothing.
   * @param storageObject
   */
  public void storeStorageObject(StorageObject storageObject)
  {
    sLog.fine("Executing storeStorageObject");
    if (storageObject.getFilePath()==null)
    {
      sLog.severe("Cannot store object in MCS, file not found on file system: "+storageObject.getFilePath() );
      return;
    }

    String requestURI =
      STORAGE_COLLECTIONS_URI + storageObject.getCollectionName() + "/objects/" + storageObject.getId();
    HashMap<String, String> httpHeadersValue = new HashMap<String, String>();
    httpHeadersValue.put("Oracle-Mobile-Name", storageObject.getId());
    httpHeadersValue.put("Content-Type", storageObject.getContentType());
    addMCSHeaderParamsIfNeeded(httpHeadersValue);
    RestServiceAdapter adp = RestServiceAdapterFactory.newFactory().createRestServiceAdapter();
    HttpConnection connection = null;
    long startTime = System.currentTimeMillis();
    try
    {
      adp.setConnectionName(getConnectionName());
      String requestEndPoint = adp.getConnectionEndPoint(getConnectionName());
      String request = requestEndPoint + requestURI;
      // Get the URI which is defined after the end point
      // Get the connection
      connection = adp.getHttpConnection("PUT", request, httpHeadersValue);
      Path path = Paths.get(storageObject.getFilePath());
      //        FileInputStream fis = new FileInputStream(storageObject.getContent());
      OutputStream os = connection.openOutputStream();
      Files.copy(path, os);

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
      InputStream is = adp.getInputStream(connection);
      boolean gzip = "gzip".equalsIgnoreCase(adp.getResponseHeaders().get("Content-Encoding"));
      String response = getResponse(is, gzip);
      logRestCall(connectionName,"PUT",requestURI,httpHeadersValue.toString(),"byte[]",response,startTime,null);
//      System.err.println(response);
      JSONObject responseObject = (JSONObject) JSONBeanSerializationHelper.fromJSON(JSONObject.class, response);
      // add the collection name to the payload, this is part of primary key and needed for DB insert/update!
      responseObject.append("collectionName", storageObject.getCollectionName());
      super.processPayloadElement(responseObject, StorageObject.class, null, storageObject);
//      System.err.println(response);
    }
    catch (Exception e)
    {
      logRestCall(connectionName,"PUT",requestURI,httpHeadersValue.toString(),"byte[]",null,startTime,e);
      String error = e.getCause() != null? e.getCause().getLocalizedMessage() : e.getLocalizedMessage();
      String message = "Error invoking REST PUT service " + requestURI + " : " + error;
      sLog.severe(message);
      handleWebServiceInvocationError(ClassMappingDescriptor.getInstance(StorageObject.class),e,false);                
    }
    finally 
    {
      if (connection!=null)
      {
        try
        {
          connection.close();
        }
        catch (IOException e)
        {
          sLog.severe("Error closing Http connection: "+e.getLocalizedMessage());
        }
      }
    }
  }

  /**
   * Removes a storage object from an MCS collection. 
   * 
   * @param storageObject
   */
  public void removeStorageObject(StorageObject storageObject)
  {
    sLog.fine("Executing removeStorageObject");
      String requestURI =
        STORAGE_COLLECTIONS_URI + storageObject.getCollectionName() + "/objects/" + storageObject.getId();
    try
    {
     invokeRestService(getConnectionName(),"DELETE",requestURI,null,null,0,false);
    }
    catch (Exception e)
    {
      handleWebServiceInvocationError(ClassMappingDescriptor.getInstance(StorageObject.class),e,false);                
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
      sLog.severe("Error getting MCS stream response: " + e.getLocalizedMessage());
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

  /**
   * Method to register mobile device with MCS so it can receive push notifications.
   * The token is the value you get passed in through the EventListener.onOpen method that
   * you need to implement to enable push notifiations. See MAF Develoepr's guide for more info.
   * @param token
   * @param appId
   * 
   * @return MCS response payload
   */
  public String registerDevice(String token, String appId, String appVersion)
  {
    sLog.fine("Executing registerDevice");
    String os = DeviceManagerFactory.getDeviceManager().getOs().equalsIgnoreCase("IOS") ? "IOS" : "ANDROID";
    String payload =
        "{\"notificationToken\": \""+token+"\",\"mobileClient\": {\"id\": \"" + appId +
        "\",\"version\": \""+appVersion+"\",\"platform\": \""+os+"\"}}";    
    sLog.fine("Request payload for registerDevice: "+payload);
    String result = invokeRestService(getConnectionName(), "POST", REGISTER_DEVICE_URI, payload, null,0, false);
    sLog.fine("Response payload for registerDevice: "+result);
    return result;
  }

  /**
   * Method to deregister mobile device with MCS so it will no longer receive push notifications.
   * 
   * @param token
   * @param appId
   * 
   * @return MCS response payload
   */
  public String deregisterDevice(String token, String appId)
  {
    sLog.fine("Executing deregisterDevice");
    String os = DeviceManagerFactory.getDeviceManager().getOs().equalsIgnoreCase("IOS") ? "IOS" : "ANDROID";
    String payload =
        "{\"notificationToken\": \""+token+"\",\"mobileClient\": {\"id\": \"" + appId +
        "\",\"platform\": \""+os+"\"}}";    
    sLog.fine("Request payload for deregisterDevice: "+payload);
    String result = invokeRestService(getConnectionName(), "POST", DEREGISTER_DEVICE_URI, payload, null,0, false);
    sLog.fine("Response payload for deregisterDevice: "+result);
    return result;
  }
  
  /**
   * Get MCS User info
   * 
   * @param username
   * @return response payload
   */
  public String findUser(String username)
  {
    sLog.fine("Executing findUser");
    String requestURI = USER_URI+"/"+username;
    return invokeRestService(getConnectionName(),"GET",requestURI,null,null,0,false);    
  }

  /**
   * Update MCS User info
   * 
   * @param username
   * @return response payload
   */
  public String updateUser(String username, String requestPayload)
  {
    sLog.fine("Executing updateUser");
    String requestURI = USER_URI+"/"+username;
    return invokeRestService(getConnectionName(),"PUT",requestURI,requestPayload,null,0,false);    
  }

}
