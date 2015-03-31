
package oracle.ateam.sample.mobile.dt.model.jaxb;

import java.util.ArrayList;
import java.util.List;

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
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}classMappingDescriptor" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="showWebServiceTimings" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="logWebServiceCalls" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
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
    "classMappingDescriptor"
  })
@XmlRootElement(name = "mobileObjectPersistence")
public class MobileObjectPersistence
{

  protected List<ClassMappingDescriptor> classMappingDescriptor;
  @XmlAttribute(name = "showWebServiceTimings")
  protected Boolean showWebServiceTimings;
  @XmlAttribute(name = "logWebServiceCalls")
  protected Boolean logWebServiceCalls;

  /**
   * Gets the value of the classMappingDescriptor property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the classMappingDescriptor property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getClassMappingDescriptor().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link ClassMappingDescriptor }
   *
   *
   */
  public List<ClassMappingDescriptor> getClassMappingDescriptor()
  {
    if (classMappingDescriptor == null)
    {
      classMappingDescriptor = new ArrayList<ClassMappingDescriptor>();
    }
    return this.classMappingDescriptor;
  }

  /**
   * Gets the value of the showWebServiceTimings property.
   *
   * @return
   *     possible object is
   *     {@link Boolean }
   *
   */
  public boolean isShowWebServiceTimings()
  {
    if (showWebServiceTimings == null)
    {
      return false;
    }
    else
    {
      return showWebServiceTimings;
    }
  }

  /**
   * Sets the value of the showWebServiceTimings property.
   *
   * @param value
   *     allowed object is
   *     {@link Boolean }
   *
   */
  public void setShowWebServiceTimings(Boolean value)
  {
    this.showWebServiceTimings = value;
  }

  /**
   * Gets the value of the logWebServiceCalls property.
   *
   * @return
   *     possible object is
   *     {@link Boolean }
   *
   */
  public boolean isLogWebServiceCalls()
  {
    if (logWebServiceCalls == null)
    {
      return false;
    }
    else
    {
      return logWebServiceCalls;
    }
  }

  /**
   * Sets the value of the logWebServiceCalls property.
   *
   * @param value
   *     allowed object is
   *     {@link Boolean }
   *
   */
  public void setLogWebServiceCalls(Boolean value)
  {
    this.logWebServiceCalls = value;
  }

}
