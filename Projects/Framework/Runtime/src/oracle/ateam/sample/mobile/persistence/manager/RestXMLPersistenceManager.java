/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 25-sep-2014   Steven Davelaar
 1.1           Added support for attributesToExclude in getSerializedDataObject
               Added method handleResponse to pass in currentEntity
 06-feb-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.manager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.json.JSONArray;
import oracle.adfmf.json.JSONObject;
import oracle.adfmf.util.KXmlUtil;
import oracle.adfmf.util.XmlAnyDefinition;

import oracle.ateam.sample.mobile.persistence.cache.EntityCache;
import oracle.ateam.sample.mobile.persistence.db.BindParamInfo;
import oracle.ateam.sample.mobile.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.persistence.metadata.AttributeMappingOneToMany;
import oracle.ateam.sample.mobile.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.persistence.metadata.Method;
import oracle.ateam.sample.mobile.persistence.metadata.MethodParameter;
import oracle.ateam.sample.mobile.persistence.metadata.ObjectPersistenceMapping;
import oracle.ateam.sample.mobile.persistence.model.Entity;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;
import oracle.ateam.sample.mobile.util.StringUtils;


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

  protected String getSerializedDataObject(Entity entity, String collectionElementName, String rowElementName, List attributesToExclude, boolean deleteRow)
  {
    StringBuffer xml = new StringBuffer();
    xml.append(getStartTag(collectionElementName));
    if (rowElementName!=null)
    {
      xml.append(getStartTag(rowElementName,deleteRow));      
    }
    Map keyValuePairs = getPayloadKeyValuePairs(entity,attributesToExclude);
    Iterator keys = keyValuePairs.keySet().iterator();
    while (keys.hasNext())
    {
      String attrName = (String) keys.next();
      String value = (String) keyValuePairs.get(attrName);
      xml.append(getStartTag(attrName));
      xml.append(value);
      xml.append(getEndTag(attrName));
    }
    if (rowElementName!=null)
    {
      xml.append(getEndTag(rowElementName));
    }
    xml.append(getEndTag(collectionElementName));
    return xml.toString();
  }

  protected List handleReadResponse(String xmlResponse, Class entityClass, String collectionElementName,
                                    String rowElementName, List parentBindParamInfos, boolean deleteAllRows)
  {
    return handleResponse(xmlResponse, entityClass,collectionElementName,
                                    rowElementName,parentBindParamInfos, null, deleteAllRows);
  }

  protected List handleResponse(String xmlResponse, Class entityClass, String collectionElementName,
                                    String rowElementName, List parentBindParamInfos, Entity currentEntity, boolean deleteAllRows)  
  {
    List entities = new ArrayList();
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
    List rows = collectionNode.getChildDefinitions(rowElementName);
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

    List elementNames = StringUtils.stringToList(collectionElementName, ".");
    XmlAnyDefinition currentNode = node;
    for (int i = 0; i < elementNames.size(); i++)
    {
      String elementName = (String) elementNames.get(i); 
      List kids = node.getChildren();
      boolean found = false;
      for (int j = 0; j < kids.size(); j++)
      {
        XmlAnyDefinition kid = (XmlAnyDefinition) kids.get(j);
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
  
  protected void findAndProcessPayloadElements(List rows, Class entityClass, List parentBindParamInfos, List entities, Entity currentEntity)
  {
    for (int i = 0; i < rows.size(); i++)
    {
      XmlAnyDefinition row = (XmlAnyDefinition) rows.get(i);
      Entity entity = processPayloadElement(row, entityClass, parentBindParamInfos, currentEntity);
      if (entity!=null)
      {
        entities.add(entity);
      }
    }
  }

  protected Entity processPayloadElement(XmlAnyDefinition row,Class entityClass, List parentBindParamInfos, Entity currentEntity)
  {
    List bindParamInfos = new ArrayList();
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);    
    List attrMappings = descriptor.getAttributeMappings();
    // map contains mapping as key, and a list of child instances as value
    Map oneToManyMappings = new HashMap();
    for (int j = 0; j < attrMappings.size(); j++)
    {
      AttributeMapping mapping = (AttributeMapping) attrMappings.get(j);
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
      List children = (List) oneToManyMappings.get(mapping);
      List childEntities = new ArrayList();
      if (children.size()>0)
      {
        List currentChildEntities = null;
        if (currentEntity!=null)
        {
          currentChildEntities = (List) currentEntity.getAttributeValue(mapping.getAttributeName());
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
          XmlAnyDefinition childRow = (XmlAnyDefinition) children.get(i);
            // recursive call to populate DB with child entity row. Note that
            // multiple child rows are NOT wrapped in own GenericType, instead each
            // child instance is just an additional attribute of type GenericType
            Entity currentChildEntity = (Entity) (currentChildEntities!=null ? currentChildEntities.get(i) : null);
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
