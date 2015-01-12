
package oracle.ateam.sample.mobile.dt.model.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
 *       &lt;sequence>
 *         &lt;element ref="{http://www.oracle.com/ateam/mobile/persistenceMapping}foreignKeyColumnReference" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="accessorMethod" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="referenceClassName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sendAsArrayIfOnlyOneEntry" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "foreignKeyColumnReference" })
@XmlRootElement(name = "oneToManyMapping")
public class OneToManyMapping extends AttributeMapping {

    @XmlElement(required = true)
    protected List<ForeignKeyColumnReference> foreignKeyColumnReference;
    @XmlAttribute(name = "accessorMethod")
    protected String accessorMethod;
    @XmlAttribute(name = "referenceClassName")
    protected String referenceClassName;
    @XmlAttribute(name = "sendAsArrayIfOnlyOneEntry")
    protected String sendAsArrayIfOnlyOneEntry;

    /**
     * Gets the value of the foreignKeyColumnReference property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the foreignKeyColumnReference property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getForeignKeyColumnReference().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ForeignKeyColumnReference }
     *
     *
     */
    public List<ForeignKeyColumnReference> getForeignKeyColumnReference() {
        if (foreignKeyColumnReference == null) {
            foreignKeyColumnReference = new ArrayList<ForeignKeyColumnReference>();
        }
        return this.foreignKeyColumnReference;
    }

    /**
     * Gets the value of the accessorMethod property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAccessorMethod() {
        return accessorMethod;
    }

    /**
     * Sets the value of the accessorMethod property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAccessorMethod(String value) {
        this.accessorMethod = value;
    }

    /**
     * Gets the value of the referenceClassName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getReferenceClassName() {
        return referenceClassName;
    }

    /**
     * Sets the value of the referenceClassName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setReferenceClassName(String value) {
        this.referenceClassName = value;
    }

    /**
     * Gets the value of the sendAsArrayIfOnlyOneEntry property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSendAsArrayIfOnlyOneEntry() {
        return sendAsArrayIfOnlyOneEntry;
    }

    /**
     * Sets the value of the sendAsArrayIfOnlyOneEntry property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSendAsArrayIfOnlyOneEntry(String value) {
        this.sendAsArrayIfOnlyOneEntry = value;
    }

}
