package com.soapboxrace.core.jpa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "USER_SB")
@NamedQueries({ //
		@NamedQuery(name = "UserEntity.findAll", query = "SELECT obj FROM UserEntity obj"),
		@NamedQuery(name = "UserEntity.findByEmail", query = "SELECT obj FROM UserEntity obj WHERE obj.email = :email"),
		@NamedQuery(name = "UserEntity.findByUserId", query = "SELECT obj FROM UserEntity obj WHERE obj.id = :id") //

})
public class UserEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "EMAIL", length = 255)
	private String email;

	@Column(name = "PASSWORD", length = 50)
	private String password;

	@Column(name = "HWID")
	private String hwid;

	private String gameHardwareHash;

	@Column(name = "IP_ADDRESS")
	private String ipAddress;

	@OneToMany(mappedBy = "user", targetEntity = PersonaEntity.class)
	private List<PersonaEntity> listOfProfile;

	@Column(name = "premium")
	private boolean premium;
	
	@Column(name = "premiumDate")
	private LocalDate premiumDate;
	
	@Column(name = "premiumType")
	private String premiumType;

	@Column(name = "isAdmin")
	private boolean isAdmin;

	@Column(name = "isModder")
	private boolean isModder;
	
	@Column(name = "created")
	private LocalDateTime created;

	@Column(name = "lastLogin")
	private LocalDateTime lastLogin;

	@Column(name = "extramoney")
	private double extramoney;
	
	@Column(name = "moneyGiven")
	private double moneyGiven;
	
	@Column(name = "boost")
	private double boost;
	
	@Column(name = "userAgent", length = 255)
	private String ua;

	@Column(name = "vinylSlotsUsed")
	private int vinylSlotsUsed;
	
	@Column(name = "ignoreHWBan")
	private boolean ignoreHWBan;
	
	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return this.id;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return this.email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

	public List<PersonaEntity> getListOfProfile() {
		return listOfProfile;
	}

	public String getHwid() {
		return hwid;
	}

	public void setHwid(String hwid) {
		this.hwid = hwid;
	}

	public String getUA() {
		return ua;
	}

	public void setUA(String ua) {
		this.ua = ua;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public boolean isPremium() {
		return premium;
	}

	public void setPremium(boolean premium) {
		this.premium = premium;
	}
	
	public LocalDate getPremiumDate() {
		return premiumDate;
	}
	
	public void setPremiumDate(LocalDate premiumDate) {
		this.premiumDate = premiumDate;
	}
	
	public String getPremiumType() {
		return premiumType;
	}
	
	public void setPremiumType(String premiumType) {
		this.premiumType = premiumType;
	}

	public boolean ownsPersona(Long id) {
		return this.listOfProfile.stream().anyMatch(p -> p.getPersonaId().equals(id));
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean admin) {
		isAdmin = admin;
	}
	
	public boolean isModder() {
		return isModder;
	}

	public void setModder(boolean modder) {
		isModder = modder;
	}

	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public String getGameHardwareHash() {
		return gameHardwareHash;
	}

	public void setGameHardwareHash(String gameHardwareHash) {
		this.gameHardwareHash = gameHardwareHash;
	}
	
	public double getExtraMoney() {
		return extramoney;
	}
	
	public void setExtraMoney(double extramoney) {
		this.extramoney = extramoney;
	}
	
	public double getMoneyGiven() {
		return moneyGiven;
	}
	
	public void setMoneyGiven(double moneyGiven) {
		this.moneyGiven = moneyGiven;
	}
	
	public double getBoost() {
		return boost;
	}
	
	public void setBoost(double boost) {
		this.boost = boost;
	}
	
	public int getVinylSlotsUsed() {
		return vinylSlotsUsed;
	}
	
	public void setVinylSlotsUsed(int vinylSlotsUsed) {
		this.vinylSlotsUsed = vinylSlotsUsed;
	}
	
	public boolean getIgnoreHWBan() {
		return ignoreHWBan;
	}

	public void setIgnoreHWBan(boolean ignoreHWBan) {
		this.ignoreHWBan = ignoreHWBan;
	}

}
