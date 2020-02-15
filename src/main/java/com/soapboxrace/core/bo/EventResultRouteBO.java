package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.core.xmpp.XmppEvent;
import com.soapboxrace.jaxb.http.ArbitrationPacket;
import com.soapboxrace.jaxb.http.ArrayOfRouteEntrantResult;
import com.soapboxrace.jaxb.http.ExitPath;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.http.RouteEntrantResult;
import com.soapboxrace.jaxb.http.RouteEventResult;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeRouteEntrantResult;
import com.soapboxrace.jaxb.xmpp.XMPP_RouteEntrantResultType;

// import com.soapboxrace.core.jpa.TeamsEntity;
// import com.soapboxrace.core.xmpp.XmppChat;
// import com.soapboxrace.jaxb.http.OwnedCarTrans;

@Stateless
public class EventResultRouteBO {

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private EventDataDAO eventDataDao;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private RewardRouteBO rewardRouteBO;

	@EJB
	private CarDamageBO carDamageBO;

	@EJB
	private AchievementsBO achievementsBO;

	@EJB
	private PersonaDAO personaDAO;
	
	@EJB
	private TeamsDAO teamsDAO;
	
	@EJB
	private TeamsBO teamsBo;
	
	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private EventDAO eventDAO;
	
	@EJB
	private EventResultBO eventResultBO;

	public RouteEventResult handleRaceEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, RouteArbitrationPacket routeArbitrationPacket) {
		Long eventSessionId = eventSessionEntity.getId();
		eventSessionEntity.setEnded(System.currentTimeMillis());
		if (eventSessionEntity.getEnded() == null) {
		    System.out.println("DEBUG some event ended with no ended time, id: " + eventSessionId);
		}

		EventDataEntity eventDataEntity = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);

		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);
		
		Long team1id = eventSessionEntity.getTeam1Id();
		Long team2id = eventSessionEntity.getTeam2Id();
		boolean preRegTeams = false;
		if (team1id != null && team2id != null) {
			preRegTeams = true;
		}
		// XKAYA's arbitration exploit fix
		if (eventDataEntity.getArbitration()) {
			System.out.println("WARINING - XKAYA's arbitration exploit attempt, driver: " + personaEntity.getName());
			return null;
		}
		eventDataEntity.setArbitration(eventDataEntity.getArbitration() ? false : true);
		achievementsBO.applyRaceAchievements(eventDataEntity, routeArbitrationPacket, personaEntity);
		achievementsBO.applyAirTimeAchievement(routeArbitrationPacket, personaEntity);
		updateEventDataEntity(eventDataEntity, routeArbitrationPacket);

		// RouteArbitrationPacket
		eventDataEntity.setBestLapDurationInMilliseconds(routeArbitrationPacket.getBestLapDurationInMilliseconds());
		eventDataEntity.setFractionCompleted(routeArbitrationPacket.getFractionCompleted());
		eventDataEntity.setLongestJumpDurationInMilliseconds(routeArbitrationPacket.getLongestJumpDurationInMilliseconds());
		eventDataEntity.setNumberOfCollisions(routeArbitrationPacket.getNumberOfCollisions());
		eventDataEntity.setPerfectStart(routeArbitrationPacket.getPerfectStart());
		eventDataEntity.setSumOfJumpsDurationInMilliseconds(routeArbitrationPacket.getSumOfJumpsDurationInMilliseconds());
		eventDataEntity.setTopSpeed(routeArbitrationPacket.getTopSpeed());

		eventDataEntity.setEventModeId(eventDataEntity.getEvent().getEventModeId());
		eventDataEntity.setPersonaId(activePersonaId);
		boolean speedBugChance = eventResultBO.speedBugChance(personaEntity.getUser().getLastLogin());
		eventDataEntity.setSpeedBugChance(speedBugChance);
		eventDataDao.update(eventDataEntity);

		ArrayOfRouteEntrantResult arrayOfRouteEntrantResult = new ArrayOfRouteEntrantResult();
		
		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			RouteEntrantResult routeEntrantResult = new RouteEntrantResult();
			routeEntrantResult.setBestLapDurationInMilliseconds(racer.getBestLapDurationInMilliseconds());
			routeEntrantResult.setEventDurationInMilliseconds(racer.getEventDurationInMilliseconds());
			routeEntrantResult.setEventSessionId(eventSessionId);
			routeEntrantResult.setFinishReason(racer.getFinishReason());
			routeEntrantResult.setPersonaId(racer.getPersonaId());
			routeEntrantResult.setRanking(racer.getRank());
			routeEntrantResult.setTopSpeed(racer.getTopSpeed());
			// Does both teams are in actual race? This is checking a racers for their teamIds
			TeamsEntity teamsEntityTest = personaDAO.findById(racer.getPersonaId()).getTeam();
			if (teamsEntityTest != null) {
				Long playerTeamIdCheck = teamsEntityTest.getTeamId();
				if (preRegTeams && playerTeamIdCheck != null) {
					if (!eventSessionEntity.getTeam1Check() && team1id == playerTeamIdCheck) {
						eventSessionEntity.setTeam1Check(true);
						eventSessionDao.update(eventSessionEntity);
					}
					if (!eventSessionEntity.getTeam2Check() && team2id == playerTeamIdCheck) {
						eventSessionEntity.setTeam2Check(true);
						eventSessionDao.update(eventSessionEntity);
					}
				}
			}
			arrayOfRouteEntrantResult.getRouteEntrantResult().add(routeEntrantResult);
		}

		RouteEventResult routeEventResult = new RouteEventResult();
		int isDropableMode = 1;
		// Give rare drop if it's a online class-restricted race
		if (eventDataEntity.getEvent().getCarClassHash() != 607077938 && arrayOfRouteEntrantResult.getRouteEntrantResult().size() >= 2) {
			isDropableMode = 2;
		}
		routeEventResult.setAccolades(rewardRouteBO.getRouteAccolades(activePersonaId, routeArbitrationPacket, eventSessionEntity, arrayOfRouteEntrantResult, isDropableMode));
		routeEventResult.setDurability(carDamageBO.updateDamageCar(activePersonaId, routeArbitrationPacket, routeArbitrationPacket.getNumberOfCollisions()));
		routeEventResult.setEntrants(arrayOfRouteEntrantResult);
		int currentEventId = eventDataEntity.getEvent().getId();
		routeEventResult.setEventId(currentEventId);
		int tournamentEventId = parameterBO.getIntParam("TOURNAMENT_EVENTID");
		if (currentEventId == tournamentEventId && !speedBugChance) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Event Session: " + eventSessionId), personaEntity.getPersonaId());
		}
		if (currentEventId == tournamentEventId && speedBugChance) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This event can be affected by SpeedBug, restart the game."), personaEntity.getPersonaId());
		}
		routeEventResult.setEventSessionId(eventSessionId);
		routeEventResult.setExitPath(ExitPath.EXIT_TO_FREEROAM);
		routeEventResult.setInviteLifetimeInMilliseconds(0);
		routeEventResult.setLobbyInviteId(0);
		routeEventResult.setPersonaId(activePersonaId);
		sendXmppPacket(eventSessionId, activePersonaId, routeArbitrationPacket);
		// +1 to play count for this track, MP
		if (eventDataEntity.getRank() == 1 && arrayOfRouteEntrantResult.getRouteEntrantResult().size() > 1) {
			EventEntity eventEntity = eventDAO.findById(currentEventId);
			eventEntity.setFinishCount(eventEntity.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity);
			personaDAO.update(personaEntity);
		}
		// +1 to play count for this track, SP
		if (arrayOfRouteEntrantResult.getRouteEntrantResult().size() < 2) {
			EventEntity eventEntity = eventDAO.findById(currentEventId);
			eventEntity.setFinishCount(eventEntity.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity);
			personaDAO.update(personaEntity);
			EventDataEntity eventDataEntitySP = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
			eventDataEntitySP.setIsSingle(true);
			eventDataDao.update(eventDataEntitySP);
		}
		// Initiate the final team action check, only if both teams are registered for event
		if (eventDataEntity.getRank() == 1 && preRegTeams) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					teamsBo.teamAccoladesBasic(eventSessionId);
				}
			}).start();
		}
		
		return routeEventResult;
	}

	private void updateEventDataEntity(EventDataEntity eventDataEntity, ArbitrationPacket arbitrationPacket) {
		eventDataEntity.setAlternateEventDurationInMilliseconds(arbitrationPacket.getAlternateEventDurationInMilliseconds());
		eventDataEntity.setCarId(arbitrationPacket.getCarId());
		eventDataEntity.setEventDurationInMilliseconds(arbitrationPacket.getEventDurationInMilliseconds());
		eventDataEntity.setFinishReason(arbitrationPacket.getFinishReason());
		eventDataEntity.setHacksDetected(arbitrationPacket.getHacksDetected());
		eventDataEntity.setRank(arbitrationPacket.getRank());
	}

	private void sendXmppPacket(Long eventSessionId, Long activePersonaId, RouteArbitrationPacket routeArbitrationPacket) {
		XMPP_RouteEntrantResultType xmppRouteResult = new XMPP_RouteEntrantResultType();
		xmppRouteResult.setBestLapDurationInMilliseconds(routeArbitrationPacket.getBestLapDurationInMilliseconds());
		xmppRouteResult.setEventDurationInMilliseconds(routeArbitrationPacket.getEventDurationInMilliseconds());
		xmppRouteResult.setEventSessionId(eventSessionId);
		xmppRouteResult.setFinishReason(routeArbitrationPacket.getFinishReason());
		xmppRouteResult.setPersonaId(activePersonaId);
		int playerRank = routeArbitrationPacket.getRank();
		xmppRouteResult.setRanking(playerRank);
		xmppRouteResult.setTopSpeed(routeArbitrationPacket.getTopSpeed());

		XMPP_ResponseTypeRouteEntrantResult routeEntrantResultResponse = new XMPP_ResponseTypeRouteEntrantResult();
		routeEntrantResultResponse.setRouteEntrantResult(xmppRouteResult);

		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			if (!racer.getPersonaId().equals(activePersonaId)) {
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendRaceEnd(routeEntrantResultResponse);
				if (playerRank == 1) {
					xmppEvent.sendEventTimingOut(eventSessionId);
				}
			}
		}
	}
}
