package com.soapboxrace.core.dao;

import java.math.BigInteger;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.RecordsEntity;
import com.soapboxrace.core.jpa.UserEntity;

@Stateless
public class RecordsDAO extends BaseDAO<RecordsEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public RecordsEntity findCurrentRace(int eventId, Long userId, boolean powerUps, int carClassHash) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.findCurrentRace", RecordsEntity.class);
		query.setParameter("eventId", eventId);
		query.setParameter("userId", userId);
		query.setParameter("powerUps", powerUps);
		query.setParameter("carClassHash", carClassHash);

		List<RecordsEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}

	public RecordsEntity getWRRecord(int eventId, Long userId, boolean powerUps, int carClassHash, Long timeMS) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.calcRecordPlace", RecordsEntity.class);
		query.setParameter("eventId", eventId);
		query.setParameter("powerUps", powerUps);
		query.setParameter("carClassHash", carClassHash);
		query.setParameter("timeMS", timeMS);
		query.setMaxResults(1);

		List<RecordsEntity> resultList = query.getResultList();
		return resultList.get(0);
	}
	
	public BigInteger countRecordPlace(int eventId, Long userId, boolean powerUps, int carClassHash, Long timeMS) {
		Query query = entityManager.createNativeQuery(
			"SELECT Count(*) from records WHERE eventId = "+eventId+" and powerUps = "+powerUps+" and carClassHash = "+carClassHash+" and timeMS < "+timeMS
		);
		BigInteger count;
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		if (!List.isEmpty()) {
		  count = List.get(0);
		  count = count.add(BigInteger.valueOf(1));
		} else {
		  count = BigInteger.valueOf(1);
		}
		return count; // 0 means 1st place
	}
	
	public void banRecords(Long userId) {
		Query createQuery = entityManager.createQuery("UPDATE RecordsEntity obj SET obj.userBan = true WHERE obj.userId = :userId");
		createQuery.setParameter("userId", userId);
		createQuery.executeUpdate();
	}
	
	public void unbanRecords(Long userId) {
		Query createQuery = entityManager.createQuery("UPDATE RecordsEntity obj SET obj.userBan = false WHERE obj.userId = :userId");
		createQuery.setParameter("userId", userId);
		createQuery.executeUpdate();
	}
}
