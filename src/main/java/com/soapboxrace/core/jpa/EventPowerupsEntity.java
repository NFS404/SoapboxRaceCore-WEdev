package com.soapboxrace.core.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "EVENT_POWERUPS")
@NamedQueries({ //
		@NamedQuery(name = "EventPowerupsEntity.findByEventDataId", query = "SELECT obj FROM EventPowerupsEntity obj WHERE obj.eventData = :eventData ") //
})
public class EventPowerupsEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long eventData;

	private int nosShot;
	private int slingshot;
	private int oneMoreLap;
	private int ready;
	private int shield;
	private int trafficMagnet;
	private int juggernaut;
	private int emergencyEvade;
	private int teamEmergencyEvade;
	private int runFlatTires;
	private int instantCooldown;
	private int teamSlingshot;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getEventData() {
		return eventData;
	}

	public void setEventData(Long eventData) {
		this.eventData = eventData;
	}
	
	public int getNosShot() {
		return nosShot;
	}

	public void setNosShot(int nosShot) {
		this.nosShot = nosShot;
	}
	
	public int getSlingshot() {
		return slingshot;
	}

	public void setSlingshot(int slingshot) {
		this.slingshot = slingshot;
	}
	
	public int getOneMoreLap() {
		return oneMoreLap;
	}

	public void setOneMoreLap(int oneMoreLap) {
		this.oneMoreLap = oneMoreLap;
	}
	
	public int getReady() {
		return ready;
	}

	public void setReady(int ready) {
		this.ready = ready;
	}
	
	public int getShield() {
		return shield;
	}

	public void setShield(int shield) {
		this.shield = shield;
	}
	
	public int getTrafficMagnet() {
		return trafficMagnet;
	}

	public void setTrafficMagnet(int trafficMagnet) {
		this.trafficMagnet = trafficMagnet;
	}
	
	public int getJuggernaut() {
		return juggernaut;
	}

	public void setJuggernaut(int juggernaut) {
		this.juggernaut = juggernaut;
	}
	
	public int getEmergencyEvade() {
		return emergencyEvade;
	}

	public void setEmergencyEvade(int emergencyEvade) {
		this.emergencyEvade = emergencyEvade;
	}
	
	public int getTeamEmergencyEvade() {
		return teamEmergencyEvade;
	}

	public void setTeamEmergencyEvade(int teamEmergencyEvade) {
		this.teamEmergencyEvade = teamEmergencyEvade;
	}
	
	public int getRunFlatTires() {
		return runFlatTires;
	}

	public void setRunFlatTires(int runFlatTires) {
		this.runFlatTires = runFlatTires;
	}
	
	public int getInstantCooldown() {
		return instantCooldown;
	}

	public void setInstantCooldown(int instantCooldown) {
		this.instantCooldown = instantCooldown;
	}
	
	public int getTeamSlingshot() {
		return teamSlingshot;
	}

	public void setTeamSlingshot(int teamSlingshot) {
		this.teamSlingshot = teamSlingshot;
	}

}
