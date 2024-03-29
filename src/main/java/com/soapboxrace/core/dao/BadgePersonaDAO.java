package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.AchievementDefinitionEntity;
import com.soapboxrace.core.jpa.AchievementRankEntity;
import com.soapboxrace.core.jpa.BadgePersonaEntity;
import com.soapboxrace.core.jpa.PersonaEntity;

@Stateless
public class BadgePersonaDAO extends BaseDAO<BadgePersonaEntity> {
	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public BadgePersonaEntity findById(Long id) {
		return entityManager.find(BadgePersonaEntity.class, id);
	}

	public List<BadgePersonaEntity> getAll() {
		return entityManager.createQuery("SELECT obj FROM BadgePersonaEntity obj", BadgePersonaEntity.class).getResultList();
	}
	
	public List<BadgePersonaEntity> findByPersona(PersonaEntity personaEntity) {
		TypedQuery<BadgePersonaEntity> query = entityManager.createNamedQuery("BadgeDefinitionEntity.findByPersona", BadgePersonaEntity.class);
		query.setParameter("persona", personaEntity);

		List<BadgePersonaEntity> resultList = query.getResultList();
		return resultList.isEmpty() ? null : resultList;
	}
	
	public BadgePersonaEntity findByPersonaAndDefinition(PersonaEntity personaEntity, AchievementDefinitionEntity achievementDefinitionEntity) {
		TypedQuery<BadgePersonaEntity> query = entityManager.createNamedQuery("BadgePersonaEntity.findByPersonaAndDefinition", BadgePersonaEntity.class);
		query.setParameter("persona", personaEntity);
		query.setParameter("achievementId", achievementDefinitionEntity);

		List<BadgePersonaEntity> resultList = query.getResultList();
		return resultList.isEmpty() ? null : resultList.get(0);
	}

	public void deleteByPersona(PersonaEntity persona) {
		Query query = entityManager.createNamedQuery("BadgePersonaEntity.deleteByPersona");
		query.setParameter("persona", persona);
		query.executeUpdate();
	}
	
	public void deleteByPersonaSlot(PersonaEntity persona, short slot) {
		Query query = entityManager.createNamedQuery("BadgePersonaEntity.deleteByPersonaSlot");
		query.setParameter("persona", persona);
		query.setParameter("slot", slot);
		query.executeUpdate();
	}
	
	public void deleteByPersonaButExcludeRank(PersonaEntity persona, AchievementRankEntity achievementRankEntity) {
		Query query = entityManager.createNamedQuery("BadgePersonaEntity.deleteByPersonaButExcludeRank");
		query.setParameter("persona", persona);
		query.setParameter("achievementRank", achievementRankEntity);
		query.executeUpdate();
	}
}
