
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
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}crudServiceClass" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}table" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}attributeMappings" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}methods" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="className" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="persisted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="dateFormat" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="dateTimeFormat" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="orderBy" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder =
  {
    "crudServiceClass", "table", "attributeMappings", "methods"
  })
@XmlRootElement(name = "classMappingDescriptor")
public class ClassMappingDescriptor
{

  protected CrudServiceClass crudServiceClass;
  protected Table table;
  protected AttributeMappings attributeMappings;
  protected Methods methods;
  @XmlAttribute(name = "className", required = true)
  protected String className;
  @XmlAttribute(name = "persisted")
  protected Boolean persisted;
  @XmlAttribute(name = "dateFormat")
  protected String dateFormat;
  @XmlAttribute(name = "dateTimeFormat")
  protected String dateTimeFormat;
  @XmlAttribute(name = "orderBy")
  protected String orderBy;

  /**
   * Gets the value of the crudServiceClass property.
   *
   * @return
   *     possible object is
   *     {@link CrudServiceClass }
   *
   */
  public CrudServiceClass getCrudServiceClass()
  {
    return crudServiceClass;
  }

  /**
   * Sets the value of the crudServiceClass property.
   *
   * @param value
   *     allowed object is
   *     {@link CrudServiceClass }
   *
   */
  public void setCrudServiceClass(CrudServiceClass value)
  {
    this.crudServiceClass = value;
  }

  /**
   * Gets the value of the table property.
   *
   * @return
   *     possible object is
   *     {@link Table }
   *
   */
  public Table getTable()
  {
    return table;
  }

  /**
   * Sets the value of the table property.
   *
   * @param value
   *     allowed object is
   *     {@link Table }
   *
   */
  public void setTable(Table value)
  {
    this.table = value;
  }

  /**
   * Gets the value of the attributeMappings property.
   *
   * @return
   *     possible object is
   *     {@link AttributeMappings }
   *
   */
  public AttributeMappings getAttributeMappings()
  {
    return attributeMappings;
  }

  /**
   * Sets the value of the attributeMappings property.
   *
   * @param value
   *     allowed object is
   *     {@link AttributeMappings }
   *
   */
  public void setAttributeMappings(AttributeMappings value)
  {
    this.attributeMappings = value;
  }

  /**
   * Gets the value of the methods property.
   *
   * @return
   *     possible object is
   *     {@link Methods }
   *
   */
  public Methods getMethods()
  {
    return methods;
  }

  /**
   * Sets the value of the methods property.
   *
   * @param value
   *     allowed object is
   *     {@link Methods }
   *
   */
  public void setMethods(Methods value)
  {
    this.methods = value;
  }

  /**
   * Gets the value of the className property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getClassName()
  {
    return className;
  }

  /**
   * Sets the value of the className property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setClassName(String value)
  {
    this.className = value;
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
   * Gets the value of the dateFormat property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getDateFormat()
  {
    return dateFormat;
  }

  /**
   * Sets the value of the dateFormat property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setDateFormat(String value)
  {
    this.dateFormat = value;
  }

  /**
   * Gets the value of the dateTimeFormat property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getDateTimeFormat()
  {
    return dateTimeFormat;
  }

  /**
   * Sets the value of the dateTimeFormat property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setDateTimeFormat(String value)
  {
    this.dateTimeFormat = value;
  }

  /**
   * Gets the value of the orderBy property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getOrderBy()
  {
    return orderBy;
  }

  /**
   * Sets the value of the orderBy property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setOrderBy(String value)
  {
    this.orderBy = value;
  }

}
