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
import java.util.List;
import java.util.Map;

import oracle.ateam.sample.mobile.dt.exception.ParseException;
import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;


public class JSONSchemaDataObjectParser
{
  private static Map<String,Class> typeMapping = new HashMap<String,Class>();  
  static
  {
    typeMapping.put("string",String.class);
    typeMapping.put("integer", Integer.class);
    typeMapping.put("number",BigDecimal.class);
    typeMapping.put("date",Date.class);
    typeMapping.put("boolean",Boolean.class);
  }

  private List<DataObjectInfo> dataObjectInfos = new ArrayList<DataObjectInfo>();
  private GsonBuilder gb = new GsonBuilder();
  private Gson gson = gb.create();
  private boolean flattenNestedObjects = false;


  public void parse(String schema, DataObjectInfo root, boolean flattenNestedObjects,
                    List<DataObjectInfo> dataObjectInfos)
  {
    this.flattenNestedObjects = flattenNestedObjects;
    this.dataObjectInfos = dataObjectInfos;
    try
    {
      HashMap map = gson.fromJson(schema, HashMap.class);
      processMap(null, map, root, "");
    }
    catch (Exception jse)
    {
      jse.printStackTrace();
      throw new ParseException("Error parsing JSON Schema" + schema + ": " + jse.getLocalizedMessage());
    }
  }

  private void processMap(String key, Map map, DataObjectInfo currentDataObject, String payloadAttrPrefix)
  {
    String type = (String) map.get("type");
    if ("object".equals(type))
    {
      String prefix = payloadAttrPrefix;
      DataObjectInfo currentDoi = currentDataObject;
      if (flattenNestedObjects)
      {
        prefix = (key == null)? prefix: prefix + key + ".";
      }
      else if (key != null)
      {
        // create child data object, if key is null we are processing root schema object, so no need
        // to create child object first
        currentDoi = createChildDataObject(key, currentDataObject);
        currentDoi.setPayloadListElementName(currentDataObject.getPayloadListElementName());
        currentDoi.setPayloadRowElementName(key);
      }
      // get object properties map
      Map<String, Map> properties = (Map<String, Map>) map.get("properties");
      for (String propertyName: properties.keySet())
      {
        // recursive call
        processMap(propertyName, properties.get(propertyName), currentDoi, prefix);
      }
    }
    else if ("array".equals(type))
    {
      // get array items map
      Map<String, Map> items = (Map<String, Map>) map.get("items");
      DataObjectInfo currentDoi = currentDataObject;
      if (key != null)
      {
        // create child data object, if key is null we are processing root schema object, so no need
        // to create child object first
        currentDoi = createChildDataObject(key, currentDataObject);
      }
      // if the key is null, it is the top level data object and we set the list element name to "root".
      // if we would leave it empty, the runtime would treat as object instead of array when serializing data object
      // for write resource (PUT/POST/etc)
      
      currentDoi.setPayloadListElementName(key==null ? "root" : key);
      processMap(key, items, currentDoi, "");
    }
    else
    {
      // add attribute
      Class javaType = typeMapping.get(type);
      if (javaType==null)
      {
        //default to String
        javaType = String.class;
      }
      AttributeInfo attr = new AttributeInfo(payloadAttrPrefix+key,javaType.getName());
      if (currentDataObject.getAttributeDef(attr.getAttrName())==null)
      {
        currentDataObject.addAttribute(attr);          
      }
    }
  }

  private DataObjectInfo createChildDataObject(String key, DataObjectInfo currentDataObject)
  {
    DataObjectInfo childDoi = new DataObjectInfo(key, currentDataObject.getAccessorPath() + "." + key);
    // check wheter a child data object wit this name already exists. This can happen when various
    // actions of same resource use different schemas 
    DataObjectInfo existingChildDoi = currentDataObject.findChildDataObject(childDoi.getName());
    if (existingChildDoi!=null)
    {
      childDoi = existingChildDoi; 
    }
    else
    {
      childDoi.setPayloadListElementName(currentDataObject.getPayloadListElementName());
      childDoi.setPayloadRowElementName(key);
      childDoi.setXmlPayload(false);
      childDoi.setParent(currentDataObject);
      dataObjectInfos.add(childDoi);
      currentDataObject.addChild(new AccessorInfo(currentDataObject, childDoi));
    }
    return childDoi;
  }
}
