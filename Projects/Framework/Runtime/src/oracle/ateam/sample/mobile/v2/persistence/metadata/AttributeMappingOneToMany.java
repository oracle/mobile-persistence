 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
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
    return getAttributeBooleanValue("sendAsArrayIfOnlyOneEntry", true);                                  
  }

  public ClassMappingDescriptor getReferenceClassMappingDescriptor()
  {
    String refClassName = getAttributeStringValue("referenceClassName");
    return ObjectPersistenceMapping.getInstance().findClassMappingDescriptor(refClassName);                                          
  }

  public String getAccessorMethod()
  {
    return getAttributeStringValue("accessorMethod");                              
  }

  /**
   * Returns a map with column mappings which are needed to construct the WHERE clause to get
   * the list of detail rows. The key in the map is the column  name in the referenced table,
   * the value is the matching column name in the table that maps to the entity of this mapping.
   * @return
   */
  public Map<String,String> getColumnMappings()
  {
    List<XmlAnyDefinition> fieldReferences = getChildDefinitions("foreignKeyColumnReference");
    Map<String,String> columnMappings = new HashMap<String,String>();
    for (XmlAnyDefinition ref :fieldReferences)
    {
      String source = ref.getAttributeStringValue("sourceColumn");
      String target = ref.getAttributeStringValue("targetColumn");
      columnMappings.put(source, target);
    }
    return columnMappings;
  }
}
