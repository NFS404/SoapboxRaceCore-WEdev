package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.FriendListEntity;

@Stateless
public class FriendListDAO extends BaseDAO<FriendListEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public List<FriendListEntity> getUserFriendList(Long userId) {
		TypedQuery<FriendListEntity> query = entityManager.createNamedQuery("FriendListEntity.getUserFriendList", FriendListEntity.class);
		query.setParameter("userId", userId);
		return query.getResultList();
	}
	
	public FriendListEntity findUsersRelationship(Long userId, Long userFriendId) {
		TypedQuery<FriendListEntity> query = entityManager.createNamedQuery("FriendListEntity.findUsersRelationship", FriendListEntity.class);
		query.setParameter("userId", userId);
		query.setParameter("userFriendId", userFriendId);
		return ( query.getResultList() != null && !query.getResultList().isEmpty() ) ? query.getResultList().get(0) : null;
	}
	
	public List<FriendListEntity> getUserBlockedList(Long userId) {
		TypedQuery<FriendListEntity> query = entityManager.createNamedQuery("FriendListEntity.getUserBlockedList", FriendListEntity.class);
		query.setParameter("userId", userId);
		return query.getResultList();
	}

}
