package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.AchievementsBO;
import com.soapboxrace.core.bo.EventBO;
import com.soapboxrace.core.bo.EventResultBO;
import com.soapboxrace.core.bo.LobbyBO;
import com.soapboxrace.core.bo.PersonaBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.jaxb.http.CustomCarTrans;
import com.soapboxrace.jaxb.http.LobbyInfo;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
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
	private PersonaDAO personaDao;
	
	@EJB
    private LobbyDAO lobbyDAO;
	
	@EJB
	private AchievementsBO achievementsBO;
	
	@EJB
	private EventResultBO eventResultBO;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@PUT
	@Secured
	@Path("/joinqueueracenow")
	@Produces(MediaType.APPLICATION_XML)
	public String joinQueueRaceNow(@HeaderParam("securityToken") String securityToken) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		CustomCarTrans customCar = personaBO.getDefaultCar(activePersonaId).getCustomCar();
//		lobbyBO.joinFastLobby(securityToken, activePersonaId, defaultCar.getCustomCar().getCarClassHash(), lobbyBO.carDivision(defaultCar.getCustomCar().getCarClassHash()), defaultCar.getCustomCar().getRaceFilter());
		lobbyBO.joinFastLobby(securityToken, activePersonaId, customCar.getCarClassHash(), customCar.getRaceFilter(), customCar.getPhysicsProfileHash());
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
	public String leaveQueue() {
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
		return "";
	}

	@GET
	@Secured
	@Path("/launchevent/{eventId}")
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
		EventSessionEntity createEventSession = eventBO.createEventSession(eventId);
		sessionInfo.setSessionId(createEventSession.getId());
		tokenSessionBO.setActiveLobbyId(securityToken, 0L);
		
		if (eventId == 1003) {
			achievementsBO.broadcastUICustom(activePersonaId, "Beat the time: 3:06.00");
		}
		if (eventId == 1004) {
			achievementsBO.broadcastUICustom(activePersonaId, "Finish 1st");
		}
		if (eventId == 1018) {
			achievementsBO.broadcastUICustom(activePersonaId, "Finish 1st after 4:15.00");
		}
		if (eventId == 1005) {
			achievementsBO.broadcastUICustom(activePersonaId, "Finish 1st");
		}
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
		return "";
	}

}
