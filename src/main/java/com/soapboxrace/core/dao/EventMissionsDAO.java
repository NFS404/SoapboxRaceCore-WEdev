package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventMissionsEntity;

@Stateless
public class EventMissionsDAO extends BaseDAO<EventMissionsEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EventMissionsEntity getEventMission(EventEntity event) {
		TypedQuery<EventMissionsEntity> query = entityManager.createNamedQuery("EventMissionsEntity.getEventMission", EventMissionsEntity.class);
		query.setParameter("event", event);
		query.setMaxResults(1);

		List<EventMissionsEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}

}
