package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.core.xmpp.XmppEvent;
import com.soapboxrace.jaxb.http.ArbitrationPacket;
import com.soapboxrace.jaxb.http.ArrayOfRouteEntrantResult;
import com.soapboxrace.jaxb.http.ExitPath;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
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
	private PersonaBO personaBO;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private DiscordWebhook discordBot;

	public RouteEventResult handleRaceEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, RouteArbitrationPacket routeArbitrationPacket) {
		Long eventSessionId = eventSessionEntity.getId();
		eventSessionEntity.setEnded(System.currentTimeMillis());

		EventDataEntity eventDataEntity = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);

		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);
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
			System.out.println("TEST Team1id = " + eventSessionEntity.getTeam1Id() + ", TEST team2id = " + eventSessionEntity.getTeam2Id());
			Long playerTeamIdCheck = personaDAO.findById(racer.getPersonaId()).getTeam().getTeamId();
			if (eventSessionEntity.getTeam1Id() != null && eventSessionEntity.getTeam2Id() != null && playerTeamIdCheck != null) {
				if (!eventSessionEntity.getTeam1Check() && eventSessionEntity.getTeam1Id() == playerTeamIdCheck) {
					eventSessionEntity.setTeam1Check(true);
					eventSessionDao.update(eventSessionEntity);
				}
				if (!eventSessionEntity.getTeam2Check() && eventSessionEntity.getTeam2Id() == playerTeamIdCheck) {
					eventSessionEntity.setTeam2Check(true);
					eventSessionDao.update(eventSessionEntity);
				}
			}
			arrayOfRouteEntrantResult.getRouteEntrantResult().add(routeEntrantResult);
		}

		RouteEventResult routeEventResult = new RouteEventResult();
		routeEventResult.setAccolades(rewardRouteBO.getRouteAccolades(activePersonaId, routeArbitrationPacket, eventSessionEntity, arrayOfRouteEntrantResult));
		routeEventResult.setDurability(carDamageBO.updateDamageCar(activePersonaId, routeArbitrationPacket, routeArbitrationPacket.getNumberOfCollisions()));
		routeEventResult.setEntrants(arrayOfRouteEntrantResult);
		routeEventResult.setEventId(eventDataEntity.getEvent().getId());
		if (eventDataEntity.getEvent().getId() == parameterBO.getIntParam("TOURNAMENT_EVENTID")) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Event Session: " + eventSessionId), personaEntity.getPersonaId());
		}
		routeEventResult.setEventSessionId(eventSessionId);
		routeEventResult.setExitPath(ExitPath.EXIT_TO_FREEROAM);
		routeEventResult.setInviteLifetimeInMilliseconds(0);
		routeEventResult.setLobbyInviteId(0);
		routeEventResult.setPersonaId(activePersonaId);
		sendXmppPacket(eventSessionId, activePersonaId, routeArbitrationPacket);
		
		// The fastest racer of his team will bring a win on this race, depending on opponent's teams position - Hypercycle
		// carClass 0 = open races for all classes
		int targetCarClass = parameterBO.getIntParam("CLASSBONUS_CARCLASSHASH");
		TeamsEntity racerTeamEntity = personaEntity.getTeam();
		if (racerTeamEntity != null && eventSessionEntity.getTeam1Check() && eventSessionEntity.getTeam2Check()) {
			Long racerTeamId = racerTeamEntity.getTeamId();
			Long team1 = eventSessionEntity.getTeam1Id();
			Long team2 = eventSessionEntity.getTeam2Id();
			Long teamWinner = eventSessionEntity.getTeamWinner();
			OwnedCarTrans defaultCar = personaBO.getDefaultCar(activePersonaId);					
				if ((racerTeamId == team1 || racerTeamId == team2) && (defaultCar.getCustomCar().getCarClassHash() == targetCarClass || targetCarClass == 0) && teamWinner == null) {
					eventSessionEntity.setTeamWinner(racerTeamId);
					eventSessionDao.update(eventSessionEntity);
					String winnerPlayerName = personaEntity.getName();
					String winnerTeamName = racerTeamEntity.getTeamName();
					racerTeamEntity.setTeamPoints(racerTeamEntity.getTeamPoints() + 1);
					teamsDAO.update(racerTeamEntity);
					int winnerTeamPointsFinal = racerTeamEntity.getTeamPoints();
					
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### " + winnerTeamName + " has won this event! +1P, total: " + winnerTeamPointsFinal), activePersonaId);
					String message = ":heavy_minus_sign:"
			        		+ "\n:trophy: **|** Nгрок **" + winnerPlayerName + "** принёс победу своей команде **" + winnerTeamName + "** в заезде (*итого очков: " + winnerTeamPointsFinal + ", сессия " + eventSessionEntity.getId() + "*)."
			        		+ "\n:trophy: **|** Player **" + winnerPlayerName + "** brought victory to his team **" + winnerTeamName + "** during race (*points: " + winnerTeamPointsFinal + ", session " + eventSessionEntity.getId() + "*).";
					discordBot.sendMessage(message, true);
				}

			if (teamWinner != null && teamWinner != racerTeamId) {
				TeamsEntity winnerTeam = teamsDAO.findById(teamWinner);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### " + winnerTeam.getTeamName() + " has won this event! +1P, total: " + winnerTeam.getTeamPoints()), activePersonaId);
			}
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
		xmppRouteResult.setRanking(routeArbitrationPacket.getRank());
		xmppRouteResult.setTopSpeed(routeArbitrationPacket.getTopSpeed());

		XMPP_ResponseTypeRouteEntrantResult routeEntrantResultResponse = new XMPP_ResponseTypeRouteEntrantResult();
		routeEntrantResultResponse.setRouteEntrantResult(xmppRouteResult);

		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			if (!racer.getPersonaId().equals(activePersonaId)) {
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendRaceEnd(routeEntrantResultResponse);
				if (routeArbitrationPacket.getRank() == 1) {
					xmppEvent.sendEventTimingOut(eventSessionId);
				}
			}
		}
	}

}
