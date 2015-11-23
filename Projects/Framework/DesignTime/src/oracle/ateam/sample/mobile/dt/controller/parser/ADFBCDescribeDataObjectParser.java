/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.controller.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.AccessorInfo.AttributeMapping;
import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DCMethodParameter;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.HeaderParam;
import oracle.ateam.sample.mobile.dt.util.StringUtils;

public class ADFBCDescribeDataObjectParser
{

  private List<DataObjectInfo> dataObjectInfos = new ArrayList<DataObjectInfo>();

  private GsonBuilder gb = new GsonBuilder();
  private Gson gson = gb.create();
  List<HeaderParam> headerParams;
  private String connectionName;
  private String connectionUri;
  private String describeContent;

  public ADFBCDescribeDataObjectParser(String describeContent, String connectionName, String connectionUri, List<HeaderParam> headerParams)
  {
    super();
    this.connectionName = connectionName;
    this.connectionUri = connectionUri;
    this.headerParams = headerParams;
    this.describeContent = describeContent;
  }

  public List<DataObjectInfo> run()
  {
    HashMap map = gson.fromJson(describeContent, HashMap.class);
    Map<String,Map> resources = (Map<String,Map>) map.get("Resources");
    Iterator<String> keys = resources.keySet().iterator(); 
    // first process all top-level VO's, so we have data objects for all of them
    // then we process the children as well. This is needed because child VO's might
    // exist of a reference to a top-level (canonical) VO, with just a a subset of the attributes
    // For such VO's we do not want to create a data object, only an accessor
    while (keys.hasNext())
    {
      String key = keys.next();
      createDataObject(key, resources.get(key),null,null,false);
    }
    keys = resources.keySet().iterator(); 
    while (keys.hasNext())
    {
      String key = keys.next();
      createDataObject(key, resources.get(key),null,null,true);
    }
    return dataObjectInfos;
  }

  private static Map<String,Class> typeMapping = new HashMap<String,Class>();  
  static
  {
    // Mapping based on http://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html#1034737
    typeMapping.put("string",String.class);
    typeMapping.put("integer", Integer.class);
    typeMapping.put("number",BigDecimal.class);
    typeMapping.put("date",Date.class);
  }


  private void createDataObject(String name, Map desc, DataObjectInfo parentDataObject, Map parentDesc, boolean processChildren)
  {
    DataObjectInfo doi = new DataObjectInfo(name,name);
    doi.setPayloadDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    doi.setPayloadDateTimeFormat("yyyy-MM-dd'T'HH:mm:ss");
    // this works because equals method is implemented comparing the name of the data objects
    int index = dataObjectInfos.indexOf(doi);
    if (index>-1)
    {
      // data object already created, must be a reference to a root VO
      doi = dataObjectInfos.get(index);
      // we only need to create the childAccessor in the parent
      if (parentDataObject!=null)
      {
        String uri = getResourceUri(desc);
        createChildAccessor(parentDataObject, parentDesc, doi, uri);
      }
    }
    else
    {
      List attributes = (List) desc.get("attributes");
      List<String> keyAttrs = getkeyAttributes(desc);
      for (int i = 0; i < attributes.size(); i++)
      {
        Map attrDef = (Map) attributes.get(i);
        String type = (String) attrDef.get("type");
        String attrName = (String) attrDef.get("name");
        AttributeInfo attr = new AttributeInfo(attrName, typeMapping.get(type).getName());
        attr.setRequired((Boolean)attrDef.get("mandatory"));
        doi.addAttribute(attr);
        if (keyAttrs.contains(attrName))
        {
          attr.setKeyAttribute(true);
        }
      }
      doi.setXmlPayload(false);
      doi.setPayloadListElementName(doi.getName());
      dataObjectInfos.add(doi);
      String uri = getResourceUri(desc);
      if (parentDataObject!=null)
      {
        createChildAccessor(parentDataObject, parentDesc, doi, uri);
      }
      else
      {
        DCMethod findAllMethod = new DCMethod(connectionName,uri,"GET");
        // set existing to true to prevent override of layload element name when we exit dataObjects wizard page
        findAllMethod.setExisting(true);
        findAllMethod.setPayloadElementName("items");      
        doi.setFindAllMethod(findAllMethod);
      }
      String canonicalResource = getCanonicalResource(desc);
      // should never be null, also used for update/delete methods
      //    if (canonicalResource!=null)
      //    {
        DCMethod canonicalMethod = new DCMethod(connectionName,canonicalResource,"GET");
        doi.setGetCanonicalMethod(canonicalMethod);
        canonicalMethod.setParameterValueProviderDataObject(doi);      
        // path params are not set up when is is child do because then we have both id and id1 in path
        if (parentDataObject!=null)
        {
          setUpPathParams(parentDataObject, doi, canonicalMethod);
        }
      //    }

      // add crud resource methods
      DCMethod method = createCrudMethod("create", desc, doi,parentDataObject,uri,"POST");
      if (method!=null)
      {
        doi.setCreateMethod(method);
      }
      method = createCrudMethod("update", desc, doi,parentDataObject,canonicalResource,"PATCH");
      if (method!=null)
      {
        doi.setUpdateMethod(method);
      }
      method = createCrudMethod("replace", desc, doi,parentDataObject,canonicalResource,"PUT");
      if (method!=null)
      {
        doi.setMergeMethod(method);
      }
      method = createCrudMethod("delete", desc, doi,parentDataObject,canonicalResource,"DELETE");
      if (method!=null)
      {
        doi.setDeleteMethod(method);
      }      
    }
     
    // recursive call when this resource has child resources
  //    "item" : {
  //         "links" : {
  //           "self" : {
  //             "rel" : "self",
  //             "href" : "http://slc01qrl.us.oracle.com:7901/stable/rest/Employees/{id}"
  //           },
  //           "DepartmentsView" : {
  //             "rel" : "child",
  //             "href" : "http://slc01qrl.us.oracle.com:7901/stable/rest/Employees/{id}/child/DepartmentsView",
  //             "cardinality" : {
  //               "value" : "1 to *",
  //               "sourceAttributes" : "EmployeeId",
  //               "destinationAttributes" : "ManagerId"
  //             }
  //           },
  //           "canonical" : {
  //             "rel" : "canonical",
  //             "href" : "http://slc01qrl.us.oracle.com:7901/stable/rest/Employees/{id}"
  //           }
  //         },
    if (processChildren)
    {
      Map children = (Map) desc.get("children");
      if (children!=null)
      {
        Iterator kids = children.keySet().iterator();
        while (kids.hasNext())
        {
          String kid = (String) kids.next();
          Map kidMap = (Map) children.get(kid);
          createDataObject(kid,kidMap,doi, desc, true);
        }      
      }      
    }
  }
  
  public String getAccessorName(DataObjectInfo doi)
  {
    String name = doi.getName();
    String attrNameWithUnderscores = StringUtils.substitute(name, ".", "_");
    return StringUtils.startWithLowerCase(StringUtils.toCamelCase(attrNameWithUnderscores));
  }

  private void setUpPathParams(DataObjectInfo parentDataObject, DataObjectInfo doi, DCMethod method)
  {
    AccessorInfo acc =parentDataObject.findChildAccessor(getAccessorName(doi));
    if (acc!=null)
    {
      if (acc.getAttributeMappings().size()>0)
      {
        AttributeMapping am = acc.getAttributeMappings().get(0);
        for (DCMethodParameter param : method.getPathParams())
        {
          if (param.getName().equals("id"))
          {
            param.setValueProvider(DCMethodParameter.DATA_OBJECT_ATTRIBUTE);
            param.setDataObjectAttribute(am.getChildAttr().getAttrName());
          }
          else if (param.getName().equals("id1"))
          {
            if (doi.getKeyAttributes().size()>0)
            {
              param.setValueProvider(DCMethodParameter.DATA_OBJECT_ATTRIBUTE);
              param.setDataObjectAttribute(doi.getKeyAttributes().get(0).getAttrName());              
            }
          }
        }        
      }
    }
  }

  private void createChildAccessor(DataObjectInfo parentDataObject, Map parentDesc, DataObjectInfo doi, String uri)
  {
//    DCMethod childAccessorMethod = new DCMethod(doi.getName()+"List",connectionName, uri, "GET");
    String accessorName = getAccessorName(doi);
    DCMethod childAccessorMethod = new DCMethod(accessorName,connectionName, uri, "GET");
    childAccessorMethod.setParameterValueProviderDataObject(parentDataObject);
    // we assume payload structure is same as one used to discover dthe data objects
    
//    childAccessorMethod.setPayloadElementName(doi.getPayloadListElementName());
    childAccessorMethod.setPayloadElementName("items");
    childAccessorMethod.setPayloadRowElementName(doi.getPayloadRowElementName());

    //      doi.setPayloadListElementName(currentDataObject.getPayloadListElementName());
    AccessorInfo childAccessor = new AccessorInfo(parentDataObject, doi);
    doi.addFindAllInParentMethod(childAccessorMethod);
    childAccessor.setChildAccessorMethod(childAccessorMethod);
    childAccessor.setChildAccessorName(accessorName);            
    
    Map<String,String> attributeMapping = getChildAccessorAttributeMapping(doi.getName(), parentDesc);
    for (String sourceAttr :attributeMapping.keySet())
    {
      AttributeInfo parentAttributeDef = parentDataObject.getAttributeDefByPayloadName(sourceAttr);
      AttributeInfo childAttributeDef = doi.getAttributeDefByPayloadName(attributeMapping.get(sourceAttr));
      if (parentAttributeDef!=null && childAttributeDef!=null)
      {
        childAccessor.addAttributeMapping(parentAttributeDef, childAttributeDef);          
      }
    }
    if (childAccessor.getAttributeMappings().size()>0)
    {
      parentDataObject.addChild(childAccessor);              
    }
    // we do not set the parent, because the child payload is not included with parent,
    // it is a separate rest call
    //      childAccessor.setChildAccessorPayloadName(doi.getName());
    //      doi.setParent(currentDataObject);
  }


  //  "collection" : {
  //         "rangeSize" : 25,
  //         "finders" : [ {
  //           "name" : "PrimaryKey",
  //           "attributes" : [ {
  //             "name" : "EmployeeId",
  //             "type" : "integer",
  //             "updatable" : true,
  //             "mandatory" : true,
  //             "queryable" : true,
  //             "precision" : 6
  //           } ]
  //         } ],
  //         "links" : {
  //           "self" : {
  //             "rel" : "self",
  //             "href" : "http://slc01qrl.us.oracle.com:7901/stable/rest/Employees"
  //           },
  //           "self" : {
  //             "rel" : "self",
  //             "href" : "http://slc01qrl.us.oracle.com:7901/stable/rest/Employees"
  //           }
  //         },
  //         "actions" : [ {
  //           "name" : "get",
  //           "method" : "GET",
  //           "responseType" : [ "application/json", "application/vnd.oracle.adf.resource+json" ]
  //         }, {
  //           "name" : "create",
  //           "method" : "POST",
  //           "responseType" : [ "application/json", "application/vnd.oracle.adf.resource+json" ],
  //           "requestType" : [ "application/vnd.oracle.adf.resource+json" ]
  //         }, {
  //           "name" : "delete",
  //           "method" : "DELETE"
  //         } ]
  //       },

  private String getResourceUri(Map desc)
  {
    Map collection = (Map) desc.get("collection");
    List links = (List) collection.get("links");
    Map self = null;
    for (int i = 0; i < links.size(); i++)
    {
      Map map = (Map) links.get(i);
      if ("self".equals(map.get("rel")))
      {
        self = map;
        break;
      }
    }
    if (self!=null) 
    {
      String url = (String) self.get("href");
      if (url.startsWith(connectionUri))
      {
        return url.substring(connectionUri.length());
      }
      else
      {
        // this should never happen
        return null;
      }      
    }
    return null;
  }

  private List<String> getkeyAttributes(Map desc)
  {
    List<String> attrs = new ArrayList<String>();
    Map collection = (Map) desc.get("collection");
    List finders = (List) collection.get("finders");    
    for (int i = 0; i < finders.size(); i++)
    {
      Map finderEntry = (Map) finders.get(i);
      if (finderEntry.get("name").equals("PrimaryKey"))
      {
        List attributes = (List) finderEntry.get("attributes");
        for (int j = 0; j < attributes.size(); j++)
        {
          Map attrDef = (Map) attributes.get(j);
          attrs.add((String) attrDef.get("name"));
        }
        break;
      }
    }
    return attrs;
  }

  private Map<String,String> getChildAccessorAttributeMapping(String accessorName, Map parentDesc)
  {
    Map collection = (Map) parentDesc.get("item");
    List links = (List) collection.get("links");
    Map child = null;
    for (int i = 0; i < links.size(); i++)
    {
      Map map = (Map) links.get(i);
      if (accessorName.equals(map.get("name")))
      {
        child = map;
        break;
      }
    }
    Map cardinality = (Map) child.get("cardinality");
    String sourceAttributes = (String) cardinality.get("sourceAttributes");
    String destinationAttributes = (String) cardinality.get("destinationAttributes");
    List<String> sourceAttrs = StringUtils.stringToList(sourceAttributes, ",");
    List<String> destAttrs = StringUtils.stringToList(destinationAttributes, ",");
    Map<String,String> attrMapping = new HashMap<String,String>();
    int counter = 0;
    for (String attr : sourceAttrs)
    {
      attrMapping.put(attr, destAttrs.get(counter));
      counter++;
    }
    return attrMapping;
  }

  private String getCanonicalResource(Map desc)
  {
    Map collection = (Map) desc.get("item");
    List links = (List) collection.get("links");
    Map canonical = null;
    for (int i = 0; i < links.size(); i++)
    {
      Map map = (Map) links.get(i);
      if ("canonical".equals(map.get("rel")))
      {
        canonical = map;
        break;
      }
    }
    if (canonical!=null)
    {
      String fullResource = (String) canonical.get("href");
      // remove the connection part
      if (fullResource.startsWith(connectionUri))
      {
        return fullResource.substring(connectionUri.length());
      }
      else
      {
        // return the whole resource for now, this should probably never happen
        return fullResource;
      }
    }
    return null;
  }

  private boolean isCrudActionSupported(Map desc, String crudAction)
  {
    // creat action is defined aginast the collection attribute , the other ones against the item attribute
    String rootAttr = "create".equals(crudAction) ? "collection" : "item";
    Map collectionOrItem = (Map) desc.get(rootAttr);
    List actions = (List) collectionOrItem.get("actions");
    boolean supported = false;
    for (int i = 0; i < actions.size(); i++)
    {
      Map action = (Map) actions.get(i);
      if (crudAction.equals(action.get("name")))
      {
        supported = true;
        break;
      }
    }
    return supported;
  }

  private DCMethod createCrudMethod(String action, Map desc, DataObjectInfo doi, DataObjectInfo parentDataObject, String resource, String requestMethod)
  {
    if (isCrudActionSupported(desc,action))
    {
      DCMethod method = new DCMethod(connectionName,resource,requestMethod);
      // ADF BC Rest changed format in 12.2.1., items [] no longer used for crud opers
//      method.setPayloadElementName(doi.getPayloadListElementName());
      method.setPayloadElementName(null);
      method.setParameterValueProviderDataObject(doi);
      // set existing to true to prevent override of layload element name when we exit dataObjects wizard page
      method.setExisting(true);
      
      if ("delete".equalsIgnoreCase(action))
      {
        method.setSendSerializedDataObjectAsPayload(false);        
      }
      else
      {
        method.setSendSerializedDataObjectAsPayload(true);                
        String contentType = "application/vnd.oracle.adf.resourceitem+json";
        HeaderParam contentTypeParam = new HeaderParam();
        contentTypeParam.setName("Content-Type");
        contentTypeParam.setValue(contentType);
        method.addHeaderParam(contentTypeParam);  
      }
      if (parentDataObject!=null)
      {
        setUpPathParams(parentDataObject,doi,method);        
      }
      return method;
    }
    return null;
  }

}
