package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ReportDAO;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.ReportEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.ArrayOfBasicBlockPlayerInfo;
import com.soapboxrace.jaxb.http.ArrayOfLong;
import com.soapboxrace.jaxb.http.BasicBlockPlayerInfo;
import com.soapboxrace.jaxb.http.PersonaBase;

@Stateless
public class SocialBO {

	@EJB
	private ReportDAO reportDao;
	
	@EJB
	private PersonaDAO personaDao;
	
	@EJB
	private DriverPersonaBO driverPersonaBO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	public void sendReport(Long personaId, Long abuserPersonaId, Integer petitionType, String description, Integer customCarID, Integer chatMinutes, Long hacksDetected) {
		ReportEntity reportEntity = new ReportEntity();
		PersonaEntity personaEntityAbuser = personaDao.findById(abuserPersonaId);
		String personaAbuserName = personaEntityAbuser.getName();
		PersonaEntity personaEntitySender = personaDao.findById(personaId);
		
		reportEntity.setAbuserPersonaId(abuserPersonaId);
		reportEntity.setChatMinutes(chatMinutes);
		reportEntity.setCustomCarID(customCarID);
		reportEntity.setDescription(description);
		if (description.startsWith("/teaminvite") && personaEntitySender.getTeam() != null) {
			description = "/teaminvite";
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Team invite has been sent."), personaId);
		}
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
	        		+ "\n:incoming_envelope: **|** Nгрок **" + reportSender + "** прислал репорт на игрока **" + personaAbuserName + "**, причина: *reportDescPlace*"
	        		+ "\n:incoming_envelope: **|** Player **" + reportSender + "** sent a report for player **" + personaAbuserName + "**, reason: *reportDescPlace*";
			discordBot.sendMessageReport(message, description);
		}
		if (hacksDetected != 32 && hacksDetected != 0) {
			String message = ":heavy_minus_sign:"
	        		+ "\n:clap: **|** Nгрок **" + personaAbuserName + "** использовал читы или стороннее ПО во время заезда (*Код " + hacksDetected + ", " + description + "*)."
	        		+ "\n:clap: **|** Player **" + personaAbuserName + "** used a cheats or 3rd-party tools during the event (*Code " + hacksDetected + ", " + description + "*).";
			discordBot.sendMessage(message);
		}
	}
	
	// Taken from SBRW-Core - https://github.com/SoapboxRaceWorld/soapbox-race-core/
	public ArrayOfBasicBlockPlayerInfo getBlockedUserList(Long userId) {
//        List<SocialRelationshipEntity> socialRelationshipEntityList =
//                this.socialRelationshipDAO.findByUserIdAndStatus(userId, 2L);
        ArrayOfBasicBlockPlayerInfo arrayOfBasicBlockPlayerInfo = new ArrayOfBasicBlockPlayerInfo();

//        for (SocialRelationshipEntity socialRelationshipEntity : socialRelationshipEntityList) {
//            this.addBlockedUserToList(arrayOfBasicBlockPlayerInfo, socialRelationshipEntity);
//        }

        return arrayOfBasicBlockPlayerInfo;
    }

    public ArrayOfLong getBlockersByUsers(Long personaId) {
        PersonaEntity personaEntity = personaDao.findById(personaId);

        if (personaEntity == null) {
            //
        }

        ArrayOfLong arrayOfLong = new ArrayOfLong();

 //       for (SocialRelationshipEntity socialRelationshipEntity :
 //               this.socialRelationshipDAO.findByRemoteUserIdAndStatus(personaEntity.getUser().getId(), 2L)) {
 //           arrayOfLong.getLong().add(socialRelationshipEntity.getUser().getId());
 //       }

        return arrayOfLong;
    }
    
    public PersonaBase blockPlayer(Long userId, Long activePersonaId, Long otherPersonaId) {
        PersonaEntity activePersonaEntity = personaDao.findById(activePersonaId);

        if (activePersonaEntity == null) {
            //
        }

        PersonaEntity otherPersonaEntity = personaDao.findById(otherPersonaId);

        if (otherPersonaEntity == null) {
            //
        }

//        SocialRelationshipEntity localSide = socialRelationshipDAO.findByLocalAndRemoteUser(userId,
//                otherPersonaEntity.getUser().getId());
//        SocialRelationshipEntity remoteSide =
//                socialRelationshipDAO.findByLocalAndRemoteUser(otherPersonaEntity.getUser().getId(),
//                        userId);

//        if (localSide == null) {
//            createNewRelationship(activePersonaEntity, otherPersonaEntity, 2L);
//        } else {
//            localSide.setStatus(2L);
//            socialRelationshipDAO.update(localSide);
//        }

//        if (remoteSide != null) {
//            socialRelationshipDAO.delete(remoteSide);
//            sendPresencePacket(activePersonaEntity, 0L, otherPersonaId);
//        }

        return driverPersonaBO.getPersonaBase(otherPersonaEntity);
    }

    public PersonaBase unblockPlayer(Long userId, Long otherPersonaId) {
        PersonaEntity otherPersonaEntity = personaDao.findById(otherPersonaId);

        if (otherPersonaEntity == null) {
            //
        }

//        SocialRelationshipEntity localSide = socialRelationshipDAO.findByLocalAndRemoteUser(userId,
//                otherPersonaEntity.getUser().getId());

//        if (localSide != null && localSide.getStatus() == 2L) {
//            socialRelationshipDAO.delete(localSide);
//        } else {
            //
//        }

        return driverPersonaBO.getPersonaBase(otherPersonaEntity);
    }
    
//    private void addBlockedUserToList(ArrayOfBasicBlockPlayerInfo arrayOfBasicBlockPlayerInfo,
//            SocialRelationshipEntity socialRelationshipEntity) {
//        for (PersonaEntity personaEntity : socialRelationshipEntity.getRemoteUser().getPersonas()) {
//            BasicBlockPlayerInfo basicBlockPlayerInfo = new BasicBlockPlayerInfo();
//
//            basicBlockPlayerInfo.setPersonaId(personaEntity.getPersonaId());
//            basicBlockPlayerInfo.setUserId(socialRelationshipEntity.getRemoteUser().getId());
//
//            arrayOfBasicBlockPlayerInfo.getBasicBlockPlayerInfo().add(basicBlockPlayerInfo);
//            }
//        }

}
