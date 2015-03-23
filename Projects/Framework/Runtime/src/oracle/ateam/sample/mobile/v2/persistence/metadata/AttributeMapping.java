 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.metadata;


import oracle.adfmf.util.XmlAnyDefinition;

/**
 * Superclass of all attribute mapping classes. Returns common information that all
 * attribute mappings provide.
 * The information is read from the TopLink XML mapping file as configured in
 * mobile-persistence-config.properties
 */
public abstract class AttributeMapping
  extends XmlAnyDefinition
{
  private ClassMappingDescriptor classMappingDescriptor;
  private boolean primaryKeyMapping = false;

  public AttributeMapping(XmlAnyDefinition xmlAnyDefinition, String[] strings, String[] strings1)
  {
    super(xmlAnyDefinition, strings, strings1);
  }

  public AttributeMapping(XmlAnyDefinition xmlAnyDefinition)
  {
    super(xmlAnyDefinition);
  }

  public AttributeMapping()
  {
    super();
  }
  
  public String getAttributeName()
  {
    return getAttributeStringValue("attributeName");    
  }
  
  public String getDateFormat()
  {
    String format = getAttributeStringValue("dateFormat");    
    return format!=null ? format : classMappingDescriptor.getDateTimeFormat();
  }

  /**
   *  Returns the value of element payloadAttributeName. 
   * @return
   */
  public String getAttributeNameInPayload()
  {
    return getAttributeStringValue("payloadAttributeName");
  }

  public abstract String getColumnName();

  public boolean isDirectMapping()
  {
    return false;
  }
  public boolean isOneToOneMapping()
  {
    return false;
  }
  public boolean isOneToManyMapping()
  {
    return false;
  }


  public void setClassMappingDescriptor(ClassMappingDescriptor classMappingDescriptor)
  {
    this.classMappingDescriptor = classMappingDescriptor;
  }

  public ClassMappingDescriptor getClassMappingDescriptor()
  {
    return classMappingDescriptor;
  }

  public boolean isPrimaryKeyMapping()
  {
    return getAttributeBooleanValue("keyAttribute",false);
  }
  
  public boolean isPersisted()
  {
    return getAttributeBooleanValue("persisted",true);  
  }

}
