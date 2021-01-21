package com.soapboxrace.core.bo;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.api.util.BanUtil;
import com.soapboxrace.core.dao.APITokenDAO;
import com.soapboxrace.core.dao.BanDAO;
import com.soapboxrace.core.jpa.APITokenEntity;
import com.soapboxrace.core.jpa.BanEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.jaxb.login.LoginStatusVO;

@Stateless
public class AuthenticationBO {
	@EJB
	private BanDAO banDAO;
	
	@EJB
	private APITokenDAO apiTokenDAO;

	public BanEntity checkUserBan(UserEntity userEntity) {
		return banDAO.findByUser(userEntity);
	}

	public LoginStatusVO checkIsBanned(String hwid, String email) {
		BanEntity banEntity;
		banEntity = banDAO.findByHWID(hwid);
		if (banEntity != null) {
			return new BanUtil(banEntity).invoke();
		}
		banEntity = banDAO.findByEmail(email);
		if (banEntity != null) {
			return new BanUtil(banEntity).invoke();
		}
		return null;
	}
	
	// Checks email only, so no matter which HWID is used, account is blocked - Hypercycle
	public LoginStatusVO checkIsBannedAccount(String email) {
		BanEntity banEntity;
		banEntity = banDAO.findByEmail(email);
		if (banEntity != null && banEntity.getType() != "CHAT_BAN") {
			return new BanUtil(banEntity).invoke();
		}
		return null;
	}
	
	public String generateTempAPIToken(String ipAddress) {
		APITokenEntity apiTokenEntity = new APITokenEntity();
		
//		int leftLimit = 97;
//		int rightLimit = 122;
//		int targetStringLength = 15;
//		Random random = new Random();
//		StringBuilder buffer = new StringBuilder(targetStringLength);
//		for (int i = 0; i < targetStringLength; i++) {
//			int randomLimitedInt = leftLimit + (int) 
//			  (random.nextFloat() * (rightLimit - leftLimit + 1));
//			buffer.append((char) randomLimitedInt);
//		}
		
		byte[] byteArray = new byte[5];
        SecureRandom rand = new SecureRandom();
        rand.nextBytes(byteArray);
        StringBuilder result = new StringBuilder();
        for (byte temp : byteArray) {
            result.append(String.format("%02x", temp));
        }
		
		String newTempToken = result.toString();
		LocalDateTime creationTime = LocalDateTime.now();
		apiTokenEntity.setToken(newTempToken);
		apiTokenEntity.setCreatedTime(creationTime);
		apiTokenEntity.setIPAddress(ipAddress);
		apiTokenDAO.insert(apiTokenEntity);
		// TODO Make the automatic clean-up of old expired tokens
		return newTempToken;
	}
}
