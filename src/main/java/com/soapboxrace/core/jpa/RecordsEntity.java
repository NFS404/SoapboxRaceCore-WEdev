package com.soapboxrace.core.jpa;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "RECORDS")
@NamedQueries({ //
@NamedQuery(name = "RecordsEntity.findCurrentRace", //
		query = "SELECT obj FROM RecordsEntity obj WHERE obj.eventId = :eventId AND obj.userId = :userId AND obj.powerUps = :powerUps AND obj.carClassHash = :carClassHash "),//
@NamedQuery(name = "RecordsEntity.calcRecordPlace", //
        query = "SELECT obj FROM RecordsEntity obj "
        		+ "WHERE obj.eventId = :eventId "
        		+ "AND obj.powerUps = :powerUps "
        		+ "AND obj.carClassHash = :carClassHash "
        		+ "AND obj.timeMS <= :timeMS "
        		+ "AND obj.carVersion = :carVersion "
                + "ORDER BY obj.timeMS "),//
})
public class RecordsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private long timeMS;
	private long timeMSAlt;
	private long timeMSOld;
	private long bestLapTimeMS;
	
	private boolean powerUps;
	private boolean perfectStart;
	private boolean isSingle;
	private float topSpeed;
	
	private int carClassHash;
	private int carPhysicsHash;
	private int carVersion;
	private LocalDateTime date;
	private String playerName;
	private String carName;
	
	private Long eventSessionId;
	private Long eventPowerupsId;
	private int eventId;
	private Long personaId;
	private Long userId;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getTimeMS() {
		return timeMS;
	}

	public void setTimeMS(Long timeMS) {
		this.timeMS = timeMS;
	}
	
	public Long getTimeMSAlt() {
		return timeMSAlt;
	}

	public void setTimeMSAlt(Long timeMSAlt) {
		this.timeMSAlt = timeMSAlt;
	}
	
	public Long getTimeMSOld() {
		return timeMSOld;
	}

	public void setTimeMSOld(Long timeMSOld) {
		this.timeMSOld = timeMSOld;
	}
	
	public Long getBestLapTimeMS() {
		return bestLapTimeMS;
	}

	public void setBestLapTimeMS(Long bestLapTimeMS) {
		this.bestLapTimeMS = bestLapTimeMS;
	}
	
	public boolean getPowerUps() {
		return powerUps;
	}

	public void setPowerUps(boolean powerUps) {
		this.powerUps = powerUps;
	}
	
	public boolean getPerfectStart() {
		return perfectStart;
	}

	public void setPerfectStart(boolean perfectStart) {
		this.perfectStart = perfectStart;
	}
	
	public boolean getIsSingle() {
		return isSingle;
	}

	public void setIsSingle(boolean isSingle) {
		this.isSingle = isSingle;
	}
	
	public float getTopSpeed() {
		return topSpeed;
	}

	public void setTopSpeed(float topSpeed) {
		this.topSpeed = topSpeed;
	}
	
	public int getCarClassHash() {
		return carClassHash;
	}

	public void setCarClassHash(int carClassHash) {
		this.carClassHash = carClassHash;
	}
	
	public int getCarPhysicsHash() {
		return carPhysicsHash;
	}

	public void setCarPhysicsHash(int carPhysicsHash) {
		this.carPhysicsHash = carPhysicsHash;
	}
	
	public int getCarVersion() {
		return carVersion;
	}

	public void setCarVersion(int carVersion) {
		this.carVersion = carVersion;
	}
	
	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	
	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public String getCarName() {
		return carName;
	}

	public void setCarName(String carName) {
		this.carName = carName;
	}
	
	public Long getEventSessionId() {
		return eventSessionId;
	}

	public void setEventSessionId(Long eventSessionId) {
		this.eventSessionId = eventSessionId;
	}
	
	public Long getEventPowerupsId() {
		return eventPowerupsId;
	}

	public void setEventPowerupsId(Long eventPowerupsId) {
		this.eventPowerupsId = eventPowerupsId;
	}
	
	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}
	
	public Long getPersonaId() {
		return personaId;
	}

	public void setPersonaId(Long personaId) {
		this.personaId = personaId;
	}
	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
}
