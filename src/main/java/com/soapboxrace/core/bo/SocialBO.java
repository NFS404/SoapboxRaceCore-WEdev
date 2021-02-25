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
        
        List<FriendListEntity> blockedPlayersList = friendListDAO.getUserBlockedList(userId);
        for (FriendListEntity blockedEntity : blockedPlayersList) {
        	boolean isOurDriverIsA = false;
        	Long entityUserId = 0L;
			if (blockedEntity.getUserId_A().equals(userId)) { // User A gets User B as Friend
				isOurDriverIsA = true;
				entityUserId = blockedEntity.getUserId_B();
			}
			else { // User B gets User A as Friend
				isOurDriverIsA = false;
				entityUserId = blockedEntity.getUserId_A();
			}
			// Block status (0 - not blocked, 1 - user A requested the block, 2 - user B requested the block, 3 - both users is blocked each other)
			if ((isOurDriverIsA && blockedEntity.getBlockStatus() == 1) || (!isOurDriverIsA && blockedEntity.getBlockStatus() == 2) 
					|| blockedEntity.getBlockStatus() == 3) {
				addBlockedUserToList(arrayOfBasicBlockPlayerInfo, entityUserId);
			}
        }
        return arrayOfBasicBlockPlayerInfo;
    }

	// Parts taken from WorldUnited.gg 
    public ArrayOfLong getBlockersByUsers(Long personaId, Long userId) {
        PersonaEntity personaEntity = personaDao.findById(personaId);
        if (personaEntity == null) {
        	System.out.println("### User " + userId + "has tried to request blocked players list without Persona ID somehow.");
        	return null;
        }

        ArrayOfLong arrayOfLong = new ArrayOfLong();
        for (FriendListEntity blockedEntity : friendListDAO.getUserBlockedList(userId)) {
        	Long blockedUserId = 0L;
        	boolean isOurDriverIsA = true;
			if (blockedEntity.getUserId_A().equals(userId)) { // User A gets User B as Friend
				blockedUserId = blockedEntity.getUserId_B();
			}
			else { // User B gets User A as Friend
				blockedUserId = blockedEntity.getUserId_A();
				isOurDriverIsA = false;
			}
			// Block status (0 - not blocked, 1 - user A requested the block, 2 - user B requested the block, 3 - both users is blocked each other)
			if ((!isOurDriverIsA && blockedEntity.getBlockStatus() == 1) || (isOurDriverIsA && blockedEntity.getBlockStatus() == 2) 
					|| blockedEntity.getBlockStatus() == 3) {
				arrayOfLong.getLong().add(blockedUserId);
			}
        }
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
        FriendListEntity relationshipEntity = friendListDAO.findUsersRelationship(userId, otherPersonaUserId);

        if (relationshipEntity == null) { // Create new FList entry
        	friendBO.createNewFriendListEntry(userId, activePersonaId, otherPersonaUserId, otherPersonaId, 0, 1);
        } 
        else { // If it's already exist
        	int blockStatus = relationshipEntity.getBlockStatus();
        	int status = relationshipEntity.getStatus();
        	if (blockStatus == 3) { // Both players has blocked each other
        		openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You has already blocked this driver."), activePersonaId);
            	return null;
        	}
        	boolean isOurDriverIsA = false;
			if (relationshipEntity.getUserId_A().equals(userId)) { // User A gets User B as Friend
				isOurDriverIsA = true;
			}
        	
        	if (status == 2 || status == 1) { // Player is Friend
        		if (isOurDriverIsA) {
        			relationshipEntity.setBlockStatus(1); // Block from player A
        		}
        		else {
        			relationshipEntity.setBlockStatus(2); // Block from player B
        		}
        		if (status == 1) { // Player have pending friend request
            		relationshipEntity.setStatus(0); // Remove the friend request
            	}
        	}
        	
        	if (blockStatus == 1 || blockStatus == 2) { // Sender or player has already being blocked
    			if (isOurDriverIsA && blockStatus == 1) {
    				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You has already blocked this driver."), activePersonaId);
                	return null;
    			}
    			if (!isOurDriverIsA && blockStatus == 1 || isOurDriverIsA && blockStatus == 2) { // Make a block of both drivers
    				relationshipEntity.setBlockStatus(3);
    			}
        	}
        }
        sendPresencePacket(activePersonaEntity, 0, otherPersonaId);

        return driverPersonaBO.getPersonaBase(otherPersonaEntity);
    }

    public PersonaBase unblockPlayer(Long userId, Long activePersonaId, Long otherPersonaId) {
        PersonaEntity otherPersonaEntity = personaDao.findById(otherPersonaId);
        if (otherPersonaEntity == null) {
        	openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Invaild player reference, try again."), activePersonaId);
        	return null;
        }
        Long otherPersonaUserId = otherPersonaEntity.getUser().getId();
        FriendListEntity relationshipEntity = friendListDAO.findUsersRelationship(userId, otherPersonaUserId);

        if (relationshipEntity != null) {
        	int blockStatus = relationshipEntity.getBlockStatus();
        	int status = relationshipEntity.getStatus();
        	boolean isOurDriverIsA = false;
			if (relationshipEntity.getUserId_A().equals(userId)) { // User A gets User B as Friend
				isOurDriverIsA = true;
			}
			
			if (blockStatus == 1) {
				if (isOurDriverIsA) { // Remove the block
					relationshipEntity.setBlockStatus(0);
				}
				else {
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You cannot un-block this driver, since he blocked you first."), activePersonaId);
		        	return null;
				}
			}
			if (blockStatus == 2) {
				if (!isOurDriverIsA) { // Remove the block
					relationshipEntity.setBlockStatus(0);
				}
				else {
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You cannot un-block this driver, since he blocked you first."), activePersonaId);
		        	return null;
				}
			}
			if (blockStatus == 3) {
				if (isOurDriverIsA) {
					relationshipEntity.setBlockStatus(2); // User B block still exists
				}
				else {
					relationshipEntity.setBlockStatus(1); // User A block still exists
				}
			}
			
			if (status == 0) { // Remove the entry when players is not friends
				friendListDAO.delete(relationshipEntity);
			}
        } 
        else { // How the player able to un-block a player who is not blocked on first place?
        	openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Request error - contact with the server staff."), activePersonaId);
        	System.out.println("### User " + userId + " has tried to un-block a persona ID " + otherPersonaId + ", who is not blocked at all.");
        	return null;
        }

        return driverPersonaBO.getPersonaBase(otherPersonaEntity);
    }
    
    private void addBlockedUserToList(ArrayOfBasicBlockPlayerInfo arrayOfBasicBlockPlayerInfo, Long userId) {
    	for (PersonaEntity personaEntity : userDAO.findById(userId).getListOfProfile()) {
        	BasicBlockPlayerInfo basicBlockPlayerInfo = new BasicBlockPlayerInfo();
        	basicBlockPlayerInfo.setPersonaId(personaEntity.getPersonaId());
        	basicBlockPlayerInfo.setUserId(userId);
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
