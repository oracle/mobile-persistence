 /*******************************************************************************
  Copyright © 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.adfmf.util.XmlAnyDefinition;

 /**
  * Mapping class that returns information for entity attributes that are mapped to a child collection.
  * 
  * The information is read from the persistenceMapping xml file as configured in
  * mobile-persistence-config.properties 
  */
public class AttributeMappingOneToMany
  extends AttributeMapping
{
  public AttributeMappingOneToMany()
  {
    super();
  }

  public AttributeMappingOneToMany(XmlAnyDefinition xmlAnyDefinition)
  {
    super(xmlAnyDefinition);
  }

  public AttributeMappingOneToMany(XmlAnyDefinition xmlAnyDefinition, String[] strings, String[] strings1)
  {
    super(xmlAnyDefinition, strings, strings1);
  }

  public boolean isOneToManyMapping()
  {
    return true;
  }

  /**
   * Returns null for this mapping
   * @return
   */
  public String getColumnName()
  {
    return null;
  }
  

  
    
  public boolean isSendAsArrayIfOnlyOneEntry()
  {
    XmlAnyDefinition sendArray = getChildDefinition("send-as-array-if-only-one-entry");
    boolean value = true;
    if (sendArray!=null)
    {
      value = "true".equalsIgnoreCase(sendArray.getText());
    }
    return value;                                  
  }

  public ClassMappingDescriptor getReferenceClassMappingDescriptor()
  {
    String refClassName = getChildDefinition("reference-class").getText();
    return ObjectPersistenceMapping.getInstance().findClassMappingDescriptor(refClassName);                                          
  }

  public String getAccessorMethod()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition("accessor-method");
    return anyDefinition!=null? anyDefinition.getText() : null;                                
  }

  /**
   * Returns a map with column mappings which are needed to construct the WHERE clause to get
   * the list of detail rows. The key in the map is the column  name in the referenced table,
   * the value is the matching column name in the table that maps to the entity of this mapping.
   * @return
   */
  public Map getColumnMappings()
  {
    List fieldReferences = getChildDefinition("target-foreign-key").getChildDefinitions("column-reference");
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
}
