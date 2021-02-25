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
	@NamedQuery(name = "FriendListEntity.getUserFriendList", query = "SELECT obj FROM FriendListEntity obj WHERE (obj.userId_A = :userId OR obj.userId_B = :userId) AND obj.blockStatus = 0"), //
	@NamedQuery(name = "FriendListEntity.findUsersRelationship", query = "SELECT obj FROM FriendListEntity obj WHERE (obj.userId_A = :userId OR obj.userId_B = :userId) AND (obj.userId_A = :userFriendId OR obj.userId_B = :userFriendId)"), //
	@NamedQuery(name = "FriendListEntity.getUserBlockedList", query = "SELECT obj FROM FriendListEntity obj WHERE (obj.userId_A = :userId OR obj.userId_B = :userId) AND obj.blockStatus <> 0") //
})
public class FriendListEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	// User-sender
	private Long userId_A;
	private Long personaId_A;
	// User-recipient
	private Long userId_B;
	private Long personaId_B;
	// Relationship status (0 - blocked, 1 - friend request pending, 2 - friends)
	private int status;
	// Block status (0 - not blocked, 1 - user A requested the block, 2 - user B requested the block, 3 - both users is blocked each other)
	private int blockStatus;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId_A() {
		return userId_A;
	}

	public void setUserId_A(Long userId_A) {
		this.userId_A = userId_A;
	}
	
	public Long getPersonaId_A() {
		return personaId_A;
	}

	public void setPersonaId_A(Long personaId_A) {
		this.personaId_A = personaId_A;
	}

	public Long getUserId_B() {
		return userId_B;
	}

	public void setUserId_B(Long userId_B) {
		this.userId_B = userId_B;
	}
	
	public Long getPersonaId_B() {
		return personaId_B;
	}

	public void setPersonaId_B(Long personaId_B) {
		this.personaId_B = personaId_B;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getBlockStatus() {
		return blockStatus;
	}

	public void setBlockStatus(int blockStatus) {
		this.blockStatus = blockStatus;
	}

}
