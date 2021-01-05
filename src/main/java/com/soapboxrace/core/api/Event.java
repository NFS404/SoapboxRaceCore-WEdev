package com.soapboxrace.core.api;

import java.io.InputStream;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.AchievementsBO;
import com.soapboxrace.core.bo.EventBO;
import com.soapboxrace.core.bo.EventResultBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventMode;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppEvent;
import com.soapboxrace.jaxb.http.DragArbitrationPacket;
import com.soapboxrace.jaxb.http.PursuitArbitrationPacket;
import com.soapboxrace.jaxb.http.PursuitEventResult;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.http.TeamEscapeArbitrationPacket;
import com.soapboxrace.jaxb.util.JAXBUtility;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeRouteEntrantResult;
import com.soapboxrace.jaxb.xmpp.XMPP_RouteEntrantResultType;

@Path("/event")
public class Event {

	@Context
	private HttpServletRequest sr;
	
	@EJB
	private TokenSessionBO tokenBO;

	@EJB
	private EventBO eventBO;

	@EJB
	private EventResultBO eventResultBO;
	
	@EJB
	private AchievementsBO achievementsBO;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private EventDataDAO eventDataDAO;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@POST
	@Secured
	@Path("/abort")
	@Produces(MediaType.APPLICATION_JSON)
	// Player disconnects from the current race
	public String abort(@HeaderParam("securityToken") String securityToken, @QueryParam("eventSessionId") Long eventSessionId) {
		Long[] infoPackage = tokenBO.getActivePersonaUserTeamId(securityToken);
		Long activePersonaId = infoPackage[0].longValue();
		Long userId = infoPackage[1].longValue();
		
		// Save the abort info on the event data
		PersonaPresenceEntity personaPresenceEntity = personaPresenceDAO.findByUserId(userId);
		EventDataEntity eventDataEntity = eventDataDAO.findById(personaPresenceEntity.getCurrentEventDataId());
		int eventMode = eventDataEntity.getEventModeId();
		eventDataEntity.setFinishReason(8202); // Aborted
		eventDataEntity.setServerEventDuration(0);
		eventDataDAO.update(eventDataEntity);
		
		if (eventMode == 4 || eventMode == 9 || eventMode == 100) { // Circuit & Sprint, and Interceptor
			RouteArbitrationPacket routeArbitrationPacket = new RouteArbitrationPacket();
			eventBO.sendXmppPacketRoute(eventSessionId, activePersonaId, routeArbitrationPacket, 0, false);
		}
		if (eventMode == 24) { // Team Escape
			eventBO.sendXmppPacketTEAbort(eventSessionId, activePersonaId);
		}
		if (eventMode == 19) { // Drag
			eventBO.sendXmppPacketDragAbort(eventSessionId, activePersonaId);
		}
		return "";
	}

	@PUT
	@Secured
	@Path("/launched")
	@Produces(MediaType.APPLICATION_XML)
	public String launched(@HeaderParam("securityToken") String securityToken, @QueryParam("eventSessionId") Long eventSessionId) {
		Long eventStarted = System.currentTimeMillis(); // Server-side event timer start
		Long activePersonaId = tokenBO.getActivePersonaId(securityToken);
		Long eventDataId = eventBO.createEventDataSession(activePersonaId, eventSessionId, eventStarted);
		eventBO.createEventPowerupsSession(activePersonaId, eventDataId);
		eventBO.createEventCarInfo(activePersonaId, eventDataId);
		EventEntity eventEntity = eventDataDAO.findById(eventDataId).getEvent();
		int eventModeId = eventEntity.getEventModeId();
		Long timeLimit = eventEntity.getTimeLimit();
		personaPresenceDAO.updateCurrentEvent(activePersonaId, eventDataId, eventModeId, eventSessionId);
		
		if (timeLimit != 0 && eventModeId != 24) { // Team Escape have it's own timeout action
			eventResultBO.timeLimitTimer(eventSessionId, timeLimit);
		}
		return "";
	}

	@POST
	@Secured
	@Path("/arbitration")
	@Produces(MediaType.APPLICATION_XML)
	public Object arbitration(InputStream arbitrationXml, @HeaderParam("securityToken") String securityToken,
			@QueryParam("eventSessionId") Long eventSessionId) {
		Long eventEnded = System.currentTimeMillis(); // Server-side event timer stop
		EventSessionEntity eventSessionEntity = eventBO.findEventSessionById(eventSessionId);
		EventEntity event = eventSessionEntity.getEvent();
		EventMode eventMode = EventMode.fromId(event.getEventModeId());
		Long activePersonaId = tokenBO.getActivePersonaId(securityToken);

		switch (eventMode) {
		case CIRCUIT:
		case SPRINT:
		case INTERCEPTOR_MP:
			RouteArbitrationPacket routeArbitrationPacket = JAXBUtility.unMarshal(arbitrationXml, RouteArbitrationPacket.class);
			return eventResultBO.handleRaceEnd(eventSessionEntity, activePersonaId, routeArbitrationPacket, eventEnded);
		case DRAG:
			DragArbitrationPacket dragArbitrationPacket = JAXBUtility.unMarshal(arbitrationXml, DragArbitrationPacket.class);
			return eventResultBO.handleDragEnd(eventSessionEntity, activePersonaId, dragArbitrationPacket, eventEnded);
		case MEETINGPLACE:
			break;
		case PURSUIT_MP:
			TeamEscapeArbitrationPacket teamEscapeArbitrationPacket = JAXBUtility.unMarshal(arbitrationXml, TeamEscapeArbitrationPacket.class);
			return eventResultBO.handleTeamEscapeEnd(eventSessionEntity, activePersonaId, teamEscapeArbitrationPacket, eventEnded);
		case PURSUIT_SP:
			PursuitArbitrationPacket pursuitArbitrationPacket = JAXBUtility.unMarshal(arbitrationXml, PursuitArbitrationPacket.class);
			return eventResultBO.handlePursuitEnd(eventSessionEntity, activePersonaId, pursuitArbitrationPacket, false, eventEnded);
		default:
			break;
		}
		personaPresenceDAO.updateCurrentEventPost(activePersonaId, null, 0, null, false);
		return "";
	}

	@POST
	@Secured
	@Path("/bust")
	@Produces(MediaType.APPLICATION_XML)
	public PursuitEventResult bust(InputStream bustXml, @HeaderParam("securityToken") String securityToken, @QueryParam("eventSessionId") Long eventSessionId) {
		EventSessionEntity eventSessionEntity = eventBO.findEventSessionById(eventSessionId);
		Long eventEnded = System.currentTimeMillis(); // Server-side event timer stop
		PursuitArbitrationPacket pursuitArbitrationPacket = (PursuitArbitrationPacket) JAXBUtility.unMarshal(bustXml, PursuitArbitrationPacket.class);
		PursuitEventResult pursuitEventResult = new PursuitEventResult();
		Long activePersonaId = tokenBO.getActivePersonaId(securityToken);
		pursuitEventResult = eventResultBO.handlePursuitEnd(eventSessionEntity, activePersonaId, pursuitArbitrationPacket, true, eventEnded);
		personaPresenceDAO.updateCurrentEventPost(activePersonaId, null, 0, null, false);
		return pursuitEventResult;
	}
}
