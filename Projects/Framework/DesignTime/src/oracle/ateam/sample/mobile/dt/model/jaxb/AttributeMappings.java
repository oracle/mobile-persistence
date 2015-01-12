
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
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}directMapping" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}oneToManyMapping" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}oneToOneMapping" maxOccurs="unbounded" minOccurs="0"/>
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
    "directMapping", "oneToManyMapping", "oneToOneMapping"
  })
@XmlRootElement(name = "attributeMappings")
public class AttributeMappings
{

  protected List<DirectMapping> directMapping;
  protected List<OneToManyMapping> oneToManyMapping;
  protected List<OneToOneMapping> oneToOneMapping;

  /**
   * Gets the value of the directMapping property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the directMapping property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getDirectMapping().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link DirectMapping }
   *
   *
   */
  public List<DirectMapping> getDirectMapping()
  {
    if (directMapping == null)
    {
      directMapping = new ArrayList<DirectMapping>();
    }
    return this.directMapping;
  }

  /**
   * Gets the value of the oneToManyMapping property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the oneToManyMapping property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getOneToManyMapping().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link OneToManyMapping }
   *
   *
   */
  public List<OneToManyMapping> getOneToManyMapping()
  {
    if (oneToManyMapping == null)
    {
      oneToManyMapping = new ArrayList<OneToManyMapping>();
    }
    return this.oneToManyMapping;
  }

  /**
   * Gets the value of the oneToOneMapping property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the oneToOneMapping property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getOneToOneMapping().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link OneToOneMapping }
   *
   *
   */
  public List<OneToOneMapping> getOneToOneMapping()
  {
    if (oneToOneMapping == null)
    {
      oneToOneMapping = new ArrayList<OneToOneMapping>();
    }
    return this.oneToOneMapping;
  }

}
