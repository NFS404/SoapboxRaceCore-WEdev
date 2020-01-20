package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.TeamsEntity;

@Stateless
public class TeamsDAO extends BaseDAO<TeamsEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public TeamsEntity findById(Long id) {
		return entityManager.find(TeamsEntity.class, id);
	}
	
	public List<TeamsEntity> findAllTeams() {
		TypedQuery<TeamsEntity> query = entityManager.createNamedQuery("TeamsEntity.findAllTeams", TeamsEntity.class);
		query.setMaxResults(15);
		return query.getResultList();
	}
	
	public TeamsEntity findByName(String teamName) {
		teamName = teamName.toUpperCase();
		TypedQuery<TeamsEntity> query = entityManager.createNamedQuery("TeamsEntity.findByName", TeamsEntity.class);
		query.setParameter("teamName", teamName);

		List<TeamsEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}
}
