package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.PersonaDAO;

@Stateless
public class DiscordBO {
	@EJB
	private PersonaDAO personaDao;

	@EJB
	private DiscordWebhook discordBot;

	// When player has been kicked from the online event, client reports about that
	public void outputNetErrorInfo(Long personaId, Long netErrorCode) {
		String playerName = personaDao.findById(personaId).getName();
		String errorStr = "!pls fix!";
		int errorCode = netErrorCode.intValue();
		switch (errorCode) { // Error 100 is a successful finish?
		case 101:
			errorStr = "Unknown Error";
			break;
		case 105:
			errorStr = "PEER_TIMEOUT";
			break;
		case 106:
			errorStr = "HANDSHAKE_ERROR";
			break;
		}
		String message = ":heavy_minus_sign:"
        		+ "\n:candle: **| " + playerName + "** вылетел с онлайн-заезда / was kicked from online-event, *error " + errorCode + " (" + errorStr + ")*.";
		discordBot.sendMessage(message);
	}
}
