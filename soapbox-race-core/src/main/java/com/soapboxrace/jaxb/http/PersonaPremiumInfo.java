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
		"EXTRA_MONEY",
		"PREMIUM",
		"PREMIUM_TYPE",
		"PREMIUM_ENDS"
	})
public class PersonaPremiumInfo {
	
	@XmlElement(name = "Success")
	private boolean SUCCESS;
	
	@XmlElement(name = "Name")
	private String NAME;
	
	@XmlElement(name = "ExtraMoney")
	private Long EXTRA_MONEY;
	
	@XmlElement(name = "Premium")
	private boolean PREMIUM;
	
	@XmlElement(name = "PremiumType")
	private String PREMIUM_TYPE;
	
	@XmlElement(name = "PremiumEnds")
	private String PREMIUM_ENDS;
	
	public PersonaPremiumInfo() {
		SUCCESS = false;
		NAME = "";
		EXTRA_MONEY = 0l;
		PREMIUM = false;
		PREMIUM_TYPE = "";
		PREMIUM_ENDS = "";
	}
	
	public PersonaPremiumInfo(
			String name, 
			double extramoney, 
			boolean premium, 
			String premiumtype, 
			LocalDate premiumends
		) {
		SUCCESS = true;
		NAME = name;
		EXTRA_MONEY = new Long((int) extramoney);
		PREMIUM = premium;
		PREMIUM_TYPE = premiumtype;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		if (premiumends != null)
			PREMIUM_ENDS = premiumends.format(formatter);
		else
			PREMIUM_ENDS = null;
	}
}
