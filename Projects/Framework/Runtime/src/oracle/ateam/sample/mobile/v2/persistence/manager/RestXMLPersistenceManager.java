 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.manager;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.adfmf.util.KXmlUtil;
import oracle.adfmf.util.XmlAnyDefinition;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;
import oracle.ateam.sample.mobile.util.StringUtils;
import oracle.ateam.sample.mobile.v2.persistence.db.BindParamInfo;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingOneToMany;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;


/**
 * Implementation of Persistence manager interface that provides basic CRUD operations using
 * the REST web services protocol against a remote server. The payload of the REST web service should
 * be in XML format.
 */
public class RestXMLPersistenceManager extends RestPersistenceManager
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(RestADFBCXMLPersistenceManager.class);

  public RestXMLPersistenceManager()
  {
  }

  /**
   * Return the string that should be used to indicate a null value in Rest payload.
   * Returns null by default. Returns "" in this class
   * Override this method if you need a different null value. 
   * @return
   */
  public String getRestNullValue()
  {
    return "";
  }
    
  protected String getSerializedDataObject(Entity entity, String collectionElementName, String rowElementName, boolean deleteRow)
  {
    return getSerializedDataObject(entity, collectionElementName, rowElementName,null,deleteRow);
  } 

  protected String getSerializedDataObject(Entity entity, String collectionElementName, String rowElementName, List<String> attributesToExclude, boolean deleteRow)
  {
    StringBuffer xml = new StringBuffer();
    xml.append(getStartTag(collectionElementName));
    if (rowElementName!=null)
    {
      xml.append(getStartTag(rowElementName,deleteRow));      
    }
    Map<String,Object>  keyValuePairs = getPayloadKeyValuePairs(entity,attributesToExclude);
    Iterator<String> keys = keyValuePairs.keySet().iterator();
    while (keys.hasNext())
    {
      String attrName = keys.next();
      Object value = keyValuePairs.get(attrName);
      if (value!=null)
      {
        xml.append(getStartTag(attrName));
        xml.append(value);
        xml.append(getEndTag(attrName));        
      }
    }
    if (rowElementName!=null)
    {
      xml.append(getEndTag(rowElementName));
    }
    xml.append(getEndTag(collectionElementName));
    return xml.toString();
  }

  protected List<Entity> handleReadResponse(String xmlResponse, Class entityClass, String collectionElementName,
                                    String rowElementName, List parentBindParamInfos, boolean deleteAllRows)
  {
    return handleResponse(xmlResponse, entityClass,collectionElementName,
                                    rowElementName,parentBindParamInfos, null, deleteAllRows);
  }

  protected List<Entity> handleResponse(String xmlResponse, Class entityClass, String collectionElementName,
                                    String rowElementName, List parentBindParamInfos, Entity currentEntity, boolean deleteAllRows)  
  {
    List<Entity> entities = new ArrayList<Entity>();
    if (deleteAllRows)
    {
      getLocalPersistenceManager().deleteAllRows(entityClass);
    }

    InputStream is = new ByteArrayInputStream(xmlResponse.getBytes());
    XmlAnyDefinition collectionNode = null;
    try
    {
      String topNodeName = getTopNodeName(xmlResponse);
      XmlAnyDefinition topNode = (XmlAnyDefinition) KXmlUtil.loadFromXml(is, XmlAnyDefinition.class,topNodeName);
      collectionNode = collectionElementName.equals(topNodeName) ? topNode : findCollectionElement(topNode, collectionElementName);     
      if (collectionNode==null)
      {
        // no rows found
        return entities;
      }
    }
    catch (Exception e)
    {
      // TODO: Add catch code
      MessageUtils.handleError("Expected collection element name "+collectionElementName+ " not found in payload.");
      return entities;
    }
    List<XmlAnyDefinition> rows = collectionNode.getChildDefinitions(rowElementName);
    if (rows!=null)
    {
      findAndProcessPayloadElements(rows,entityClass,parentBindParamInfos, entities, currentEntity);      
    }
    return entities;
  }
  
  protected String getTopNodeName(String xml)
  {
    String tag = null;
    if (xml.trim().startsWith("<?"))
    {
      int pos = xml.indexOf("?>");
      xml = xml.substring(pos+2);
    }  
    int startTag = xml.indexOf("<");
    int endTag = xml.indexOf(">");
    if (startTag>-1 && endTag>startTag)
    {
     tag = xml.substring(startTag+1,endTag);      
    }
    return tag;    
  }
  
  protected XmlAnyDefinition findCollectionElement(XmlAnyDefinition node, String collectionElementName)
  {

    List<String> elementNames = StringUtils.stringToList(collectionElementName, ".");
    XmlAnyDefinition currentNode = node;
    for (String elementName : elementNames)
    {
      List<XmlAnyDefinition> kids = node.getChildren();
      boolean found = false;
      for (XmlAnyDefinition kid : kids)
      {
        if (elementName.equals(kid.getElementName()))
        {
          currentNode = kid;
          found = true;
          break;
        }
      } 
      if (!found)
      {
        currentNode = null;
        break;
      }
    }
    return currentNode;

//    XmlAnyDefinition element = null;
//    List kids = node.getChildren();
//    for (int i = 0; i < kids.size(); i++)
//    {
//      XmlAnyDefinition kid = (XmlAnyDefinition) kids.get(i);
//      if (collectionElementName.equals(kid.getElementName()))
//      {
//        element = kid;
//        break;
//      }
//      else
//      {
//        // recursive call
//        element = findCollectionElement(kid, collectionElementName);
//        if (element!=null)
//        {
//          break;
//        }
//      }
//    }
//    return element;
  }
  
  protected void findAndProcessPayloadElements(List<XmlAnyDefinition> rows, Class entityClass, List parentBindParamInfos, List<Entity> entities, Entity currentEntity)
  {
    for (XmlAnyDefinition row : rows)
    {
      Entity entity = processPayloadElement(row, entityClass, parentBindParamInfos, currentEntity);
      if (entity!=null)
      {
        entities.add(entity);
      }
    }
  }

  protected Entity processPayloadElement(XmlAnyDefinition row,Class entityClass, List<BindParamInfo> parentBindParamInfos, Entity currentEntity)
  {
    List<BindParamInfo> bindParamInfos = new ArrayList<BindParamInfo>();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);    
    List<AttributeMapping> attrMappings = descriptor.getAttributeMappings();
    // map contains mapping as key, and a list of child instances as value
    Map<AttributeMappingOneToMany,List<XmlAnyDefinition>> oneToManyMappings = new HashMap<AttributeMappingOneToMany,List<XmlAnyDefinition>>();
    for (AttributeMapping mapping : attrMappings)
    {
      String elemName = mapping.getAttributeNameInPayload();
      if (elemName==null)
      {
        continue;
      }
      XmlAnyDefinition attrElem = row.getChildDefinition(elemName);
      if (attrElem!=null)
      {
        if (mapping.isOneToManyMapping())
        {
          AttributeMappingOneToMany mapping1m = (AttributeMappingOneToMany) mapping; 
          if (mapping1m.getAccessorMethod()!=null)
          {
            // skip this mapping as it needs to fire another rest call, we want lazy loading
            // when the child list is really needed in UI
            continue;
          }
          if (attrElem.getChildren()!=null)
          {
            oneToManyMappings.put(mapping1m,attrElem.getChildren());
          }
          continue;
        }
        Object rawValue = attrElem.getText();
    //          checkRequired(mapping, rawValue);
        BindParamInfo bpInfo = createBindParamInfoFromPayloadAttribute(entityClass, mapping, rawValue);
        if (bpInfo!=null)
        {
          bindParamInfos.add(bpInfo);        
        }
      }
    }
    // loop over parent-populated attributes without payload element and populate with corresponding parent attribute
    // if available in parent bindParamInfos
    addParentPopulatedBindParamInfos(entityClass, parentBindParamInfos, bindParamInfos, descriptor);

    if (descriptor.isPersisted() && !isAllPrimaryKeyBindParamInfosPopulated(descriptor,bindParamInfos))
    {
      // we cannot insert a row in SQLite when a PK value is null, so skip this row
      return null;
    }

    // get the primary key, and check the cache for existing entity instance with this key
    // if it exists, update this instance which is then always the same as currentEntity instance
    // otherwise, when currentEntity is not null, this means the PK has changed.
    Entity entity = createOrUpdateEntityInstance(entityClass, bindParamInfos, currentEntity);

    if (descriptor.isPersisted())
    {
      DBPersistenceManager dbpm = getLocalPersistenceManager();
      dbpm.mergeRow(bindParamInfos, true);          
    }

    // loop over one-to-many mappings to do recursive call to process child entities
    // And pass in the parent bindParamInfos, because the child payload might not contain the attribute
    // referencing the parent as it is already sent in hierarchical format in the payload 
    Iterator mappings = oneToManyMappings.keySet().iterator();
    while (mappings.hasNext())
    {
      AttributeMappingOneToMany mapping = (AttributeMappingOneToMany) mappings.next(); 
      Class refClass = mapping.getReferenceClassMappingDescriptor().getClazz();
      List<XmlAnyDefinition> children = oneToManyMappings.get(mapping);
      List<Entity> childEntities = new ArrayList<Entity>();
      if (children.size()>0)
      {
        List<Entity> currentChildEntities = null;
        if (currentEntity!=null)
        {
          currentChildEntities = (List<Entity>) currentEntity.getAttributeValue(mapping.getAttributeName());
          if (currentChildEntities.size()!=children.size())
          {
            // this should never happen, because current entity child list is send as payload for write action
            // and the number of rows returned by ws call should be the same, if it is not, we can no longer match
            // entities by index and we dont pass in currentChildEntity
            currentChildEntities = null;
          }
        }
        for (int i = 0; i < children.size(); i++)
        {
          XmlAnyDefinition childRow = children.get(i);
            // recursive call to populate DB with child entity row. Note that
            // multiple child rows are NOT wrapped in own GenericType, instead each
            // child instance is just an additional attribute of type GenericType
            Entity currentChildEntity = currentChildEntities!=null ? currentChildEntities.get(i) : null;
            Entity childEntity = processPayloadElement(childRow, refClass, bindParamInfos,currentChildEntity);
            childEntities.add(childEntity);
        }
        if (childEntities.size()>0)
        {
          entity.setAttributeValue(mapping.getAttributeName(), childEntities);
        }        
      }
    }  
    return entity;
  }

  protected String getStartTag(String elemName)
  {
    return "<" + elemName + ">";
  }

  protected String getStartTag(String elemName, boolean addBc4jRemoveAttr)
  {
    return getStartTag(elemName);
  }

  protected String getEndTag(String elemName)
  {
    return "</" + elemName + ">";
  }

}
