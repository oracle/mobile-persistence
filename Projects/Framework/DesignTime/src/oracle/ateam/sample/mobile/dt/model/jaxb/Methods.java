
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
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}findAllMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}findAllInParentMethod" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}findMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}getAsParentMethod" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}getCanonicalMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}createMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}updateMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}removeMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}mergeMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}customMethod" maxOccurs="unbounded" minOccurs="0"/>
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
    "findAllMethod", "findAllInParentMethod", "findMethod", "getAsParentMethod", "getCanonicalMethod", "createMethod",
    "updateMethod", "removeMethod", "mergeMethod", "customMethod"
  })
@XmlRootElement(name = "methods")
public class Methods
{

  protected Method findAllMethod;
  protected List<Method> findAllInParentMethod;
  protected Method findMethod;
  protected List<Method> getAsParentMethod;
  protected Method getCanonicalMethod;
  protected Method createMethod;
  protected Method updateMethod;
  protected Method removeMethod;
  protected Method mergeMethod;
  protected List<Method> customMethod;

  /**
   * Gets the value of the findAllMethod property.
   *
   * @return
   *     possible object is
   *     {@link Method }
   *
   */
  public Method getFindAllMethod()
  {
    return findAllMethod;
  }

  /**
   * Sets the value of the findAllMethod property.
   *
   * @param value
   *     allowed object is
   *     {@link Method }
   *
   */
  public void setFindAllMethod(Method value)
  {
    this.findAllMethod = value;
  }

  /**
   * Gets the value of the findAllInParentMethod property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the findAllInParentMethod property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getFindAllInParentMethod().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link Method }
   *
   *
   */
  public List<Method> getFindAllInParentMethod()
  {
    if (findAllInParentMethod == null)
    {
      findAllInParentMethod = new ArrayList<Method>();
    }
    return this.findAllInParentMethod;
  }

  /**
   * Gets the value of the findMethod property.
   *
   * @return
   *     possible object is
   *     {@link Method }
   *
   */
  public Method getFindMethod()
  {
    return findMethod;
  }

  /**
   * Sets the value of the findMethod property.
   *
   * @param value
   *     allowed object is
   *     {@link Method }
   *
   */
  public void setFindMethod(Method value)
  {
    this.findMethod = value;
  }

  /**
   * Gets the value of the getAsParentMethod property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the getAsParentMethod property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getGetAsParentMethod().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link Method }
   *
   *
   */
  public List<Method> getGetAsParentMethod()
  {
    if (getAsParentMethod == null)
    {
      getAsParentMethod = new ArrayList<Method>();
    }
    return this.getAsParentMethod;
  }

  /**
   * Gets the value of the getCanonicalMethod property.
   *
   * @return
   *     possible object is
   *     {@link Method }
   *
   */
  public Method getGetCanonicalMethod()
  {
    return getCanonicalMethod;
  }

  /**
   * Sets the value of the getCanonicalMethod property.
   *
   * @param value
   *     allowed object is
   *     {@link Method }
   *
   */
  public void setGetCanonicalMethod(Method value)
  {
    this.getCanonicalMethod = value;
  }

  /**
   * Gets the value of the createMethod property.
   *
   * @return
   *     possible object is
   *     {@link Method }
   *
   */
  public Method getCreateMethod()
  {
    return createMethod;
  }

  /**
   * Sets the value of the createMethod property.
   *
   * @param value
   *     allowed object is
   *     {@link Method }
   *
   */
  public void setCreateMethod(Method value)
  {
    this.createMethod = value;
  }

  /**
   * Gets the value of the updateMethod property.
   *
   * @return
   *     possible object is
   *     {@link Method }
   *
   */
  public Method getUpdateMethod()
  {
    return updateMethod;
  }

  /**
   * Sets the value of the updateMethod property.
   *
   * @param value
   *     allowed object is
   *     {@link Method }
   *
   */
  public void setUpdateMethod(Method value)
  {
    this.updateMethod = value;
  }

  /**
   * Gets the value of the removeMethod property.
   *
   * @return
   *     possible object is
   *     {@link Method }
   *
   */
  public Method getRemoveMethod()
  {
    return removeMethod;
  }

  /**
   * Sets the value of the removeMethod property.
   *
   * @param value
   *     allowed object is
   *     {@link Method }
   *
   */
  public void setRemoveMethod(Method value)
  {
    this.removeMethod = value;
  }

  /**
   * Gets the value of the mergeMethod property.
   *
   * @return
   *     possible object is
   *     {@link Method }
   *
   */
  public Method getMergeMethod()
  {
    return mergeMethod;
  }

  /**
   * Sets the value of the mergeMethod property.
   *
   * @param value
   *     allowed object is
   *     {@link Method }
   *
   */
  public void setMergeMethod(Method value)
  {
    this.mergeMethod = value;
  }

  /**
   * Gets the value of the customMethod property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the customMethod property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getCustomMethod().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link Method }
   *
   *
   */
  public List<Method> getCustomMethod()
  {
    if (customMethod == null)
    {
      customMethod = new ArrayList<Method>();
    }
    return this.customMethod;
  }

}
