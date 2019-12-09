package com.soapboxrace.jaxb.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Список классов машин
 * @author Vadimka
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfCarClassHash", propOrder = {
	"carClasses"
})
public class ArrayOfCarClassHash {
	@XmlElement(name = "CarClass", nillable = true)
	private List<CarClass> carClasses;
	
	public ArrayOfCarClassHash () {
		carClasses = new ArrayList<CarClass>();
	}
	/**
	 * Добавить класс машины
	 * @param carClass - клас машины
	 * @param count - количество машин с данным классом
	 */
	public void add(int carClass, int count) {
		carClasses.add(new CarClass(carClass, count));
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "CarClass", propOrder = {
		"carClass",
		"count"
	})
	private static class CarClass {
		@XmlElement(name = "CarClass")
		private String carClass;
		@XmlElement(name = "Count")
		private int count;
		
		public CarClass(int carClass, int count) {
			this.count = count;
			switch(carClass) {
			case 872416321:
				this.carClass = "E";
				break;
			case 415909161:
				this.carClass = "D";
				break;
			case 1866825865:
				this.carClass = "C";
				break;
			case -406473455:
				this.carClass = "B";
				break;
			case -405837480:
				this.carClass = "A";
				break;
			case -2142411446:
				this.carClass = "S";
				break;
			case 607077938 :
				this.carClass = "OR";
				break;
			case 1337 :
				this.carClass = "NPC";
				break;
			default:
				this.carClass = "";
				break;
			}
		}
	}
}
