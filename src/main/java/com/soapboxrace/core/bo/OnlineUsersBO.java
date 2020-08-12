package com.soapboxrace.core.bo;

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
import com.soapboxrace.core.dao.ServerInfoDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.jpa.ServerInfoEntity;

@Singleton
public class OnlineUsersBO {

	@EJB
	OpenFireRestApiCli openFireRestApiCli;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private TokenSessionDAO tokenDAO;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private PersonaDAO personaDAO;
	
	@EJB
	private ServerInfoDAO serverInfoDAO;
    
	@Schedule(minute = "*/1", hour = "*", persistent = false)
	@Lock(LockType.READ)
	public void updateOnlineUsers() {
		ServerInfoEntity serverInfoEntity = serverInfoDAO.findInfo();
		serverInfoEntity.setOnlineNumber(openFireRestApiCli.getTotalOnlineUsers());
		serverInfoDAO.update(serverInfoEntity);
	}
	
	@Schedule(minute = "*/10", hour = "*", persistent = false)
	public void OnlineCountDiscord() {
		if (parameterBO.getBoolParam("DISCORD_ONLINECOUNT")) {
			ServerInfoEntity serverInfoEntity = serverInfoDAO.findInfo();
			int onlineCount = serverInfoEntity.getOnlineNumber();
			String message = ":heavy_minus_sign:"
	        		+ "\n:cityscape: **|** Сейчас игроков на сервере: **" + onlineCount + "**"
	        		+ "\n:cityscape: **|** Players online now on server: **" + onlineCount + "**"
			        + "\n:cityscape: **|** Текущая ротация трасс / Track Rotation: **#" + parameterBO.getIntParam("ROTATIONID") + "**";
			discordBot.sendMessage(message);
		}
	}
	
	// Give a money to random player online, every hour (can be unoptimized!)
//	@Schedule(hour = "*/1", persistent = false)
//	public void ScheduleGiveaway() {
//		if (parameterBO.getBoolParam("DISCORD_1HCASHGIVEAWAY")) {

//			int moneyAmount = 1000000;
//			List<TokenSessionEntity> onlinePlayersList = openFireRestApiCli.getTotalOnlineList();
//			Random rand = new Random();
//			int randNumber = rand.nextInt(onlinePlayersList.size());
//			TokenSessionEntity playerWinnerToken = onlinePlayersList.get(randNumber);

//			PersonaEntity personaEntity = personaDAO.findById(playerWinnerToken.getActivePersonaId());
//			personaEntity.setCash(personaEntity.getCash() + (double) moneyAmount);
//			String winnerName = personaEntity.getName();
//			personaDAO.update(personaEntity);
			
//			String message = ":heavy_minus_sign:"
//	        		+ "\n:moneybag: **|** Nгрок **" + winnerName + "** получил *1,000,000 $* в ходе раздачи!"
//	        		+ "\n:moneybag: **|** Player **" + winnerName + "** has won *1,000,000 $* during giveaway!";
//			discordBot.sendMessage(message);
//		}
//	}
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