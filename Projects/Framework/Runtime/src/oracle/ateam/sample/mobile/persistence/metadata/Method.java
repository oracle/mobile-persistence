/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 25-sep-2014   Steven Davelaar
 1.0           Added support for attributesToExclude attribute
 02-feb-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.metadata;

import java.util.ArrayList;
import java.util.List;

import oracle.adfmf.util.XmlAnyDefinition;

import oracle.ateam.sample.mobile.util.StringUtils;

/**
 * Class that represents a method instance as defined in persistenceMapping.xml file.
 */
public class Method extends XmlAnyDefinition
{
  
  private List params = null;
  private List headerParams = null;
  
  public Method(XmlAnyDefinition xmlAnyDefinition)
  {
    super(xmlAnyDefinition);
  }
  
  public String getName()
  {
    return (String) getAttributeValue("name");
  }

  public String getDataControlName()
  {
    return (String) getAttributeValue("dataControlName");
  }

  public String getConnectionName()
  {
    return (String) getAttributeValue("connectionName");
  }

  public String getRequestType()
  {
    return (String) getAttributeValue("requestType");
  }
  
  public String getRequestUri()
  {
    return (String) getAttributeValue("uri");
  }

  public String getPayloadElementName()
  {
    return (String) getAttributeValue("payloadElementName");
  }

  public String getPayloadRowElementName()
  {
    return (String) getAttributeValue("payloadRowElementName");
  }

  public boolean isSecured()
  {
    return "true".equals(getAttributeValue("secured"));
  }

  public boolean isSendDataObjectAsPayload()
  {
    return "true".equals(getAttributeValue("sendDataObjectAsPayload"));
  }
      
  /**
   * Returns a list of attribute names that should not be included in the serialized data
   * object. If the "attributesToExclude" attribute is not set on the method, an empty list
   * is returned.
   * @return
   */
  public List getAttributesToIgnore()
  {
    String attrNames = (String) getAttributeValue("attributesToExclude");
    if (attrNames==null || attrNames.trim().equals(""))
    {
      return new ArrayList();
    }
    List attrs = StringUtils.stringToList(attrNames, ",");
    return attrs;
  }

  public String getOAuthConfigName()
  {
    return (String) getAttributeValue("oauthConfig");
  }

  public List getParams()
  {
    if (params==null)
    {
      params = new ArrayList();
      List paramElems = getChildDefinitions("parameter");
      for (int i = 0; i < paramElems.size(); i++)
      {
        MethodParameter param = new XMLMethodParameter((XmlAnyDefinition) paramElems.get(i)); 
        params.add(param);
      }
    }
    return params;
  }

  public List getHeaderParams()
  {
    if (headerParams==null)
    {
      headerParams = new ArrayList();
      List paramElems = getChildDefinitions("header-parameter");
      for (int i = 0; i < paramElems.size(); i++)
      {
        MethodHeaderParameter param = new XMLMethodHeaderParameter((XmlAnyDefinition) paramElems.get(i)); 
        headerParams.add(param);
      }
    }
    return headerParams;
  }
  
}
