
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
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="valueProvider" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="dataObjectAttribute" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pathParam" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="javaType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "parameter")
public class Parameter
{

  @XmlAttribute(name = "name")
  protected String name;
  @XmlAttribute(name = "value")
  protected String value;
  @XmlAttribute(name = "valueProvider")
  protected String valueProvider;
  @XmlAttribute(name = "dataObjectAttribute")
  protected String dataObjectAttribute;
  @XmlAttribute(name = "pathParam")
  protected Boolean pathParam;
  @XmlAttribute(name = "javaType")
  protected String javaType;

  /**
   * Gets the value of the name property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getName()
  {
    return name;
  }

  /**
   * Sets the value of the name property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setName(String value)
  {
    this.name = value;
  }

  /**
   * Gets the value of the value property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getValue()
  {
    return value;
  }

  /**
   * Sets the value of the value property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   * Gets the value of the valueProvider property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getValueProvider()
  {
    return valueProvider;
  }

  /**
   * Sets the value of the valueProvider property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setValueProvider(String value)
  {
    this.valueProvider = value;
  }

  /**
   * Gets the value of the dataObjectAttribute property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getDataObjectAttribute()
  {
    return dataObjectAttribute;
  }

  /**
   * Sets the value of the dataObjectAttribute property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setDataObjectAttribute(String value)
  {
    this.dataObjectAttribute = value;
  }

  /**
   * Gets the value of the pathParam property.
   *
   * @return
   *     possible object is
   *     {@link Boolean }
   *
   */
  public Boolean isPathParam()
  {
    return pathParam;
  }

  /**
   * Sets the value of the pathParam property.
   *
   * @param value
   *     allowed object is
   *     {@link Boolean }
   *
   */
  public void setPathParam(Boolean value)
  {
    this.pathParam = value;
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

}
