package com.soapboxrace.core.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.AchievementRankEntity;
import com.soapboxrace.core.jpa.ProductEntity;
import com.soapboxrace.core.jpa.ProductType;
import com.soapboxrace.core.jpa.RewardDropEntity;

@Stateless
public class RewardDropDAO extends BaseDAO<RewardDropEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public RewardDropEntity findById(Long id) {
		return entityManager.find(RewardDropEntity.class, id);
	}

	public List<RewardDropEntity> getRewardDrops(AchievementRankEntity achievementRank, boolean isCardPack) {
		List<RewardDropEntity> rewardDropList = new ArrayList<>();
		StringBuilder sqlWhere = new StringBuilder();
		sqlWhere.append(" WHERE obj.achievementRank = :achievementRank ");

		StringBuilder sqlCount = new StringBuilder();
		sqlCount.append("SELECT COUNT(*) FROM RewardDropEntity obj ");
		sqlCount.append(sqlWhere.toString());

		Query countQuery = entityManager.createQuery(sqlCount.toString());
		countQuery.setParameter("achievementRank", achievementRank);
		Long count = (Long) countQuery.getSingleResult();

		StringBuilder sqlProduct = new StringBuilder();
		sqlProduct.append("SELECT obj FROM RewardDropEntity obj");
		sqlProduct.append(sqlWhere.toString());

		TypedQuery<RewardDropEntity> rewardDropQuery = entityManager.createQuery(sqlProduct.toString(), RewardDropEntity.class);
		rewardDropQuery.setParameter("achievementRank", achievementRank);

		int number = count.intValue();
		Random random = new Random();
		rewardDropQuery.setMaxResults(1);
		if (isCardPack) {
			int max = Math.max(1, achievementRank.getNumberOfRewards());
			for (int i = 0; i < max; i++) {
				number = random.nextInt(count.intValue());
				rewardDropQuery.setFirstResult(number);
				rewardDropList.add(rewardDropQuery.getSingleResult());
			}
		} else {
			number = random.nextInt(count.intValue());
			rewardDropQuery.setFirstResult(number);
			rewardDropList.add(rewardDropQuery.getSingleResult());
		}
		return rewardDropList;
	}
	
	public List<ProductEntity> getBundleDrops(String productType) {
		List<ProductEntity> productList = new ArrayList<>();
		StringBuilder sqlWhere = new StringBuilder();
		sqlWhere.append(" WHERE obj.productType = :productType AND obj.isDropableMode <> 0");

		StringBuilder sqlCount = new StringBuilder();
		sqlCount.append("SELECT COUNT(*) FROM ProductEntity obj ");
		sqlCount.append(sqlWhere.toString());

		Query countQuery = entityManager.createQuery(sqlCount.toString());
		countQuery.setParameter("productType", productType);
		Long count = (Long) countQuery.getSingleResult();

		StringBuilder sqlProduct = new StringBuilder();
		sqlProduct.append("SELECT obj FROM ProductEntity obj");
		sqlProduct.append(sqlWhere.toString());

		TypedQuery<ProductEntity> productQuery = entityManager.createQuery(sqlProduct.toString(), ProductEntity.class);
		productQuery.setParameter("productType", productType);

		int number = count.intValue();
		Random random = new Random();
		productQuery.setMaxResults(1);
		
		boolean isPackReduced = random.nextBoolean();
		int cardAmount = (isPackReduced ? 5 : 3);
		
		int max = Math.max(1, cardAmount);
		for (int i = 0; i < max; i++) {
			number = random.nextInt(count.intValue());
			productQuery.setFirstResult(number);
			productList.add(productQuery.getSingleResult());
		}
		return productList;
	}
}
