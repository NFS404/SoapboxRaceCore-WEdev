package com.soapboxrace.jaxb.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Список иконок профиля
 * @author Vadimka
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfProfileIcon", propOrder = {
	"icons"
})
public class ArrayOfProfileIcon {

	@XmlElement(name = "ProfileIcon")
	private List<ProfileIcon> icons;
	
	public ArrayOfProfileIcon() {
		icons = new ArrayList<ArrayOfProfileIcon.ProfileIcon>();
	}
	
	public void add(int iconid, int count) {
		icons.add(new ProfileIcon(iconid, count));
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "ProfileIcon", propOrder = {
		"iconID",
		"count"
	})
	public static class ProfileIcon {
		@XmlElement(name = "IconID")
		private int iconID;
		@XmlElement(name = "Count")
		private int count;
		
		public ProfileIcon(int iconid, int count) {
			this.iconID = iconid;
			this.count = count;
		}
	}
	
}
