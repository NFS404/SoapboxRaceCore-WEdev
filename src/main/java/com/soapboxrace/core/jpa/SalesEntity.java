package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "SALES")
//@NamedQueries({ //
//		@NamedQuery(name = "ProductEntity.findByLevelEnabled", //
//				query = "SELECT obj FROM ProductEntity obj WHERE " //
//						+ "obj.enabled = :enabled AND "//
//						+ "obj.minLevel <= :minLevel AND " //
//						+ "(obj.premium = false or obj.premium = :premium ) AND " //
//						+ "obj.categoryName = :categoryName AND "//
//						+ "obj.productType = :productType AND" //
//		                + "(obj.pFull = false or obj.pFull = :pFull )"), //
//})
public class SalesEntity {

	@Id
	private Long id;

	private String car1;
	private String car2;
	private String car3;
	private String car4;
	private String saleTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getCar1() {
		return car1;
	}

	public void setCar1(String car1) {
		this.car1 = car1;
	}
	
	public String getCarId2() {
		return car2;
	}

	public void setCar2(String car2) {
		this.car2 = car2;
	}
	
	public String getCarId3() {
		return car3;
	}

	public void setCar3(String car3) {
		this.car3 = car3;
	}
	
	public String getCarId4() {
		return car4;
	}

	public void setCar4(String car4) {
		this.car4 = car4;
	}

	public String getSaleTime() {
		return saleTime;
	}

	public void setSaleTime(String saleTime) {
		this.saleTime = saleTime;
	}

}
