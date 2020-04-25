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

	public void updatePersonaPresence(Long personaId, Integer personaPresence) {
		Query query = entityManager.createNamedQuery("PersonaPresenceEntity.updatePersonaPresence");
		query.setParameter("personaId", personaId);
		query.setParameter("personaPresence", personaPresence);
		query.executeUpdate();
	}
	
	public void updatePowerUpsInRace(Long personaId, boolean powerUpsInRace) {
		Query query = entityManager.createNamedQuery("PersonaPresenceEntity.updatePowerUpsInRace");
		query.setParameter("personaId", personaId);
		query.setParameter("powerUpsInRace", powerUpsInRace);
		query.executeUpdate();
	}

}
