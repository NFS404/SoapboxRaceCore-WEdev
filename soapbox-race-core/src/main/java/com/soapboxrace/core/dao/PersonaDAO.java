package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PersonaTopRaceEntity;
import com.soapboxrace.core.jpa.PersonaTopTreasureHunt;
import com.soapboxrace.core.jpa.ProfileIconEntity;

@Stateless
public class PersonaDAO extends BaseDAO<PersonaEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public PersonaEntity findById(Long id) {
		return entityManager.find(PersonaEntity.class, id);
	}

	public PersonaEntity findByName(String name) {
		name = name.toUpperCase();
		TypedQuery<PersonaEntity> query = entityManager.createNamedQuery("PersonaEntity.findByName", PersonaEntity.class);
		query.setParameter("name", name);

		List<PersonaEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}

	public List<PersonaEntity> getAllPaged(int offset, int max) {
		TypedQuery<PersonaEntity> query = entityManager.createQuery("SELECT obj FROM PersonaEntity obj ", PersonaEntity.class);
		query.setMaxResults(max);
		query.setFirstResult(offset);
		return query.getResultList();
	}
	/**
	 * Получить название текущей машины профиля
	 * @param personaID - Идентификатор профиля
	 * @return
	 */
	public String getCurrentCar(Long personaID) {
		Query query = entityManager.createNativeQuery("SELECT cc.name " + 
				"FROM persona p, customcar cc " + 
				"WHERE cc.id = p.curcarindex AND p.id="+personaID);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<String> list = query.getResultList();
		String carname = "";
		if (!list.isEmpty()) carname = list.get(0);
		return carname;
	}
	/**
	 * Получает топ профилей по очкам	
	 * @param onPage - Из скольки профилей будет состоять топ
	 * @return List<PersonaEntity>
	 * @author Vadimka
	 */
	public List<PersonaEntity> getTopScore(int onPage) {
		TypedQuery<PersonaEntity> query = entityManager.createQuery("SELECT obj FROM PersonaEntity obj ORDER BY score DESC", PersonaEntity.class);
		query.setMaxResults(onPage);
		return query.getResultList();
	}
	/**
	 * Получает топ профилей по количеству проехавших трасс
	 * @param onPage - Из скольки профилей будет состоять топ
	 * @return List<PersonaTopRaceEntity>
	 * @author Vadimka
	 */
	public List<PersonaTopRaceEntity> getTopRacers(int onPage) {
		TypedQuery<PersonaTopRaceEntity> query = entityManager.createNamedQuery("top",PersonaTopRaceEntity.class);
		query.setMaxResults(onPage);
		return query.getResultList();
	}
	/**
	 * Получает топ профилей по количеству дней, за которые собраны кристалики
	 * @param onPage - Из скольки профилей будет состоять топ
	 * @author Vadimka
	 */
	public List<PersonaTopTreasureHunt> getTopTreasureHunt(int onPage) {
		TypedQuery<PersonaTopTreasureHunt> query = entityManager.createNamedQuery("PersonaTopTreasureHunt.top",PersonaTopTreasureHunt.class);
		query.setMaxResults(onPage);
		return query.getResultList();
	}
	/**
	 * Получить список наболее често используемых иконок
	 * @param onPage - Из скольки профилей будет состоять топ
	 * @author Vadimka
	 */
	public List<ProfileIconEntity> getPopularIcons(int onPage) {
		TypedQuery<ProfileIconEntity> query = entityManager.createNamedQuery("ProfileIconEntity.count", ProfileIconEntity.class);
		query.setMaxResults(onPage);
		return query.getResultList();
	}
}
