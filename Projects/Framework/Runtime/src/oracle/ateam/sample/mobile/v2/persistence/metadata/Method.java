 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;
import java.util.ArrayList;
import java.util.List;

import oracle.adfmf.util.XmlAnyDefinition;

import oracle.ateam.sample.mobile.util.StringUtils;

/**
 * Class that represents a method instance as defined in persistenceMapping.xml file.
 */
public class Method extends XmlAnyDefinition
{
  
  private List<MethodParameter> params = null;
  private List<MethodHeaderParameter> headerParams = null;
  
  public Method(XmlAnyDefinition xmlAnyDefinition)
  {
    super(xmlAnyDefinition);
  }
  
  public String getName()
  {
    return getAttributeStringValue("name");
  }

  public String getDataControlName()
  {
    return getAttributeStringValue("dataControlName");
  }

  public String getConnectionName()
  {
    return getAttributeStringValue("connectionName");
  }

  public String getRequestType()
  {
    return getAttributeStringValue("requestType");
  }
  
  public String getRequestUri()
  {
    return getAttributeStringValue("uri");
  }

  public String getPayloadElementName()
  {
    return getAttributeStringValue("payloadElementName");
  }

  public String getPayloadRowElementName()
  {
    return getAttributeStringValue("payloadRowElementName");
  }

  public boolean isSecured()
  {
    return getAttributeBooleanValue("secured",false);
  }

  public boolean isSendDataObjectAsPayload()
  {
    return getAttributeBooleanValue("sendDataObjectAsPayload",false);
  }
      
  /**
   * Returns a list of attribute names that should not be included in the serialized data
   * object. If the "attributesToExclude" attribute is not set on the method, an empty list
   * is returned.
   * @return
   */
  public List<String> getAttributesToIgnore()
  {
    String attrNames = (String) getAttributeValue("attributesToExclude");
    if (attrNames==null || attrNames.trim().equals(""))
    {
      return new ArrayList<String>();
    }
    List<String> attrs = StringUtils.stringToList(attrNames, ",");
    return attrs;
  }

  public String getOAuthConfigName()
  {
    return getAttributeStringValue("oauthConfig");
  }

  public List<MethodParameter> getParams()
  {
    if (params==null)
    {
      params = new ArrayList<MethodParameter>();
      List<XmlAnyDefinition> paramElems = getChildDefinitions("parameter");
      for (XmlAnyDefinition paramElem : paramElems)
      {
        MethodParameter param = new XMLMethodParameter(paramElem); 
        params.add(param);
      }
    }
    return params;
  }

  public List<MethodHeaderParameter> getHeaderParams()
  {
    if (headerParams==null)
    {
      headerParams = new ArrayList<MethodHeaderParameter>();
      List<XmlAnyDefinition> paramElems = getChildDefinitions("headerParameter");
      for (XmlAnyDefinition paramElem : paramElems)
      {
        MethodHeaderParameter param = new XMLMethodHeaderParameter(paramElem); 
        headerParams.add(param);
      }
    }
    return headerParams;
  }
  
}
