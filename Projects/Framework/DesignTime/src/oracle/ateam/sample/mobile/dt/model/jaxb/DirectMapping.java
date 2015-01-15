
package oracle.ateam.sample.mobile.dt.model.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.oracle.com/ateam/mobile/persistenceMapping}attributeMapping">
 *       &lt;attribute name="columnName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="columnDataType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="required" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="persisted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="parentClass" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="parentAttributeName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="javaType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="keyAttribute" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "directMapping")
public class DirectMapping
  extends AttributeMapping
{

  @XmlAttribute(name = "columnName", required = true)
  protected String columnName;
  @XmlAttribute(name = "columnDataType", required = true)
  protected String columnDataType;
  @XmlAttribute(name = "required")
  protected Boolean required;
  @XmlAttribute(name = "persisted")
  protected Boolean persisted;
  @XmlAttribute(name = "parentClass")
  protected String parentClass;
  @XmlAttribute(name = "parentAttributeName")
  protected String parentAttributeName;
  @XmlAttribute(name = "javaType")
  protected String javaType;
  @XmlAttribute(name = "keyAttribute")
  protected Boolean keyAttribute;

  /**
   * Gets the value of the columnName property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getColumnName()
  {
    return columnName;
  }

  /**
   * Sets the value of the columnName property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setColumnName(String value)
  {
    this.columnName = value;
  }

  /**
   * Gets the value of the columnDataType property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getColumnDataType()
  {
    return columnDataType;
  }

  /**
   * Sets the value of the columnDataType property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setColumnDataType(String value)
  {
    this.columnDataType = value;
  }

  /**
   * Gets the value of the required property.
   *
   * @return
   *     possible object is
   *     {@link Boolean }
   *
   */
  public boolean isRequired()
  {
    if (required == null)
    {
      return false;
    }
    else
    {
      return required;
    }
  }

  /**
   * Sets the value of the required property.
   *
   * @param value
   *     allowed object is
   *     {@link Boolean }
   *
   */
  public void setRequired(Boolean value)
  {
    this.required = value;
  }

  /**
   * Gets the value of the persisted property.
   *
   * @return
   *     possible object is
   *     {@link Boolean }
   *
   */
  public boolean isPersisted()
  {
    if (persisted == null)
    {
      return true;
    }
    else
    {
      return persisted;
    }
  }

  /**
   * Sets the value of the persisted property.
   *
   * @param value
   *     allowed object is
   *     {@link Boolean }
   *
   */
  public void setPersisted(Boolean value)
  {
    this.persisted = value;
  }

  /**
   * Gets the value of the parentClass property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getParentClass()
  {
    return parentClass;
  }

  /**
   * Sets the value of the parentClass property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setParentClass(String value)
  {
    this.parentClass = value;
  }

  /**
   * Gets the value of the parentAttributeName property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getParentAttributeName()
  {
    return parentAttributeName;
  }

  /**
   * Sets the value of the parentAttributeName property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setParentAttributeName(String value)
  {
    this.parentAttributeName = value;
  }

  /**
   * Gets the value of the javaType property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getJavaType()
  {
    return javaType;
  }

  /**
   * Sets the value of the javaType property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setJavaType(String value)
  {
    this.javaType = value;
  }

  /**
   * Gets the value of the keyAttribute property.
   *
   * @return
   *     possible object is
   *     {@link Boolean }
   *
   */
  public boolean isKeyAttribute()
  {
    if (keyAttribute == null)
    {
      return false;
    }
    else
    {
      return keyAttribute;
    }
  }

  /**
   * Sets the value of the keyAttribute property.
   *
   * @param value
   *     allowed object is
   *     {@link Boolean }
   *
   */
  public void setKeyAttribute(Boolean value)
  {
    this.keyAttribute = value;
  }

}
