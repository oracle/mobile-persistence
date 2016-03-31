/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.metadata;

import java.math.BigDecimal;

import java.sql.Clob;
import java.sql.Date;
import java.sql.Types;

import java.util.HashMap;

import java.util.Map;

import oracle.adfmf.util.XmlAnyDefinition;

/**
 * Superclass of all attribute mapping classes. Returns common information that all
 * attribute mappings provide.
 * The information is read from the TopLink XML mapping file as configured in
 * mobile-persistence-config.properties
 * 
 * @deprecated Use the class with same name in oracle.ateam.sample.mobile.v2.persistence.* instead
 */
public abstract class AttributeMapping
  extends XmlAnyDefinition
{
  private ClassMappingDescriptor classMappingDescriptor;
  private boolean primaryKeyMapping = false;;

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
    return getChildDefinition("attribute-name").getText();    
  }

  /**
   *  Returns the value of element <payload-attribute-name>. 
   * @return
   */
  public String getAttributeNameInPayload()
  {
    if (getChildDefinition("payload-attribute-name")!=null)
    {
      String value = getChildDefinition("payload-attribute-name").getText();    
      return value;
    }
    return null;
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

  public void setPrimaryKeyMapping(boolean primaryKeyMapping)
  {
    this.primaryKeyMapping = primaryKeyMapping;
  }

  public boolean isPrimaryKeyMapping()
  {
    return primaryKeyMapping;
  }
  
  public boolean isPersisted()
  {
    XmlAnyDefinition anyDefinition = getChildDefinition("persisted");
    return anyDefinition!=null ? !("false".equalsIgnoreCase(anyDefinition.getText())) : true;    
  }

}
