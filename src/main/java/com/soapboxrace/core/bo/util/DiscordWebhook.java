package com.soapboxrace.core.bo.util;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.ParameterBO;

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import com.mrpowergamerbr.temmiewebhook.embed.ThumbnailEmbed;

@Stateless
// Taken from WorldOnlinePL, by Metonator
public class DiscordWebhook {
	@EJB
	private ParameterBO parameterBO;

	public void sendMessage(String message, String webHookUrl, String botName) {
		TemmieWebhook temmie = new TemmieWebhook(webHookUrl);
		try {
			message = new String (message.getBytes("cp1251"),"UTF-8");
		}
		catch (IOException ioe) {
		}
		DiscordMessage dm = DiscordMessage.builder().username(botName).content(message).build();
		temmie.sendMessage(dm);
	}
	
	public void sendMessageReport(String message, String reportDesc, String webHookUrl, String botName) {
		TemmieWebhook temmie = new TemmieWebhook(webHookUrl);
		try {
			// Russian letters encoding (from core), in-game cyrillic text is fine without additional encoding
			message = new String (message.getBytes("cp1251"));
			message = message.replace("reportDescPlace", reportDesc);
		}
		catch (IOException ioe) {
		}
		DiscordMessage dm = DiscordMessage.builder().username(botName).content(message).build();
		temmie.sendMessage(dm);
	}

	public void sendMessage(String message, String webHookUrl) {
		sendMessage(message, webHookUrl, parameterBO.getStrParam("DISCORD_WEBHOOK_DEFAULTNAME"));
	}

	public void sendMessage(String message) {
		sendMessage(message, parameterBO.getStrParam("DISCORD_WEBHOOK_DEFAULTURL"), parameterBO.getStrParam("DISCORD_WEBHOOK_DEFAULTNAME"));
	}
	
	public void sendMessageReport(String message, String reportDesc) {
		sendMessageReport(message, reportDesc, parameterBO.getStrParam("DISCORD_WEBHOOK_DEFAULTURL"), parameterBO.getStrParam("DISCORD_WEBHOOK_DEFAULTNAME"));
	}
	
	public void sendMessage(String message, boolean teamAction) {
		sendMessage(message, parameterBO.getStrParam("DISCORD_WEBHOOK_TEAMSURL"), parameterBO.getStrParam("DISCORD_WEBHOOK_DEFAULTNAME"));
	}
}