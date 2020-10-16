package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.EventCarInfoEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventMissionsEntity;

@Stateless
public class EventCarInfoDAO extends BaseDAO<EventCarInfoEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EventCarInfoEntity findByEventData(Long eventDataId) {
		TypedQuery<EventCarInfoEntity> query = entityManager.createNamedQuery("EventCarInfoEntity.findByEventData", EventCarInfoEntity.class);
		query.setParameter("eventData", eventDataId);
		query.setMaxResults(1);

		List<EventCarInfoEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}

}
