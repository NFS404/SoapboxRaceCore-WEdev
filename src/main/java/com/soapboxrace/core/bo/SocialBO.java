package com.soapboxrace.core.bo;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.FriendListDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ReportDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.FriendListEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.ReportEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.ArrayOfBasicBlockPlayerInfo;
import com.soapboxrace.jaxb.http.ArrayOfLong;
import com.soapboxrace.jaxb.http.BasicBlockPlayerInfo;
import com.soapboxrace.jaxb.http.PersonaBase;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypePersonaBase;

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
	private FriendListDAO friendListDAO;
	
	@EJB
	private FriendBO friendBO;
	
	@EJB
	private UserDAO userDAO;
	
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
	
	// Parts taken from WorldUnited.gg 
	public ArrayOfBasicBlockPlayerInfo getBlockedUserList(Long userId) {
        ArrayOfBasicBlockPlayerInfo arrayOfBasicBlockPlayerInfo = new ArrayOfBasicBlockPlayerInfo();
        
        List<FriendListEntity> blockedPlayersList = friendListDAO.findBlockedByOwnerId(userId);
        for (FriendListEntity friendListEntity : blockedPlayersList) {
        	// System.out.println("blockedUserList userId: " + friendListEntity.getUserId() + ", userOwnerId: " + friendListEntity.getUserOwnerId());
            this.addBlockedUserToList(arrayOfBasicBlockPlayerInfo, friendListEntity);
        }
        return arrayOfBasicBlockPlayerInfo;
    }

	// Parts taken from WorldUnited.gg 
	// FIXME For unknown reason, when player gets his blockers list, he will be unable to see own messages on public chats
    public ArrayOfLong getBlockersByUsers(Long personaId, Long userId) {
//      PersonaEntity personaEntity = personaDao.findById(personaId);
//      if (personaEntity == null) {
//      	System.out.println("### User " + userId + "has tried to request blocked players list without Persona ID somehow.");
//      	return null;
//      }

        ArrayOfLong arrayOfLong = new ArrayOfLong();
//      for (FriendListEntity friendListEntity : friendListDAO.findByRemoteUserBlockedId(userId)) {
//      	System.out.println("blockersByUsers userId: " + friendListEntity.getUserId() + ", userOwnerId: " + friendListEntity.getUserOwnerId());
//          arrayOfLong.getLong().add(friendListEntity.getUserId());
//      }
        return arrayOfLong;
    }
    
    // Parts taken from WorldUnited.gg 
    public PersonaBase blockPlayer(Long userId, Long activePersonaId, Long otherPersonaId) {
        PersonaEntity activePersonaEntity = personaDao.findById(activePersonaId);
        if (activePersonaEntity == null) {
        	System.out.println("### User " + userId + "has sent the player block request without Persona ID somehow.");
        	return null;
        }

        PersonaEntity otherPersonaEntity = personaDao.findById(otherPersonaId);
        if (otherPersonaEntity == null) {
        	openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Invaild player reference, try again."), activePersonaId);
        	return null;
        }

        Long otherPersonaUserId = otherPersonaEntity.getUser().getId();
        FriendListEntity localSide = friendListDAO.findByOwnerIdAndFriendPersona(userId, otherPersonaUserId);
        FriendListEntity remoteSide = friendListDAO.findByOwnerIdAndFriendPersona(otherPersonaUserId, userId);

        if (localSide == null) { // Create new FList entry
        	friendBO.createNewFriendListEntry(otherPersonaId, userId, otherPersonaUserId, false, true);
        } 
        else { // Friend became Enemy...
            localSide.setIsBlocked(true);
            friendListDAO.update(localSide);
        }
        if (remoteSide != null) { // Remove the FList entry on the other side, since we have a block
        	friendListDAO.delete(remoteSide);
            sendPresencePacket(activePersonaEntity, 0, otherPersonaId);
        }
        return driverPersonaBO.getPersonaBase(otherPersonaEntity);
    }

    public PersonaBase unblockPlayer(Long userId, Long activePersonaId, Long otherPersonaId) {
        PersonaEntity otherPersonaEntity = personaDao.findById(otherPersonaId);
        if (otherPersonaEntity == null) {
        	openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Invaild player reference, try again."), activePersonaId);
        	return null;
        }
        FriendListEntity localSide = friendListDAO.findByOwnerIdAndFriendPersona(userId, otherPersonaId);

        if (localSide != null && localSide.getIsBlocked()) {
        	friendListDAO.delete(localSide);
        } 
        else { // How the player able to un-block a player who is not blocked on first place?
        	openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Request error - contact with the server staff."), activePersonaId);
        	System.out.println("### User " + userId + " has tried to un-block a persona ID " + otherPersonaId + ", who is not blocked at all.");
        	return null;
        }

        return driverPersonaBO.getPersonaBase(otherPersonaEntity);
    }
    
    private void addBlockedUserToList(ArrayOfBasicBlockPlayerInfo arrayOfBasicBlockPlayerInfo, FriendListEntity friendListEntity) {
    	Long targetUserId = friendListEntity.getUserId();
    	for (PersonaEntity personaEntity : userDAO.findById(targetUserId).getListOfProfile()) {
    		if (personaEntity.isHidden()) {
				continue; // Hidden persona is excluded
			}
        	BasicBlockPlayerInfo basicBlockPlayerInfo = new BasicBlockPlayerInfo();
        	basicBlockPlayerInfo.setPersonaId(personaEntity.getPersonaId());
        	basicBlockPlayerInfo.setUserId(targetUserId);
        	arrayOfBasicBlockPlayerInfo.getBasicBlockPlayerInfo().add(basicBlockPlayerInfo);
        }
    }
    
    // Parts taken from WorldUnited.gg 
    private void sendPresencePacket(PersonaEntity personaEntity, int presence, Long targetPersonaId) {
    	XMPP_ResponseTypePersonaBase personaPacket = new XMPP_ResponseTypePersonaBase();
    	PersonaBase xmppPersonaBase = driverPersonaBO.getPersonaBase(personaEntity);
    	xmppPersonaBase.setPresence(presence);
    	personaPacket.setPersonaBase(xmppPersonaBase);
    	openFireSoapBoxCli.send(personaPacket, targetPersonaId);
    }
}
