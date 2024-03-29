package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.UserEntity;

@Stateless
public class UserDAO extends BaseDAO<UserEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public UserEntity findById(Long id) {
		return entityManager.find(UserEntity.class, id);
	}

	public UserEntity findByEmail(String email) {
		email = email.toLowerCase();
		TypedQuery<UserEntity> query = entityManager.createNamedQuery("UserEntity.findByEmail", UserEntity.class);
		query.setParameter("email", email);

		List<UserEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}
	
	public void ignoreHWBan(Long userId) {
		Query createQuery = entityManager.createQuery("UPDATE UserEntity obj SET obj.ignoreHWBan = true WHERE obj.id = :id");
		createQuery.setParameter("id", userId);
		createQuery.executeUpdate();
	}
	
	public void ignoreHWBanDisable(Long userId) {
		Query createQuery = entityManager.createQuery("UPDATE UserEntity obj SET obj.ignoreHWBan = false WHERE obj.id = :id");
		createQuery.setParameter("id", userId);
		createQuery.executeUpdate();
	}
	
	public void resetMoneySendLimit() {
		Query createQuery = entityManager.createQuery("UPDATE UserEntity obj SET obj.moneyGiven = 0");
		createQuery.executeUpdate();
	}
}
