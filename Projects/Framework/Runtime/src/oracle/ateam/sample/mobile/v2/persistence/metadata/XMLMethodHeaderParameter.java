 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;
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
    return getAttributeStringValue("name");
  }

  public String getValue()
  {
    return getAttributeStringValue("value");
  }
}
