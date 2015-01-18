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
  * Mapping class that returns information for entity attributes that are mapped one-to-one to another entity.
  * 
  * The information is read from the persistenceMapping xml file as configured in
  * mobile-persistence-config.properties 
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
    return getAttributeStringValue("accessorMethod");                              
  }
  
  public ClassMappingDescriptor getReferenceClassMappingDescriptor()
  {
    String refClassName = getAttributeStringValue("referenceClassName");
    return ObjectPersistenceMapping.getInstance().findClassMappingDescriptor(refClassName);                                          
  }

  /**
   * Returns a map with column mappings which are needed to construct the WHERE clause to get
   * the parent row. The key in the map is the column  name in the child table,
   * the value is the matching column name in the parent table that maps to the reference entity of this mapping.
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

  public String getColumnName()
  {
    return null;
  }
}
