
package oracle.ateam.sample.mobile.dt.model.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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

}
