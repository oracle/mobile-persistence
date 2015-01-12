
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
 *       &lt;attribute name="className" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="autoIncrementPrimaryKey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="localPersistenceManager" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="remotePersistenceManager" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="remoteReadInBackground" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="remoteWriteInBackground" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="showWebServiceInvocationErrors" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="autoQuery" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "crudServiceClass")
public class CrudServiceClass {

    @XmlAttribute(name = "className")
    protected String className;
    @XmlAttribute(name = "autoIncrementPrimaryKey")
    protected String autoIncrementPrimaryKey;
    @XmlAttribute(name = "localPersistenceManager")
    protected String localPersistenceManager;
    @XmlAttribute(name = "remotePersistenceManager")
    protected String remotePersistenceManager;
    @XmlAttribute(name = "remoteReadInBackground")
    protected String remoteReadInBackground;
    @XmlAttribute(name = "remoteWriteInBackground")
    protected String remoteWriteInBackground;
    @XmlAttribute(name = "showWebServiceInvocationErrors")
    protected String showWebServiceInvocationErrors;
    @XmlAttribute(name = "autoQuery")
    protected String autoQuery;

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
     * Gets the value of the autoIncrementPrimaryKey property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAutoIncrementPrimaryKey() {
        return autoIncrementPrimaryKey;
    }

    /**
     * Sets the value of the autoIncrementPrimaryKey property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAutoIncrementPrimaryKey(String value) {
        this.autoIncrementPrimaryKey = value;
    }

    /**
     * Gets the value of the localPersistenceManager property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLocalPersistenceManager() {
        return localPersistenceManager;
    }

    /**
     * Sets the value of the localPersistenceManager property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLocalPersistenceManager(String value) {
        this.localPersistenceManager = value;
    }

    /**
     * Gets the value of the remotePersistenceManager property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRemotePersistenceManager() {
        return remotePersistenceManager;
    }

    /**
     * Sets the value of the remotePersistenceManager property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRemotePersistenceManager(String value) {
        this.remotePersistenceManager = value;
    }

    /**
     * Gets the value of the remoteReadInBackground property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRemoteReadInBackground() {
        return remoteReadInBackground;
    }

    /**
     * Sets the value of the remoteReadInBackground property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRemoteReadInBackground(String value) {
        this.remoteReadInBackground = value;
    }

    /**
     * Gets the value of the remoteWriteInBackground property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRemoteWriteInBackground() {
        return remoteWriteInBackground;
    }

    /**
     * Sets the value of the remoteWriteInBackground property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRemoteWriteInBackground(String value) {
        this.remoteWriteInBackground = value;
    }

    /**
     * Gets the value of the showWebServiceInvocationErrors property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getShowWebServiceInvocationErrors() {
        return showWebServiceInvocationErrors;
    }

    /**
     * Sets the value of the showWebServiceInvocationErrors property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setShowWebServiceInvocationErrors(String value) {
        this.showWebServiceInvocationErrors = value;
    }

    /**
     * Gets the value of the autoQuery property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAutoQuery() {
        return autoQuery;
    }

    /**
     * Sets the value of the autoQuery property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAutoQuery(String value) {
        this.autoQuery = value;
    }

}
