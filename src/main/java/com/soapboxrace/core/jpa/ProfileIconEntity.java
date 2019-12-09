package com.soapboxrace.core.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

/**
 * Иконка профиля
 * @author Vadimka
 */
@Entity
@Table(name = "PERSONA")
@NamedNativeQuery(name = "ProfileIconEntity.count", query = 
		"SELECT DISTINCT COUNT(iconindex) cout, iconindex " + 
		"FROM persona " + 
		"GROUP BY iconindex " + 
		"ORDER BY COUNT(iconindex) DESC",
		resultClass = ProfileIconEntity.class
	) //
public class ProfileIconEntity {
	
	@Column(name = "cout")
	private int cout;

	@Id
	@Column(name = "iconindex", nullable = false)
	private int iconid;
	/**
	 * Получить количество профилей с этой иконкой
	 * @return int
	 */
	public int getCount() {
		return cout;
	}
	/**
	 * Получить ID иконки
	 * @return int
	 */
	public int getIconid() {
		return iconid;
	}

}
