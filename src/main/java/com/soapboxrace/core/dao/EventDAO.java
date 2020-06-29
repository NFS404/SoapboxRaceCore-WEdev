package com.soapboxrace.core.dao;

import java.math.BigInteger;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.EventEntity;

@Stateless
public class EventDAO extends BaseDAO<EventEntity> {

	@EJB
	private ParameterBO parameterBO;
	
	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EventEntity findById(int id) {
		return entityManager.find(EventEntity.class, id);
	}

	public List<EventEntity> findAll() {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findAll", EventEntity.class);
		return query.getResultList();
	}
	/**
	 * Все включенные эвенты
	 * @author Vadimka
	 */
	public List<EventEntity> findAllEnabled() {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findAllEnabled", EventEntity.class);
		query.setParameter("rotation", parameterBO.getIntParam("ROTATIONID"));
		return query.getResultList();
	}
	/**
	 * Количество всех эвентов
	 * @author Vadimka
	 */
	public BigInteger countAll(boolean all) {
        String sqlQ = "SELECT Count(*) cout FROM event";
        if (!all)
            sqlQ += " WHERE isenabled = true";
        Query query = entityManager.createNativeQuery(sqlQ);
        query.setMaxResults(1);
        @SuppressWarnings("unchecked")
        List<BigInteger> list = query.getResultList();
        if (list.isEmpty()) 
            return new BigInteger("0");
        else
            return list.get(0);
    }
	
	public BigInteger countBestTime(int eventid) {
        Query query = entityManager.createNativeQuery("SELECT Count(*) cout FROM event_data d, customcar cc, persona p "
                + "WHERE d.carid = cc.id AND p.id = d.personaid AND d.eventid = "+eventid);
        query.setMaxResults(1);
        @SuppressWarnings("unchecked")
        List<BigInteger> list = query.getResultList();
        if (list.isEmpty())
            return new BigInteger("0");
        else return list.get(0);
    }
	
	public List<EventEntity> findByLevel(int level) {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findByLevel", EventEntity.class);
		query.setParameter("level", level);
		return query.getResultList();
	}
	
	public List<EventEntity> findByRotation(int level) {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findByRotation", EventEntity.class);
		query.setParameter("level", level);
		query.setParameter("rotation", parameterBO.getIntParam("ROTATIONID"));
		return query.getResultList();
	}

}
