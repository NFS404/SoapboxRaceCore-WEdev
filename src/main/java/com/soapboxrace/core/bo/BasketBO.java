package com.soapboxrace.core.bo;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.bo.util.OwnedCarConverter;
import com.soapboxrace.core.bo.util.RewardDestinyType;
import com.soapboxrace.core.bo.util.RewardType;
import com.soapboxrace.core.dao.AchievementRankDAO;
import com.soapboxrace.core.dao.BasketDefinitionDAO;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CarSlotDAO;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.InventoryDAO;
import com.soapboxrace.core.dao.InventoryItemDAO;
import com.soapboxrace.core.dao.OwnedCarDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ProductDAO;
import com.soapboxrace.core.dao.RewardDropDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.TreasureHuntDAO;
import com.soapboxrace.core.dao.VisualPartDAO;
import com.soapboxrace.core.jpa.AchievementRankEntity;
import com.soapboxrace.core.jpa.AchievementStateEntity;
import com.soapboxrace.core.jpa.BasketDefinitionEntity;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CarSlotEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.InventoryEntity;
import com.soapboxrace.core.jpa.InventoryItemEntity;
import com.soapboxrace.core.jpa.OwnedCarEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.ProductEntity;
import com.soapboxrace.core.jpa.ProductType;
import com.soapboxrace.core.jpa.RewardDropEntity;
import com.soapboxrace.core.jpa.TreasureHuntEntity;
import com.soapboxrace.jaxb.http.AchievementRewards;
import com.soapboxrace.jaxb.http.AchievementState;
import com.soapboxrace.jaxb.http.ArrayOfCommerceItemTrans;
import com.soapboxrace.jaxb.http.ArrayOfInventoryItemTrans;
import com.soapboxrace.jaxb.http.ArrayOfOwnedCarTrans;
import com.soapboxrace.jaxb.http.ArrayOfWalletTrans;
import com.soapboxrace.jaxb.http.CommerceItemTrans;
import com.soapboxrace.jaxb.http.CommerceResultStatus;
import com.soapboxrace.jaxb.http.CommerceResultTrans;
import com.soapboxrace.jaxb.http.InvalidBasketTrans;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
import com.soapboxrace.jaxb.http.WalletTrans;
import com.soapboxrace.jaxb.util.UnmarshalXML;

@Stateless
public class BasketBO {

	@EJB
	private PersonaBO personaBo;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private BasketDefinitionDAO basketDefinitionsDAO;

	@EJB
	private CarSlotDAO carSlotDAO;

	@EJB
	private OwnedCarDAO ownedCarDAO;

	@EJB
	private CustomCarDAO customCarDAO;

	@EJB
	private TokenSessionDAO tokenDAO;

	@EJB
	private ProductDAO productDao;

	@EJB
	private PersonaDAO personaDao;

	@EJB
	private TokenSessionBO tokenSessionBO;

	@EJB
    private TreasureHuntDAO treasureHuntDAO;
	
	@EJB
	private InventoryDAO inventoryDao;

	@EJB
	private InventoryItemDAO inventoryItemDao;
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private VisualPartDAO visualPartDAO;
	
	@EJB
	private RewardDropDAO rewardDropDAO;

	@EJB
	private DropBO dropBO;
	
	@EJB
	private InventoryBO inventoryBO;
	
	@EJB
	private AchievementRankDAO achievementRankDAO;

	private OwnedCarTrans getCar(String productId) {
		BasketDefinitionEntity basketDefinitonEntity = basketDefinitionsDAO.findById(productId);
		if (basketDefinitonEntity == null) {
			throw new IllegalArgumentException(String.format("No basket definition for %s", productId));
		}
		String ownedCarTrans = basketDefinitonEntity.getOwnedCarTrans();
		return UnmarshalXML.unMarshal(ownedCarTrans, OwnedCarTrans.class);
	}

	public CommerceResultStatus repairCar(String productId, PersonaEntity personaEntity) {
		CarSlotEntity defaultCarEntity = personaBo.getDefaultCarEntity(personaEntity.getPersonaId());
		int price = (int) (productDao.findByProductId(productId).getPrice() * (100 - defaultCarEntity.getOwnedCar().getDurability()));
		if (personaEntity.getCash() < price) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
		}
		if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
			personaEntity.setCash(personaEntity.getCash() - price);
		}
		personaDao.update(personaEntity);

		defaultCarEntity.getOwnedCar().setDurability(100);

		carSlotDAO.update(defaultCarEntity);
		return CommerceResultStatus.SUCCESS;
	}
	
	public CommerceResultStatus restoreTreasureHunt(String productId, PersonaEntity personaEntity) {
        int price = (int) productDao.findByProductId(productId).getPrice();

        if(personaEntity.getCash() < price) {
            return CommerceResultStatus.FAIL_LOCKED_PRODUCT_NOT_ACCESSIBLE_TO_THIS_USER;
        }

        if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
            personaEntity.setCash(personaEntity.getCash() - price);
        }

        Long personaId = personaEntity.getPersonaId();
        TreasureHuntEntity treasureHuntEntity = treasureHuntDAO.findById(personaId);
        treasureHuntEntity.setIsStreakBroken(false);
        treasureHuntDAO.update(treasureHuntEntity);
        personaDao.update(personaEntity);

        return CommerceResultStatus.SUCCESS;
    }

	public CommerceResultStatus buyPowerups(String productId, PersonaEntity personaEntity) {
		if (!parameterBO.getBoolParam("ENABLE_ECONOMY")) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
		}
		if (!parameterBO.getBoolParam("ENABLE_POWERUP_PURCHASE")) {
			return CommerceResultStatus.FAIL_INVALID_BASKET;
		}
		ProductEntity powerupProduct = productDao.findByProductId(productId);
		InventoryEntity inventoryEntity = inventoryDao.findByPersonaId(personaEntity.getPersonaId());

		if (powerupProduct == null) {
			return CommerceResultStatus.FAIL_INVALID_BASKET;
		}

		if (personaEntity.getCash() < powerupProduct.getPrice()) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
		}

		InventoryItemEntity item = null;

		for (InventoryItemEntity i : inventoryEntity.getItems()) {
			if (i.getHash().equals(powerupProduct.getHash().intValue())) {
				item = i;
				break;
			}
		}

		if (item == null) {
			return CommerceResultStatus.FAIL_INVALID_BASKET;
		}

		boolean upgradedAmount = false;

		int newUsageCount = item.getRemainingUseCount() + 15;

		if (newUsageCount > 99)
			newUsageCount = 99;

		if (item.getRemainingUseCount() != newUsageCount)
			upgradedAmount = true;

		item.setRemainingUseCount(newUsageCount);
		inventoryItemDao.update(item);

		if (upgradedAmount) {
			personaEntity.setCash(personaEntity.getCash() - powerupProduct.getPrice());
			personaDao.update(personaEntity);
		}

		return CommerceResultStatus.SUCCESS;
	}

	public CommerceResultStatus buyCar(String productId, PersonaEntity personaEntity, String securityToken) {
		if (getPersonaCarCount(personaEntity.getPersonaId()) >= personaEntity.getCarSlots()) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_CAR_SLOTS;
		}

		ProductEntity productEntity = productDao.findByProductId(productId);
		if (productEntity == null || personaEntity.getCash() < productEntity.getPrice()) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
		}
		
		OwnedCarTrans ownedCarTrans = getCar(productId);
		ownedCarTrans.setId(0L);
		ownedCarTrans.getCustomCar().setId(0);
		CarSlotEntity carSlotEntity = new CarSlotEntity();
		carSlotEntity.setPersona(personaEntity);

		OwnedCarEntity ownedCarEntity = new OwnedCarEntity();
		ownedCarEntity.setCarSlot(carSlotEntity);
		CustomCarEntity customCarEntity = new CustomCarEntity();
		customCarEntity.setOwnedCar(ownedCarEntity);
		ownedCarEntity.setCustomCar(customCarEntity);
		carSlotEntity.setOwnedCar(ownedCarEntity);
		OwnedCarConverter.trans2Entity(ownedCarTrans, ownedCarEntity);
		OwnedCarConverter.details2NewEntity(ownedCarTrans, ownedCarEntity);

		carSlotDAO.insert(carSlotEntity);

		if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
			personaEntity.setCash(personaEntity.getCash() - productEntity.getPrice());
		}
		personaDao.update(personaEntity);

		personaBo.changeDefaultCar(personaEntity.getPersonaId(), carSlotEntity.getOwnedCar().getId());
		if (parameterBO.getBoolParam("DISABLE_ITEM_AFTER_BUY")) {
			productEntity.setEnabled(false);
			productDao.update(productEntity);
		}
		return CommerceResultStatus.SUCCESS;
	}
	
	// Re-used code from achievements reward drops
	// FIXME Add the cash & cars drop ability
	public CommerceResultStatus buyCardPack(String productId, PersonaEntity personaEntity, CommerceResultTrans commerceResultTrans) {
        ProductEntity bundleProduct = productDao.findByProductId(productId);
        if (bundleProduct == null) {
            return CommerceResultStatus.FAIL_INVALID_BASKET;
        }
        if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
        	float bundlePrice = bundleProduct.getPrice();
        	if (personaEntity.getCash() < bundlePrice) {
	        	return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
			}
			personaEntity.setCash(personaEntity.getCash() - bundlePrice);
		}
		personaDao.update(personaEntity);
       
        ArrayOfCommerceItemTrans arrayOfCommerceItemTrans = new ArrayOfCommerceItemTrans();
        commerceResultTrans.setCommerceItems(arrayOfCommerceItemTrans);
		List<CommerceItemTrans> commerceItems = new ArrayList<>();
		String cardPackType = "";
		switch (productId) {
		case "SRV-CARDPACK1":
			cardPackType = "VISUALPART";
			break;
		case "SRV-CARDPACK2":
			cardPackType = "PERFORMANCEPART";
			break;
		case "SRV-CARDPACK3":
			cardPackType = "SKILLMODPART";
			break;
		}
		List<ProductEntity> productDrops = rewardDropDAO.getBundleDrops(cardPackType);
		Collections.shuffle(productDrops);
		for (ProductEntity productDropEntity : productDrops) {
			CommerceItemTrans item = new CommerceItemTrans();
//			if (productDropEntity.getProductType().contentEquals("CASH")) { // Not used
//				float cashDrop = productDropEntity.getPrice();
//				personaEntity.setCash(personaEntity.getCash() + cashDrop);
//				personaDao.update(personaEntity);
//				item.setHash(-429893590);
//				String moneyFormat = NumberFormat.getNumberInstance(Locale.US).format(cashDrop);
//				item.setTitle("$" + moneyFormat);
//			}
//			else {
				String productTitle = productDropEntity.getProductTitle();
				String title = productTitle.replace(" x15", "");
				item.setHash(productDropEntity.getHash());
				item.setTitle(title);
				productDropEntity.setUseCount(1);
				inventoryBO.addDroppedItem(productDropEntity, personaEntity);
//			}
			commerceItems.add(item);
		}
		arrayOfCommerceItemTrans.getCommerceItemTrans().addAll(commerceItems);
		commerceResultTrans.setCommerceItems(arrayOfCommerceItemTrans);
		return CommerceResultStatus.SUCCESS;
    }
	
	// Gives a random available stock car, 25% chance
	// FIXME Replace the arrays to normal car-list load
	public CommerceResultStatus buyCarRandom(String productId, PersonaEntity personaEntity, String securityToken) {
		if (getPersonaCarCount(personaEntity.getPersonaId()) >= personaEntity.getCarSlots()) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_CAR_SLOTS;
		}

		ProductEntity productEntity = productDao.findByProductId(productId);
		if (productEntity == null || personaEntity.getCash() < productEntity.getPrice()) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
		}
		Random rand = new Random();
		boolean isGoodRange = false;
		String randomProductId = "";
		String[] productIdGoodArray = { "SRV-CAR24","SRV-CAR370","SRV-CAR125","SRV-CAR307","SRV-CAR104","SRV-CAR111","SRV-CAR94","SRV-CAR3",
				"SRV-CAR51","SRV-CAR52","SRV-CAR162","SRV-CAR20","SRV-CAR130","SRV-CAR147","SRV-CAR1","SRV-CAR114","SRV-CAR365","SRV-CAR163",
				"SRV-CAR351","SRV-CAR368","SRV-CAR99","SRV-CAR133","SRV-CAR179","SRV-CAR131","SRV-CAR48","SRV-FCAR5","SRV-CAR34","SRV-CAR152",
				"SRV-CAR367","SRV-CAR71","SRV-CAR144","SRV-CAR107","SRV-CAR78","SRV-CAR85","SRV-CAR68","SRV-CAR134","SRV-CAR167","SRV-CAR274",
				"SRV-CAR7","SRV-CAR54","SRV-CAR12","SRV-CAR41","SRV-CAR136","SRV-CAR79","SRV-CAR364","SRV-CAR153","SRV-CAR116","SRV-CAR47",
				"SRV-CAR355","SRV-CAR105","SRV-CAR73","SRV-CAR160","SRV-CAR62","SRV-CAR83","SRV-CAR45","SRV-CAR14","SRV-CAR22","SRV-CAR28",
				"SRV-CAR100","SRV-CAR137","SRV-CAR91","SRV-CAR172","SRV-CAR35","SRV-CAR126","SRV-CAR26","SRV-CAR123","SRV-CAR13","SRV-CAR33",
				"SRV-FCAR4","SRV-CAR170","SRV-CAR59","SRV-CAR5","SRV-CAR135","SRV-CAR86","SRV-CAR57","SRV-FCAR0","SRV-CAR109","SRV-CAR46",
				"SRV-CAR98","SRV-CAR97","SRV-CAR56","SRV-CAR305","SRV-CAR306","SRV-CAR119","SRV-CAR375","SRV-FCAR6","SRV-FCAR1","SRV-FCAR28",
				"SRV-BRERA1","SRV-CAR239" }; // "SRV-FCAR11" - MW05 cop
		String[] productIdBadArray = { "SRV-CAR40","SRV-CAR151","SRV-CAR165","SRV-CAR143","SRV-CAR154","SRV-CAR173","SRV-CAR141","SRV-CAR142",
				"SRV-CAR181","SRV-CAR159","SRV-CAR121","SRV-CAR96","SRV-CAR93","SRV-CAR169","SRV-CAR127","SRV-CAR81","SRV-CAR21","SRV-CAR148",
				"SRV-CAR155","SRV-CAR11","SRV-CAR18","SRV-CAR164","SRV-CAR53","SRV-CAR139","SRV-CAR6","SRV-CAR65","SRV-CAR158","SRV-CAR166",
				"SRV-CAR76","SRV-CAR74","SRV-CAR176","SRV-CAR149","SRV-CAR178","SRV-CAR182","SRV-CAR69","SRV-CAR115","SRV-CAR108","SRV-CAR171",
				"SRV-CAR31","SRV-CAR38","SRV-FCAR3","SRV-CAR113","SRV-CAR25","SRV-CAR174","SRV-CAR49","SRV-CAR112","SRV-CAR23","SRV-CAR129",
				"SRV-CAR16","SRV-CAR95","SRV-CAR156","SRV-CAR101","SRV-CAR117","SRV-CAR75","SRV-CAR17","SRV-CAR58","SRV-CAR180","SRV-CAR168",
				"SRV-CAR146","SRV-CAR4","SRV-CAR82","SRV-CAR64","SRV-CAR60","SRV-FCAR24","SRV-CAR61","SRV-FCAR2","SRV-CAR92","SRV-FCAR26",
				"SRV-CAR177","SRV-CAR145","SRV-CAR157","SRV-CAR36","SRV-CAR175","SRV-CAR161","SRV-FCAR29" };
		isGoodRange = rand.nextBoolean();
		// Cutting down the chance from 50% to 25%
		if (isGoodRange) {
			isGoodRange = rand.nextBoolean();
		}
		// And we get...
		if (isGoodRange) {
			randomProductId = productIdGoodArray[rand.nextInt(productIdGoodArray.length)];
		}
		if (!isGoodRange) {
			randomProductId = productIdBadArray[rand.nextInt(productIdBadArray.length)];
		}
		if (randomProductId.contentEquals("SRV-BRERA1") && !parameterBO.getBoolParam("ITEM_LOOTBOX_BRERA")) { // Alfa Romeo Brera is supposed to be a rare car
			return CommerceResultStatus.FAIL_INVALID_BASKET;
		}
		OwnedCarTrans ownedCarTrans = getCar(randomProductId);
		ownedCarTrans.setId(0L);
		ownedCarTrans.getCustomCar().setId(0);
		CarSlotEntity carSlotEntity = new CarSlotEntity();
		carSlotEntity.setPersona(personaEntity);

		OwnedCarEntity ownedCarEntity = new OwnedCarEntity();
		ownedCarEntity.setCarSlot(carSlotEntity);
		CustomCarEntity customCarEntity = new CustomCarEntity();
		
		customCarEntity.setOwnedCar(ownedCarEntity);
		ownedCarEntity.setCustomCar(customCarEntity);
		carSlotEntity.setOwnedCar(ownedCarEntity);
		OwnedCarConverter.trans2Entity(ownedCarTrans, ownedCarEntity);
		OwnedCarConverter.details2NewEntity(ownedCarTrans, ownedCarEntity);

		carSlotDAO.insert(carSlotEntity);

		if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
			personaEntity.setCash(personaEntity.getCash() - productEntity.getPrice());
		}
		personaDao.update(personaEntity);
		personaBo.changeDefaultCar(personaEntity.getPersonaId(), carSlotEntity.getOwnedCar().getId());
		
		String playerName = personaEntity.getName();
		CarClassesEntity randomCarEntity = carClassesDAO.findByHash(customCarEntity.getPhysicsProfileHash());
		String message = ":heavy_minus_sign:"
        		+ "\n:shopping_cart: **|** Nгрок **" + playerName + "** купил контейнер с автомобилем и получил **" + randomCarEntity.getFullName() + "**!"
        		+ "\n:shopping_cart: **|** Player **" + playerName + "** has bought the car container and got a **" + randomCarEntity.getFullName() + "**!";
		discordBot.sendMessage(message);

		return CommerceResultStatus.SUCCESS;
	}

	public int getPersonaCarCount(Long personaId) {
		return getPersonasCar(personaId).size();
	}

	public List<CarSlotEntity> getPersonasCar(Long personaId) {
		List<CarSlotEntity> findByPersonaId = carSlotDAO.findByPersonaId(personaId);
		for (CarSlotEntity carSlotEntity : findByPersonaId) {
			CustomCarEntity customCar = carSlotEntity.getOwnedCar().getCustomCar();
			customCar.getPaints().size();
			customCar.getPerformanceParts().size();
			customCar.getSkillModParts().size();
			customCar.getVisualParts().size();
			customCar.getVinyls().size();
		}
		return findByPersonaId;
	}

	public boolean sellCar(String securityToken, Long personaId, Long serialNumber) {
		this.tokenSessionBO.verifyPersona(securityToken, personaId);

		OwnedCarEntity ownedCarEntity = ownedCarDAO.findById(serialNumber);
		if (ownedCarEntity == null) {
			return false;
		}
		CarSlotEntity carSlotEntity = ownedCarEntity.getCarSlot();
		if (carSlotEntity == null) {
			return false;
		}
		int personaCarCount = getPersonaCarCount(personaId);
		if (personaCarCount <= 1) {
			return false;
		}

		PersonaEntity personaEntity = personaDao.findById(personaId);

		final int maxCash = parameterBO.getMaxCash(securityToken);
		if (personaEntity.getCash() < maxCash) {
			int cashTotal = (int) (personaEntity.getCash() + ownedCarEntity.getCustomCar().getResalePrice());
			if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
				personaEntity.setCash(Math.max(0, Math.min(maxCash, cashTotal)));
			}
		}

		CarSlotEntity defaultCarEntity = personaBo.getDefaultCarEntity(personaId);

		int curCarIndex = personaEntity.getCurCarIndex();
		if (defaultCarEntity.getId().equals(carSlotEntity.getId())) {
			curCarIndex = 0;
		} else {
			List<CarSlotEntity> personasCar = personaBo.getPersonasCar(personaId);
			int curCarIndexTmp = curCarIndex;
			for (int i = 0; i < curCarIndexTmp; i++) {
				if (personasCar.get(i).getId().equals(carSlotEntity.getId())) {
					curCarIndex--;
					break;
				}
			}
		}
		carSlotDAO.delete(carSlotEntity);
		personaEntity.setCurCarIndex(curCarIndex);
		personaDao.update(personaEntity);
		return true;
	}

}
