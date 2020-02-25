package com.soapboxrace.core.bo;


import java.util.List;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.LockType;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;
import com.soapboxrace.core.jpa.TokenSessionEntity;

@Singleton
public class OnlineUsersBO {

	@EJB
	OpenFireRestApiCli openFireRestApiCli;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private TokenSessionDAO tokenDAO;
	
	private int onlineUsers;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private PersonaDAO personaDAO;
	
	// FIXME Weird mess with online count triggers
	public int getNumberOfUsersOnlineNow() {
		return onlineUsers;
	}
    
	@Schedule(minute = "*", hour = "*", persistent = false)
	@Lock(LockType.READ)
	public void updateOnlineUsers() {
		onlineUsers = openFireRestApiCli.getTotalOnlineUsers();
	}
	
	@Schedule(minute = "*/10", hour = "*", persistent = false)
	public void OnlineCountDiscord() {
		if (parameterBO.getBoolParam("DISCORD_ONLINECOUNT")) {
			String message = ":heavy_minus_sign:"
	        		+ "\n:cityscape: **|** Сейчас игроков на сервере: **" + openFireRestApiCli.getTotalOnlineUsers() + "**"
	        		+ "\n:cityscape: **|** Players online now on server: **" + openFireRestApiCli.getTotalOnlineUsers() + "**";
			discordBot.sendMessage(message);
		}
	}
	
	// Give a money to random player online, every hour (can be unoptimized!)
	@Schedule(hour = "*/1", persistent = false)
	public void ScheduleGiveaway() {
		if (parameterBO.getBoolParam("DISCORD_1HCASHGIVEAWAY")) {
//			int moneyAmount = parameterBO.getIntParam("DISCORD_1HCASHGIVEAWAY_VALUE");
			int moneyAmount = 1000000;
			List<TokenSessionEntity> onlinePlayersList = openFireRestApiCli.getTotalOnlineList();
			Random rand = new Random();
			int randNumber = rand.nextInt(onlinePlayersList.size());
			TokenSessionEntity playerWinnerToken = onlinePlayersList.get(randNumber);
//			PersonaPresenceEntity playerWinnerPresence = personaPresenceDAO.findByUserId(playerWinnerToken.getUserId());
			PersonaEntity personaEntity = personaDAO.findById(playerWinnerToken.getActivePersonaId());
			personaEntity.setCash(personaEntity.getCash() + (double) moneyAmount);
			String winnerName = personaEntity.getName();
			personaDAO.update(personaEntity);
			
			String message = ":heavy_minus_sign:"
	        		+ "\n:moneybag: **|** Nгрок **" + winnerName + "** получил *1,000,000 $* в ходе раздачи!"
	        		+ "\n:moneybag: **|** Player **" + winnerName + "** has won *1,000,000 $* during giveaway!";
			discordBot.sendMessage(message);
		}
	}
//	@Schedule(minute = "*/60", hour = "*", persistent = false)
//	public void serverVersionDiscord() {
//		com.soapboxrace.jaxb.http.SystemInfo systemInfo = new com.soapboxrace.jaxb.http.SystemInfo();
//		if (parameterBO.getBoolParam("DISCORD_ONLINECOUNT")) {
//			String message = ":heavy_minus_sign:"
//	        		+ "\n:earth_africa: **|** Версия сервера: **" + systemInfo.getVersion() + "**"
//	        		+ "\n:earth_africa: **|** Server version: **" + systemInfo.getVersion() + "**";
//			discordBot.sendMessage(message);
//		}
//	}
}