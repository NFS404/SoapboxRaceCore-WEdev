package com.soapboxrace.core.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

/**
 * Список классов машин
 * @author Vadimka
 */
@Entity
@Table(name = "CUSTOMCAR")
@NamedNativeQuery(name = "ClassCountEntity.count", query = 
		"SELECT DISTINCT COUNT(carclasshash) cout, carclasshash " + 
		"FROM customcar " + 
		"WHERE carclasshash IN ( " + 
				"872416321, 415909161, 1866825865, " + 
				"-406473455, -405837480, -2142411446, " + 
				"607077938, 1337 " + 
			") " + 
		"GROUP BY carclasshash " + 
		"ORDER BY COUNT(carclasshash) DESC",
		resultClass = ClassCountEntity.class
	) //
public class ClassCountEntity {
	
	@Column(name = "cout")
	private int cout;

	@Id
	@Column(name = "carclasshash", nullable = false)
	private int carclasshash;
	
	public int getCount() {
		return cout;
	}
	
	public int getClassHash() {
		return carclasshash;
	}
}
