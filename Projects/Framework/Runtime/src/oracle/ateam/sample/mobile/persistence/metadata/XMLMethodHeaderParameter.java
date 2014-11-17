/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 03-nov-2014   Steven Davelaar
 1.0           initial creation (renamed from MethodHeaderParameter)
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.metadata;

import oracle.adfmf.util.XmlAnyDefinition;

 /**
  * Implementation of SOAP method header parameter or REST resource header parameter that reads values from 
  * persistenceMapping.xml
  */
public class XMLMethodHeaderParameter extends XmlAnyDefinition implements MethodHeaderParameter 
{
  public XMLMethodHeaderParameter(XmlAnyDefinition xmlAnyDefinition)
  {
    super(xmlAnyDefinition);
  }

  public String getName()
  {
    return (String) getAttributeValue("name");
  }

  public String getValue()
  {
    return (String) getAttributeValue("value");
  }
}
