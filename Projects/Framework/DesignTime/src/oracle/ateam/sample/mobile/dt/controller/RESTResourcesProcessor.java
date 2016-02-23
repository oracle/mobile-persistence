/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.adf.model.connection.rest.RestConnection;

import oracle.adfdtinternal.model.adapter.webservice.utils.RestUtil;

import oracle.ateam.sample.mobile.dt.controller.parser.ADFBCDescribeDataObjectParser;
import oracle.ateam.sample.mobile.dt.controller.parser.JSONExampleDataObjectParser;
import oracle.ateam.sample.mobile.dt.controller.parser.XMLExampleDataObjectParser;
import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DCMethodParameter;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.HeaderParam;
import oracle.ateam.sample.mobile.dt.util.StringUtils;

import oracle.ide.panels.TraversalException;

import sun.misc.BASE64Encoder;

public class RESTResourcesProcessor
{
  private List<DataObjectInfo> dataObjectInfos = new ArrayList<DataObjectInfo>();
  private Map<String,String> pathParams = new HashMap<String,String>();
  private String connectionUri;
  private String connectionName;
  private List<HeaderParam> headerParams;
  private List<HeaderParam> mcsHeaderParams;
  private List<DCMethod> resources;
  private boolean flattenNestedObjects;
  private boolean adfbcDescribe;

  public RESTResourcesProcessor(List<DCMethod> resources, String connectionName, String connectionUri, List<HeaderParam> headerParams, List<HeaderParam> mcsHeaderParams, Map<String,String> pathParams, boolean flattenNestedObjects, boolean adfbcDescribe)
  {
    super();
    this.resources = resources;
    this.connectionName = connectionName;
    this.connectionUri = connectionUri;
    this.headerParams = headerParams;
    this.mcsHeaderParams = mcsHeaderParams;
    this.pathParams = pathParams;
    this.flattenNestedObjects = flattenNestedObjects;
    this.adfbcDescribe = adfbcDescribe;
  }


  public List<DataObjectInfo> run()
    throws TraversalException
  {
    for (DCMethod resource: resources)
    {
      String urlString = getUrlString(connectionUri, resource);
      if (urlString == null)
      {
        continue;
      }
      try
      {
        URL url = new URL(urlString);
  //        URLConnection urlConn = oracle.adfdtinternal.model.adapter.url.URLUtil.fetchURLConnection(model.getConnectionName());
        RestConnection  urlConn = RestUtil.fetchRestConnection(connectionName);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (urlConn.getAuthenticationScheme()!=null)
        {
          String userName = String.valueOf(urlConn.getAuthenticationScheme().getCredentials().get("username"));
          String password = String.valueOf(urlConn.getAuthenticationScheme().getCredentials().get("password"));
          if (userName!=null && password!=null)
          {
            String userCredentials = userName+":"+password;
            String basicAuth = "Basic " + new BASE64Encoder().encode(userCredentials.getBytes());
            HeaderParam authParam = new HeaderParam();
            authParam.setName("Authorization");
            authParam.setValue(basicAuth);
            if (!headerParams.contains(authParam))
            {
              headerParams.add(authParam);
            }
            resource.setIsSecured(true);
          }
        }
        for(HeaderParam param : headerParams)
        {          
          conn.setRequestProperty(param.getName(),param.getValue());          
        }
        if (mcsHeaderParams!=null)
        {
          // Add MCS headers as specified as part of MCS connection details/
          // These headers should NOT be added to each data object method, because
          // the MCSPersistenceManager will automatically add them.
          for(HeaderParam param : mcsHeaderParams)
          {          
            conn.setRequestProperty(param.getName(),param.getValue());          
          }
        }
        conn.setRequestMethod(resource.getRequestType());
        conn.setDoOutput(true);
        boolean samplePayloadSet = resource.getSamplePayload()!=null && !"".equals(resource.getSamplePayload().trim());
        String response = samplePayloadSet ? resource.getSamplePayload() : getResponse(conn.getInputStream());
        //        String response = getResponse(model.getConnectionName(), resource.getFullUri(), resource.getRequestType());
        if (response == null || "".equals(response.trim()))
        {
          throw new TraversalException("Response payload is empty for " + urlString);
        }
//        if (samplePayloadSet && response.startsWith("#%RAML")) {
//            RAMLParser processor = new RAMLParser(response, connectionName, connectionUri, headerParams,flattenNestedObjects);
//            dataObjectInfos = processor.run();
//            continue;
//        }
        else if (adfbcDescribe)
        {
          ADFBCDescribeDataObjectParser processor =
            new ADFBCDescribeDataObjectParser(response, connectionName, connectionUri, headerParams);          
          dataObjectInfos = processor.run();
          continue;
        }
        String resourceName = resource.getName();
        if (resourceName.endsWith("/"))
        {
          resourceName = resourceName.substring(0,resourceName.length()-1);
        }
        int lastSlashPos = resourceName.lastIndexOf("/");
        String rootName = lastSlashPos > 0? resourceName.substring(lastSlashPos + 1):resourceName;
        DataObjectInfo root = new DataObjectInfo(rootName, rootName);
        root.setPayloadListElementName("root");
  //        root.setAccessorPath("root");
          root.setAccessorPath(rootName);
        dataObjectInfos.add(root);
        root.setFindAllMethod(resource);
        resource.setPayloadElementName(root.getPayloadListElementName());
        if (response.trim().startsWith("<"))
        {
          root.setXmlPayload(true);
          XMLExampleDataObjectParser parser = new XMLExampleDataObjectParser();
          parser.parse(resource, response, root, this.dataObjectInfos);
        }
        else
        {
          root.setXmlPayload(false);
          JSONExampleDataObjectParser parser = new JSONExampleDataObjectParser();
          parser.parse(response, root,flattenNestedObjects, this.dataObjectInfos);
        }
        removeAndReparentDataObjects();
      }
  //      catch (MalformedURLException e)
  //      {
  //        throw new TraversalException("Invalid URL: " + urlString);
  //      }
      catch (Exception e)
      {
        e.printStackTrace();
        throw new TraversalException("Cannot access URL " + urlString+": "+e.getClass().getName()+": "+e.getLocalizedMessage()+". Check the JDeveloper Http Proxy Server settings under Tools -> Preferences.");
      }
    }
    // clean up attr names
    for (DataObjectInfo doi: dataObjectInfos)
    {
      doi.cleanUpAttrNames();
    }
    return dataObjectInfos;
  }

  private String getResponse(InputStream is)
    throws TraversalException
  {
    BufferedReader rd;
    StringBuffer response = new StringBuffer("");
    try
    {
      rd = new BufferedReader(new InputStreamReader(is));
      String line;
      while ((line = rd.readLine()) != null)
      {
        response.append(line + "\n");
      }
      rd.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      throw new TraversalException("Error reading REST response: " + e.getLocalizedMessage());
    }
    finally
    {
      try
      {
        is.close();        
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    return response.toString();
  }

  private String getUrlString(String connectionUri, DCMethod resource)
    throws TraversalException
  {
    if (resource.getFullUri() == null || "".equals(resource.getFullUri().trim()))
    {
      return null;
    }
    if (resource.getRequestType() == null || "".equals(resource.getRequestType().trim()))
    {
      throw new TraversalException("You need to specify the request type for resource: " + resource.getName());
    }
    String urlString = null;
    // prevent doube slashes while concatenating connection and uri, results in error
    if (connectionUri.endsWith("/") && resource.getFullUri().startsWith("/"))
    {
      urlString = connectionUri + resource.getFullUri().substring(1);
    }
    else if (!connectionUri.endsWith("/") && !resource.getFullUri().startsWith("/"))
    {
      urlString = connectionUri + "/" + resource.getFullUri();
    }
    else
    {
      urlString = connectionUri + resource.getFullUri();
    }
    // replace pathParams with sample values stored in pathParamsMap
    for(DCMethodParameter param : resource.getParams())
    {
      if (param.isPathParam())
      {
        String oldValue = "{"+param.getName()+"}";
        String newValue = pathParams.get(param.getName());
        urlString = StringUtils.substitute(urlString, oldValue, newValue);
      }
    }
    return urlString;
  }

  private void removeAndReparentDataObjects()
  {
    // if a data object does not have own attrs, we remove it again
    // and reparent to the one above that does have attrs (if it exists)
    List<DataObjectInfo> toRemove = new ArrayList();
    for (DataObjectInfo doi: dataObjectInfos)
    {
      if (doi.getAttributeDefs().size() == 0)
      {
        for (AccessorInfo child: doi.getAllChildren())
        {
          DataObjectInfo newParent = null;
          if (doi.getParent() != null && !toRemove.contains(doi.getParent()))
          {
            newParent = doi.getParent();
            // and in the new  parent, there is child accessor to the current doi, and in this child accessor
            // the child data objects must be changed to this lower-level child because we take out the intermediate current doi !
            for (AccessorInfo parentChild: newParent.getAllChildren())
            {
              if (parentChild.getChildDataObject() == doi)
              {
                parentChild.setChildDataObject(child.getChildDataObject());
              }
            }
          }
          child.getChildDataObject().setParent(newParent);

        }
        // set generate to false so it wont show up in one-to-many mapping panel either.
        // This is the easy way, we could also removew the child accessor in the parent still pointing to
        // this doi
        doi.setGenerate(false);
        toRemove.add(doi);
      }
    }
    for (DataObjectInfo doi: toRemove)
    {
      dataObjectInfos.remove(doi);
    }
  }
}
