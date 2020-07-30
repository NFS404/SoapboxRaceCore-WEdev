package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.core.xmpp.XmppEvent;
import com.soapboxrace.jaxb.http.ArrayOfDragEntrantResult;
import com.soapboxrace.jaxb.http.DragArbitrationPacket;
import com.soapboxrace.jaxb.http.DragEntrantResult;
import com.soapboxrace.jaxb.http.DragEventResult;
import com.soapboxrace.jaxb.http.ExitPath;
import com.soapboxrace.jaxb.xmpp.XMPP_DragEntrantResultType;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeDragEntrantResult;

@Stateless
public class EventResultDragBO {

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private EventDataDAO eventDataDao;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private RewardDragBO rewardDragBO;

	@EJB
	private CarDamageBO carDamageBO;

	@EJB
	private AchievementsBO achievementsBO;

	@EJB
	private PersonaDAO personaDAO;
	
	@EJB
	private EventDAO eventDAO;
	
	@EJB
	private EventResultBO eventResultBO;
	
	@EJB
	private RecordsBO recordsBO;
	
	@EJB
	private CustomCarDAO customCarDAO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private CarClassesDAO carClassesDAO;

	public DragEventResult handleDragEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, DragArbitrationPacket dragArbitrationPacket, Long eventEnded) {
		Long eventSessionId = eventSessionEntity.getId();
		eventSessionEntity.setEnded(System.currentTimeMillis());

		eventSessionDao.update(eventSessionEntity);
		
		XMPP_DragEntrantResultType xmppDragResult = new XMPP_DragEntrantResultType();
		xmppDragResult.setEventDurationInMilliseconds(dragArbitrationPacket.getEventDurationInMilliseconds());
		xmppDragResult.setEventSessionId(eventSessionId);
		xmppDragResult.setFinishReason(dragArbitrationPacket.getFinishReason());
		xmppDragResult.setPersonaId(activePersonaId);
		xmppDragResult.setRanking(dragArbitrationPacket.getRank());
		xmppDragResult.setTopSpeed(dragArbitrationPacket.getTopSpeed());

		XMPP_ResponseTypeDragEntrantResult dragEntrantResultResponse = new XMPP_ResponseTypeDragEntrantResult();
		dragEntrantResultResponse.setDragEntrantResult(xmppDragResult);
		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);
		String playerName = personaEntity.getName();
		
		EventDataEntity eventDataEntity = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
		// XKAYA's arbitration exploit fix
		if (eventDataEntity.getArbitration()) {
			System.out.println("WARINING - XKAYA's arbitration exploit attempt, driver: " + personaEntity.getName());
			return null;
		}
		eventDataEntity.setArbitration(eventDataEntity.getArbitration() ? false : true);
		achievementsBO.applyAirTimeAchievement(dragArbitrationPacket, personaEntity);
		achievementsBO.applyDragAchievement(eventDataEntity, dragArbitrationPacket, activePersonaId);
		achievementsBO.applyEventKmsAchievement(personaEntity, (long) eventDataEntity.getEvent().getTrackLength());

		int currentEventId = eventDataEntity.getEvent().getId();
		EventEntity eventEntity = eventDataEntity.getEvent();
		eventDataEntity.setAlternateEventDurationInMilliseconds(dragArbitrationPacket.getAlternateEventDurationInMilliseconds());
		eventDataEntity.setCarId(dragArbitrationPacket.getCarId());
		eventDataEntity.setEventDurationInMilliseconds(dragArbitrationPacket.getEventDurationInMilliseconds());
		eventDataEntity.setEventModeId(eventDataEntity.getEvent().getEventModeId());
		eventDataEntity.setFinishReason(dragArbitrationPacket.getFinishReason());
		eventDataEntity.setFractionCompleted(dragArbitrationPacket.getFractionCompleted());
		eventDataEntity.setHacksDetected(dragArbitrationPacket.getHacksDetected());
		eventDataEntity.setLongestJumpDurationInMilliseconds(dragArbitrationPacket.getLongestJumpDurationInMilliseconds());
		eventDataEntity.setNumberOfCollisions(dragArbitrationPacket.getNumberOfCollisions());
		eventDataEntity.setPerfectStart(dragArbitrationPacket.getPerfectStart());
		eventDataEntity.setPersonaId(activePersonaId);
		eventDataEntity.setRank(dragArbitrationPacket.getRank());
		eventDataEntity.setSumOfJumpsDurationInMilliseconds(dragArbitrationPacket.getSumOfJumpsDurationInMilliseconds());
		eventDataEntity.setTopSpeed(dragArbitrationPacket.getTopSpeed());
		boolean speedBugChance = eventResultBO.speedBugChance(personaEntity.getUser().getLastLogin());
		eventDataEntity.setSpeedBugChance(speedBugChance);
		int carVersion = eventResultBO.carVersionCheck(activePersonaId);
		eventDataEntity.setCarVersion(carVersion);
		eventDataEntity.setServerEventDuration(eventEnded - eventDataEntity.getServerEventDuration());
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
		
		ArrayOfDragEntrantResult arrayOfDragEntrantResult = new ArrayOfDragEntrantResult();
		// +1 to play count for this track, MP
		if (eventDataEntity.getRank() == 1 && arrayOfDragEntrantResult.getDragEntrantResult().size() > 1) {
			eventEntity.setFinishCount(eventEntity.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity);
			personaDAO.update(personaEntity);
		}
		// +1 to play count for this track, SP
		if (arrayOfDragEntrantResult.getDragEntrantResult().size() < 2) {
			eventEntity.setFinishCount(eventEntity.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity);
			personaDAO.update(personaEntity);
			EventDataEntity eventDataEntitySP = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
			eventDataEntitySP.setIsSingle(true);
			eventDataDao.update(eventDataEntitySP);
		}
		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			DragEntrantResult dragEntrantResult = new DragEntrantResult();
			dragEntrantResult.setEventDurationInMilliseconds(racer.getEventDurationInMilliseconds());
			dragEntrantResult.setEventSessionId(eventSessionId);
			dragEntrantResult.setFinishReason(racer.getFinishReason());
			dragEntrantResult.setPersonaId(racer.getPersonaId());
			dragEntrantResult.setRanking(racer.getRank());
			dragEntrantResult.setTopSpeed(racer.getTopSpeed());
			arrayOfDragEntrantResult.getDragEntrantResult().add(dragEntrantResult);

			if (!racer.getPersonaId().equals(activePersonaId)) {
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendDragEnd(dragEntrantResultResponse);
				if (dragArbitrationPacket.getRank() == 1) {
					xmppEvent.sendEventTimingOut(eventSessionId);
				}
			}
		}

		DragEventResult dragEventResult = new DragEventResult();
		int isDropableMode = 1;
		// Give weak drop if it's a single-player drag
		if (arrayOfDragEntrantResult.getDragEntrantResult().size() < 2) {
			isDropableMode = 3;
		}
		dragEventResult.setAccolades(rewardDragBO.getDragAccolades(activePersonaId, dragArbitrationPacket, eventSessionEntity, arrayOfDragEntrantResult, isDropableMode));
		dragEventResult.setDurability(carDamageBO.updateDamageCar(activePersonaId, dragArbitrationPacket, dragArbitrationPacket.getNumberOfCollisions()));
		dragEventResult.setEntrants(arrayOfDragEntrantResult);
		dragEventResult.setEventId(currentEventId);
		dragEventResult.setEventSessionId(eventSessionId);
		dragEventResult.setExitPath(ExitPath.EXIT_TO_FREEROAM);
		dragEventResult.setInviteLifetimeInMilliseconds(0);
		dragEventResult.setLobbyInviteId(0);
		dragEventResult.setPersonaId(activePersonaId);
		
		// Separate race stats
		boolean raceIssues = false;
		Long raceHacks = dragArbitrationPacket.getHacksDetected();
		Long raceTime = eventDataEntity.getEventDurationInMilliseconds();
		Long timeDiff = raceTime - eventDataEntity.getAlternateEventDurationInMilliseconds(); // If the time & altTime is differs so much, the player's data might be wrong
		int playerPhysicsHash = customCarEntity.getPhysicsProfileHash();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(playerPhysicsHash);
		
		if (speedBugChance || dragArbitrationPacket.getFinishReason() != 22 || (raceHacks != 0 && raceHacks != 32) 
				|| eventEntity.getMinTime() >= raceTime || (timeDiff > 1000 || timeDiff < -1000) || raceTime > 2000000 || customCarEntity.getCarClassHash() == 0) {
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Invaild race session, restart the game and try again."), activePersonaId);
		}
		if (carClassesEntity.getModelSmall() == null) { // If the car doesn't have a modelSmall name - we will not allow it for records
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Records cannot be saved on this car."), activePersonaId);
		}
		if (!raceIssues) {
			recordsBO.submitRecord(eventEntity, personaEntity, eventDataEntity, customCarEntity, carClassesEntity);
		}
		
		return dragEventResult;
	}

}
