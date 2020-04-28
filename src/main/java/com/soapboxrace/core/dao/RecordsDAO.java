package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.RecordsEntity;

@Stateless
public class RecordsDAO extends BaseDAO<RecordsEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public RecordsEntity findCurrentRace(int eventId, Long userId, boolean powerUps, int carClassHash) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.findCurrentRace", RecordsEntity.class);
		query.setParameter("eventId", eventId);
		query.setParameter("userId", userId);
		query.setParameter("powerUps", powerUps);
		query.setParameter("carClassHash", carClassHash);

		List<RecordsEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}

	public List<RecordsEntity> getRecordPlace(int eventId, Long userId, boolean powerUps, int carClassHash, Long timeMS) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.calcRecordPlace", RecordsEntity.class);
		query.setParameter("eventId", eventId);
		query.setParameter("powerUps", powerUps);
		query.setParameter("carClassHash", carClassHash);
		query.setParameter("timeMS", timeMS);

		List<RecordsEntity> resultList = query.getResultList();
		return resultList;
	}
}
