package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ReportDAO;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.ReportEntity;

@Stateless
public class SocialBO {

	@EJB
	private ReportDAO reportDao;
	
	@EJB
	private PersonaDAO personaDao;
	
	@EJB
	private DiscordWebhook discordBot;
	
	public void sendReport(Long personaId, Long abuserPersonaId, Integer petitionType, String description, Integer customCarID, Integer chatMinutes, Long hacksDetected) {
		ReportEntity reportEntity = new ReportEntity();
		PersonaEntity personaEntityAbuser = personaDao.findById(abuserPersonaId);
		PersonaEntity personaEntitySender = personaDao.findById(personaId);
		
		reportEntity.setAbuserPersonaId(abuserPersonaId);
		reportEntity.setChatMinutes(chatMinutes);
		reportEntity.setCustomCarID(customCarID);
		reportEntity.setDescription(description);
		reportEntity.setPersonaId(personaId);
		reportEntity.setPetitionType(petitionType);
		reportEntity.setHacksDetected(hacksDetected);
		reportDao.insert(reportEntity);
		
		if (hacksDetected == 0) {
			String reportSender = null;
			if (personaId == 0) {
				reportSender = "TICKIE THE BOT";
			}
			else {
				reportSender = personaEntitySender.getName();
			}
			String message = ":heavy_minus_sign:"
	        		+ "\n:incoming_envelope: **|** Nгрок **" + reportSender + "** прислал репорт на игрока **" + personaEntityAbuser.getName() + "**, причина: *" + description + "*"
	        		+ "\n:incoming_envelope: **|** Player **" + reportSender + "** sent a report for player **" + personaEntityAbuser.getName() + "**, reason: *" + description + "*";
			discordBot.sendMessage(message);
		}
		if (hacksDetected != 32 && hacksDetected != 0) {
			String message = ":heavy_minus_sign:"
	        		+ "\n:clap: **|** Nгрок **" + personaEntityAbuser.getName() + "** использовал читы или стороннее ПО во время заезда (*Код " + hacksDetected + ", " + description + "*)."
	        		+ "\n:clap: **|** Player **" + personaEntityAbuser.getName() + "** used a cheats or 3rd-party tools during the event (*Code " + hacksDetected + ", " + description + "*).";
			discordBot.sendMessage(message);
		}
	}

}
