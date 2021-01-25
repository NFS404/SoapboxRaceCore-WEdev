package com.soapboxrace.jaxb.http;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Информация о премиуме игрока
 * @author Vadimka
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Persona", propOrder = {
		"SUCCESS",
		"NAME",
		"PREMIUM",
		"PREMIUM_TYPE",
		"PREMIUM_ENDS"
	})
public class PersonaPremiumInfo {
	
	@XmlElement(name = "Success")
	private boolean SUCCESS;
	
	@XmlElement(name = "Name")
	private String NAME;
	
	@XmlElement(name = "Premium")
	private boolean PREMIUM;
	
	@XmlElement(name = "PremiumType")
	private String PREMIUM_TYPE;
	
	@XmlElement(name = "PremiumEnds")
	private String PREMIUM_ENDS;
	
	public PersonaPremiumInfo() {
		SUCCESS = false;
		NAME = "";
		PREMIUM = false;
		PREMIUM_TYPE = "";
		PREMIUM_ENDS = "";
	}
	
	public PersonaPremiumInfo(
			String name, 
			boolean premium, 
			String premiumtype, 
			LocalDate premiumends
		) {
		SUCCESS = true;
		NAME = name;
		PREMIUM = premium;
		PREMIUM_TYPE = premiumtype;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		if (premiumends != null)
			PREMIUM_ENDS = premiumends.format(formatter);
		else
			PREMIUM_ENDS = null;
	}
}
