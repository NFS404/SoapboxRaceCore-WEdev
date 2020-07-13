package com.soapboxrace.core.dao;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.ProductEntity;

@Stateless
public class ProductDAO extends BaseDAO<ProductEntity> {
	
	@EJB
	private ParameterBO parameterBO;

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public ProductEntity findById(Long id) {
		return entityManager.find(ProductEntity.class, id);
	}

	public List<ProductEntity> findByLevelEnabled(String categoryName, String productType, int minLevel, boolean enabled, boolean premium, boolean pFull, boolean isModder) {
		TypedQuery<ProductEntity> query = entityManager.createNamedQuery("ProductEntity.findByLevelEnabled", ProductEntity.class);
		query.setParameter("categoryName", categoryName);
		query.setParameter("productType", productType);
		query.setParameter("enabled", enabled);
		query.setParameter("minLevel", minLevel);
		query.setParameter("premium", premium);
		query.setParameter("pFull", pFull);
		query.setParameter("modder", isModder);
		return query.getResultList();
	}

	public List<ProductEntity> findForEndRace(String categoryName, String productType, int level) {
		TypedQuery<ProductEntity> query = entityManager.createNamedQuery("ProductEntity.findForEndRace", ProductEntity.class);
		query.setParameter("categoryName", categoryName);
		query.setParameter("productType", productType);
		query.setParameter("level", level);
		query.setParameter("isDropableMode", 1); // Not used
		return query.getResultList();
	}

	public ProductEntity findByProductId(String productId) {
		TypedQuery<ProductEntity> query = entityManager.createNamedQuery("ProductEntity.findByProductId", ProductEntity.class);
		query.setParameter("productId", productId);

		List<ProductEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}
	
	public ProductEntity findByLongDesc(String name) {
		TypedQuery<ProductEntity> query = entityManager.createNamedQuery("ProductEntity.findByLongDesc", ProductEntity.class);
		query.setParameter("longDescription", name);

		List<ProductEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}

	public ProductEntity findByHash(Integer hash) {
		TypedQuery<ProductEntity> query = entityManager.createNamedQuery("ProductEntity.findByHash", ProductEntity.class);
		query.setParameter("hash", hash);

		List<ProductEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}

	public ProductEntity getRandomDrop(String productType, int isDropableMode, boolean isTeamRace) {
		StringBuilder sqlWhere = new StringBuilder();
		// 1 - main drop items (1 + 3), 2 - main + rare (1 + 2 + 3), 3 - weak drop items (3), 4 - card-packs only
		if (isDropableMode == 1) {
			sqlWhere.append(" WHERE obj.isDropableMode <> 0 AND obj.isDropableMode <> 2 AND obj.isDropableMode <> 4 ");
		}
		if (isDropableMode == 2) {
			sqlWhere.append(" WHERE obj.isDropableMode <> 0 AND obj.isDropableMode <> 4");
		}
		if (isDropableMode == 3) {
			sqlWhere.append(" WHERE obj.isDropableMode <> 0 AND obj.isDropableMode <> 1 AND obj.isDropableMode <> 2 AND obj.isDropableMode <> 4");
		}
		sqlWhere.append(" AND obj.productType= :productType");

		StringBuilder sqlCount = new StringBuilder();
		sqlCount.append("SELECT COUNT(*) FROM ProductEntity obj ");
		sqlCount.append(sqlWhere.toString());

		Query countQuery = entityManager.createQuery(sqlCount.toString());
		countQuery.setParameter("productType", productType);
		Long count = (Long) countQuery.getSingleResult();

//		SecureRandom randomS = new SecureRandom(); // Important to have a more "fair" random for SB-drops
		Random random = new Random();
		int number = random.nextInt(count.intValue());
//		int isSBCard = 0;
//		if (isTeamRace) {
//			isSBCard = randomS.nextInt(12); // Chance to got the SpeedBoost card IF value is 1
//			if (isSBCard == 5) {
//				// FIXME Hard-coded item, should be in Product table
//				ProductEntity speedBoostProduct = new ProductEntity();
//				int sbAmount = parameterBO.getIntParam("REWARD_SB_AMOUNT");
//				speedBoostProduct.setProductTitle(sbAmount + " SPEEDBOOST");
//				speedBoostProduct.setHash(723701634);
//				speedBoostProduct.setIcon("package_4_3");
//				speedBoostProduct.setUseCount(1);
//				speedBoostProduct.setResalePrice(0);
//				speedBoostProduct.setProductType("REWARD");
//				return speedBoostProduct;
//			}
//		}

		StringBuilder sqlProduct = new StringBuilder();
		sqlProduct.append("SELECT obj FROM ProductEntity obj");
		sqlProduct.append(sqlWhere.toString());

		TypedQuery<ProductEntity> productQuery = entityManager.createQuery(sqlProduct.toString(), ProductEntity.class);
		productQuery.setParameter("productType", productType);

		productQuery.setFirstResult(number);
		productQuery.setMaxResults(1);
		return productQuery.getSingleResult();
	}

}
