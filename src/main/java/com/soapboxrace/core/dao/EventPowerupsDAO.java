package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.EventPowerupsEntity;

@Stateless
public class EventPowerupsDAO extends BaseDAO<EventPowerupsEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EventPowerupsEntity findByEventDataId(Long eventDataId) {
		TypedQuery<EventPowerupsEntity> query = entityManager.createNamedQuery("EventPowerupsEntity.findByEventDataId", EventPowerupsEntity.class);
		query.setParameter("eventData", eventDataId);
		List<EventPowerupsEntity> resultList = query.getResultList();
		if (resultList.isEmpty()) {
			return null;
		}
		return resultList.get(0);
	}
	
	public EventPowerupsEntity findById(Long id) {
		return entityManager.find(EventPowerupsEntity.class, id);
	}

}
