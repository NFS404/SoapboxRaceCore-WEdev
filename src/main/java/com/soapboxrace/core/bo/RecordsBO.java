package com.soapboxrace.core.bo;

import java.time.LocalDateTime;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.bo.util.TimeReadConverter;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.RecordsDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.RecordsEntity;
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

	public void submitRecord(EventEntity eventEntity, PersonaEntity personaEntity, EventDataEntity eventDataEntity) {
//		System.out.println("RecordEntry start");
		boolean recordCaptureFinished = false;
		Long personaId = personaEntity.getPersonaId();
		int eventId = eventEntity.getId();
		Long userId = personaEntity.getUser().getId();
		String playerName = personaEntity.getName();
		int carClassHash = eventEntity.getCarClassHash();
//		String eventName = eventEntity.getName();
		Long eventDuration = eventDataEntity.getEventDurationInMilliseconds();
		
		int playerPhysicsHash = customCarDAO.findById(eventDataEntity.getCarId()).getPhysicsProfileHash();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(playerPhysicsHash);
		String carName = carClassesEntity.getModelSmall();
		int carVersion = carClassesEntity.getCarVersion();
		boolean powerUpsInRace = personaPresenceDAO.findByUserId(userId).getPowerUpsInRace();
		String powerUpsMode = "";
		if (powerUpsInRace) {powerUpsMode = "P"; }
		else {powerUpsMode = "N"; }
		
		RecordsEntity recordsEntity = recordsDAO.findCurrentRace(eventId, userId, powerUpsInRace, carClassHash);
		if (recordsEntity == null) {
			// Making the new record entry
			RecordsEntity recordsEntityNew = new RecordsEntity();
			
			recordsEntityNew.setTimeMS(eventDuration);
			recordsEntityNew.setTimeMSAlt(eventDataEntity.getAlternateEventDurationInMilliseconds());
			recordsEntityNew.setTimeMSOld((long) 0); // There is no previous results yet
			recordsEntityNew.setBestLapTimeMS(eventDataEntity.getBestLapDurationInMilliseconds());
				
			recordsEntityNew.setPowerUps(powerUpsInRace); 
			if (eventDataEntity.getPerfectStart() != 0) {recordsEntityNew.setPerfectStart(true); }
			else {recordsEntityNew.setPerfectStart(false); }
			recordsEntityNew.setIsSingle(eventDataEntity.getIsSingle());
			recordsEntityNew.setTopSpeed(eventDataEntity.getTopSpeed());
				
			recordsEntityNew.setCarClassHash(eventEntity.getCarClassHash());
			recordsEntityNew.setCarPhysicsHash(playerPhysicsHash);
			recordsEntityNew.setCarVersion(carVersion);
			recordsEntityNew.setDate(LocalDateTime.now());
			recordsEntityNew.setPlayerName(playerName); // If the player want to delete his profile, the nickname will be saved for record
			recordsEntityNew.setCarName(carName); // Small car model name for output
				
			recordsEntityNew.setEventSessionId(eventDataEntity.getEventSessionId());
			recordsEntityNew.setEventId(eventEntity.getId());
			recordsEntityNew.setPersonaId(personaId);
			recordsEntityNew.setUserId(personaEntity.getUser().getId());
				
			recordCaptureFinished = true;
			recordsDAO.insert(recordsEntityNew);
			int recordPlace = recordsDAO.calcRecordPlace(eventId, userId, powerUpsInRace, carClassHash, carVersion, eventDuration);
			String eventTime = timeReadConverter.convertRecord(eventDuration);
			
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### NEW Personal Best | " + powerUpsMode + ": " + eventTime + " (#" + recordPlace + ")"), personaId);
			
//			String carFullName = carClassesEntity.getFullName();
//			String message = ":camera_with_flash: **|** *" + playerName + "* **:** *" + carFullName + "* **: " + eventName + " (" + eventTime + ") :** *" + powerUpsMode + "*";
//			discordBot.sendMessage(message, true);
		}
		if ((recordsEntity != null && recordsEntity.getTimeMS() > eventDuration) || (recordsEntity != null && recordsEntity.getCarVersion() != carVersion) && !recordCaptureFinished) {
			// Update the existing record entry	
			recordsEntity.setTimeMSOld(recordsEntity.getTimeMS());
			recordsEntity.setTimeMS(eventDuration);
			recordsEntity.setTimeMSAlt(eventDataEntity.getAlternateEventDurationInMilliseconds());
			recordsEntity.setBestLapTimeMS(eventDataEntity.getBestLapDurationInMilliseconds());
				
			recordsEntity.setPowerUps(powerUpsInRace); 
			if (eventDataEntity.getPerfectStart() != 0) {recordsEntity.setPerfectStart(true); }
			else {recordsEntity.setPerfectStart(false); }
			recordsEntity.setIsSingle(eventDataEntity.getIsSingle());
			recordsEntity.setTopSpeed(eventDataEntity.getTopSpeed());
				
//			recordsEntity.setCarClassHash(eventEntity.getCarClassHash());
			recordsEntity.setCarPhysicsHash(playerPhysicsHash);
			recordsEntity.setCarVersion(carVersion);
			recordsEntity.setDate(LocalDateTime.now());
			recordsEntity.setPlayerName(playerName); // If the player want to delete his profile, the nickname will be saved for record
			recordsEntity.setCarName(carName); // Small car model name for output
				
			recordsEntity.setEventSessionId(eventDataEntity.getEventSessionId());
//			recordsEntity.setEventId(eventEntity.getId());
			recordsEntity.setPersonaId(personaId);
//			recordsEntity.setUserId(personaEntity.getUser().getId());
				
			recordCaptureFinished = true;
			recordsDAO.update(recordsEntity);
			int recordPlace = recordsDAO.calcRecordPlace(eventId, userId, powerUpsInRace, carClassHash, carVersion, eventDuration);
			String eventTime = timeReadConverter.convertRecord(eventDataEntity.getEventDurationInMilliseconds());
			String eventTimeOld = timeReadConverter.convertRecord(recordsEntity.getTimeMSOld());
			
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### NEW Personal Best | " + powerUpsMode + ": " + eventTime + " (#" + recordPlace + ")\n"
					+ "## Previous Time | " + powerUpsMode + ": " + eventTimeOld + " / " + recordsEntity.getCarName()), personaId);

//			String carFullName = carClassesEntity.getFullName();
//			String message = ":camera_with_flash: **|** *" + playerName + "* **:** *" + carFullName + "* **: " + eventName + " (" + eventTime + ") :** *" + powerUpsMode + "*";
//			discordBot.sendMessage(message, true);
		}
		// Player's best is not changed
		Long eventExistedTime = recordsEntity.getTimeMS();
		if (recordsEntity != null && recordsEntity.getTimeMS() < eventDuration && !recordCaptureFinished) {
			recordCaptureFinished = true;
			int recordPlace = recordsDAO.calcRecordPlace(eventId, userId, powerUpsInRace, carClassHash, carVersion, eventDuration);
			String eventTime = timeReadConverter.convertRecord(eventExistedTime);
			
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your Current Record | " + powerUpsMode + ": " + eventTime + " (#" + recordPlace + ") / " + recordsEntity.getCarName()), personaId);
		}
//		System.out.println("RecordEntry end");
	}
}
