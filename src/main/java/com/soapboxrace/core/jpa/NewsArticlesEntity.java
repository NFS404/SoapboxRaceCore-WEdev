package com.soapboxrace.core.jpa;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "NEWS_ARTICLES")
@NamedQueries({ //
		@NamedQuery(name = "NewsArticlesEntity.loadCommon", //
				query = "SELECT obj FROM NewsArticlesEntity obj WHERE obj.personaId = 0") //
})
public class NewsArticlesEntity {
	
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private LocalDateTime expiryTime;
	private int filters;
	private int iconType;
	private String longTextHALId;
	private String parameters;
	private Long personaId;
	private String shortTextHALId;
	private int sticky;
	private LocalDateTime timeStamp;
	private int type;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDateTime getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(LocalDateTime expiryTime) {
		this.expiryTime = expiryTime;
	}
	
	public int getFilters() {
		return filters;
	}

	public void setFilters(int filters) {
		this.filters = filters;
	}
	
	public int getIconType() {
		return iconType;
	}

	public void setIconType(int iconType) {
		this.iconType = iconType;
	}
	
	public String getLongTextHALId() {
		return longTextHALId;
	}

	public void setLongTextHALId(String longTextHALId) {
		this.longTextHALId = longTextHALId;
	}
	
	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public Long getPersonaId() {
		return personaId;
	}

	public void setPersonaId(Long personaId) {
		this.personaId = personaId;
	}

	public String getShortTextHALId() {
		return shortTextHALId;
	}

	public void setShortTextHALId(String shortTextHALId) {
		this.shortTextHALId = shortTextHALId;
	}
	
	public int getSticky() {
		return sticky;
	}
	
	public void setSticky(int sticky) {
		this.sticky = sticky;
	}
	
	public LocalDateTime getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(LocalDateTime timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
