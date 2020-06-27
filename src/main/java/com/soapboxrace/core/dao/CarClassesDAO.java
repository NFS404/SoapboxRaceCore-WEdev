package com.soapboxrace.core.dao;

import java.math.BigInteger;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;

@Stateless
public class CarClassesDAO extends BaseDAO<CarClassesEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public CarClassesEntity findById(String productId) {
		return entityManager.find(CarClassesEntity.class, productId);
	}

	public CarClassesEntity findByHash(int hash) {
		TypedQuery<CarClassesEntity> query = entityManager.createQuery("SELECT obj FROM CarClassesEntity obj WHERE obj.hash = :hash", CarClassesEntity.class);
		query.setParameter("hash", hash);
		try {
			return query.getSingleResult();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	public CarClassesEntity findByStoreName(String customCarName) {
		TypedQuery<CarClassesEntity> query = entityManager.createQuery("SELECT obj FROM CarClassesEntity obj WHERE obj.storeName = :storeName", CarClassesEntity.class);
		query.setParameter("storeName", customCarName);
		try {
			return query.getSingleResult();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	public String getFullCarName(String carName) {
		Query query = entityManager.createNativeQuery(
			"SELECT full_name from car_classes WHERE store_name = '"+carName+"'");
		@SuppressWarnings("unchecked")
		List<String> fullCarName = query.getResultList();
		if (fullCarName.isEmpty()) {
			return "temp350";
		}
		return fullCarName.get(0); 
	}
}
