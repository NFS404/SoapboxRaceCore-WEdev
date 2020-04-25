package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.ClassCountEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.RecordsEntity;
import com.soapboxrace.core.jpa.CarNameEntity;

@Stateless
public class CustomCarDAO extends BaseDAO<CustomCarEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	/**
	 * Список популярных машин
	 * Сортируется по наиболее частому классу
	 * @param onPage - Из скольки профилей будет состоять топ
	 * @author Vadimka
	 */
	public List<ClassCountEntity> getPopularCarsByClass(int onPage) {
		TypedQuery<ClassCountEntity> query = entityManager.createNamedQuery("ClassCountEntity.count", ClassCountEntity.class);
		query.setMaxResults(onPage);
		return query.getResultList();
	}
	/**
	 * Получить топ имени используемых машин
	 * @param onPage - Из скольки профилей будет состоять топ
	 * @author Vadimka
	 */
	public List<CarNameEntity> getTopCarName(int onPage) {
		TypedQuery<CarNameEntity> query = entityManager.createNamedQuery("CarNameEntity.mostPopular", CarNameEntity.class);
		query.setMaxResults(onPage);
		return query.getResultList();
	}
	
	public CustomCarEntity findById(Long id) {
		TypedQuery<CustomCarEntity> query = entityManager.createNamedQuery("CustomCarEntity.findById", CustomCarEntity.class);
		query.setParameter("id", id);

		List<CustomCarEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}
}
