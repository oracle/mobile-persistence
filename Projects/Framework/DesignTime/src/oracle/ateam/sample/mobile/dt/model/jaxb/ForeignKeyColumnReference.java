
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
 *       &lt;attribute name="sourceTable" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sourceColumn" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="targetTable" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="targetColumn" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "foreignKeyColumnReference")
public class ForeignKeyColumnReference
{

  @XmlAttribute(name = "sourceTable", required = true)
  protected String sourceTable;
  @XmlAttribute(name = "sourceColumn", required = true)
  protected String sourceColumn;
  @XmlAttribute(name = "targetTable", required = true)
  protected String targetTable;
  @XmlAttribute(name = "targetColumn", required = true)
  protected String targetColumn;

  /**
   * Gets the value of the sourceTable property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getSourceTable()
  {
    return sourceTable;
  }

  /**
   * Sets the value of the sourceTable property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setSourceTable(String value)
  {
    this.sourceTable = value;
  }

  /**
   * Gets the value of the sourceColumn property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getSourceColumn()
  {
    return sourceColumn;
  }

  /**
   * Sets the value of the sourceColumn property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setSourceColumn(String value)
  {
    this.sourceColumn = value;
  }

  /**
   * Gets the value of the targetTable property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getTargetTable()
  {
    return targetTable;
  }

  /**
   * Sets the value of the targetTable property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setTargetTable(String value)
  {
    this.targetTable = value;
  }

  /**
   * Gets the value of the targetColumn property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getTargetColumn()
  {
    return targetColumn;
  }

  /**
   * Sets the value of the targetColumn property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setTargetColumn(String value)
  {
    this.targetColumn = value;
  }

}
