/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.controller.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oracle.ateam.sample.mobile.dt.exception.ParseException;
import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;

import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLText;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class XMLExampleDataObjectParser
{
  private List<DataObjectInfo> dataObjectInfos = new ArrayList<DataObjectInfo>();
  private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private DocumentBuilder builder = null;
  
  public XMLExampleDataObjectParser()
  {
    try
    {
      factory.setNamespaceAware(true);
      builder = factory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e)
    {
    }    
  }

  public void parse(DCMethod method, String response, DataObjectInfo dataObjectInfo, List<DataObjectInfo> dataObjectInfos)
  {
    this.dataObjectInfos = dataObjectInfos;
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
      throw new ParseException("Error parsing XML payload: " + e.getLocalizedMessage());
    }
    catch (IOException e)
    {
      e.printStackTrace();
      throw new ParseException("Error parsing XML payload: " + e.getLocalizedMessage());
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
