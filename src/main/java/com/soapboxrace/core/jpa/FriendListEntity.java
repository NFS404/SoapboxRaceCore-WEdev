package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "FRIEND_LIST")
@NamedQueries({ //
	@NamedQuery(name = "FriendListEntity.findByOwnerId", query = "SELECT obj FROM FriendListEntity obj WHERE obj.userOwnerId = :userOwnerId"), //
	@NamedQuery(name = "FriendListEntity.findAcceptedByOwnerId", query = "SELECT obj FROM FriendListEntity obj WHERE obj.userOwnerId = :userOwnerId AND obj.isAccepted = true"), //
	@NamedQuery(name = "FriendListEntity.findBlockedByOwnerId", query = "SELECT obj FROM FriendListEntity obj WHERE obj.userOwnerId = :userOwnerId AND obj.isBlocked = true"), //
	@NamedQuery(name = "FriendListEntity.findByRemoteUserBlockedId", query = "SELECT obj FROM FriendListEntity obj WHERE obj.userId = :userId AND obj.isBlocked = true"), //
	@NamedQuery(name = "FriendListEntity.findByOwnerIdAndFriendPersona", query = "SELECT obj FROM FriendListEntity obj WHERE obj.userOwnerId = :userOwnerId AND obj.personaId = :personaId"), //
	@NamedQuery(name = "FriendListEntity.deleteByPersona", query = "DELETE FROM FriendListEntity obj WHERE obj.personaId = :personaId") //
})
public class FriendListEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long userOwnerId;
	private Long personaId;
	private Long userId;
	private Boolean isAccepted;
	private Boolean isBlocked;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserOwnerId() {
		return userOwnerId;
	}

	public void setUserOwnerId(Long userOwnerId) {
		this.userOwnerId = userOwnerId;
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
	
	public Boolean getIsAccepted() {
		return isAccepted;
	}
	
	public void setIsAccepted(Boolean isAccepted) {
		this.isAccepted = isAccepted;
	}
	
	public Boolean getIsBlocked() {
		return isBlocked;
	}
	
	public void setIsBlocked(Boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

}
