package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.NewsArticlesEntity;

@Stateless
public class NewsArticlesDAO extends BaseDAO<NewsArticlesEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public NewsArticlesEntity findById(int id) {
		return entityManager.find(NewsArticlesEntity.class, id);
	}

	public NewsArticlesEntity findByShortText(String shortTextHALId) {
		TypedQuery<NewsArticlesEntity> query = entityManager.createNamedQuery("NewsArticlesEntity.findByShortText", NewsArticlesEntity.class);
		query.setParameter("shortTextHALId", shortTextHALId);
		return query.getSingleResult();
	}
	
	public List<NewsArticlesEntity> loadCommon() {
		TypedQuery<NewsArticlesEntity> query = entityManager.createNamedQuery("NewsArticlesEntity.loadCommon", NewsArticlesEntity.class);
		return query.getResultList();
	}
	
	// Custom names for persistent news articles
	public NewsArticlesEntity findByName(String name) {
		TypedQuery<NewsArticlesEntity> query = entityManager.createNamedQuery("NewsArticlesEntity.findByName", NewsArticlesEntity.class);
		query.setParameter("name", name);
		return query.getSingleResult();
	}

}
