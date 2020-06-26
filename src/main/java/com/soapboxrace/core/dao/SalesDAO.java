package com.soapboxrace.core.dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.SalesEntity;

@Stateless
public class SalesDAO extends BaseDAO<SalesEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public SalesEntity findById(Long id) {
		return entityManager.find(SalesEntity.class, id);
	}

}
