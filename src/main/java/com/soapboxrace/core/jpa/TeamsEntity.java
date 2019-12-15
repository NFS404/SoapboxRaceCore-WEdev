
package com.soapboxrace.core.jpa;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "TEAMS")
@NamedQueries({ //
		@NamedQuery(name = "TeamsEntity.findByName", query = "SELECT obj FROM TeamsEntity obj WHERE obj.teamName = :teamName"),
		@NamedQuery(name = "TeamsEntity.findAllTeams", query = "SELECT obj FROM TeamsEntity obj WHERE obj.active = true ORDER BY obj.teamPoints DESC") //
})
public class TeamsEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long teamId;
	private String teamName;
	private boolean openEntry;
	private int teamPoints;
	private boolean active;
	private int playersCount;

	@ManyToOne
	@JoinColumn(name = "LEADERID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "TEAMS_PERSONA_FK"))
	private PersonaEntity persona;
	
	@OneToMany(mappedBy = "team", targetEntity = PersonaEntity.class)
	private List<PersonaEntity> listOfTeammates;

	@Column(name = "created")
	private LocalDateTime created;

	public int getTeamPoints() {
		return teamPoints;
	}

	public void setTeamPoints(int teamPoints) {
		this.teamPoints = teamPoints;
	}
	
	public int getPlayersCount() {
		return playersCount;
	}

	public void setPlayersCount(int playersCount) {
		this.playersCount = playersCount;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public List<PersonaEntity> getListOfTeammates() {
		return listOfTeammates;
	}
	
	public PersonaEntity getLeader() {
		return persona;
	}

	public void setLeader(PersonaEntity persona) {
		this.persona = persona;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}
	
	public boolean getOpenEntry() {
		return openEntry;
	}

	public void setOpenEntry(boolean openEntry) {
		this.openEntry = openEntry;
	}
	
	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
