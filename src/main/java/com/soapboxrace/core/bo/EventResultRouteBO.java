package com.soapboxrace.core.bo;

import java.math.BigInteger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.RecordsDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
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
	
	@EJB
	private RecordsBO recordsBO;
	
	@EJB
	private CustomCarDAO customCarDAO;
	
	@EJB
	private RecordsDAO recordsDAO;
	
	@EJB
	private CarClassesDAO carClassesDAO;

	public RouteEventResult handleRaceEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, RouteArbitrationPacket routeArbitrationPacket, Long eventEnded) {
		Long eventSessionId = eventSessionEntity.getId();
		eventSessionEntity.setEnded(System.currentTimeMillis());
		if (eventSessionEntity.getEnded() == null) {
		    System.out.println("DEBUG some event ended with no ended time, id: " + eventSessionId);
		}
		EventDataEntity eventDataEntity = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
		EventEntity eventEntity = eventDataEntity.getEvent();
		int eventClass = eventEntity.getCarClassHash();
		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);
		String playerName = personaEntity.getName();
		
		Long team1id = eventSessionEntity.getTeam1Id();
		Long team2id = eventSessionEntity.getTeam2Id();
		boolean preRegTeams = false;
		if (team1id != null && team2id != null) {
			preRegTeams = true;
		}
		// XKAYA's arbitration exploit fix
		boolean arbitStatus = eventDataEntity.getArbitration();
		if (arbitStatus) {
			System.out.println("WARINING - XKAYA's arbitration exploit attempt, driver: " + playerName);
			return null;
		}
		eventDataEntity.setArbitration(arbitStatus ? false : true);
		achievementsBO.applyRaceAchievements(eventDataEntity, routeArbitrationPacket, personaEntity);
		achievementsBO.applyAirTimeAchievement(routeArbitrationPacket, personaEntity);
		achievementsBO.applyEventKmsAchievement(personaEntity, (long) eventEntity.getTrackLength());
		
		eventDataEntity.setServerEventDuration(eventEnded - eventDataEntity.getServerEventDuration());
		updateEventDataEntity(eventDataEntity, routeArbitrationPacket);

		// RouteArbitrationPacket
		eventDataEntity.setBestLapDurationInMilliseconds(routeArbitrationPacket.getBestLapDurationInMilliseconds());
		eventDataEntity.setFractionCompleted(routeArbitrationPacket.getFractionCompleted());
		eventDataEntity.setLongestJumpDurationInMilliseconds(routeArbitrationPacket.getLongestJumpDurationInMilliseconds());
		eventDataEntity.setNumberOfCollisions(routeArbitrationPacket.getNumberOfCollisions());
		eventDataEntity.setPerfectStart(routeArbitrationPacket.getPerfectStart());
		eventDataEntity.setSumOfJumpsDurationInMilliseconds(routeArbitrationPacket.getSumOfJumpsDurationInMilliseconds());
		eventDataEntity.setTopSpeed(routeArbitrationPacket.getTopSpeed());

		eventDataEntity.setEventModeId(eventEntity.getEventModeId());
		eventDataEntity.setPersonaId(activePersonaId);
		boolean speedBugChance = eventResultBO.speedBugChance(personaEntity.getUser().getLastLogin());
		eventDataEntity.setSpeedBugChance(speedBugChance);
		int carVersion = eventResultBO.carVersionCheck(activePersonaId);
		eventDataEntity.setCarVersion(carVersion);
		eventDataDao.update(eventDataEntity);
		
		CustomCarEntity customCarEntity = customCarDAO.findById(eventDataEntity.getCarId());
		int carPhysicsHash = customCarEntity.getPhysicsProfileHash();
		if (carPhysicsHash == 202813212 || carPhysicsHash == -840317713 || carPhysicsHash == -845093474 || carPhysicsHash == -133221572 || carPhysicsHash == -409661256) {
			// Player on ModCar cannot finish any event (since he is restricted from), but if he somehow was finished it, we should know
			System.out.println("Player " + playerName + "has illegally finished the event on ModCar.");
			String message = ":heavy_minus_sign:"
	        		+ "\n:japanese_goblin: **|** Nгрок **" + playerName + "** участвовал в гонках на **моддерском слоте**, покончите с ним."
	        		+ "\n:japanese_goblin: **|** Player **" + playerName + "** was finished the event on **modder vehicle**, finish him.";
			discordBot.sendMessage(message);
		}

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
		if (eventClass != 607077938 && arrayOfRouteEntrantResult.getRouteEntrantResult().size() >= 2) {
			isDropableMode = 2;
		}
		routeEventResult.setAccolades(rewardRouteBO.getRouteAccolades(activePersonaId, routeArbitrationPacket, eventSessionEntity, arrayOfRouteEntrantResult, isDropableMode));
		routeEventResult.setDurability(carDamageBO.updateDamageCar(activePersonaId, routeArbitrationPacket, routeArbitrationPacket.getNumberOfCollisions()));
		routeEventResult.setEntrants(arrayOfRouteEntrantResult);
		int currentEventId = eventEntity.getId();
		routeEventResult.setEventId(currentEventId);
		int tournamentEventId = parameterBO.getIntParam("TOURNAMENT_EVENTID");
		Long personaId = personaEntity.getPersonaId();
		if (currentEventId == tournamentEventId && !speedBugChance) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Event Session: " + eventSessionId), personaId);
		}
		if (currentEventId == tournamentEventId && speedBugChance) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This event can be affected by SpeedBug, restart the game."), personaId);
		}
		routeEventResult.setEventSessionId(eventSessionId);
		routeEventResult.setExitPath(ExitPath.EXIT_TO_FREEROAM);
		routeEventResult.setInviteLifetimeInMilliseconds(0);
		routeEventResult.setLobbyInviteId(0);
		routeEventResult.setPersonaId(activePersonaId);
		sendXmppPacket(eventSessionId, activePersonaId, routeArbitrationPacket);
		EventEntity eventEntity2 = eventDAO.findById(currentEventId);
		// +1 to play count for this track, MP
		if (eventDataEntity.getRank() == 1 && arrayOfRouteEntrantResult.getRouteEntrantResult().size() > 1) {
			eventEntity2.setFinishCount(eventEntity2.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity2);
			personaDAO.update(personaEntity);
		}
		// +1 to play count for this track, SP
		if (arrayOfRouteEntrantResult.getRouteEntrantResult().size() < 2) {
			eventEntity2.setFinishCount(eventEntity2.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity2);
			personaDAO.update(personaEntity);
			EventDataEntity eventDataEntitySP = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
			eventDataEntitySP.setIsSingle(true);
			eventDataDao.update(eventDataEntitySP);
		}
		
		// Separate race stats
		boolean raceIssues = false;
		
		Long raceHacks = routeArbitrationPacket.getHacksDetected();
		Long raceTime = eventDataEntity.getEventDurationInMilliseconds();
		Long timeDiff = raceTime - eventDataEntity.getAlternateEventDurationInMilliseconds(); // If the time & altTime is differs so much, the player's data might be wrong
		int playerPhysicsHash = customCarEntity.getPhysicsProfileHash();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(playerPhysicsHash);
		
		if (speedBugChance || routeArbitrationPacket.getFinishReason() != 22 || (raceHacks != 0 && raceHacks != 32) 
				|| eventEntity.getMinTime() >= raceTime || (timeDiff > 1000 || timeDiff < -1000) || raceTime > 2000000 
				|| (eventClass != 607077938 && eventClass != customCarEntity.getCarClassHash())) {
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Invaild race session, restart the game and try again."), personaId);
		}
		if (carClassesEntity.getModelSmall() == null) { // If the car doesn't have a modelSmall name - we will not allow it for records
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Records cannot be saved on this car."), personaId);
		}
		// If some server admin did a manual player unban via DB, and forgot to uncheck the userBan field for him, this player should know about it
		BigInteger zeroCheck = new BigInteger("0");
		if (!recordsDAO.countBannedRecords(personaEntity.getUser().getId()).equals(zeroCheck)) {
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Some records on this account is still banned, contact to server staff."), personaId);
		}
		if (!raceIssues) {
			recordsBO.submitRecord(eventEntity, personaEntity, eventDataEntity, customCarEntity, carClassesEntity);
		}
		
		// Initiate the final team action check, only if both teams are registered for event
		// FIXME If the players "fast enough", this sequence will be executed more than 1 time, since PersonaWinner will be null for multiple players
		Long isWinnerPresented = eventSessionEntity.getPersonaWinner();
		if (isWinnerPresented == null) {
			eventSessionEntity.setPersonaWinner(activePersonaId);
			eventSessionDao.update(eventSessionEntity);
			if (preRegTeams) {
				System.out.println("### TEAMS: EventSession " + eventSessionId + "has been completed, check");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Debug - Teams finish, init, " + eventSessionId), personaId);
				new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println("### TEAMS: EventSession " + eventSessionId + "has been completed, init");
						teamsBo.teamAccoladesBasic(eventSessionId);
					}
				}).start();
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
