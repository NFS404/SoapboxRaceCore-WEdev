package com.soapboxrace.core.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "EVENT_SESSION")
public class EventSessionEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "EVENTID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_EVENTSESSION_EVENT"))
	private EventEntity event;

	@Column(name = "STARTED")
	private Long started;

	@Column(name = "ENDED")
	private Long ended;
	
	@Column(name = "TEAM1ID")
	private Long team1Id;
	
	@Column(name = "TEAM2ID")
	private Long team2Id;
	
	@Column(name = "TEAMWINNER")
	private Long teamWinner;
	
	@Column(name = "TEAM1CHECK")
	private boolean team1Check;
	
	@Column(name = "TEAM2CHECK")
	private boolean team2Check;
	
	@Column(name = "TEAMNOS")
	private boolean teamNOS;
	
	@Column(name = "PERSONAWINNER")
	private Long personaWinner;
	
	@Column(name = "PERSONARACERS")
	private String personaRacers;
	
	@Column(name = "PERSONACOPS")
	private String personaCops;
	
	@Column(name = "ISCOPSFAILED")
	private boolean isCopsFailed;
	
	@Column(name = "PLAYERLIST")
	private String playerList;
	
	@Column(name = "ISPRIVATE")
	private boolean isPrivate;
	
	@Column(name = "LOBBYID")
	private Long lobbyId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public EventEntity getEvent() {
		return event;
	}

	public void setEvent(EventEntity event) {
		this.event = event;
	}

	public Long getEnded() {
		return ended;
	}

	public void setEnded(Long ended) {
		this.ended = ended;
	}

	public Long getStarted() {
		return started;
	}

	public void setStarted(Long started) {
		this.started = started;
	}
	
	public Long getTeam1Id() {
		return team1Id;
	}

	public void setTeam1Id(Long team1Id) {
		this.team1Id = team1Id;
	}
	
	public Long getTeam2Id() {
		return team2Id;
	}

	public void setTeam2Id(Long team2Id) {
		this.team2Id = team2Id;
	}
	
	public Long getTeamWinner() {
		return teamWinner;
	}

	public void setTeamWinner(Long teamWinner) {
		this.teamWinner = teamWinner;
	}
	
	public boolean getTeam1Check() {
		return team1Check;
	}

	public void setTeam1Check(boolean team1Check) {
		this.team1Check = team1Check;
	}
	
	public boolean getTeam2Check() {
		return team2Check;
	}

	public void setTeam2Check(boolean team2Check) {
		this.team2Check = team2Check;
	}
	
	public boolean getTeamNOS() {
		return teamNOS;
	}

	public void setTeamNOS(boolean teamNOS) {
		this.teamNOS = teamNOS;
	}
	
	public Long getPersonaWinner() {
		return personaWinner;
	}

	public void setPersonaWinner(Long personaWinner) {
		this.personaWinner = personaWinner;
	}
	
	public String getPersonaRacers() {
		return personaRacers;
	}

	public void setPersonaRacers(String personaRacers) {
		this.personaRacers = personaRacers;
	}
	
	public String getPersonaCops() {
		return personaCops;
	}

	public void setPersonaCops(String personaCops) {
		this.personaCops = personaCops;
	}
	
	public boolean isCopsFailed() {
		return isCopsFailed;
	}

	public void setIsCopsFailed(boolean isCopsFailed) {
		this.isCopsFailed = isCopsFailed;
	}
	
	public String getPlayerList() {
		return playerList;
	}

	public void setPlayerList(String playerList) {
		this.playerList = playerList;
	}
	
	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
	
	public Long getLobbyId() {
		return lobbyId;
	}

	public void setLobbyId(Long lobbyId) {
		this.lobbyId = lobbyId;
	}
}
