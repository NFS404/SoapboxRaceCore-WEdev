package com.soapboxrace.core.bo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Session;

import org.apache.commons.codec.digest.DigestUtils;

import com.soapboxrace.core.dao.AchievementRankDAO;
import com.soapboxrace.core.dao.AchievementStateDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ProductDAO;
import com.soapboxrace.core.dao.PromoCodeDAO;
import com.soapboxrace.core.dao.SalesDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.AchievementRankEntity;
import com.soapboxrace.core.jpa.AchievementStateEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PromoCodeEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.jaxb.http.AchievementState;

@Stateless
public class PromoCodeBO {

	@EJB
	private PromoCodeDAO promoCodeDao;

	@EJB
	private UserDAO userDao;
	
	@EJB
	private PersonaDAO personaDao;
	
	@EJB
	private AchievementStateDAO achievementStateDao;
	
	@EJB
	private AchievementRankDAO achievementRankDao;
	
	@EJB
	private ProductDAO productDAO;
	
	@EJB
	private SalesDAO salesDAO;
	
	@EJB
	private ParameterBO parameterBO;

	@Resource(mappedName = "java:jboss/mail/Gmail")
	private Session mailSession;

	public String createPromoCode(String codeType) {
		String promoCode = "WE-" + (Long.toHexString(Double.doubleToLongBits(Math.random()))).toUpperCase();
		PromoCodeEntity promoCodeEntity = new PromoCodeEntity();
		promoCodeEntity.setIsUsed(false);
		promoCodeEntity.setPromoCode(promoCode);
		promoCodeEntity.setCodeType(codeType);
		promoCodeDao.insert(promoCodeEntity);
		return "Premium Code (" + codeType + ") is created: " + promoCode;
	}

	private UserEntity checkLogin(String email, String password) {
		password = (DigestUtils.sha1Hex(password));
		if (email != null && !email.isEmpty() && !password.isEmpty()) {
			UserEntity userEntity = userDao.findByEmail(email);
			if (userEntity != null) {
				if (password.equals(userEntity.getPassword())) {
					return userEntity;
				}
			}
		}
		return null;
	}

	public String usePromoCode(String promoCode, String email, String password, String nickname, String token) {
		UserEntity userEntity = null;
		// If user himself activates a code
		if (token == null) {
			userEntity = checkLogin(email, password);
			if (userEntity == null) {
				return "ERROR: invalid email or password";
			}
		}
//		if (userEntity.isPremium()) {
//			return "ERROR: this account already have a premium";
//		}
		PromoCodeEntity promoCodeEntity = promoCodeDao.findByCode(promoCode);
		if (promoCodeEntity == null) {
			return "ERROR: invalid promo code";
		}
		if (promoCodeEntity.getIsUsed()) {
			return "ERROR: this promo code is expired or used";
		}
		
		PersonaEntity personaEntity = personaDao.findByName(nickname);
		if (personaEntity == null) {
			return "ERROR: wrong nickname, please try again";
		}
		if (token != null) {
			userEntity = personaEntity.getUser();
		}
		
		String premiumCodeType = promoCodeEntity.getCodeType();
		int maxCashFreeAcc = parameterBO.getIntParam("MAX_PLAYER_CASH_FREE");
		int maxCashPremiumAcc = parameterBO.getIntParam("MAX_PLAYER_CASH_PREMIUM");
		double premiumMoneyValue = 0;
		double playerInitialCash = personaEntity.getCash();
		double finalValue = 0;
		double cashPreValue = 0;
		double extraMoneyTransit = 0;
		int playerInitialLevel = personaEntity.getLevel();
		int maxLevelCap = parameterBO.getIntParam("MAX_LEVEL");
		// Predefined World Evolved premium types - Hypercycle
		// TODO Kick the player while applying the premium?
		switch (premiumCodeType) {
		    case "powerup":
		    	if (!userEntity.isPremium()) {
		    		premiumAchievementApply(126, personaEntity);
		    	
		    		cashPreValue = personaEntity.getCash();
		    		premiumMoneyValue = 5000000;
		    		finalValue = playerInitialCash + premiumMoneyValue;
		    		if (finalValue > maxCashFreeAcc) {
		    			extraMoneyTransit = (finalValue - maxCashFreeAcc);
		    			finalValue = maxCashFreeAcc;
		    			userEntity.setExtraMoney(userEntity.getExtraMoney() + extraMoneyTransit);
		    		}
		    		personaEntity.setCash(finalValue);
		    		if (playerInitialLevel < 25) {
		    			personaEntity.setLevel(25);
		    		}
		    		personaDao.update(personaEntity);
		    		userEntity.setPremiumType(premiumCodeType);
		    		userDao.update(userEntity);
		    	
		    		promoCodeEntity.setIsUsed(true);
					promoCodeEntity.setUser(userEntity);
					promoCodeDao.update(promoCodeEntity);
					System.out.println("Player " + nickname + " got the Promo Code (Money amount BEFORE: " + cashPreValue + ")");
					return "Power-Up is activated (restart the game), thank you! ;)";
		    	}
		    	return "ERROR: this account is already got a higher Premium";
		    case "base":
		    	premiumAchievementApply(126, personaEntity);
		    	premiumAchievementApply(501, personaEntity);
		    	premiumCarSlots(200, userEntity);
		    	
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumCodeType);
		    	userEntity.setPremiumDate(LocalDate.now());
		    	userDao.update(userEntity);
		    	
				promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code.");
				return "Premium Base is activated (restart the game), thank you! ;)";
		    case "plus":
		    	premiumAchievementApply(126, personaEntity);
		    	premiumAchievementApply(501, personaEntity);
		    	premiumAchievementApply(502, personaEntity);
		    	premiumCarSlots(200, userEntity);
		    	
		    	cashPreValue = personaEntity.getCash();
		    	premiumMoneyValue = 10000000;
	    		finalValue = playerInitialCash + premiumMoneyValue;
	    		if (finalValue > maxCashPremiumAcc) {
	    			extraMoneyTransit = (finalValue - maxCashPremiumAcc);
	    			finalValue = maxCashPremiumAcc;
	    			userEntity.setExtraMoney(userEntity.getExtraMoney() + extraMoneyTransit);
	    		}
	    		personaEntity.setCash(finalValue);
	    		if (playerInitialLevel < 40) {
	    			personaEntity.setLevel(40);
	    		}
	    		personaDao.update(personaEntity);
		    	
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumCodeType);
		    	userEntity.setPremiumDate(LocalDate.now());
		    	userDao.update(userEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code (Money amount BEFORE: " + cashPreValue + ")");
				return "Premium+ is activated (restart the game), thank you! ;)";
		    case "full":
		    	premiumAchievementApply(126, personaEntity);
		    	premiumAchievementApply(501, personaEntity);
		    	premiumAchievementApply(502, personaEntity);
		    	premiumAchievementApply(503, personaEntity);
		    	premiumCarSlots(200, userEntity);
		    	
		    	cashPreValue = personaEntity.getCash();
		    	premiumMoneyValue = 30000000;
	    		finalValue = playerInitialCash + premiumMoneyValue;
	    		if (finalValue > maxCashPremiumAcc) {
	    			extraMoneyTransit = (finalValue - maxCashPremiumAcc);
	    			finalValue = maxCashPremiumAcc;
	    			userEntity.setExtraMoney(userEntity.getExtraMoney() + extraMoneyTransit);
	    		}
	    		personaEntity.setCash(finalValue);
	    		if (playerInitialLevel < 75) {
	    			personaEntity.setLevel(75);
	    		}
		    	personaDao.update(personaEntity);
		    	
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumCodeType);
		    	userEntity.setPremiumDate(LocalDate.now());
		    	userDao.update(userEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code (Money amount BEFORE: " + cashPreValue + ")");
				return "Premium Full is activated (restart the game), thank you! ;)";
		    case "moneydrop":
		    	cashPreValue = personaEntity.getCash();
		    	premiumMoneyValue = 10000000;
		    	finalValue = playerInitialCash + premiumMoneyValue;
		    	if (finalValue > maxCashPremiumAcc) {
		    		extraMoneyTransit = (finalValue - maxCashPremiumAcc);
		    		finalValue = maxCashPremiumAcc;
		    		userEntity.setExtraMoney(userEntity.getExtraMoney() + extraMoneyTransit);
		    	}
		    	personaEntity.setCash(finalValue);
		    	personaDao.update(personaEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code (Money amount BEFORE: " + cashPreValue + ")");
				return "Money Drop is activated (restart the game), thank you! ;)";
		    case "garage50":
		    	premiumCarSlots(250, userEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code.");
				return "Garage50+ is activated (restart the game), thank you! ;)";
		    case "garage150":
		    	premiumCarSlots(350, userEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code.");
				return "Garage150+ is activated (restart the game), thank you! ;)";
		    case "levelup":
		    	int newLevel = playerInitialLevel + 25;
		    	if (newLevel > maxLevelCap) {
		    		newLevel = maxLevelCap;
		    	}
		    	personaEntity.setLevel(newLevel);
		    	personaDao.update(personaEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code.");
				return "Level-Up is activated (restart the game), thank you! ;)";
            default:
            	return "ERROR: invaild Premium code, please contact to server support";
		}
	}
	
	// debug temporary premium activation stuff - Hypercycle
	public String useDebug(String premiumType, String extraMoney, String nickname, String timeYear, String timeMonth, String timeDay) {
		UserEntity userEntity = null;
		PersonaEntity personaEntity = personaDao.findByName(nickname);
		if (personaEntity == null) {
			return "ERROR: wrong nickname";
		}
		userEntity = personaEntity.getUser();
		double extraMoneyConvert = Double.parseDouble(extraMoney);
		// yes yes, parsing the single string would be more efficient
		int timeYearConvert = Integer.parseInt(timeYear);
		int timeMonthConvert = Integer.parseInt(timeMonth);
		int timeDayConvert = Integer.parseInt(timeDay);
		switch (premiumType) {
		    case "powerup":
		    	premiumAchievementApply(126, personaEntity);
		    	userEntity.setExtraMoney(extraMoneyConvert);
		    	userDao.update(userEntity);
				return nickname + " - DONE";
		    case "base":
		    	premiumAchievementApply(126, personaEntity);
		    	premiumAchievementApply(501, personaEntity);
		    	userEntity.setExtraMoney(extraMoneyConvert);
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumType);
		    	userEntity.setPremiumDate(LocalDate.of(timeYearConvert, timeMonthConvert, timeDayConvert));
		    	userDao.update(userEntity);
				return nickname + " - DONE";
		    case "plus":
		    	premiumAchievementApply(126, personaEntity);
		    	premiumAchievementApply(501, personaEntity);
		    	premiumAchievementApply(502, personaEntity);
		    	userEntity.setExtraMoney(extraMoneyConvert);
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumType);
		    	userEntity.setPremiumDate(LocalDate.of(timeYearConvert, timeMonthConvert, timeDayConvert));
		    	userDao.update(userEntity);
				return nickname + " - DONE";
		    case "full":
		    	premiumAchievementApply(126, personaEntity);
		    	premiumAchievementApply(501, personaEntity);
		    	premiumAchievementApply(502, personaEntity);
		    	premiumAchievementApply(503, personaEntity);
		    	userEntity.setExtraMoney(extraMoneyConvert);
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumType);
		    	userEntity.setPremiumDate(LocalDate.of(timeYearConvert, timeMonthConvert, timeDayConvert));
		    	userDao.update(userEntity);
				return nickname + " - DONE";
		    case "unlim":
		    	premiumAchievementApply(126, personaEntity);
		    	premiumAchievementApply(501, personaEntity);
		    	premiumAchievementApply(502, personaEntity);
		    	premiumAchievementApply(503, personaEntity);
		    	premiumAchievementApply(504, personaEntity);
		    	userEntity.setExtraMoney(extraMoneyConvert);
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumType);
		    	userEntity.setPremiumDate(LocalDate.of(timeYearConvert, timeMonthConvert, timeDayConvert));
		    	userDao.update(userEntity);
				return nickname + " - DONE";
            default:
            	return "ERROR: invaild infos, try again";
		}
	}
	
	private void premiumAchievementApply (int rankId, PersonaEntity personaEntity) {
		AchievementRankEntity achievementRankEntity = achievementRankDao.findById((long) rankId);
		if (achievementStateDao.findByPersonaAchievementRank(personaEntity, achievementRankEntity) == null) {
			AchievementStateEntity achievementStateEntity = new AchievementStateEntity();
			achievementStateEntity.setAchievedOn(LocalDateTime.now());
			achievementStateEntity.setAchievementRank(achievementRankEntity);
			achievementStateEntity.setAchievementState(AchievementState.COMPLETED);
			achievementStateEntity.setPersona(personaEntity);
			achievementStateDao.insert(achievementStateEntity);
		}
	}
	
	private void premiumCarSlots (int carSlots, UserEntity userEntity) {
		List<PersonaEntity> listOfProfile = userEntity.getListOfProfile();
		for (PersonaEntity personaEntityUser : listOfProfile) {
			personaEntityUser.setCarSlots(carSlots);
			personaDao.update(personaEntityUser);
		}
	}
}
