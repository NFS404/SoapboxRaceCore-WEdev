package com.soapboxrace.core.dao;

import java.math.BigInteger;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.RecordsEntity;
import com.soapboxrace.core.jpa.UserEntity;

@Stateless
public class RecordsDAO extends BaseDAO<RecordsEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public RecordsEntity findCurrentRace(EventEntity event, UserEntity user, boolean powerUps, int carClassHash) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.findCurrentRace", RecordsEntity.class);
		query.setParameter("event", event);
		query.setParameter("user", user);
		query.setParameter("powerUps", powerUps);
		query.setParameter("carClassHash", carClassHash);

		List<RecordsEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}

	public RecordsEntity getWRRecord(EventEntity event, boolean powerUps, int carClassHash, Long timeMS) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.calcRecordPlace", RecordsEntity.class);
		query.setParameter("event", event);
		query.setParameter("powerUps", powerUps);
		query.setParameter("carClassHash", carClassHash);
		query.setParameter("timeMS", timeMS);
		query.setMaxResults(1);

		List<RecordsEntity> resultList = query.getResultList();
		return resultList.get(0);
	}
	
	public BigInteger countRecordPlace(int eventId, boolean powerUps, int carClassHash, Long timeMS) {
		Query query = entityManager.createNativeQuery(
			"SELECT Count(*) from records WHERE eventId = "+eventId+" and powerUps = "+powerUps+" and carClassHash = "+carClassHash
					+ "and timeMS < "+timeMS+" and userBan = false"
		);
		BigInteger count;
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		count = List.get(0);
		count = count.add(BigInteger.valueOf(1));
		return count; // 0 means 1st place
	}
	
	public BigInteger countRecords(int eventId, boolean powerUps, int carClassHash) {
		Query query = entityManager.createNativeQuery(
			"SELECT Count(*) from records WHERE eventId = "+eventId+" and powerUps = "+powerUps+" and carClassHash = "+carClassHash+" and userBan = false"
		);
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		if (List.isEmpty())
			return new BigInteger("0");
		else return List.get(0);
	}
	
	public BigInteger countRecordsAll(int eventId, boolean powerUps) {
		Query query = entityManager.createNativeQuery(
//			"SELECT Count(DISTINCT userId) from records WHERE eventId = "+eventId+" and powerUps = "+powerUps+" and userBan = false");
			"SELECT Count(*) from records WHERE eventId = "+eventId+" and powerUps = "+powerUps+" and userBan = false");
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		if (List.isEmpty())
			return new BigInteger("0");
		else return List.get(0);
	}
	
	public BigInteger countRecordsPersona(int eventId, boolean powerUps, Long userId) {
		Query query = entityManager.createNativeQuery(
			"SELECT Count(*) from records WHERE eventId = "+eventId+" and userId ="+userId+" and powerUps = "+powerUps);
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		if (List.isEmpty())
			return new BigInteger("0");
		else return List.get(0);
	}
	
	// If some server admin did a manual player unban via DB, and forgot to uncheck the userBan field for him, this player should know about it
	public BigInteger countBannedRecords(Long userId) {
		Query query = entityManager.createNativeQuery(
			"SELECT Count(*) from records WHERE userId = "+userId+" and userBan = true");
		BigInteger count;
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		count = List.get(0);
		return count; 
	}
	
	public void banRecords(UserEntity user) {
		Query createQuery = entityManager.createQuery("UPDATE RecordsEntity obj SET obj.userBan = true WHERE obj.user = :user");
		createQuery.setParameter("user", user);
		createQuery.executeUpdate();
	}
	
	public void unbanRecords(UserEntity user) {
		Query createQuery = entityManager.createQuery("UPDATE RecordsEntity obj SET obj.userBan = false WHERE obj.user = :user");
		createQuery.setParameter("user", user);
		createQuery.executeUpdate();
	}
	
	/**
	 * Получить список лучших заездов по времени в классе
	 * @param eventid - номер трассы
	 * @param powerups - наличие бонусов (true/false)
	 * @param carclasshash - номер класса машин
	 * @param page - Номер страницы
	 * @param onPage - Сколько позиций на странице
	 * @author Vadimka, Hypercycle
	 */
	public List<RecordsEntity> statsEventClass(EventEntity event, boolean powerups, int carClassHash, int page, int onPage) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.statsEventClass",RecordsEntity.class);
		query.setParameter("event", event);
		query.setParameter("powerUps", powerups);
		query.setParameter("carClassHash", carClassHash);
		query.setFirstResult((page-1) * onPage);
		query.setMaxResults(onPage);
		return query.getResultList();
	}
	
	/**
	 * Получить список лучших заездов по времени
	 * @param eventid - номер трассы
	 * @param powerups - наличие бонусов (true/false)
	 * @param page - Номер страницы
	 * @param onPage - Сколько позиций на странице
	 * @author Vadimka, Hypercycle
	 */
	public List<RecordsEntity> statsEventAll(EventEntity event, boolean powerups, int page, int onPage) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.statsEventAll", RecordsEntity.class);
		query.setParameter("event", event);
		query.setParameter("powerUps", powerups);
		query.setFirstResult((page-1) * onPage);
		query.setMaxResults(onPage);
		return query.getResultList();
	}
	
	/**
	 * Получить список лучших заездов во всех вариациях трассы. Фильтрация по имени профиля
	 * @param eventid - номер трассы
	 * @param userId - номер аккаунта игрока
	 * @param page - Номер страницы
	 * @param onPage - Сколько позиций на странице
	 * @author Vadimka, Hypercycle
	 */
	public List<RecordsEntity> statsEventPersona(EventEntity event, boolean powerups, UserEntity userEntity) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.statsEventPersona", RecordsEntity.class);
		query.setParameter("event", event);
		query.setParameter("powerUps", powerups);
		query.setParameter("user", userEntity);
		return query.getResultList();
	}
}
