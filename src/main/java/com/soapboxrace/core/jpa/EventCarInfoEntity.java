package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "EVENT_CARINFO")
@NamedQueries({ //
@NamedQuery(name = "EventCarInfoEntity.findByEventData", //
		query = "SELECT obj FROM EventCarInfoEntity obj WHERE obj.eventData = :eventData ") //
})
public class EventCarInfoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long eventData;
	private Long personaId;
	private int rating;
	
	private String skillParts;
	private String perfParts;
	private boolean bodykit;
	private boolean spoiler;
	private boolean lowkit;
	
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
	
	public Long getPersonaId() {
		return personaId;
	}

	public void setPersonaId(Long personaId) {
		this.personaId = personaId;
	}
	
	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public String getSkillParts() {
		return skillParts;
	}

	public void setSkillParts(String skillParts) {
		this.skillParts = skillParts;
	}
	
	public String getPerfParts() {
		return perfParts;
	}

	public void setPerfParts(String perfParts) {
		this.perfParts = perfParts;
	}
	
	public boolean getBodykit() {
		return bodykit;
	}

	public void setBodykit(boolean bodykit) {
		this.bodykit = bodykit;
	}
	
	public boolean getSpoiler() {
		return spoiler;
	}

	public void setSpoiler(boolean spoiler) {
		this.spoiler = spoiler;
	}
	
	public boolean getLowkit() {
		return lowkit;
	}

	public void setLowkit(boolean lowkit) {
		this.lowkit = lowkit;
	}
	
}
