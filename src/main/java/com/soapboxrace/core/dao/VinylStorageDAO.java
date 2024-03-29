package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.VinylStorageEntity;

@Stateless
public class VinylStorageDAO extends BaseDAO<VinylStorageEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public VinylStorageEntity findByCode(String code) {
		TypedQuery<VinylStorageEntity> query = entityManager.createNamedQuery("VinylStorageEntity.findByCode", VinylStorageEntity.class);
		query.setParameter("code", code);

		List<VinylStorageEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}
	
	public void deleteAllVinyls(Long userId) {
		Query query = entityManager.createNamedQuery("VinylStorageEntity.deleteAllVinyls");
		query.setParameter("userId", userId);
		query.executeUpdate();
	}

}
