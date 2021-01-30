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
@Table(name = "PERSONAPRESENCE")
@NamedQueries({ //
		@NamedQuery(name = "PersonaPresenceEntity.findByUserId", query = "SELECT obj FROM PersonaPresenceEntity obj WHERE obj.userId = :userId"), //
		@NamedQuery(name = "PersonaPresenceEntity.updatePersonaPresence", //
				query = "UPDATE PersonaPresenceEntity obj " // 
						+ "SET obj.personaPresence = :personaPresence WHERE obj.activePersonaId = :personaId"), //
		@NamedQuery(name = "PersonaPresenceEntity.updateCurrentEvent", //
		query = "UPDATE PersonaPresenceEntity obj " // 
				+ "SET obj.currentEventDataId = :currentEventDataId, obj.currentEventModeId = :currentEventModeId, obj.currentEventSessionId = :currentEventSessionId, obj.personaPresence = :personaPresence WHERE obj.activePersonaId = :personaId"), //
		@NamedQuery(name = "PersonaPresenceEntity.updateCurrentEventPost", //
		query = "UPDATE PersonaPresenceEntity obj " // 
				+ "SET obj.currentEventDataId = :currentEventDataId, obj.currentEventModeId = :currentEventModeId, obj.currentEventSessionId = :currentEventSessionId, obj.disablePU = :disablePU WHERE obj.activePersonaId = :personaId"), //
		@NamedQuery(name = "PersonaPresenceEntity.updateDisablePU", //
		query = "UPDATE PersonaPresenceEntity obj " // 
				+ "SET obj.disablePU = :disablePU WHERE obj.activePersonaId = :personaId"), //
		@NamedQuery(name = "PersonaPresenceEntity.isUserOnline", query = "SELECT obj FROM PersonaPresenceEntity obj WHERE obj.userId = :userId AND obj.personaPresence <> 0") //
})
public class PersonaPresenceEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;
	private Long activePersonaId;
	private int personaPresence;
	private Long currentEventDataId;
	private Long currentEventModeId;
	private Long currentEventSessionId;
	private boolean disablePU;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getActivePersonaId() {
		return activePersonaId;
	}

	public void setActivePersonaId(Long activePersonaId) {
		this.activePersonaId = activePersonaId;
	}

	public int getPersonaPresence() {
		return personaPresence;
	}

	public void setPersonaPresence(int personaPresence) {
		this.personaPresence = personaPresence;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getCurrentEventDataId() {
		return currentEventDataId;
	}

	public void setCurrentEventDataId(Long currentEventDataId) {
		this.currentEventDataId = currentEventDataId;
	}
	
	public Long getCurrentEventModeId() {
		return currentEventModeId;
	}

	public void setCurrentEventModeId(Long currentEventModeId) {
		this.currentEventModeId = currentEventModeId;
	}
	
	public Long getCurrentEventSessionId() {
		return currentEventSessionId;
	}

	public void setCurrentEventSessionId(Long currentEventSessionId) {
		this.currentEventSessionId = currentEventSessionId;
	}
	
	public boolean getDisablePU() {
		return disablePU;
	}

	public void setDisablePU(boolean disablePU) {
		this.disablePU = disablePU;
	}

}
