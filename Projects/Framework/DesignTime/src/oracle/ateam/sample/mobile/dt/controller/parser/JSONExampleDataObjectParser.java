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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;

import oracle.ide.panels.TraversalException;

public class JSONExampleDataObjectParser
{
  private List<DataObjectInfo> dataObjectInfos = new ArrayList<DataObjectInfo>();
  private GsonBuilder gb = new GsonBuilder();
  private Gson gson = gb.create();
  private boolean flattenNestedObjects = false;

  public void parse(String response, DataObjectInfo root, boolean flattenNestedObjects, List<DataObjectInfo> dataObjectInfos)
    throws TraversalException
  {
    this.flattenNestedObjects = flattenNestedObjects;
    this.dataObjectInfos = dataObjectInfos;
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
          // check wheter a child data object wit this name already exists. This can happen when processing
          // multiple actions for a resource in RAMLParser
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
          // process map,also for exiostign child, might result in additional attrs
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
        // Only add attribute if it does not exist yet. When parsing RAML we add attributes for each example we find
        // for get/PUT/POST so most likely the same attrs are included in multiple examples
        AttributeInfo attributeInfo = new AttributeInfo(payloadAttrPrefix + key, clazz.getName());
        if (currentDataObject.getAttributeDef(attributeInfo.getAttrName())==null)
        {
          currentDataObject.addAttribute(attributeInfo);          
        }
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
        if ("root".equals(key) || currentDataObject.getPayloadListElementName()==null)
        {
          // we can add attributes straight to existing root data object
          // if currentDataObject has null value for getPayloadListElementName it is coming from RAML example
          // and we can add the attributes straight to this data objwect
          if (currentDataObject.getPayloadListElementName()==null)
          {
            currentDataObject.setPayloadListElementName(key);
          }
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

}
