/*
 * Taken from SBRW-Core - https://github.com/SoapboxRaceWorld/soapbox-race-core/
 */

package com.soapboxrace.jaxb.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ArrayOfBasicBlockPlayerInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ArrayOfBasicBlockPlayerInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BasicBlockPlayerInfo" type="{}BasicBlockPlayerInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfBasicBlockPlayerInfo", propOrder = {
        "basicBlockPlayerInfo"
})
public class ArrayOfBasicBlockPlayerInfo {

    @XmlElement(name = "BasicBlockPlayerInfo", nillable = true)
    protected List<BasicBlockPlayerInfo> basicBlockPlayerInfo;

    /**
     * Gets the value of the basicBlockPlayerInfo property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the basicBlockPlayerInfo property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBasicBlockPlayerInfo().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BasicBlockPlayerInfo }
     */
    public List<BasicBlockPlayerInfo> getBasicBlockPlayerInfo() {
        if (basicBlockPlayerInfo == null) {
            basicBlockPlayerInfo = new ArrayList<BasicBlockPlayerInfo>();
        }
        return this.basicBlockPlayerInfo;
    }

}