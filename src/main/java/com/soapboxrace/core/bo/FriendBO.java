package com.soapboxrace.core.bo;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.FriendListDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.ReportDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.dao.VinylStorageDAO;
import com.soapboxrace.core.jpa.FriendListEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;
import com.soapboxrace.core.jpa.TokenSessionEntity;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.jaxb.http.ArrayOfBadgePacket;
import com.soapboxrace.jaxb.http.ArrayOfFriendPersona;
import com.soapboxrace.jaxb.http.FriendPersona;
import com.soapboxrace.jaxb.http.PersonaBase;
import com.soapboxrace.jaxb.http.PersonaFriendsList;
import com.soapboxrace.jaxb.xmpp.XMPP_FriendResultType;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeFriendResult;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypePersonaBase;
import com.soapboxrace.xmpp.openfire.XmppFriend;

@Stateless
public class FriendBO {

	@EJB
	private PersonaDAO personaDAO;

	@EJB
	private FriendListDAO friendListDAO;

	@EJB
	private DriverPersonaBO driverPersonaBO;

	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private TeamsBO teamsBO;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private OpenFireRestApiCli openFireRestApiCli;

	@EJB
	private TokenSessionDAO tokenSessionDAO;
	
	@EJB
	private TeamsDAO teamsDAO;
	
	@EJB
	private ReportDAO reportDAO;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private CommerceBO commerceBO;
	
	@EJB
	private VinylStorageDAO vinylStorageDAO;
	
	@EJB
	private VinylStorageBO vinylStorageBO;
	
	@EJB
	private UserDAO userDAO;
	
	@EJB
	private UserBO userBO;
	
	@EJB
	private AchievementsBO achievementsBO;

	// FIXME Players who sent the friend request can teleport to the player, no matter which request status is
	// Loading the player friend-list
	public PersonaFriendsList getFriendListFromUserId(Long userId) {
		ArrayOfFriendPersona arrayOfFriendPersona = new ArrayOfFriendPersona();
		List<FriendPersona> friendPersonaList = arrayOfFriendPersona.getFriendPersona();

		List<FriendListEntity> friendList = friendListDAO.getUserFriendList(userId);
		for (FriendListEntity friendEntity : friendList) {
			PersonaEntity friendPersonaEntity = null;
			Long friendUserId = 0L;
			boolean isOurDriverIsA = true;
			if (friendEntity.getUserId_A().equals(userId)) { // User A gets User B as Friend
				friendPersonaEntity = personaDAO.findById(friendEntity.getPersonaId_B());
				friendUserId = friendEntity.getUserId_B();
				isOurDriverIsA = true;
			}
			else { // User B gets User A as Friend
				friendPersonaEntity = personaDAO.findById(friendEntity.getPersonaId_A());
				friendUserId = friendEntity.getUserId_A();
				isOurDriverIsA = false;
			}
			
			if (friendPersonaEntity == null) { // If some error or illegal Persona deletion happens
				System.out.println("### FriendEntity ID " + friendEntity + " contains invaild Persona ID reference!");
				continue;
			}
			int presence = 3; // 0 - offline, 1 - freeroam, 2 - racing or safehouse, 3 - pending friend request
			if (isOurDriverIsA && friendEntity.getStatus() == 1) { // User A awaits for the User B invite decision, display as "offline"
				presence = 0;
			}
			if (friendEntity.getStatus() == 2) { // Invite accepted, so get the current presence
				presence = personaPresenceDAO.findByUserId(friendUserId).getPersonaPresence();
			}
			addPersonaToFriendList(friendPersonaList, friendPersonaEntity, presence, friendUserId);
		}

		PersonaFriendsList personaFriendsList = new PersonaFriendsList();
		personaFriendsList.setFriendPersona(arrayOfFriendPersona);
		return personaFriendsList;
	}

	private void addPersonaToFriendList(List<FriendPersona> friendPersonaList, PersonaEntity personaEntity, int presence, Long friendUserId) {
		FriendPersona friendPersona = new FriendPersona();
		friendPersona.setIconIndex(personaEntity.getIconIndex());
		friendPersona.setLevel(personaEntity.getLevel());
		friendPersona.setName(personaEntity.getName());
		friendPersona.setOriginalName(personaEntity.getName());
		friendPersona.setPersonaId(personaEntity.getPersonaId());
		friendPersona.setPresence(presence);
		friendPersona.setSocialNetwork(0);
		friendPersona.setUserId(friendUserId);
		friendPersonaList.add(friendPersona);
	}

	// Accept or decline the friend request
	public PersonaBase sendResponseFriendRequest(Long personaId, Long friendPersonaId, int resolution) {
		PersonaEntity personaInvited = personaDAO.findById(personaId);
		PersonaEntity personaSender = personaDAO.findById(friendPersonaId);
		if (personaInvited == null || personaSender == null) {
			System.out.println("### Some ResponseFriendRequest contains invaild Persona ID references.");
			return null;
		}

		Long personaSenderId = personaSender.getPersonaId();
		Long personaSenderUser = personaSender.getUser().getId();
		Long personaInvitedId = personaInvited.getPersonaId();
		Long personaInvitedUser = personaInvited.getUser().getId();

		FriendListEntity friendListEntity = friendListDAO.findUsersRelationship(personaSenderUser, personaInvitedUser);
		if (friendListEntity == null) {
			System.out.println("### User ID " + personaSenderUser + " has tried to accept the FriendEntity request, which is not exist!");
			return null;
		}
		if (resolution == 0) { // Resolution: 0 - request declined, 1 - request accepted
			int blockStatus = friendListEntity.getBlockStatus();
			if (blockStatus == 0) { 
				removeFriend(personaInvitedId, personaSenderId);
				return null;
			}
			else {
				friendListEntity.setStatus(0);
				friendListDAO.update(friendListEntity);
				return null;
			}
		}
		friendListEntity.setStatus(2); // Relationship status (0 - blocked, 1 - friend request pending, 2 - friends)
		friendListEntity.setBlockStatus(0);
		friendListDAO.update(friendListEntity);

		int invitedPresence = 3;
		PersonaPresenceEntity invitedPresenceEntity = personaPresenceDAO.findByUserId(personaInvitedUser);
		if (invitedPresenceEntity.getActivePersonaId().equals(personaInvitedId)) {
			invitedPresence = invitedPresenceEntity.getPersonaPresence();
		}
		
		// Send all info to personaSender
		FriendPersona friendPersona = new FriendPersona();
		friendPersona.setIconIndex(personaInvited.getIconIndex());
		friendPersona.setLevel(personaInvited.getLevel());
		friendPersona.setName(personaInvited.getName());
		friendPersona.setOriginalName(personaInvited.getName());
		friendPersona.setPersonaId(personaInvitedId);
		friendPersona.setPresence(invitedPresence);
		friendPersona.setUserId(personaInvitedUser);

		XMPP_FriendResultType friendResultType = new XMPP_FriendResultType();
		friendResultType.setFriendPersona(friendPersona);
		friendResultType.setResult(resolution);

		XMPP_ResponseTypeFriendResult responseTypeFriendResult = new XMPP_ResponseTypeFriendResult();
		responseTypeFriendResult.setFriendResult(friendResultType);

		XmppFriend xmppFriend = new XmppFriend(personaSenderId, openFireSoapBoxCli);
		xmppFriend.sendResponseFriendRequest(responseTypeFriendResult);

		PersonaBase personaBase = new PersonaBase();
		personaBase.setBadges(new ArrayOfBadgePacket());
		personaBase.setIconIndex(personaSender.getIconIndex());
		personaBase.setLevel(personaSender.getLevel());
		personaBase.setMotto(personaSender.getMotto());
		personaBase.setName(personaSender.getName());
		personaBase.setPersonaId(personaSenderId);
		personaBase.setPresence(0);
		personaBase.setScore(personaSender.getScore());
		personaBase.setUserId(personaSenderUser);
		return personaBase;
	}

	public void createNewFriendListEntry(Long userSenderId, Long senderPersonaId, Long userInvitedId, Long invitedPersonaId, 
			int status, int blockStatus) {
		FriendListEntity friendListInsert = new FriendListEntity();
		friendListInsert.setUserId_A(userSenderId);
		friendListInsert.setPersonaId_A(senderPersonaId);
		friendListInsert.setUserId_B(userInvitedId);
		friendListInsert.setPersonaId_B(invitedPersonaId);
		friendListInsert.setStatus(status);
		friendListInsert.setBlockStatus(blockStatus);
		friendListDAO.insert(friendListInsert);
	}
	
	// Remove the relationship entry
	public void removeFriend(Long personaId, Long friendPersonaId) {
		PersonaEntity personaInvited = personaDAO.findById(personaId);
		PersonaEntity personaSender = personaDAO.findById(friendPersonaId);
		if (personaInvited == null || personaSender == null) {
			System.out.println("### Some RemoveFriendRequest contains invaild Persona ID references.");
			return;
		}
		Long personaSenderUser = personaSender.getUser().getId();
		Long personaInvitedUser = personaInvited.getUser().getId();
		FriendListEntity friendListEntity = friendListDAO.findUsersRelationship(personaSenderUser, personaInvitedUser);
		if (friendListEntity != null) {
			friendListDAO.delete(friendListEntity);
		}
	}

	public void sendXmppPresence(PersonaEntity personaEntity, int presence, Long to) {
		XMPP_ResponseTypePersonaBase personaPacket = new XMPP_ResponseTypePersonaBase();
		PersonaBase xmppPersonaBase = new PersonaBase();
		xmppPersonaBase.setBadges(new ArrayOfBadgePacket());
		xmppPersonaBase.setIconIndex(personaEntity.getIconIndex());
		xmppPersonaBase.setLevel(personaEntity.getLevel());
		xmppPersonaBase.setMotto(personaEntity.getMotto());
		xmppPersonaBase.setName(personaEntity.getName());
		xmppPersonaBase.setPersonaId(personaEntity.getPersonaId());
		xmppPersonaBase.setPresence(presence);
		xmppPersonaBase.setScore(personaEntity.getScore());
		xmppPersonaBase.setUserId(personaEntity.getUser().getId());
		personaPacket.setPersonaBase(xmppPersonaBase);
		openFireSoapBoxCli.send(personaPacket, to);
	}

	// Update the player status for his friend-list
	public void sendXmppPresenceToAllFriends(PersonaEntity personaEntity, int presence) {
		Long userId = personaEntity.getUser().getId();
		List<FriendListEntity> friendList = friendListDAO.getUserFriendList(userId);
		if (friendList != null) {
			for (FriendListEntity friend : friendList) {
				Long friendUserId = 0L;
				Long friendPersonaId = 0L;
				if (friend.getUserId_A().equals(userId)) { // User A gets User B as Friend
					friendPersonaId = friend.getPersonaId_B();
					friendUserId = friend.getUserId_B();
				}
				else { // User B gets User A as Friend
					friendPersonaId = friend.getPersonaId_A();
					friendUserId = friend.getUserId_A();
				}
				if (!personaPresenceDAO.isUserNotOnline(friendUserId)) {
					sendXmppPresence(personaEntity, presence, friendPersonaId);
				}
			}
		}
	}

//	@Schedule(minute = "*", hour = "*", persistent = false)
	public void updateOfflinePresence() {
		List<TokenSessionEntity> findByActive = tokenSessionDAO.findByActive();
		for (TokenSessionEntity tokenSessionEntity : findByActive) {
			Long activePersonaId = tokenSessionEntity.getActivePersonaId();
			if (!openFireRestApiCli.isOnline(activePersonaId)) {
				PersonaEntity personaEntity = personaBO.getPersonaById(activePersonaId);
				sendXmppPresenceToAllFriends(personaEntity, 0);
				personaPresenceDAO.updatePersonaPresence(activePersonaId, 0);
			}
		}
	}
}
