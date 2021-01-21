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
@Table(name = "API_TOKEN")
@NamedQueries({ //
@NamedQuery(name = "APITokenEntity.isTokenExpired", //
		query = "SELECT obj FROM APITokenEntity obj WHERE obj.token = :token "),// TODO Finish it
})
public class APITokenEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String token;
	private LocalDateTime created;
	private String ipAddress;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public String getIPAddress() {
		return ipAddress;
	}

	public void setIPAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public LocalDateTime getCreatedTime() {
		return created;
	}

	public void setCreatedTime(LocalDateTime created) {
		this.created = created;
	}

}
