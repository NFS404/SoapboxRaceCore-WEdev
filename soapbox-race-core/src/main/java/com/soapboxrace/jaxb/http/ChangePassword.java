package com.soapboxrace.jaxb.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChangePassword", propOrder = {
	"status",
	"message"
})
public class ChangePassword {
	@XmlElement(name = "Status")
	private boolean status;
	@XmlElement(name = "Message")
	private String message;
	public ChangePassword(boolean status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public boolean isOk() {
		return status;
	}
}
