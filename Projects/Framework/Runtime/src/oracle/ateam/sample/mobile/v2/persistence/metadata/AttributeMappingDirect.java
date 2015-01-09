 /*******************************************************************************
  Copyright © 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;
import oracle.adfmf.util.XmlAnyDefinition;

 /**
  * Mapping class that returns information for entity attributes that are directly mapped to a table column
  * 
  * The information is read from the persistenceMapping xml file as configured in
  * mobile-persistence-config.properties 
  */
public class AttributeMappingDirect
  extends AttributeMapping
{
  public AttributeMappingDirect()
  {
    super();
  }

  public AttributeMappingDirect(XmlAnyDefinition xmlAnyDefinition)
  {
    super(xmlAnyDefinition);
  }

  public AttributeMappingDirect(XmlAnyDefinition xmlAnyDefinition, String[] strings, String[] strings1)
  {
    super(xmlAnyDefinition, strings, strings1);
  }
  
  public String getColumnName()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition("column");
    return (String) (anyDefinition != null? anyDefinition.getAttributeValue("name"): null);    
  }

  public String getParentAttribute()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition("parent-attribute-name");
    return anyDefinition!=null ? anyDefinition.getText() : null;    
  }

  public boolean isDirectMapping()
  {
    return true;
  }

  public boolean isRequired()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition("required");
    return anyDefinition!=null ? "true".equalsIgnoreCase(anyDefinition.getText()) : false;    
  }


}
