package com.soapboxrace.core.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

/**
 * Самый популярный заезд
 * @author Vadimka, Hypercycle
 */
@Entity
@Table(name = "EVENT_DATA")
@NamedNativeQueries({
	@NamedNativeQuery(
			name = "MostPopularEventEntity.mostPopular",
			query = "SELECT " + 
					"	DISTINCT " + 
					"	e.name event_name, " + 
					"	e.eventmodeid eventModeId, " + 
					"	e.carclasshash classHash, " + 
					"	e.finishcount finishCount " + 
					"FROM EVENT e " + 
					"WHERE e.eventmodeid = :mode " + 
					"GROUP BY e.eventmodeid, e.id " + 
					"ORDER BY e.finishcount DESC " + 
					"LIMIT :count",
			resultClass = MostPopularEventEntity.class
		)
})
public class MostPopularEventEntity {
	
	@Id
	@Column(name="eventModeId", nullable = false)
	private int eventModeId;
	
	@Column(name="finishCount")
	private int finishCount;
	
	@Column(name="event_name")
	private String event_name;
	
	@Column(name="classHash")
	private int classHash;

	public String getName() {
		return event_name;
	}

	public int getEventModeId() {
		return eventModeId;
	}

	public int getFinishCount() {
		return finishCount;
	}

	public int getClassHash() {
		return classHash;
	}

}
