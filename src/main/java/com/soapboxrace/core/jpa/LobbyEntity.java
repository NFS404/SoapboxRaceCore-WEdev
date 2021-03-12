package com.soapboxrace.core.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
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
import javax.persistence.Transient;

@Entity
@Table(name = "LOBBY")
@NamedQueries({ //
		@NamedQuery(name = "LobbyEntity.findAll", query = "SELECT obj FROM UserEntity obj"), //
		@NamedQuery(name = "LobbyEntity.findAllOpen", //
				query = "SELECT obj FROM LobbyEntity obj WHERE obj.lobbyDateTimeStart between :dateTime1 and :dateTime2 "), //
		@NamedQuery(name = "LobbyEntity.findAllOpenByCarClass", //
				query = "SELECT obj FROM LobbyEntity obj " //
						+ "WHERE (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null) " //
						+ "and (obj.event.carClassHash = 607077938 or obj.event.carClassHash = :carClassHash) AND obj.isPrivate = false "
						+ "AND obj.event.searchAvailable = true "),
		
		// CarDivision check
		// and (obj.carDivision = 'bas' or obj.carDivision = :carDivision) 
		
		@NamedQuery(name = "LobbyEntity.findAllMPLobbiesClasses", // All Races (Classes)
		query = "SELECT obj FROM LobbyEntity obj " //
				+ "WHERE (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null) " //
				+ "AND obj.isPrivate = false "
				+ "AND obj.event.searchAvailable = true AND obj.event.carClassHash = :carClassHash ORDER BY obj.lobbyDateTimeStart ASC"),
		@NamedQuery(name = "LobbyEntity.findAllMPLobbiesOpen", // All Races (Open)
		query = "SELECT obj FROM LobbyEntity obj " //
				+ "WHERE (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null)" //
				+ "AND obj.isPrivate = false "
				+ "AND obj.event.searchAvailable = true AND obj.event.carClassHash = 607077938 ORDER BY obj.lobbyDateTimeStart ASC"),
		
		@NamedQuery(name = "LobbyEntity.findMPLobbiesP2PClasses", // Circuits and Sprints (Classes)
		query = "SELECT obj FROM LobbyEntity obj " //
				+ "WHERE (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null)" //
				+ "AND obj.isPrivate = false "
				+ "AND obj.event.searchAvailable = true AND obj.event.carClassHash = :carClassHash AND (obj.event.eventModeId = 4 or obj.event.eventModeId = 9) ORDER BY obj.lobbyDateTimeStart ASC"),
		@NamedQuery(name = "LobbyEntity.findMPLobbiesP2POpen", // Circuits and Sprints (Open)
		query = "SELECT obj FROM LobbyEntity obj " //
				+ "WHERE (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null) " //
				+ "AND obj.isPrivate = false "
				+ "AND obj.event.searchAvailable = true AND obj.event.carClassHash = 607077938 AND (obj.event.eventModeId = 4 or obj.event.eventModeId = 9) ORDER BY obj.lobbyDateTimeStart ASC"),
		
		@NamedQuery(name = "LobbyEntity.findMPLobbiesDragClasses", // Drags (Classes)
		query = "SELECT obj FROM LobbyEntity obj " //
				+ "WHERE (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null) " //
				+ "AND obj.isPrivate = false "
				+ "AND obj.event.searchAvailable = true AND obj.event.carClassHash = :carClassHash AND obj.event.eventModeId = 19 ORDER BY obj.lobbyDateTimeStart ASC"),
		@NamedQuery(name = "LobbyEntity.findMPLobbiesDragOpen", // Drags (Open)
		query = "SELECT obj FROM LobbyEntity obj " //
				+ "WHERE (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null) " //
				+ "AND obj.isPrivate = false "
				+ "AND obj.event.searchAvailable = true AND obj.event.carClassHash = 607077938 AND obj.event.eventModeId = 19 ORDER BY obj.lobbyDateTimeStart ASC"),
		
		@NamedQuery(name = "LobbyEntity.findMPLobbiesRaceClasses", // Circuits, Sprints, Drags (Classes)
		query = "SELECT obj FROM LobbyEntity obj " //
				+ "WHERE (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null) " //
				+ "AND obj.isPrivate = false "
				+ "AND obj.event.searchAvailable = true AND obj.event.carClassHash = :carClassHash AND (obj.event.eventModeId = 4 or obj.event.eventModeId = 9 or obj.event.eventModeId = 19) ORDER BY obj.lobbyDateTimeStart ASC"),
		@NamedQuery(name = "LobbyEntity.findMPLobbiesRaceOpen", // Circuits, Sprints, Drags (Open)
		query = "SELECT obj FROM LobbyEntity obj " //
				+ "WHERE (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null) " //
				+ "AND obj.isPrivate = false "
				+ "AND obj.event.searchAvailable = true AND obj.event.carClassHash = 607077938 AND (obj.event.eventModeId = 4 or obj.event.eventModeId = 9 or obj.event.eventModeId = 19) ORDER BY obj.lobbyDateTimeStart ASC"),
		
		@NamedQuery(name = "LobbyEntity.findMPLobbiesPursuitClasses", // Team Escapes (Classes)
		query = "SELECT obj FROM LobbyEntity obj " //
				+ "WHERE (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null) " //
				+ "AND obj.isPrivate = false "
				+ "AND obj.event.searchAvailable = true AND obj.event.carClassHash = :carClassHash AND (obj.event.eventModeId = 24 or obj.event.eventModeId = 100) ORDER BY obj.lobbyDateTimeStart ASC"),
		@NamedQuery(name = "LobbyEntity.findMPLobbiesPursuitOpen", // Team Escapes (Open)
		query = "SELECT obj FROM LobbyEntity obj " //
				+ "WHERE (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null) " //
				+ "AND obj.isPrivate = false "
				+ "AND obj.event.searchAvailable = true AND obj.event.carClassHash = 607077938 AND (obj.event.eventModeId = 24 or obj.event.eventModeId = 100) ORDER BY obj.lobbyDateTimeStart ASC"),
		
		@NamedQuery(name = "LobbyEntity.findByEventStarted", query = "SELECT obj FROM LobbyEntity obj WHERE obj.event = :event AND (obj.lobbyDateTimeStart between :dateTime1 AND :dateTime2) OR (obj.lobbyDateTimeStart = null) AND obj.isPrivate = false"), //
		@NamedQuery(name = "LobbyEntity.findByEventAndPersona", query = "SELECT obj FROM LobbyEntity obj WHERE obj.event = :event AND (obj.lobbyDateTimeStart between :dateTime1 AND :dateTime2) OR (obj.lobbyDateTimeStart = null) AND obj.isPrivate = true AND obj.personaId = :personaId"), //
		@NamedQuery(name = "LobbyEntity.findByHosterPersona", query = "SELECT obj FROM LobbyEntity obj WHERE obj.personaId = :personaId"), //
		@NamedQuery(name = "LobbyEntity.isThisLobbyReserved", query = "SELECT obj FROM LobbyEntity obj WHERE obj.id = :id AND obj.isReserved = false"), //
		@NamedQuery(name = "LobbyEntity.deleteAll", query = "DELETE FROM LobbyEntity obj") //
})
public class LobbyEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "EVENTID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_LOBBY_EVENT"))
	private EventEntity event;

	@OneToMany(mappedBy = "lobby", targetEntity = LobbyEntrantEntity.class, cascade = CascadeType.MERGE)
	private List<LobbyEntrantEntity> entrants;

	private Date lobbyDateTimeStart;

	private Boolean isPrivate;

	private Long personaId;
	
	private String carDivision;
	
	private Long team1Id;
	
	private Long team2Id;
	
	private Boolean isReserved;

	@Transient
	private Long lobbyCountdownInMilliseconds = 45000L;

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

	public List<LobbyEntrantEntity> getEntrants() {
		return entrants;
	}

	public void setEntrants(List<LobbyEntrantEntity> entrants) {
		this.entrants = entrants;
	}

	public Date getLobbyDateTimeStart() {
		return lobbyDateTimeStart;
	}

	public void setLobbyDateTimeStart(Date lobbyDateTimeStart) {
		this.lobbyDateTimeStart = lobbyDateTimeStart;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
	
	public String getCarDivision() {
		return carDivision;
	}
	
	public void setCarDivision(String carDivision) {
		this.carDivision = carDivision;
	}

	public Long getPersonaId() {
		return personaId;
	}

	public void setPersonaId(Long personaId) {
		this.personaId = personaId;
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
	
	public Boolean isReserved() {
		return isReserved;
	}

	public void setIsReserved(Boolean isReserved) {
		this.isReserved = isReserved;
	}

	public boolean add(LobbyEntrantEntity e) {
		if (entrants == null) {
			entrants = new ArrayList<>();
		}
		return entrants.add(e);
	}

}
