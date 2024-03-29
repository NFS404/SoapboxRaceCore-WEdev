package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "REPORT")
@NamedQueries({ @NamedQuery(name = "ReportEntity.findTeamInvite", //
		query = "SELECT obj FROM ReportEntity obj WHERE obj.personaId = :personaId AND obj.abuserPersonaId = :abuserPersonaId AND obj.description = '/teaminvite'"),
		@NamedQuery(name = "ReportEntity.deleteTeamInvite", //
		query = "DELETE FROM ReportEntity obj WHERE obj.personaId = :personaId AND obj.abuserPersonaId = :abuserPersonaId AND obj.description = '/teaminvite'") //
})
public class ReportEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long personaId;
	private Long abuserPersonaId;
	private String description;
	private Integer petitionType;
	private Integer customCarID;
	private Integer chatMinutes;
	private Long hacksdetected;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPersonaId() {
		return personaId;
	}

	public void setPersonaId(Long personaId) {
		this.personaId = personaId;
	}
	
	public Long getAbuserPersonaId() {
		return abuserPersonaId;
	}

	public void setAbuserPersonaId(Long abuserPersonaId) {
		this.abuserPersonaId = abuserPersonaId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Integer getPetitionType() {
		return petitionType;
	}

	public void setPetitionType(Integer petitionType) {
		this.petitionType = petitionType;
	}
	
	public Integer getCustomCarID() {
		return customCarID;
	}

	public void setCustomCarID(Integer customCarID) {
		this.customCarID = customCarID;
	}
	
	public Integer getChatMinutes() {
		return chatMinutes;
	}

	public void setChatMinutes(Integer chatMinutes) {
		this.chatMinutes = chatMinutes;
	}
	
	public Long getHacksDetected() {
		return hacksdetected;
	}
	
	public void setHacksDetected(Long hacksdetected) {
		this.hacksdetected = hacksdetected;
	}

}