//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2021.03.12 at 08:22:12 AM IST
//


package com.bosch.tisched.rteconfig.generator.osconfig;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>
 * Java class for OS_GENERALCFGType complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="OS_GENERALCFGType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OS_MINISRPRIO" type="{}OS_PRIOBaseType"/>
 *         &lt;element name="OS_MINPREEMPPRIO" type="{}OS_PRIOBaseType"/>
 *         &lt;element name="MMDCS_COMPOSITIONPATH" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TISCHED_VERSION" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OS_GENERALCFGType", propOrder = {
    "osminisrprio",
    "osminpreempprio",
    "triggerconnectionauto",
    "mmdcscompositionpath",
    "tischedversion" })
public class OSGENERALCFGType {

  @XmlElement(name = "OS_MINISRPRIO", required = true)
  protected BigInteger osminisrprio;
  @XmlElement(name = "OS_MINPREEMPPRIO", required = true)
  protected BigInteger osminpreempprio;
  @XmlElement(name = "TRIGGER_CONNECTION_AUTO", required = true)
  protected boolean triggerconnectionauto;
  @XmlElement(name = "MMDCS_COMPOSITIONPATH", required = true)
  protected String mmdcscompositionpath;
  @XmlElement(name = "TISCHED_VERSION")
  protected String tischedversion;

  /**
   * Gets the value of the osminisrprio property.
   * 
   * @return possible object is {@link BigInteger }
   */
  public BigInteger getOSMINISRPRIO() {
    return this.osminisrprio;
  }

  /**
   * Sets the value of the osminisrprio property.
   * 
   * @param value allowed object is {@link BigInteger }
   */
  public void setOSMINISRPRIO(final BigInteger value) {
    this.osminisrprio = value;
  }

  /**
   * Gets the value of the osminpreempprio property.
   * 
   * @return possible object is {@link BigInteger }
   */
  public BigInteger getOSMINPREEMPPRIO() {
    return this.osminpreempprio;
  }

  /**
   * Sets the value of the osminpreempprio property.
   * 
   * @param value allowed object is {@link BigInteger }
   */
  public void setOSMINPREEMPPRIO(final BigInteger value) {
    this.osminpreempprio = value;
  }

  /**
   * Gets the value of the mmdcscompositionpath property.
   * 
   * @return possible object is {@link String }
   */
  public String getMMDCSCOMPOSITIONPATH() {
    return this.mmdcscompositionpath;
  }

  /**
   * Sets the value of the mmdcscompositionpath property.
   * 
   * @param value allowed object is {@link String }
   */
  public void setMMDCSCOMPOSITIONPATH(final String value) {
    this.mmdcscompositionpath = value;
  }

  /**
   * Gets the value of the tischedversion property.
   * 
   * @return possible object is {@link String }
   */
  public String getTISCHEDVERSION() {
    return this.tischedversion;
  }

  /**
   * Sets the value of the tischedversion property.
   * 
   * @param value allowed object is {@link String }
   */
  public void setTISCHEDVERSION(final String value) {
    this.tischedversion = value;
  }

    /**
     * Gets the value of the triggerconnectionauto property.
     * 
     */
    public boolean isTRIGGERCONNECTIONAUTO() {
        return triggerconnectionauto;
    }

    /**
     * Sets the value of the triggerconnectionauto property.
     * 
     */
    public void setTRIGGERCONNECTIONAUTO(boolean value) {
        this.triggerconnectionauto = value;
    }

}
