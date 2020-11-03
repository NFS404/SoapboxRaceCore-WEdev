package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ACHIEVEMENT_BRANDS")

public class AchievementBrandsEntity {

	@Id
	private Long personaId;
	
	private int alfaRomeoWins;
	private int astonMartinWins;
	private int audiWins;

	public Long getPersonaId() {
		return personaId;
	}

	public void setPersonaId(Long personaId) {
		this.personaId = personaId;
	}

	public int getAlfaRomeoWins() {
		return alfaRomeoWins;
	}

	public void setAlfaRomeoWins(int alfaRomeoWins) {
		this.alfaRomeoWins = alfaRomeoWins;
	}
	
	public int getAstonMartinWins() {
		return astonMartinWins;
	}

	public void setAstonMartinWins(int astonMartinWins) {
		this.astonMartinWins = astonMartinWins;
	}
	
	public int getAudiWins() {
		return audiWins;
	}

	public void setAudiWins(int audiWins) {
		this.audiWins = audiWins;
	}
	
}
