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
import com.soapboxrace.core.dao.TokenSessionDAO;

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