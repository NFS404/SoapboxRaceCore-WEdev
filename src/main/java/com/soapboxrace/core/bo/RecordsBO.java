package com.soapboxrace.core.bo;

import java.math.BigInteger;
import java.time.LocalDateTime;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.bo.util.TimeReadConverter;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.EventPowerupsDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.RecordsDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventPowerupsEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.RecordsEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;

@Stateless
public class RecordsBO {

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private PersonaDAO personaDAO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private RecordsDAO recordsDAO;
	
	@EJB
	private CustomCarDAO customCarDAO;
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private TimeReadConverter timeReadConverter;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private EventPowerupsBO eventPowerupsBO;
	
	@EJB
	private EventPowerupsDAO eventPowerupsDAO;
	
	@EJB
	private EventResultBO eventResultBO;

	public void submitRecord(EventEntity eventEntity, PersonaEntity personaEntity, EventDataEntity eventDataEntity, CustomCarEntity customCarEntity) {
//		System.out.println("RecordEntry start");
		boolean recordCaptureFinished = false;
		Long personaId = personaEntity.getPersonaId();
		UserEntity userEntity = personaEntity.getUser();
		int eventId = eventEntity.getId();
		String playerName = personaEntity.getName();
		int carClassHash = eventEntity.getCarClassHash();
		boolean openClass = false;
		if (carClassHash == 607077938) { // If our race is open-class, we will save the player's car value
			openClass = true;
			carClassHash = customCarEntity.getCarClassHash();
		}
//		String eventName = eventEntity.getName();
		Long eventDuration = eventDataEntity.getEventDurationInMilliseconds();
		Long eventAltDuration = eventDataEntity.getAlternateEventDurationInMilliseconds();
		Long eventSrvDuration = eventDataEntity.getServerEventDuration();
		
		int playerPhysicsHash = customCarEntity.getPhysicsProfileHash();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(playerPhysicsHash);
		String carName = carClassesEntity.getModelSmall();
		int carVersion = carClassesEntity.getCarVersion();
		int eventMode = eventEntity.getEventModeId();
		String carClassLetter = "";
		
		Long eventDataId = eventDataEntity.getId();
		EventPowerupsEntity eventPowerupsEntity = eventPowerupsDAO.findByEventDataId(eventDataId);
		boolean powerUpsInRace = eventPowerupsBO.isPowerupsUsed(eventPowerupsEntity);
		String modeSymbol = ""; // Mode symbol, appears as the indicator 
		String chatIndicator = "";
		if (powerUpsInRace) {modeSymbol = "P"; }
		if (!powerUpsInRace) {modeSymbol = "N"; }
		if (openClass) {
			carClassLetter = eventResultBO.getCarClassLetter(carClassHash);
			chatIndicator = carClassLetter;
		}
		if (eventMode == 9 || eventMode == 4) { // Sprints and Circuits
			chatIndicator = modeSymbol + "/" + carClassLetter;
		}
		
		RecordsEntity recordsEntity = recordsDAO.findCurrentRace(eventEntity, userEntity, powerUpsInRace, carClassHash);
		if (recordsEntity == null) {
			// Making the new record entry
			RecordsEntity recordsEntityNew = new RecordsEntity();
			
			recordsEntityNew.setTimeMS(eventDuration);
			recordsEntityNew.setTimeMSAlt(eventAltDuration);
			recordsEntityNew.setTimeMSSrv(eventSrvDuration); // Server-sided event timer, can differs from the main time but unaffected by game's bugs
			recordsEntityNew.setTimeMSOld((long) 0); // There is no previous results yet
			recordsEntityNew.setBestLapTimeMS(eventDataEntity.getBestLapDurationInMilliseconds());
				
			recordsEntityNew.setPowerUps(powerUpsInRace); 
			if (eventDataEntity.getPerfectStart() != 0) {recordsEntityNew.setPerfectStart(true); }
			else {recordsEntityNew.setPerfectStart(false); }
			recordsEntityNew.setIsSingle(eventDataEntity.getIsSingle());
			recordsEntityNew.setTopSpeed(eventDataEntity.getTopSpeed());
			recordsEntityNew.setAirTimeMS(eventDataEntity.getSumOfJumpsDurationInMilliseconds());
				
			recordsEntityNew.setCarClassHash(carClassHash);
			recordsEntityNew.setCarPhysicsHash(playerPhysicsHash);
			recordsEntityNew.setCarVersion(carVersion);
			recordsEntityNew.setDate(LocalDateTime.now());
			recordsEntityNew.setPlayerName(playerName); // If the player want to delete his profile, the nickname will be saved for record
			recordsEntityNew.setCarName(carName); // Small car model name for output
				
			recordsEntityNew.setEventSessionId(eventDataEntity.getEventSessionId());
			recordsEntityNew.setEventDataId(eventDataId);
			recordsEntityNew.setEventPowerups(eventPowerupsEntity);
			recordsEntityNew.setEvent(eventEntity);
			recordsEntityNew.setEventModeId(eventEntity.getEventModeId());
			recordsEntityNew.setPersona(personaEntity);
			recordsEntityNew.setUser(userEntity);
				
			recordCaptureFinished = true;
			recordsDAO.insert(recordsEntityNew);
			
			BigInteger recordRank = recordsDAO.countRecordPlace(eventId, powerUpsInRace, carClassHash, eventDuration);
			RecordsEntity wrEntity = recordsDAO.getWRRecord(eventEntity, powerUpsInRace, carClassHash, eventDuration);
			String wrPlayerName = wrEntity.getPlayerName();
			String wrCarName = wrEntity.getCarName();
			String wrEventTime = timeReadConverter.convertRecord(wrEntity.getTimeMS());
			String eventTime = timeReadConverter.convertRecord(eventDuration);
//			int skillpoints = calcSkillPoints(eventId, powerUpsInRace, carClassHash, recordRank.intValue());
			
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### NEW Personal Best | " + chatIndicator + ": " + eventTime + " (#" + recordRank + ")\n"
					+ "## WR | " + chatIndicator + ": " + wrPlayerName + " with " + wrEventTime + " / " + wrCarName), personaId);
//			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### SP Earned: " + skillpoints), personaId);
			
//			String carFullName = carClassesEntity.getFullName();
//			String message = ":camera_with_flash: **|** *" + playerName + "* **:** *" + carFullName + "* **: " + eventName + " (" + eventTime + ") :** *" + powerUpsMode + "*";
//			discordBot.sendMessage(message, true);
		}
		if ((recordsEntity != null && recordsEntity.getTimeMS() > eventDuration) || 
				(recordsEntity != null && (playerPhysicsHash == recordsEntity.getCarPhysicsHash() && recordsEntity.getCarVersion() != carVersion)) 
				&& !recordCaptureFinished) {
			// Update the existing record entry	
			recordsEntity.setTimeMSOld(recordsEntity.getTimeMS());
			recordsEntity.setTimeMS(eventDuration);
			recordsEntity.setTimeMSAlt(eventAltDuration);
			recordsEntity.setTimeMSSrv(eventSrvDuration); // Server-sided event timer, can differs from the main time but unaffected by game's bugs
			recordsEntity.setBestLapTimeMS(eventDataEntity.getBestLapDurationInMilliseconds());
				
			recordsEntity.setPowerUps(powerUpsInRace); 
			if (eventDataEntity.getPerfectStart() != 0) {recordsEntity.setPerfectStart(true); }
			else {recordsEntity.setPerfectStart(false); }
			recordsEntity.setIsSingle(eventDataEntity.getIsSingle());
			recordsEntity.setTopSpeed(eventDataEntity.getTopSpeed());
			recordsEntity.setAirTimeMS(eventDataEntity.getSumOfJumpsDurationInMilliseconds());
				
			recordsEntity.setCarClassHash(carClassHash);
			recordsEntity.setCarPhysicsHash(playerPhysicsHash);
			recordsEntity.setCarVersion(carVersion);
			recordsEntity.setDate(LocalDateTime.now());
			recordsEntity.setPlayerName(playerName); // If the player want to delete his profile, the nickname will be saved for record
			String oldCar = recordsEntity.getCarName();
			recordsEntity.setCarName(carName); // Small car model name for output
				
			recordsEntity.setEventSessionId(eventDataEntity.getEventSessionId());
			recordsEntity.setEventDataId(eventDataId);
			recordsEntity.setEventPowerups(eventPowerupsEntity);
//			recordsEntity.setEvent(eventEntity);
//			recordsEntity.setEventModeId(eventEntity.getEventModeId());
			recordsEntity.setPersona(personaEntity);
//			recordsEntity.setUser(personaEntity.getUser());
				
			recordCaptureFinished = true;
			recordsDAO.update(recordsEntity);
			
			BigInteger recordRank = recordsDAO.countRecordPlace(eventId, powerUpsInRace, carClassHash, eventDuration);
			RecordsEntity wrEntity = recordsDAO.getWRRecord(eventEntity, powerUpsInRace, carClassHash, eventDuration);
			String wrPlayerName = wrEntity.getPlayerName();
			String wrCarName = wrEntity.getCarName();
			String wrEventTime = timeReadConverter.convertRecord(wrEntity.getTimeMS());
			String eventTime = timeReadConverter.convertRecord(eventDataEntity.getEventDurationInMilliseconds());
			String eventTimeOld = timeReadConverter.convertRecord(recordsEntity.getTimeMSOld());
//			int skillpoints = calcSkillPoints(eventId, powerUpsInRace, carClassHash, recordRank.intValue());
			
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### NEW Personal Best | " + chatIndicator + ": " + eventTime + " (#" + recordRank + ")\n"
					+ "## Previous Time | " + chatIndicator + ": " + eventTimeOld + " / " + oldCar
					+ "\n## WR | " + chatIndicator + ": " + wrPlayerName + " with " + wrEventTime + " / " + wrCarName), personaId);
//			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### SP Earned: " + skillpoints), personaId);

//			String carFullName = carClassesEntity.getFullName();
//			String message = ":camera_with_flash: **|** *" + playerName + "* **:** *" + carFullName + "* **: " + eventName + " (" + eventTime + ") :** *" + powerUpsMode + "*";
//			discordBot.sendMessage(message, true);
		}
		// Player's best is not changed
		if (recordsEntity != null && recordsEntity.getTimeMS() < eventDuration && !recordCaptureFinished) {
			recordCaptureFinished = true;
			Long currentTimeMS = recordsEntity.getTimeMS();
			BigInteger recordRank = recordsDAO.countRecordPlace(eventId, powerUpsInRace, carClassHash, currentTimeMS);
			RecordsEntity wrEntity = recordsDAO.getWRRecord(eventEntity, powerUpsInRace, carClassHash, currentTimeMS);
			String wrPlayerName = wrEntity.getPlayerName();
			String wrCarName = wrEntity.getCarName();
			String wrEventTime = timeReadConverter.convertRecord(wrEntity.getTimeMS());
			String eventTime = timeReadConverter.convertRecord(currentTimeMS);
			
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your Current Record | " + chatIndicator + ": " + eventTime + " (#" + recordRank + ") / " + recordsEntity.getCarName()
			+ "\n## WR | " + chatIndicator + ": " + wrPlayerName + " with " + wrEventTime + " / " + wrCarName), personaId);
		}
//		System.out.println("RecordEntry end");
	}
	
	// TrackMania-like score points system
	public int calcSkillPoints(int eventId, boolean powerUpsInRace, int carClassHash, int recordRank) {
		int recordCount = recordsDAO.countRecords(eventId, powerUpsInRace, carClassHash).intValue();
		int skillpoints = 0;
		if (recordCount < 2) {
			skillpoints = 25; // 1st from of 2 players will got 50 SP, so for the single result on the event, player will have initial 25 SP
		}
		else {
			skillpoints = (recordCount - 1) * 50 / recordRank;
		}
		return skillpoints;
	}
	
}
