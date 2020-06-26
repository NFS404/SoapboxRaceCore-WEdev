package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SALES")
public class SalesEntity {

	@Id
	private Long id;

	private String car1;
	private String car2;
	private String car3;
	private String car4;
	private int car1Lvl;
	private int car2Lvl;
	private int car3Lvl;
	private int car4Lvl;
	private float car1Cost;
	private float car2Cost;
	private float car3Cost;
	private float car4Cost;

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
	
	public String getCar2() {
		return car2;
	}

	public void setCar2(String car2) {
		this.car2 = car2;
	}
	
	public String getCar3() {
		return car3;
	}

	public void setCar3(String car3) {
		this.car3 = car3;
	}
	
	public String getCar4() {
		return car4;
	}

	public void setCar4(String car4) {
		this.car4 = car4;
	}
	
	public int getCar1Lvl() {
		return car1Lvl;
	}

	public void setCar1Lvl(int car1Lvl) {
		this.car1Lvl = car1Lvl;
	}
	
	public int getCar2Lvl() {
		return car2Lvl;
	}

	public void setCar2Lvl(int car2Lvl) {
		this.car2Lvl = car2Lvl;
	}
	
	public int getCar3Lvl() {
		return car3Lvl;
	}

	public void setCar3Lvl(int car3Lvl) {
		this.car3Lvl = car3Lvl;
	}
	
	public int getCar4Lvl() {
		return car4Lvl;
	}

	public void setCar4Lvl(int car4Lvl) {
		this.car4Lvl = car4Lvl;
	}
	
	public float getCar1Cost() {
		return car1Cost;
	}

	public void setCar1Cost(float car1Cost) {
		this.car1Cost = car1Cost;
	}
	
	public float getCar2Cost() {
		return car2Cost;
	}

	public void setCar2Cost(float car2Cost) {
		this.car2Cost = car2Cost;
	}
	
	public float getCar3Cost() {
		return car3Cost;
	}

	public void setCar3Cost(float car3Cost) {
		this.car3Cost = car3Cost;
	}
	
	public float getCar4Cost() {
		return car4Cost;
	}

	public void setCar4Cost(float car4Cost) {
		this.car4Cost = car4Cost;
	}
}
