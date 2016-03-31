/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.adfmf.util.XmlAnyDefinition;

 /**
  * Mapping class that returns information for entity attributes that are mapped one-to-one to another entity.
  * 
  * The information is read from the persistenceMapping xml file as configured in
  * mobile-persistence-config.properties 
  * 
  * @deprecated Use the class with same name in oracle.ateam.sample.mobile.v2.persistence.* instead
  */
public class AttributeMappingOneToOne
  extends AttributeMapping
{
  public AttributeMappingOneToOne()
  {
    super();
  }

  public AttributeMappingOneToOne(XmlAnyDefinition xmlAnyDefinition)
  {
    super(xmlAnyDefinition);
  }

  public AttributeMappingOneToOne(XmlAnyDefinition xmlAnyDefinition, String[] strings, String[] strings1)
  {
    super(xmlAnyDefinition, strings, strings1);
  }
  
  public boolean isOneToOneMapping()
  {
    return true;
  }

  public String getAccessorMethod()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition("accessor-method");
    return anyDefinition!=null? anyDefinition.getText() : null;                                
  }
  
  public ClassMappingDescriptor getReferenceClassMappingDescriptor()
  {
    String refClassName = getChildDefinition("reference-class").getText();
    return ObjectPersistenceMapping.getInstance().findClassMappingDescriptor(refClassName);                                          
  }

  /**
   * Returns a map with column mappings which are needed to construct the WHERE clause to get
   * the parent row. The key in the map is the column  name in the child table,
   * the value is the matching column name in the parent table that maps to the reference entity of this mapping.
   * @return
   */
  public Map getColumnMappings()
  {
    List fieldReferences = getChildDefinition("foreign-key").getChildDefinitions("column-reference");
    Map columnMappings = new HashMap();
    for (int i = 0; i < fieldReferences.size(); i++)
    {
      XmlAnyDefinition ref = (XmlAnyDefinition) fieldReferences.get(i);
      String source = (String) ref.getChildDefinition("source-column").getAttributeValue("name");
      String target = (String) ref.getChildDefinition("target-column").getAttributeValue("name");
      columnMappings.put(source, target);
    }
    return columnMappings;
  }

  public String getColumnName()
  {
    return null;
  }
}
