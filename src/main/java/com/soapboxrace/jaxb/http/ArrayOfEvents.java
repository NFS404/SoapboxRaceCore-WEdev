package com.soapboxrace.jaxb.http;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Постраничный список всех трасс
 * @author Vadimka
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Races", propOrder = {
	"count",
	"races"
})
public class ArrayOfEvents {
	@XmlElement(name = "Count")
	private BigInteger count;
	@XmlElement(name = "Races")
	private List<Race> races;
	
	public ArrayOfEvents() {
		races = new ArrayList<Race>();
	}
	/**
	 * Установить количество трасс всего
	 * @param count
	 */
	public void setCount(BigInteger count) {
		this.count = count;
	}
	/**
	 * Добавить трассу в список
	 * @param id - Идентификатор трассы
	 * @param name - Имя трассы
	 */
	public void add(int id, String name, int carClass, String race_type) {
		races.add(new Race(id, name, carClass, race_type));
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "Race", propOrder = {
		"ID",
		"NAME",
		"CAR_CLASS",
		"TYPE"
	})
	public static class Race {

		@XmlElement(name = "ID")
		private int ID;
		@XmlElement(name = "Name")
		private String NAME;
		@XmlElement(name = "Class")
		private String CAR_CLASS;
		@XmlElement(name = "Type")
		private String TYPE;
		
		protected Race (int id, String name, int carClass, String race_type) {
			ID = id;
			NAME = name;
			switch(carClass) {
			case 872416321:
				this.CAR_CLASS = "E";
				break;
			case 415909161:
				this.CAR_CLASS = "D";
				break;
			case 1866825865:
				this.CAR_CLASS = "C";
				break;
			case -406473455:
				this.CAR_CLASS = "B";
				break;
			case -405837480:
				this.CAR_CLASS = "A";
				break;
			case -2142411446:
				this.CAR_CLASS = "S";
				break;
			case 607077938 :
				this.CAR_CLASS = "OR";
				break;
			case 1337 :
				this.CAR_CLASS = "NPC";
				break;
			default:
				this.CAR_CLASS = "";
				break;
			}
			TYPE = race_type;
		}
	}
	
}
