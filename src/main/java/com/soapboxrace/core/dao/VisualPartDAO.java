package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.VinylProductEntity;
import com.soapboxrace.core.jpa.VisualPartEntity;

@Stateless
public class VisualPartDAO extends BaseDAO<VisualPartEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void deleteByCustomCar(CustomCarEntity customCarEntity) {
		Query query = entityManager.createNamedQuery("VisualPartEntity.deleteByCustomCar");
		query.setParameter("customCar", customCarEntity);
		query.executeUpdate();
	}
	
	public void deleteHiddenItems(CustomCarEntity customCarEntity) {
		Query query = entityManager.createNamedQuery("VisualPartEntity.deleteHiddenItems");
		query.setParameter("customCar", customCarEntity);
		query.executeUpdate();
	}
	
	public VisualPartEntity findCopLightsPart(CustomCarEntity customCarEntity) {
		TypedQuery<VisualPartEntity> query = entityManager.createNamedQuery("VisualPartEntity.findCopLightsPart", VisualPartEntity.class);
		query.setParameter("customCar", customCarEntity);

		List<VisualPartEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}

}
