package com.soapboxrace.core.api;

import java.io.InputStream;

import javax.ejb.EJB;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.AchievementsBO;
import com.soapboxrace.core.bo.EventBO;
import com.soapboxrace.core.bo.EventResultBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventMode;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.jaxb.http.DragArbitrationPacket;
import com.soapboxrace.jaxb.http.PursuitArbitrationPacket;
import com.soapboxrace.jaxb.http.PursuitEventResult;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.http.TeamEscapeArbitrationPacket;
import com.soapboxrace.jaxb.util.UnmarshalXML;

@Path("/event")
public class Event {

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

	@POST
	@Secured
	@Path("/abort")
	@Produces(MediaType.APPLICATION_XML)
	public String abort(@QueryParam("eventSessionId") Long eventSessionId) {
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
		personaPresenceDAO.updateCurrentEvent(activePersonaId, eventDataId);
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
			RouteArbitrationPacket routeArbitrationPacket = UnmarshalXML.unMarshal(arbitrationXml, RouteArbitrationPacket.class);
//			if (event.getId() == 1003 && routeArbitrationPacket.getEventDurationInMilliseconds() < 186000) { // Test
//				achievementsBO.broadcastUICustom(activePersonaId, "Challenge Completed");
//			}
//			if (event.getId() == 1003 && routeArbitrationPacket.getEventDurationInMilliseconds() > 186000) { // Test
//				achievementsBO.broadcastUICustom(activePersonaId, "Challenge Failed");
//			}
//			if (event.getId() == 1004 && routeArbitrationPacket.getRank() == 1) { // Test
//				achievementsBO.broadcastUICustom(activePersonaId, "Challenge Completed");
//			}
//			if (event.getId() == 1004 && routeArbitrationPacket.getRank() > 1) { // Test
//				achievementsBO.broadcastUICustom(activePersonaId, "Challenge Failed");
//			}
//			if (event.getId() == 1018 && routeArbitrationPacket.getRank() == 1 && routeArbitrationPacket.getEventDurationInMilliseconds() > 255000) { // Test
//				achievementsBO.broadcastUICustom(activePersonaId, "Challenge Completed");
//			}
//			if (event.getId() == 1018 && (routeArbitrationPacket.getEventDurationInMilliseconds() <= 255000 || routeArbitrationPacket.getRank() > 1)) { // Test
//				achievementsBO.broadcastUICustom(activePersonaId, "Challenge Failed");
//			}
//			if (event.getId() == 1005 && routeArbitrationPacket.getRank() == 1) { // Test
//				achievementsBO.broadcastUICustom(activePersonaId, "Challenge Completed");
//			}
//			if (event.getId() == 1005 && routeArbitrationPacket.getRank() > 1) { // Test
//				achievementsBO.broadcastUICustom(activePersonaId, "Challenge Failed");
//			}
			return eventResultBO.handleRaceEnd(eventSessionEntity, activePersonaId, routeArbitrationPacket, eventEnded);
		case DRAG:
			DragArbitrationPacket dragArbitrationPacket = UnmarshalXML.unMarshal(arbitrationXml, DragArbitrationPacket.class);
			return eventResultBO.handleDragEnd(eventSessionEntity, activePersonaId, dragArbitrationPacket, eventEnded);
		case MEETINGPLACE:
			break;
		case PURSUIT_MP:
			TeamEscapeArbitrationPacket teamEscapeArbitrationPacket = UnmarshalXML.unMarshal(arbitrationXml, TeamEscapeArbitrationPacket.class);
			return eventResultBO.handleTeamEscapeEnd(eventSessionEntity, activePersonaId, teamEscapeArbitrationPacket);
		case PURSUIT_SP:
			PursuitArbitrationPacket pursuitArbitrationPacket = UnmarshalXML.unMarshal(arbitrationXml, PursuitArbitrationPacket.class);
			return eventResultBO.handlePursitEnd(eventSessionEntity, activePersonaId, pursuitArbitrationPacket, false);
		default:
			break;
		}
		personaPresenceDAO.updateCurrentEvent(activePersonaId, null);
		return "";
	}

	@POST
	@Secured
	@Path("/bust")
	@Produces(MediaType.APPLICATION_XML)
	public PursuitEventResult bust(InputStream bustXml, @HeaderParam("securityToken") String securityToken, @QueryParam("eventSessionId") Long eventSessionId) {
		EventSessionEntity eventSessionEntity = eventBO.findEventSessionById(eventSessionId);
		PursuitArbitrationPacket pursuitArbitrationPacket = (PursuitArbitrationPacket) UnmarshalXML.unMarshal(bustXml, PursuitArbitrationPacket.class);
		PursuitEventResult pursuitEventResult = new PursuitEventResult();
		Long activePersonaId = tokenBO.getActivePersonaId(securityToken);
		pursuitEventResult = eventResultBO.handlePursitEnd(eventSessionEntity, activePersonaId, pursuitArbitrationPacket, true);
		personaPresenceDAO.updateCurrentEvent(activePersonaId, null);
		return pursuitEventResult;
	}
}
