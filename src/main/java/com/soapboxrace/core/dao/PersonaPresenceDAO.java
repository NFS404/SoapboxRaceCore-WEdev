package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;

@Stateless
public class PersonaPresenceDAO extends BaseDAO<PersonaPresenceEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public PersonaPresenceEntity findByUserId(Long userId) {
		TypedQuery<PersonaPresenceEntity> query = entityManager.createNamedQuery("PersonaPresenceEntity.findByUserId", PersonaPresenceEntity.class);
		query.setParameter("userId", userId);
		List<PersonaPresenceEntity> resultList = query.getResultList();
		if (resultList.isEmpty()) {
			return null;
		}
		return resultList.get(0);
	}
	
	public boolean isUserNotOnline(Long userId) {
		TypedQuery<PersonaPresenceEntity> query = entityManager.createNamedQuery("PersonaPresenceEntity.isUserOnline", PersonaPresenceEntity.class);
		query.setParameter("userId", userId);
		return query.getResultList().isEmpty();
	}

	public void updatePersonaPresence(Long personaId, Integer personaPresence) {
		Query query = entityManager.createNamedQuery("PersonaPresenceEntity.updatePersonaPresence");
		query.setParameter("personaId", personaId);
		query.setParameter("personaPresence", personaPresence);
		query.executeUpdate();
	}
	
	public void updateCurrentEvent(Long personaId, Long eventDataId, int eventModeId, Long eventSessionId, int presence) {
		Query query = entityManager.createNamedQuery("PersonaPresenceEntity.updateCurrentEvent");
		query.setParameter("personaId", personaId);
		query.setParameter("currentEventDataId", eventDataId);
		query.setParameter("currentEventModeId", (long) eventModeId);
		query.setParameter("currentEventSessionId", eventSessionId);
		query.setParameter("personaPresence", presence);
		query.executeUpdate();
	}
	
	public void updateCurrentEventPost(Long personaId, Long eventDataId, int eventModeId, Long eventSessionId, boolean icRacer) {
		Query query = entityManager.createNamedQuery("PersonaPresenceEntity.updateCurrentEventPost");
		query.setParameter("personaId", personaId);
		query.setParameter("currentEventDataId", eventDataId);
		query.setParameter("currentEventModeId", (long) eventModeId);
		query.setParameter("currentEventSessionId", eventSessionId);
		query.setParameter("icRacer", icRacer);
		query.executeUpdate();
	}
	
	public void updateICRacer(Long personaId, boolean icRacer) {
		Query query = entityManager.createNamedQuery("PersonaPresenceEntity.updateICRacer");
		query.setParameter("personaId", personaId);
		query.setParameter("icRacer", icRacer);
		query.executeUpdate();
	}

}
