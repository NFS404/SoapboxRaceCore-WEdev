package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.OwnedCarDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.OwnedCarEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.jaxb.http.ExitPath;
import com.soapboxrace.jaxb.http.PursuitArbitrationPacket;
import com.soapboxrace.jaxb.http.PursuitEventResult;

@Stateless
public class EventResultPursuitBO {

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private EventDataDAO eventDataDao;

	@EJB
	private RewardPursuitBO rewardPursuitBO;

	@EJB
	private CarDamageBO carDamageBO;

	@EJB
	private AchievementsBO achievementsBO;

	@EJB
	private PersonaDAO personaDAO;
	
    @EJB
    private OwnedCarDAO ownedCarDAO;

    @EJB
    private PersonaBO personaBO;
    
    @EJB
	private DiscordWebhook discordBot;
    
    @EJB
	private EventDAO eventDAO;
    
    @EJB
	private EventResultBO eventResultBO;
    
    @EJB
	private ParameterBO parameterBO;
    
    @EJB
	private EventBO eventBO;
    
    @EJB
	private CustomCarDAO customCarDAO;

	public PursuitEventResult handlePursuitEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, PursuitArbitrationPacket pursuitArbitrationPacket,
			Boolean isBusted, Long eventEnded) {
		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);
		
		Long eventSessionId = eventSessionEntity.getId();
		eventSessionEntity.setEnded(System.currentTimeMillis());

		eventSessionDao.update(eventSessionEntity);
		String playerName = personaEntity.getName();

		EventDataEntity eventDataEntity = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
		// XKAYA's arbitration exploit fix
		if (eventDataEntity.getArbitration()) {
			System.out.println("WARINING - XKAYA's arbitration exploit attempt, driver: " + personaEntity.getName());
			return null;
		}
		int currentEventId = eventDataEntity.getEvent().getId();
		eventDataEntity.setServerEventDuration(eventEnded - eventDataEntity.getServerEventDuration());
		eventDataEntity.setArbitration(eventDataEntity.getArbitration() ? false : true);
		eventDataEntity.setAlternateEventDurationInMilliseconds(pursuitArbitrationPacket.getAlternateEventDurationInMilliseconds());
		eventDataEntity.setCarId(pursuitArbitrationPacket.getCarId());
		eventDataEntity.setCopsDeployed(pursuitArbitrationPacket.getCopsDeployed());
		eventDataEntity.setCopsDisabled(pursuitArbitrationPacket.getCopsDisabled());
		eventDataEntity.setCopsRammed(pursuitArbitrationPacket.getCopsRammed());
		eventDataEntity.setCostToState(pursuitArbitrationPacket.getCostToState());
		long eventTimeCheck = pursuitArbitrationPacket.getEventDurationInMilliseconds();
		eventDataEntity.setEventDurationInMilliseconds(eventTimeCheck);
		eventDataEntity.setEventModeId(eventDataEntity.getEvent().getEventModeId());
		eventDataEntity.setFinishReason(pursuitArbitrationPacket.getFinishReason());
		eventDataEntity.setHacksDetected(pursuitArbitrationPacket.getHacksDetected());
		eventDataEntity.setHeat(pursuitArbitrationPacket.getHeat());
		eventDataEntity.setInfractions(pursuitArbitrationPacket.getInfractions());
		eventDataEntity.setLongestJumpDurationInMilliseconds(pursuitArbitrationPacket.getLongestJumpDurationInMilliseconds());
		eventDataEntity.setPersonaId(activePersonaId);
		eventDataEntity.setRoadBlocksDodged(pursuitArbitrationPacket.getRoadBlocksDodged());
		eventDataEntity.setSpikeStripsDodged(pursuitArbitrationPacket.getSpikeStripsDodged());
		eventDataEntity.setSumOfJumpsDurationInMilliseconds(pursuitArbitrationPacket.getSumOfJumpsDurationInMilliseconds());
		eventDataEntity.setTopSpeed(pursuitArbitrationPacket.getTopSpeed());
		eventDataEntity.setIsSingle(true);
		boolean speedBugChance = eventResultBO.speedBugChance(personaEntity.getUser().getLastLogin());
		eventDataEntity.setSpeedBugChance(speedBugChance);
		int carVersion = eventResultBO.carVersionCheck(activePersonaId);
		eventDataEntity.setCarVersion(carVersion);
		
		// +1 to play count for this track, SP
		EventEntity eventEntity = eventDAO.findById(currentEventId);
		eventEntity.setFinishCount(eventEntity.getFinishCount() + 1);
		personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
		eventDAO.update(eventEntity);
		personaDAO.update(personaEntity);
		
		eventDataDao.update(eventDataEntity);
		
		if (!isBusted && (eventTimeCheck > parameterBO.getIntParam("PURSUIT_MINIMUM_TIME"))) {
			achievementsBO.applyOutlawAchievement(personaEntity);
			achievementsBO.applyPursuitCostToState(pursuitArbitrationPacket, personaEntity);
			achievementsBO.applyAirTimeAchievement(pursuitArbitrationPacket, personaEntity);
		}
		
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
		Long eventDataId = eventDataEntity.getId();
		eventBO.updateEventCarInfo(activePersonaId, eventDataId, customCarEntity);
		
		// Discord detailed report - Hypercycle
		long reportHacks = pursuitArbitrationPacket.getHacksDetected();
		int reportDisabled = pursuitArbitrationPacket.getCopsDisabled();
		int reportRammed = pursuitArbitrationPacket.getCopsRammed();
		if ((reportHacks != 0 && reportHacks != 32) || (reportDisabled >= 15 && reportRammed <= 5) || reportDisabled >= 999) {
			float reportSpeed = pursuitArbitrationPacket.getTopSpeed();
			float reportHeat = pursuitArbitrationPacket.getHeat();
			int reportCOS = pursuitArbitrationPacket.getCostToState();
			int reportBlocks = pursuitArbitrationPacket.getRoadBlocksDodged();
			int reportSpikes = pursuitArbitrationPacket.getSpikeStripsDodged();
			long reportTime = pursuitArbitrationPacket.getEventDurationInMilliseconds();
			
			if (reportSpeed > 2.0) {
				
				String message = ":heavy_minus_sign:"
		        		+ "\n:oncoming_police_car: **|** В прошедшей погоне **" + ((int) reportHeat) + "** уровня, нарушитель **" + personaEntity.getName() + "** нанёс ущерба в **" + reportCOS + " $**, остановил **" + reportDisabled + "** полицейских (*" + reportRammed + " задето нарушителем*), также он обошёл **" + reportBlocks + "** заграждений (*" + reportSpikes + " из них с шипами*), с длительностью погони в **" + reportTime + "** мс. (*Код нарушения " + reportHacks + "*)."
		        		+ "\n:oncoming_police_car: **|** In the past pursuit of **" + ((int) reportHeat) + "** level, suspect **" + personaEntity.getName() + "** made a cost to state for **" + reportCOS + " $**, destroyed **" + reportDisabled + "** police cars (*" + reportRammed + " of them was rammed*), also he avoided **" + reportBlocks + "** roadblocks (*" + reportSpikes + " of them with spikes*), with pursuit time of **" + reportTime + "** ms. (*Violation code " + reportHacks + "*).";
				discordBot.sendMessage(message);
			}
            if (reportSpeed <= 2.0) {
				
				String message = ":heavy_minus_sign:"
		        		+ "\n:oncoming_police_car: **|** В прошедшей погоне **" + ((int) reportHeat) + "** уровня, нарушитель **" + personaEntity.getName() + "** нанёс ущерба в **" + reportCOS + " $**, остановил **" + reportDisabled + "** полицейских (*" + reportRammed + " задето нарушителем*), также он обошёл **" + reportBlocks + "** заграждений (*" + reportSpikes + " из них с шипами*), с длительностью погони в **" + reportTime + "** мс., **стоя на месте всю погоню!** (*Код нарушения " + reportHacks + "*)."
		        		+ "\n:oncoming_police_car: **|** In the past pursuit of **" + ((int) reportHeat) + "** level, suspect **" + personaEntity.getName() + "** made a cost to state for **" + reportCOS + " $**, destroyed **" + reportDisabled + "** police cars (*" + reportRammed + " of them was rammed*), also he avoided **" + reportBlocks + "** roadblocks (*" + reportSpikes + " of them with spikes*), with pursuit time of **" + reportTime + "** ms., **without moving the entire pursuit!** (*Violation code " + reportHacks + "*).";
				discordBot.sendMessage(message);
			}
		}
		
		PursuitEventResult pursuitEventResult = new PursuitEventResult();
		pursuitEventResult.setAccolades(rewardPursuitBO.getPursuitAccolades(activePersonaId, pursuitArbitrationPacket, eventSessionEntity, isBusted, 3));
		pursuitEventResult.setDurability(carDamageBO.updateDamageCar(activePersonaId, pursuitArbitrationPacket, 0));
		pursuitEventResult.setEventId(currentEventId);
		pursuitEventResult.setEventSessionId(eventSessionId);
		pursuitEventResult.setExitPath(ExitPath.EXIT_TO_FREEROAM);
		pursuitEventResult.setHeat(isBusted ? 1 : pursuitArbitrationPacket.getHeat());
		pursuitEventResult.setInviteLifetimeInMilliseconds(0);
		pursuitEventResult.setLobbyInviteId(0);
		pursuitEventResult.setPersonaId(activePersonaId);
		
	    OwnedCarEntity ownedCarEntity = personaBO.getDefaultCarEntity(activePersonaId).getOwnedCar();
	    ownedCarEntity.setHeat(isBusted ? 1 : pursuitArbitrationPacket.getHeat());
	    ownedCarDAO.update(ownedCarEntity);
	    
		return pursuitEventResult;
	}

}
