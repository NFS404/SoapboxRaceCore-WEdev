package com.soapboxrace.core.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

/**
 * Выборка - сколько машин с данным именем
 * @author Vadimka
 */
@Entity
@Table(name = "CUSTOMCAR")
@NamedNativeQuery(
	name = "CarNameEntity.mostPopular",
	query = "SELECT DISTINCT COUNT(NAME) cout, name " + 
			"FROM customcar " + 
			"GROUP BY name " + 
			"ORDER BY COUNT(name) DESC",
	resultClass = CarNameEntity.class
)
public class CarNameEntity {
	
	@Column(name="cout")
	private int cout;
	
	@Id
	@Column(name="name")
	private String name;
	
	/**
	 * Получить количество машин с этим именем
	 */
	public int getCount() {
		return cout;
	}
	/**
	 * Получить имя машины
	 */
	public String getName() {
		return name;
	}
}
