package com.soapboxrace.core.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

/**
 * Самый популярный заезд
 * @author Vadimka
 */
@Entity
@Table(name = "EVENT_DATA")
@NamedNativeQueries({
	@NamedNativeQuery(
			name = "MostPopularEventEntity.mostPopular",
			query = "SELECT " + 
					"	DISTINCT " + 
					"	COUNT(e.id) cout, " + 
					"	e.name event_name, " + 
					"	d.eventmodeid eventModeId, " + 
					"	e.carclasshash classHash " + 
					"FROM EVENT_DATA d " + 
					"INNER JOIN EVENT e ON d.eventid = e.id " + 
					"WHERE d.eventmodeid = :mode " + 
					"GROUP BY d.eventmodeid, e.id " + 
					"ORDER BY cout DESC " + 
					"LIMIT :count",
			resultClass = MostPopularEventEntity.class
		)
})
public class MostPopularEventEntity {
	
	@Id
	@Column(name="eventModeId", nullable = false)
	private int eventModeId;
	
	@Column(name="cout")
	private int cout;
	
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

	public int getCount() {
		return cout;
	}

	public int getClassHash() {
		return classHash;
	}

}
