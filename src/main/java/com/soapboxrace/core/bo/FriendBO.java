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
import com.soapboxrace.core.jpa.ReportEntity;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.jpa.TokenSessionEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.ArrayOfBadgePacket;
import com.soapboxrace.jaxb.http.ArrayOfFriendPersona;
import com.soapboxrace.jaxb.http.FriendPersona;
import com.soapboxrace.jaxb.http.FriendResult;
import com.soapboxrace.jaxb.http.PersonaBase;
import com.soapboxrace.jaxb.http.PersonaFriendsList;
import com.soapboxrace.jaxb.xmpp.XMPP_FriendPersonaType;
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

	public PersonaFriendsList getFriendListFromUserId(Long userId) {
		ArrayOfFriendPersona arrayOfFriendPersona = new ArrayOfFriendPersona();
		List<FriendPersona> friendPersonaList = arrayOfFriendPersona.getFriendPersona();

		List<FriendListEntity> friendList = friendListDAO.findByOwnerId(userId);
		for (FriendListEntity entity : friendList) {

			PersonaEntity personaEntity = personaDAO.findById(entity.getPersonaId());
			if (personaEntity == null) {
				continue;
			}

			int presence = 3;
			if (entity.getIsAccepted()) {
				presence = driverPersonaBO.getPersonaPresenceByName(personaEntity.getName()).getPresence();
			}
			addPersonaToFriendList(friendPersonaList, personaEntity, presence);
		}

		PersonaFriendsList personaFriendsList = new PersonaFriendsList();
		personaFriendsList.setFriendPersona(arrayOfFriendPersona);
		return personaFriendsList;
	}

	private void addPersonaToFriendList(List<FriendPersona> friendPersonaList, PersonaEntity personaEntity, int presence) {
		FriendPersona friendPersona = new FriendPersona();
		friendPersona.setIconIndex(personaEntity.getIconIndex());
		friendPersona.setLevel(personaEntity.getLevel());
		friendPersona.setName(personaEntity.getName());
		friendPersona.setOriginalName(personaEntity.getName());
		friendPersona.setPersonaId(personaEntity.getPersonaId());
		friendPersona.setPresence(presence);
		friendPersona.setSocialNetwork(0);
		friendPersona.setUserId(personaEntity.getUser().getId());
		friendPersonaList.add(friendPersona);
	}

	// Teams actions parser into "add a friend" window - Hypercycle
	// XMPP messages can go into timeouts, if ' symbol is used
	// so i did some weird 'else' outputs for messages
	public FriendResult sendFriendRequest(Long personaId, String displayName, String reqMessage) {
		boolean teamsActionInit = false;
		PersonaEntity personaSender = personaDAO.findById(personaId);
		if (displayName.contains("/TEAMJOIN ")) {
			teamsActionInit = true;
			String teamName = displayName.replaceFirst("/TEAMJOIN ", "");
			TeamsEntity teamToJoin = teamsDAO.findByName(teamName);
			if (teamToJoin == null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This team is not exist."), personaId);
				return null;
			}
			if (personaSender.getLevel() < 30) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### To participate on teams, LVL 30 and higher is required."), personaId);
				return null;
			}
			if (!teamToJoin.getOpenEntry()) {
				Long teamleaderId = teamToJoin.getLeader().getPersonaId();
				List<ReportEntity> teamInviteCheck = reportDAO.findTeamInvite(teamleaderId, personaId);
				if (teamToJoin.getPlayersCount() >= 8) {
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This team is full."), personaId);
					return null;
				}
				if (!teamInviteCheck.isEmpty() && teamToJoin.getPlayersCount() < 8) {
					teamsBO.teamJoinIG(personaSender, teamToJoin);
					reportDAO.deleteTeamInvite(teamleaderId, personaId);
//					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You're joined to team!"), personaId);
					return null;
				}
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This team is invite-only."), personaId);
				return null;
			}
			if (personaSender.getTeam() == teamToJoin) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You already on this team..."), personaId);
				return null;
			}
			if (personaSender.getTeam() != null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You already on another team..."), personaId);
				return null;
			}
			if (teamToJoin.getPlayersCount() >= 8) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This team is full."), personaId);
				return null;
			}
			teamsBO.teamJoinIG(personaSender, teamToJoin);
			return null;
		}
		if (displayName.contains("/TEAMLEAVE")) {
			teamsActionInit = true;
			TeamsEntity playerTeamLeave = personaSender.getTeam();
			if (playerTeamLeave == null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Get a team first..."), personaId);
				return null;
			}
			if (playerTeamLeave.getLeader() == personaSender) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You can't leave your own team."), personaId);
				return null;
			}
			teamsBO.teamLeaveIG(personaSender, playerTeamLeave);
			return null;
		}
		if (displayName.contains("/TEAMKICK ")) {
			// System.out.println("TeamKick init");
			teamsActionInit = true;
			TeamsEntity leaderTeam = personaSender.getTeam();
			String badTeammateName = displayName.replaceFirst("/TEAMKICK ", "");
			PersonaEntity badTeammate = personaDAO.findByName(badTeammateName);
			if (leaderTeam == null) {
				// System.out.println("TeamKick leaderTeam null");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Get a team first..."), personaId);
				return null;
			}
			if (leaderTeam.getLeader() != personaSender) {
				// System.out.println("TeamKick wrongLeader");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You're is not a team leader."), personaId);
				return null;
			}
			if (leaderTeam.getLeader() == badTeammate) {
				// System.out.println("TeamKick KickYourself");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You can't leave your own team."), personaId);
				return null;
			}
			if (badTeammate == null) {
				// System.out.println("TeamKick wrongNick");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Wrong nickname."), personaId);
				return null;
			}
			if (badTeammate.getTeam() != leaderTeam) {
				// System.out.println("TeamKick wrongPlayerTeam");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This player is not on your team..."), personaId);
				return null;
			}
			else {
				// System.out.println("TeamKick else");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Player is no longer on this team."), personaId);
			}
			// System.out.println("TeamKick leave init");
			teamsBO.teamLeaveIG(badTeammate, leaderTeam);
			return null;
		}
		if (displayName.contains("/TEAMBREAK")) {
			System.out.println("TeamBreak init");
			teamsActionInit = true;
			TeamsEntity leaderTeam = personaSender.getTeam();
			if (leaderTeam == null) {
				System.out.println("TeamBreak leaderTeam null");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Get a team first..."), personaId);
				return null;
			}
			if (leaderTeam.getLeader() != personaSender) {
				System.out.println("TeamBreak wrongLeader");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You're is not a team leader."), personaId);
				return null;
			}
			System.out.println("TeamBreak exit init");
			teamsBO.teamBreakIG(personaSender, leaderTeam);
			return null;
		}
		if (displayName.contains("/TEAMPLAYERS ")) {
			teamsActionInit = true;
			String teamPlayers = "";
			String teamName = displayName.replaceFirst("/TEAMPLAYERS ", "");
			TeamsEntity teamToCheck = teamsDAO.findByName(teamName);
			if (teamToCheck == null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This team is not exist."), personaId);
				return null;
			}
			if (teamToCheck.getPlayersCount() >= 1) {
				List<PersonaEntity> listOfProfiles = teamToCheck.getListOfTeammates();
				for (PersonaEntity personaEntityTeam : listOfProfiles) {
					teamPlayers = teamPlayers.concat(personaEntityTeam.getName() + " ");
				}
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Team players: " + teamPlayers), personaId);
				return null;
			}
			return null;
		}
		if (displayName.contains("/TEAMENTRY ")) {
			teamsActionInit = true;
			TeamsEntity leaderTeam = personaSender.getTeam();
			String entryValue = displayName.replaceFirst("/TEAMENTRY ", "");
			boolean openEntryBool = false;
			if (entryValue.contentEquals("PUBLIC")) {
				openEntryBool = true;
			}
			if (entryValue.contentEquals("PRIVATE")) {
				openEntryBool = false;
			}
			if (leaderTeam == null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Get a team first..."), personaId);
				return null;
			}
			if (leaderTeam.getLeader() != personaSender) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You're is not a team leader."), personaId);
				return null;
			}
			if (leaderTeam.getOpenEntry() == openEntryBool) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Team already has that status."), personaId);
				return null;
			}
			if (entryValue.contentEquals("PUBLIC") || entryValue.contentEquals("PRIVATE")) {
				teamsBO.teamEntryIG(openEntryBool, leaderTeam);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Team's entry rule has changed."), personaId);
				return null;
			}
			return null;
		}
		
		// Applies the vinyl from DB, uses OwnedCarTrans as a blank for already existed scripts (Not the ideal way...)
		if (displayName.contains("/VINYL ")) {
			vinylStorageBO.vinylStorageApply(personaId, displayName);
			return null;
		}
		if (displayName.contentEquals("/VINYLUPLOAD")) {
			vinylStorageBO.vinylStorageUpload(personaId);
			return null;
		}
		if (displayName.contains("/VINYLREMOVE ")) {
			vinylStorageBO.vinylStorageRemove(personaId, displayName);
			return null;
		}
		if (displayName.contains("/VINYLWIPEALL")) {
			vinylStorageBO.vinylStorageRemoveAll(personaId);
			return null;
		}
		if (displayName.contains("/MODDER")) {
			UserEntity userEntity = personaSender.getUser();
			if (userEntity.isModder()) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You're already have a Modder status."), personaId);
				return null;
			}
			userEntity.setModder(true);
			userDAO.update(userEntity);
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Modder access is enabled, please restart the game."), personaId);
			return null;
		}
		// Send persona's money to another persona (/SENDMONEY nickName money)
		if (displayName.contains("/SENDMONEY ")) {
			userBO.sendMoney(personaSender, displayName);
			return null;
		}
		// Get extra reserve money to current persona
		if (displayName.contains("/GETMONEY")) {
			userBO.getMoney(personaSender);
			return null;
		}
		// Freeroam Sync module switch (experimental)
		if (displayName.contains("/SYNCSWITCH")) {
			UserEntity userEntity = personaSender.getUser();
			if (userEntity.getFRSyncAlt()) {
				userEntity.setFRSyncAlt(false);
				userDAO.update(userEntity);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Sync shard is Main now, go to the Garage and back."), personaId);
				return null;
			}
			userEntity.setFRSyncAlt(true);
			userDAO.update(userEntity);
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Sync shard is Alternative now, go to the Garage and back."), personaId);
			return null;
		}
		// Re-calc persona's score counter
		if (displayName.contains("/RECALCSCORE")) {
			achievementsBO.forceScoreCalc(personaSender);
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Score is re-calculated, re-login into persona."), personaId);
			return null;
		}
		// default add-a-friend interaction
		if (!teamsActionInit) {
			PersonaEntity personaInvited = personaDAO.findByName(displayName);
			if (personaSender == null || personaInvited == null) {
				return null;
			}
			if (personaSender.getPersonaId() == personaInvited.getPersonaId()) {
				return null;
			}
			FriendListEntity friendListEntity = friendListDAO.findByOwnerIdAndFriendPersona(personaSender.getUser().getId(), personaInvited.getPersonaId());
			if (friendListEntity != null) {
				return null;
			}
			XMPP_FriendPersonaType friendPersonaType = new XMPP_FriendPersonaType();
			friendPersonaType.setIconIndex(personaSender.getIconIndex());
			friendPersonaType.setLevel(personaSender.getLevel());
			friendPersonaType.setName(personaSender.getName());
			friendPersonaType.setOriginalName(personaSender.getName());
			friendPersonaType.setPersonaId(personaSender.getPersonaId());
			friendPersonaType.setPresence(3);
			friendPersonaType.setUserId(personaSender.getUser().getId());

			XmppFriend xmppFriend = new XmppFriend(personaInvited.getPersonaId(), openFireSoapBoxCli);
			xmppFriend.sendFriendRequest(friendPersonaType);

			// Insert db record for invited player
			FriendListEntity friendListInsert = new FriendListEntity();
			friendListInsert.setUserOwnerId(personaInvited.getUser().getId());
			friendListInsert.setUserId(personaSender.getUser().getId());
			friendListInsert.setPersonaId(personaSender.getPersonaId());
			friendListInsert.setIsAccepted(false);
			friendListDAO.insert(friendListInsert);

			FriendPersona friendPersona = new FriendPersona();
			friendPersona.setIconIndex(personaInvited.getIconIndex());
			friendPersona.setLevel(personaInvited.getLevel());
			friendPersona.setName(personaInvited.getName());
			friendPersona.setOriginalName(personaInvited.getName());
			friendPersona.setPersonaId(personaInvited.getPersonaId());
			friendPersona.setPresence(0);
			friendPersona.setSocialNetwork(0);
			friendPersona.setUserId(personaInvited.getUser().getId());

			FriendResult friendResult = new FriendResult();
			friendResult.setPersona(friendPersona);
			friendResult.setResult(0);
			return friendResult;
		}
		return null;
	}

	public PersonaBase sendResponseFriendRequest(Long personaId, Long friendPersonaId, int resolution) {
		// Execute some DB things
		PersonaEntity personaInvited = personaDAO.findById(personaId);
		PersonaEntity personaSender = personaDAO.findById(friendPersonaId);
		if (personaInvited == null || personaSender == null) {
			return null;
		}

		if (resolution == 0) {
			removeFriend(personaInvited.getPersonaId(), personaSender.getPersonaId());
			return null;
		}

		FriendListEntity friendListEntity = friendListDAO.findByOwnerIdAndFriendPersona(personaInvited.getUser().getId(), personaSender.getPersonaId());
		if (friendListEntity == null) {
			return null;
		}

		friendListEntity.setIsAccepted(true);
		friendListDAO.update(friendListEntity);

		// Insert db record for sender player
		friendListEntity = friendListDAO.findByOwnerIdAndFriendPersona(personaSender.getUser().getId(), personaInvited.getPersonaId());
		if (friendListEntity == null) {
			FriendListEntity friendListInsert = new FriendListEntity();
			friendListInsert.setUserOwnerId(personaSender.getUser().getId());
			friendListInsert.setUserId(personaInvited.getUser().getId());
			friendListInsert.setPersonaId(personaInvited.getPersonaId());
			friendListInsert.setIsAccepted(true);
			friendListDAO.insert(friendListInsert);
		}

		// Send all info to personaSender
		FriendPersona friendPersona = new FriendPersona();
		friendPersona.setIconIndex(personaInvited.getIconIndex());
		friendPersona.setLevel(personaInvited.getLevel());
		friendPersona.setName(personaInvited.getName());
		friendPersona.setOriginalName(personaInvited.getName());
		friendPersona.setPersonaId(personaInvited.getPersonaId());
		friendPersona.setPresence(3);
		friendPersona.setUserId(personaInvited.getUser().getId());

		XMPP_FriendResultType friendResultType = new XMPP_FriendResultType();
		friendResultType.setFriendPersona(friendPersona);
		friendResultType.setResult(resolution);

		XMPP_ResponseTypeFriendResult responseTypeFriendResult = new XMPP_ResponseTypeFriendResult();
		responseTypeFriendResult.setFriendResult(friendResultType);

		XmppFriend xmppFriend = new XmppFriend(personaSender.getPersonaId(), openFireSoapBoxCli);
		xmppFriend.sendResponseFriendRequest(responseTypeFriendResult);

		PersonaBase personaBase = new PersonaBase();
		personaBase.setBadges(new ArrayOfBadgePacket());
		personaBase.setIconIndex(personaSender.getIconIndex());
		personaBase.setLevel(personaSender.getLevel());
		personaBase.setMotto(personaSender.getMotto());
		personaBase.setName(personaSender.getName());
		personaBase.setPersonaId(personaSender.getPersonaId());
		personaBase.setPresence(0);
		personaBase.setScore(personaSender.getScore());
		personaBase.setUserId(personaSender.getUser().getId());
		return personaBase;
	}

	public void removeFriend(Long personaId, Long friendPersonaId) {
		PersonaEntity personaInvited = personaDAO.findById(personaId);
		PersonaEntity personaSender = personaDAO.findById(friendPersonaId);
		if (personaInvited == null || personaSender == null) {
			return;
		}

		FriendListEntity friendListEntity = friendListDAO.findByOwnerIdAndFriendPersona(personaInvited.getUser().getId(), personaSender.getPersonaId());
		if (friendListEntity != null) {
			friendListDAO.delete(friendListEntity);
		}

		friendListEntity = friendListDAO.findByOwnerIdAndFriendPersona(personaSender.getUser().getId(), personaInvited.getPersonaId());
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

	public void sendXmppPresenceToAllFriends(PersonaEntity personaEntity, int presence) {
//		if (!openFireRestApiCli.isRestApiEnabled()) {
//			return;
//		}
		List<FriendListEntity> friends = friendListDAO.findByOwnerId(personaEntity.getUser().getId());
		if (friends != null) {
			for (FriendListEntity friend : friends) {
				if (friend.getIsAccepted() && !tokenSessionDAO.isUserNotOnline(friend.getUserId())) {
					sendXmppPresence(personaEntity, presence, friend.getPersonaId());
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
