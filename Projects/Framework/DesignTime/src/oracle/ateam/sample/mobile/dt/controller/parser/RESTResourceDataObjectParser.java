package oracle.ateam.sample.mobile.dt.controller.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.math.BigDecimal;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oracle.adf.model.connection.rest.RestConnection;

import oracle.adfdtinternal.model.adapter.webservice.utils.RestUtil;

import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DCMethodParameter;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.HeaderParam;
import oracle.ateam.sample.mobile.dt.util.StringUtils;

import oracle.ide.panels.TraversalException;

import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLText;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import sun.misc.BASE64Encoder;

public class RESTResourceDataObjectParser
{
  private List<DataObjectInfo> dataObjectInfos = new ArrayList<DataObjectInfo>();
  private GsonBuilder gb = new GsonBuilder();
  private Gson gson = gb.create();
  private Map<String,String> pathParams = new HashMap<String,String>();
  private String connectionUri;
  private String connectionName;
  private List<HeaderParam> headerParams;
  private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private DocumentBuilder builder = null;
  private List<DCMethod> resources;
  private boolean flattenNestedObjects;

  public RESTResourceDataObjectParser(List<DCMethod> resources, String connectionName, String connectionUri, List<HeaderParam> headerParams, Map<String,String> pathParams, boolean flattenNestedObjects)
  {
    super();
    this.resources = resources;
    this.connectionName = connectionName;
    this.connectionUri = connectionUri;
    this.headerParams = headerParams;
    this.pathParams = pathParams;
    this.flattenNestedObjects = flattenNestedObjects;
    try
    {
      factory.setNamespaceAware(true);
      builder = factory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e)
    {
    }
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
        conn.setRequestMethod(resource.getRequestType());
        conn.setDoOutput(true);
        boolean samplePayloadSet = resource.getSamplePayload()!=null && !"".equals(resource.getSamplePayload().trim());
        String response = samplePayloadSet ? resource.getSamplePayload() : getResponse(conn.getInputStream());
        //        String response = getResponse(model.getConnectionName(), resource.getFullUri(), resource.getRequestType());
        if (response == null || "".equals(response.trim()))
        {
          throw new TraversalException("Response payload is empty for " + urlString);
        }
        if (urlString.endsWith("/describe"))
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
          parseXMLPayload(resource, response, root);
        }
        else
        {
          root.setXmlPayload(false);
          parseJSONPayload(response, root);
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

  private void processMap(Map map, DataObjectInfo currentDataObject, String payloadAttrPrefix)
  {
    Iterator keys = map.keySet().iterator();
    while (keys.hasNext())
    {
      String key = (String) keys.next();
      Object value = map.get(key);
      if (value instanceof Map)
      {
        // if flatten is set to yes, we include the attributes of this map in the current doi,
        // where the attr names are prefixed with the key of the map
        if (flattenNestedObjects)
        {
          processMap((Map) value, currentDataObject, payloadAttrPrefix+key+".");                    
        }
        else
        {
          // not a list op objects, but a single object with a number of attrs
          // in this case, we have to set the list element name to the name of parent doi,
          // and the row element name to the current key
          DataObjectInfo childDoi = new DataObjectInfo(key, currentDataObject.getAccessorPath() + "." + key);
          childDoi.setPayloadListElementName(currentDataObject.getPayloadListElementName());
          childDoi.setPayloadRowElementName(key);
          childDoi.setXmlPayload(false);
          childDoi.setParent(currentDataObject);
          dataObjectInfos.add(childDoi);
          currentDataObject.addChild(new AccessorInfo(currentDataObject, childDoi));
          processMap((Map) value, childDoi, payloadAttrPrefix);          
        }
      }
      else if (value instanceof List)
      {
        // now done in DataObjectsPanel.onExit, is saver
        //        if (resource.getPayloadReturnElementName() == null)
        //        {
        //          resource.setPayloadReturnElementName(key);
        //        }
        processJSONList(currentDataObject, key, value,payloadAttrPrefix);
      }
      else
      {
        Class clazz = value != null? value.getClass(): String.class;
        if (Double.class == clazz)
        {
          // Use BigDecimal instead of double, because double always rendered with ".0" when doing toString.
          // This might cause problems wit SQL where queries. BigDecimal only adds decimal when there is a decimal
          // value
          clazz = BigDecimal.class;
        }
        // remove the dots from prefix and make it camelcase after each dot
        currentDataObject.addAttribute(new AttributeInfo(payloadAttrPrefix+key,clazz.getName()));
      }
    }
  }

  private void processJSONList(DataObjectInfo currentDataObject, String key, Object value, String payloadAttrPrefix)
  {
    List children = (List) value;
    if (children.size() > 0)
    {
      Object child = children.get(0);
      if (child instanceof Map)
      {
        if ("root".equals(key))
        {
          // we can attributes straight to existing root data object
          processMap((Map) child, currentDataObject,"");
        }
        else
        {
          DataObjectInfo childDoi = new DataObjectInfo(key, currentDataObject.getAccessorPath()+"."+ payloadAttrPrefix + key);
          String curPayloadListElementName = currentDataObject.getPayloadListElementName();
          String curPayloadRowElementName = currentDataObject.getPayloadRowElementName();
          if ("root".equals(curPayloadListElementName))
          {
            childDoi.setPayloadListElementName(key);             
          }
          else
          {
            childDoi.setPayloadListElementName(curPayloadListElementName);       
            String rowElement = curPayloadRowElementName!=null ? curPayloadRowElementName+"."+key : key;
            childDoi.setPayloadRowElementName(rowElement);                      
          }
          childDoi.setXmlPayload(false);
          childDoi.setParent(currentDataObject);
          // we don't know at this point which data objects will be selected for generation
          // so we need to set findAll method on all of them
          childDoi.setFindAllMethod(currentDataObject.getFindAllMethod());
          dataObjectInfos.add(childDoi);
          AccessorInfo accessorInfo = new AccessorInfo(currentDataObject, childDoi);
          accessorInfo.setChildAccessorPayloadName(payloadAttrPrefix+key);
          currentDataObject.addChild(accessorInfo);
          processMap((Map) child, childDoi,"");
        }
      }
    }
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

  private void parseJSONPayload(String response, DataObjectInfo root)
    throws TraversalException
  {
    try
    {
      HashMap map = gson.fromJson(response, HashMap.class);
      if (map == null)
      {
        throw new TraversalException("Response JSON payload is invalid: " + response);
      }
      processMap(map, root,"");
    }
    catch (Exception jse)
    {
      // try to parse as list
      try
      {
        List list = gson.fromJson(response, ArrayList.class);        
        if (list!=null)
        {
          processJSONList(root, "root", list,"");
        }
      }
      catch (Exception e)
      {
        // TODO: Add catch code
        jse.printStackTrace();
        throw new TraversalException("Error parsing JSON payload " + response + ": " + jse.getLocalizedMessage());
      }
    }
  }

/******************** methods for XML parsing ******************/

  private void parseXMLPayload(DCMethod method, String response, DataObjectInfo dataObjectInfo)
    throws TraversalException
  {
    Document doc;
    try
    {
      doc = builder.parse(new ByteArrayInputStream(response.getBytes()));
      NodeList nodes = doc.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++)
      {
        Node node = nodes.item(i);
        if (node instanceof XMLElement)
        {
          processNode(method, dataObjectInfo, (XMLElement) node);
        }
      }
    }
    catch (SAXException e)
    {
      e.printStackTrace();
      throw new TraversalException("Error parsing XML payload: " + e.getLocalizedMessage());
    }
    catch (IOException e)
    {
      e.printStackTrace();
      throw new TraversalException("Error parsing XML payload: " + e.getLocalizedMessage());
    }
  }

  private void processNode(DCMethod resource, DataObjectInfo currentDataObject, XMLElement node)
  {

    if (node.getAttributes() != null && node.getAttributes().getLength() > 0)
    {
      NamedNodeMap attributes = node.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++)
      {
        Node attr = attributes.item(i);
        currentDataObject.addAttribute(new AttributeInfo(attr.getNodeName(), "java.lang.String"));
      }
    }
    NodeList children = node.getChildNodes();
    if (children.getLength() == 0 || (children.getLength() == 1 && children.item(0) instanceof XMLText))
    {
      // it is an attribute
      // if no children it is an emty attribute
      // if no children it is an emty attribute
      currentDataObject.addAttribute(new AttributeInfo(node.getNodeName(), "java.lang.String"));

    }
    else if (children.getLength() > 0)
    {
      DataObjectInfo childDoi =
        new DataObjectInfo(node.getNodeName(), currentDataObject.getAccessorPath() + "." + node.getNodeName());
      if (dataObjectInfos.contains(childDoi))
      {
        // contains method returns true when DOI with same name already created (see equals method in DataObjectInfo)
        return;
      }
      childDoi.setXmlPayload(true);
      childDoi.setPayloadListElementName(node.getParentNode().getNodeName());
      resource.setPayloadElementName(childDoi.getPayloadListElementName());
      childDoi.setPayloadRowElementName(node.getNodeName());
      childDoi.setParent(currentDataObject);
      childDoi.setFindAllMethod(resource);
      dataObjectInfos.add(childDoi);
      currentDataObject.addChild(new AccessorInfo(currentDataObject, childDoi));
      int childElementCount = 0;
      for (int i = 0; i < children.getLength(); i++)
      {
        Node kid = (Node) children.item(i);
        if (kid instanceof XMLElement)
        {
          childElementCount++;
          processNode(resource, childDoi, (XMLElement) kid);
        }
      }
      // now done in DataObjectsPanel.onExit, is saver
      //      if (childElementCount > 0 && resource.getPayloadReturnElementName() == null)
      //      {
      //        resource.setPayloadReturnElementName(node.getNodeName());
      //      }
    }
  }

}
