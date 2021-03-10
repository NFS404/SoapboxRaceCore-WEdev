package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.EventBO;
import com.soapboxrace.core.bo.EventMissionsBO;
import com.soapboxrace.core.bo.EventResultBO;
import com.soapboxrace.core.bo.FriendBO;
import com.soapboxrace.core.bo.LobbyBO;
import com.soapboxrace.core.bo.LobbyCountdownBO;
import com.soapboxrace.core.bo.MatchmakingBO;
import com.soapboxrace.core.bo.PersonaBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.dao.LobbyEntrantDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.CustomCarTrans;
import com.soapboxrace.jaxb.http.LobbyInfo;
import com.soapboxrace.jaxb.http.SecurityChallenge;
import com.soapboxrace.jaxb.http.SessionInfo;

@Path("/matchmaking")
public class MatchMaking {

	@EJB
	private EventBO eventBO;

	@EJB
	private LobbyBO lobbyBO;

	@EJB
	private TokenSessionBO tokenSessionBO;

	@EJB
	private PersonaBO personaBO;
	
	@EJB
    private LobbyDAO lobbyDAO;
	
	@EJB
	private EventResultBO eventResultBO;
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private EventMissionsBO eventMissionsBO;
	
	@EJB
	private EventDAO eventDAO;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private FriendBO friendBO;
	
	@EJB
	private PersonaDAO personaDAO;
	
	@EJB
	private MatchmakingBO matchmakingBO;
	
	@EJB
	private LobbyCountdownBO lobbyCountdownBO;
	
	@EJB
	private LobbyEntrantDAO lobbyEntrantDAO;
	
	@Context
	private HttpServletRequest sr;

	@PUT
	@Secured
	@Path("/joinqueueracenow")
	@Produces(MediaType.APPLICATION_XML)
	public String joinQueueRaceNow(@HeaderParam("securityToken") String securityToken) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		CustomCarTrans customCar = personaBO.getDefaultCar(activePersonaId).getCustomCar();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(customCar.getPhysicsProfileHash());
//		lobbyBO.joinFastLobby(securityToken, activePersonaId, defaultCar.getCustomCar().getCarClassHash(), lobbyBO.carDivision(defaultCar.getCustomCar().getCarClassHash()), defaultCar.getCustomCar().getRaceFilter());
		if (!carClassesEntity.getQuickRaceAllowed()) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You cannot join to racing on this vehicle."), activePersonaId);
		}
		else {
			lobbyBO.joinFastLobby(securityToken, activePersonaId, customCar.getCarClassHash(), customCar.getRaceFilter());
		}
		return "";
	}

	@PUT
	@Secured
	@Path("/joinqueueevent/{eventId}")
	@Produces(MediaType.APPLICATION_XML)
	public String joinQueueEvent(@HeaderParam("securityToken") String securityToken, @PathParam("eventId") int eventId) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		CustomCarTrans customCar = personaBO.getDefaultCar(activePersonaId).getCustomCar();
		lobbyBO.joinQueueEvent(activePersonaId, eventId, customCar.getCarClassHash());
		return "";
	}

	@PUT
	@Secured
	@Path("/leavequeue")
	@Produces(MediaType.APPLICATION_XML)
	public String leaveQueue(@HeaderParam("securityToken") String securityToken) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		matchmakingBO.removePlayerFromQueue(activePersonaId);
		LobbyEntity lobbyEntity = lobbyDAO.findByHosterPersona(activePersonaId);
		if (lobbyEntity != null && lobbyEntrantDAO.isLobbyEmpty(lobbyEntity) && !lobbyEntity.isActiveLobby()) { // Delete the empty lobby
			System.out.println("### /leavequeue delete");
			lobbyCountdownBO.endLobby(lobbyEntity);
		}
		return "";
	}

	@PUT
	@Secured
	@Path("/leavelobby")
	@Produces(MediaType.APPLICATION_XML)
	public String leavelobby(@HeaderParam("securityToken") String securityToken) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		Long activeLobbyId = tokenSessionBO.getActiveLobbyId(securityToken);
		if (activeLobbyId != null && !activeLobbyId.equals(0L)) {
			lobbyBO.deleteLobbyEntrant(activePersonaId, activeLobbyId);
		}
		LobbyEntity lobbyEntity = lobbyDAO.findById(activeLobbyId);
		System.out.println("### /leavelobby");
		if (lobbyEntrantDAO.isLobbyEmpty(lobbyEntity) && !lobbyEntity.isActiveLobby()) { // Delete the empty lobby
			System.out.println("### /leavelobby delete");
			lobbyCountdownBO.endLobby(lobbyEntity);
		}
		return "";
	}

	@GET
	@Secured
	@Path("/launchevent/{eventId}") // Starts single-player event
	@Produces(MediaType.APPLICATION_XML)
	public SessionInfo launchEvent(@HeaderParam("securityToken") String securityToken, @PathParam("eventId") int eventId) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		SecurityChallenge securityChallenge = new SecurityChallenge();
		securityChallenge.setChallengeId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		securityChallenge.setLeftSize(14);
		securityChallenge.setPattern("FFFFFFFFFFFFFFFF");
		securityChallenge.setRightSize(50);
		SessionInfo sessionInfo = new SessionInfo();
		sessionInfo.setChallenge(securityChallenge);
		sessionInfo.setEventId(eventId);
		EventSessionEntity createEventSession = eventBO.createSPEventSession(eventId, activePersonaId);
		Long eventSessionId = createEventSession.getId();
		
		sessionInfo.setSessionId(eventSessionId);
		tokenSessionBO.setActiveLobbyId(securityToken, 0L);
	
		EventEntity eventEntity = eventDAO.findById(eventId);
		eventMissionsBO.getEventMissionInfo(eventEntity, activePersonaId);
		
		return sessionInfo;
	}

	@PUT
	@Secured
	@Path("/makeprivatelobby/{eventId}")
	@Produces(MediaType.APPLICATION_XML)
	public String makePrivateLobby(@HeaderParam("securityToken") String securityToken, @PathParam("eventId") int eventId, @PathParam("carClassHash") int carClassHash) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		CustomCarTrans customCar = personaBO.getDefaultCar(activePersonaId).getCustomCar();
		lobbyBO.createPrivateLobby(activePersonaId, eventId, customCar.getCarClassHash());
		return "";
	}

	@PUT
	@Secured
	@Path("/acceptinvite")
	@Produces(MediaType.APPLICATION_XML)
	public LobbyInfo acceptInvite(@HeaderParam("securityToken") String securityToken, @QueryParam("lobbyInviteId") Long lobbyInviteId) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		tokenSessionBO.setActiveLobbyId(securityToken, lobbyInviteId);
		return lobbyBO.acceptinvite(activePersonaId, lobbyInviteId);
	}

	@PUT
	@Secured
	@Path("/declineinvite")
	@Produces(MediaType.APPLICATION_XML)
	public String declineInvite(@HeaderParam("securityToken") String securityToken, @QueryParam("lobbyInviteId") Long lobbyInviteId) {
		LobbyEntity lobbyEntity = lobbyDAO.findById(lobbyInviteId);
		System.out.println("### /declineinvite");
		if (lobbyEntrantDAO.isLobbyEmpty(lobbyEntity) && !lobbyEntity.isActiveLobby()) { // Delete the empty lobby
			System.out.println("### /declineinvite delete");
			lobbyCountdownBO.endLobby(lobbyEntity);
		}
		return "";
	}

}
