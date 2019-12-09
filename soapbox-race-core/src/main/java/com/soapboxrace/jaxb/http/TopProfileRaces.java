package com.soapboxrace.jaxb.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Хранит профили с количеством отыгранных гонок
 * @author Vadimka
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TopProfile", propOrder = {
	"profileData"
})
public class TopProfileRaces {

	@XmlElement(name = "Profile", nillable = true)
	protected List<ProfileDataRaces> profileData;
	
	public TopProfileRaces() {
		profileData = new ArrayList<ProfileDataRaces>();
	}
	/**
	 * Добавить профиль в список
	 * @param profile - объект профиля
	 */
	public void add(ProfileDataRaces profile) {
		profileData.add(profile);
	}
	/**
	 * Профиль с именем, иконкой и количеством отыгранных заездов
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "profile", propOrder = {
		"iconindex",
		"name",
		"races",
	})
	public static class ProfileDataRaces {
		@XmlElement(name = "Name")
		protected String name;
		@XmlElement(name = "IconIndex")
		protected int iconindex;
		@XmlElement(name = "Races")
		protected int races;
		public ProfileDataRaces(String name, int iconID, int races) {
			this.name = name;
			this.iconindex = iconID;
			this.races = races;
		}
		/**
		 * Получить имя профиля
		 */
		public String getName() {
			return name;
		}
		/**
		 * Получить ID иконки
		 */
		public int getIconIndex() {
			return iconindex;
		}
		/**
		 * Получить очки профиля
		 */
		public int getRaces() {
			return races;
		}
	}
}
