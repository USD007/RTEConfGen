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
 * <p>Java class for OS_APPLICATIONIDMAPPINGType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OS_APPLICATIONIDMAPPINGType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OS_APPLICATIONID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CORENUMBER" type="{}OS_CORENUMBERType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OS_APPLICATIONIDMAPPINGType", propOrder = {
    "osapplicationid",
    "corenumber"
})
public class OSAPPLICATIONIDMAPPINGType {

    @XmlElement(name = "OS_APPLICATIONID", required = true)
    protected String osapplicationid;
    @XmlElement(name = "CORENUMBER", required = true)
    protected BigInteger corenumber;

    /**
     * Gets the value of the osapplicationid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOSAPPLICATIONID() {
        return osapplicationid;
    }

    /**
     * Sets the value of the osapplicationid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOSAPPLICATIONID(String value) {
        this.osapplicationid = value;
    }

    /**
     * Gets the value of the corenumber property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCORENUMBER() {
        return corenumber;
    }

    /**
     * Sets the value of the corenumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCORENUMBER(BigInteger value) {
        this.corenumber = value;
    }

}
