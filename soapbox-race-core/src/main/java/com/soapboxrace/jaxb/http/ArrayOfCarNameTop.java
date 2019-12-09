package com.soapboxrace.jaxb.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Список топа использованного имени машин
 * @author Vadimka	
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfCarNameTop", propOrder = {
	"names"
})
public class ArrayOfCarNameTop {

	@XmlElement(name = "CarName")
	private List<CarName> names;
	
	public ArrayOfCarNameTop() {
		names = new ArrayList<ArrayOfCarNameTop.CarName>();
	}
	/**
	 * Добавить имя машины
	 * @param count - Сколько раз используется это имя
	 * @param name - Имя машины
	 */
	public void add(int count, String name) {
		names.add(new CarName(count, name));
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "ArrayOfCarClass", propOrder = {
		"count",
		"name"
	})
	public static class CarName {
		@XmlElement(name = "Count")
		private int count;
		@XmlElement(name = "name")
		private String name;
		
		public CarName(int count, String name) {
			this.count = count;
			this.name = name;
		}
		
		public int getCount() {
			return count;
		}
		
		public String getName() {
			return name;
		}
	}
}
