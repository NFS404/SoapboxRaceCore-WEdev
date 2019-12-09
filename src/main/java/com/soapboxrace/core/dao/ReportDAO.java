package com.soapboxrace.core.dao;

import java.math.BigInteger;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.ReportEntity;

@Stateless
public class ReportDAO extends BaseDAO<ReportEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public ReportEntity findById(Long id) {
		return entityManager.find(ReportEntity.class, id);
	}
	/**
	 * Количество репортов на профиль
	 * @param personaID
	 * @author Vadimka
	 */
	public BigInteger countReportsOnPersona(Long personaID) {
		Query query= entityManager.createNativeQuery(
			"SELECT Count(*) from report WHERE abuserpersonaid = "+personaID+" and personaid <> 0 and hacksDetected = 0 and hacksDetected <> 32"
		);
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		BigInteger count = new BigInteger("0");
		if (!List.isEmpty()) count = List.get(0);
		return count;
	}

}
