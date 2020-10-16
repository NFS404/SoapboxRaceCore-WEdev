package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "EVENT_MISSIONS")
@NamedQueries({ //
@NamedQuery(name = "EventMissionsEntity.pickCurrentWeek", //
		query = "SELECT obj FROM EventMissionsEntity obj WHERE obj.week = :week "), //
@NamedQuery(name = "EventMissionsEntity.getEventMission", //
        query = "SELECT obj FROM EventMissionsEntity obj WHERE obj.event = :event ") //
})
public class EventMissionsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "EVENTID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "EVENT_MISSIONS_FK"))
	private EventEntity event;
	
	private String eventType;
	private String category;
	private boolean customGoal;
	private boolean isEnabled;
	
	private Long timeOutrun;
	private Long timeHold;
	private int copsDisabled;
	private int copsRammed;
	private int copsRBPassed;
	private int copsSpikePassed;
	private int copsCostToState;
	private float minTopSpeed;
	private Long airTime;
	
	private int week;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	public boolean getCustomGoal() {
		return customGoal;
	}

	public void setCustomGoal(boolean customGoal) {
		this.customGoal = customGoal;
	}
	
	public boolean getIsEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public Long getTimeOutrun() {
		return timeOutrun;
	}

	public void setTimeOutrun(Long timeOutrun) {
		this.timeOutrun = timeOutrun;
	}
	
	public Long getTimeHold() {
		return timeHold;
	}

	public void setTimeHold(Long timeHold) {
		this.timeHold = timeHold;
	}
	
	public int getCopsDisabled() {
		return copsDisabled;
	}

	public void setCopsDisabled(int copsDisabled) {
		this.copsDisabled = copsDisabled;
	}
	
	public int getCopsRammed() {
		return copsRammed;
	}

	public void setCopsRammed(int copsRammed) {
		this.copsRammed = copsRammed;
	}
	
	public int getCopsRBPassed() {
		return copsRBPassed;
	}

	public void setCopsRBPassed(int copsRBPassed) {
		this.copsRBPassed = copsRBPassed;
	}
	
	public int getCopsSpikePassed() {
		return copsSpikePassed;
	}

	public void setCopsSpikePassed(int copsSpikePassed) {
		this.copsSpikePassed = copsSpikePassed;
	}
	
	public int getCopsCostToState() {
		return copsCostToState;
	}

	public void setCopsCostToState(int copsCostToState) {
		this.copsCostToState = copsCostToState;
	}
	
	public float getMinTopSpeed() {
		return minTopSpeed;
	}

	public void setMinTopSpeed(float minTopSpeed) {
		this.minTopSpeed = minTopSpeed;
	}
	
	public Long getAirTime() {
		return airTime;
	}

	public void setAirTime(Long airTime) {
		this.airTime = airTime;
	}
	
	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}
}
