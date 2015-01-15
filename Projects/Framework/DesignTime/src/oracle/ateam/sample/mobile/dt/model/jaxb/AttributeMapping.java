
package oracle.ateam.sample.mobile.dt.model.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for attributeMapping complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="attributeMapping">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="attributeName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="payloadAttributeName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "attributeMapping")
@XmlSeeAlso(
  {
    DirectMapping.class, OneToOneMapping.class, OneToManyMapping.class
  })
public class AttributeMapping
{

  @XmlAttribute(name = "attributeName", required = true)
  protected String attributeName;
  @XmlAttribute(name = "payloadAttributeName")
  protected String payloadAttributeName;

  /**
   * Gets the value of the attributeName property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getAttributeName()
  {
    return attributeName;
  }

  /**
   * Sets the value of the attributeName property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setAttributeName(String value)
  {
    this.attributeName = value;
  }

  /**
   * Gets the value of the payloadAttributeName property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getPayloadAttributeName()
  {
    return payloadAttributeName;
  }

  /**
   * Sets the value of the payloadAttributeName property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setPayloadAttributeName(String value)
  {
    this.payloadAttributeName = value;
  }

}
