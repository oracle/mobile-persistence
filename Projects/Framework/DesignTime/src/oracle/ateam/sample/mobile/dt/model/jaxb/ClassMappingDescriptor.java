
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
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}crudServiceClass" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}table" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}directMapping" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}oneToManyMapping" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}oneToOneMapping" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}findAllMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}findAllInParentMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}findMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}getAsParentMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}getCanonicalMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}createMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}updateMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}removeMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}mergeMethod" minOccurs="0"/>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}customMethod" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="className" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="persisted" type="{http://www.w3.org/2001/XMLSchema}string" />
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
@XmlType(name = "", propOrder = {
         "crudServiceClass", "table", "directMapping", "oneToManyMapping", "oneToOneMapping", "findAllMethod",
         "findAllInParentMethod", "findMethod", "getAsParentMethod", "getCanonicalMethod", "createMethod",
         "updateMethod", "removeMethod", "mergeMethod", "customMethod"
    })
@XmlRootElement(name = "classMappingDescriptor")
public class ClassMappingDescriptor {

    protected CrudServiceClass crudServiceClass;
    protected Table table;
    protected List<DirectMapping> directMapping;
    protected List<OneToManyMapping> oneToManyMapping;
    protected List<OneToOneMapping> oneToOneMapping;
    protected Method findAllMethod;
    protected Method findAllInParentMethod;
    protected Method findMethod;
    protected Method getAsParentMethod;
    protected Method getCanonicalMethod;
    protected Method createMethod;
    protected Method updateMethod;
    protected Method removeMethod;
    protected Method mergeMethod;
    protected List<Method> customMethod;
    @XmlAttribute(name = "className")
    protected String className;
    @XmlAttribute(name = "persisted")
    protected String persisted;
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
    public CrudServiceClass getCrudServiceClass() {
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
    public void setCrudServiceClass(CrudServiceClass value) {
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
    public Table getTable() {
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
    public void setTable(Table value) {
        this.table = value;
    }

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
    public List<DirectMapping> getDirectMapping() {
        if (directMapping == null) {
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
    public List<OneToManyMapping> getOneToManyMapping() {
        if (oneToManyMapping == null) {
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
    public List<OneToOneMapping> getOneToOneMapping() {
        if (oneToOneMapping == null) {
            oneToOneMapping = new ArrayList<OneToOneMapping>();
        }
        return this.oneToOneMapping;
    }

    /**
     * Gets the value of the findAllMethod property.
     *
     * @return
     *     possible object is
     *     {@link Method }
     *
     */
    public Method getFindAllMethod() {
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
    public void setFindAllMethod(Method value) {
        this.findAllMethod = value;
    }

    /**
     * Gets the value of the findAllInParentMethod property.
     *
     * @return
     *     possible object is
     *     {@link Method }
     *
     */
    public Method getFindAllInParentMethod() {
        return findAllInParentMethod;
    }

    /**
     * Sets the value of the findAllInParentMethod property.
     *
     * @param value
     *     allowed object is
     *     {@link Method }
     *
     */
    public void setFindAllInParentMethod(Method value) {
        this.findAllInParentMethod = value;
    }

    /**
     * Gets the value of the findMethod property.
     *
     * @return
     *     possible object is
     *     {@link Method }
     *
     */
    public Method getFindMethod() {
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
    public void setFindMethod(Method value) {
        this.findMethod = value;
    }

    /**
     * Gets the value of the getAsParentMethod property.
     *
     * @return
     *     possible object is
     *     {@link Method }
     *
     */
    public Method getGetAsParentMethod() {
        return getAsParentMethod;
    }

    /**
     * Sets the value of the getAsParentMethod property.
     *
     * @param value
     *     allowed object is
     *     {@link Method }
     *
     */
    public void setGetAsParentMethod(Method value) {
        this.getAsParentMethod = value;
    }

    /**
     * Gets the value of the getCanonicalMethod property.
     *
     * @return
     *     possible object is
     *     {@link Method }
     *
     */
    public Method getGetCanonicalMethod() {
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
    public void setGetCanonicalMethod(Method value) {
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
    public Method getCreateMethod() {
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
    public void setCreateMethod(Method value) {
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
    public Method getUpdateMethod() {
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
    public void setUpdateMethod(Method value) {
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
    public Method getRemoveMethod() {
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
    public void setRemoveMethod(Method value) {
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
    public Method getMergeMethod() {
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
    public void setMergeMethod(Method value) {
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
    public List<Method> getCustomMethod() {
        if (customMethod == null) {
            customMethod = new ArrayList<Method>();
        }
        return this.customMethod;
    }

    /**
     * Gets the value of the className property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getClassName() {
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
    public void setClassName(String value) {
        this.className = value;
    }

    /**
     * Gets the value of the persisted property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPersisted() {
        return persisted;
    }

    /**
     * Sets the value of the persisted property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPersisted(String value) {
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
    public String getDateFormat() {
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
    public void setDateFormat(String value) {
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
    public String getDateTimeFormat() {
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
    public void setDateTimeFormat(String value) {
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
    public String getOrderBy() {
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
    public void setOrderBy(String value) {
        this.orderBy = value;
    }

}
