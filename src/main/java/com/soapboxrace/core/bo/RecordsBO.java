package com.soapboxrace.core.bo;

import java.time.LocalDateTime;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.bo.util.TimeReadConverter;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.PersonaDAO;
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

	public void submitRecord(EventEntity eventEntity, PersonaEntity personaEntity, EventDataEntity eventDataEntity) {
		boolean recordCaptureFinished = false;
		Long personaId = personaEntity.getPersonaId();
		int eventId = eventEntity.getId();
		Long userId = personaEntity.getUser().getId();
		int carClassHash = eventEntity.getCarClassHash();
		
		int playerPhysicsHash = customCarDAO.findById(eventDataEntity.getCarId()).getPhysicsProfileHash();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(playerPhysicsHash);
		String carName = carClassesEntity.getModelSmall();
		int carVersion = carClassesEntity.getCarVersion();
		
		RecordsEntity recordsEntity = recordsDAO.findCurrentRace(eventId, userId, false, carClassHash);
		if (recordsEntity == null) {
			// Making the new record entry
			RecordsEntity recordsEntityNew = new RecordsEntity();
			
			recordsEntityNew.setTimeMS(eventDataEntity.getEventDurationInMilliseconds());
			recordsEntityNew.setTimeMSAlt(eventDataEntity.getAlternateEventDurationInMilliseconds());
			recordsEntityNew.setTimeMSOld((long) 0); // There is no previous results yet
			recordsEntityNew.setBestLapTimeMS(eventDataEntity.getBestLapDurationInMilliseconds());
				
			recordsEntityNew.setPowerUps(false); // PowerUps detection is not done
			if (eventDataEntity.getPerfectStart() != 0) {recordsEntityNew.setPerfectStart(true); }
			else {recordsEntityNew.setPerfectStart(false); }
			recordsEntityNew.setIsSingle(eventDataEntity.getIsSingle());
			recordsEntityNew.setTopSpeed(eventDataEntity.getTopSpeed());
				
			recordsEntityNew.setCarClassHash(eventEntity.getCarClassHash());
			recordsEntityNew.setCarPhysicsHash(playerPhysicsHash);
			recordsEntityNew.setCarVersion(carVersion);
			recordsEntityNew.setDate(LocalDateTime.now());
			recordsEntityNew.setPlayerName(personaEntity.getName()); // If the player want to delete his profile, the nickname will be saved for record
			recordsEntityNew.setCarName(carName); // Small car model name for output
				
			recordsEntityNew.setEventSessionId(eventDataEntity.getEventSessionId());
			recordsEntityNew.setEventId(eventEntity.getId());
			recordsEntityNew.setPersonaId(personaId);
			recordsEntityNew.setUserId(personaEntity.getUser().getId());
				
			recordCaptureFinished = true;
			recordsDAO.insert(recordsEntityNew);
			int recordPlace = recordsDAO.calcRecordPlace(eventId, userId, false, carClassHash, carVersion);
			String eventTime = timeReadConverter.convertRecord(eventDataEntity.getEventDurationInMilliseconds());
			
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### NEW Personal Best: " + eventTime + " (#" + recordPlace + ") / " + carName), personaId);
		}
		if (recordsEntity != null && recordsEntity.getTimeMS() > eventDataEntity.getEventDurationInMilliseconds() && !recordCaptureFinished) {
			// Update the existing record entry	
			recordsEntity.setTimeMSOld(recordsEntity.getTimeMS());
			recordsEntity.setTimeMS(eventDataEntity.getEventDurationInMilliseconds());
			recordsEntity.setTimeMSAlt(eventDataEntity.getAlternateEventDurationInMilliseconds());
			recordsEntity.setBestLapTimeMS(eventDataEntity.getBestLapDurationInMilliseconds());
				
			recordsEntity.setPowerUps(false); // PowerUps detection is not done
			if (eventDataEntity.getPerfectStart() != 0) {recordsEntity.setPerfectStart(true); }
			else {recordsEntity.setPerfectStart(false); }
			recordsEntity.setIsSingle(eventDataEntity.getIsSingle());
			recordsEntity.setTopSpeed(eventDataEntity.getTopSpeed());
				
//			recordsEntity.setCarClassHash(eventEntity.getCarClassHash());
			recordsEntity.setCarPhysicsHash(playerPhysicsHash);
			recordsEntity.setCarVersion(carVersion);
			recordsEntity.setDate(LocalDateTime.now());
			recordsEntity.setPlayerName(personaEntity.getName()); // If the player want to delete his profile, the nickname will be saved for record
			recordsEntity.setCarName(carName); // Small car model name for output
				
			recordsEntity.setEventSessionId(eventDataEntity.getEventSessionId());
//			recordsEntity.setEventId(eventEntity.getId());
			recordsEntity.setPersonaId(personaId);
//			recordsEntity.setUserId(personaEntity.getUser().getId());
				
			recordCaptureFinished = true;
			recordsDAO.update(recordsEntity);
			int recordPlace = recordsDAO.calcRecordPlace(eventId, userId, false, carClassHash, carVersion);
			String eventTime = timeReadConverter.convertRecord(eventDataEntity.getEventDurationInMilliseconds());
			String eventTimeOld = timeReadConverter.convertRecord(recordsEntity.getTimeMSOld());
			
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### NEW Personal Best: " + eventTime + " (#" + recordPlace + ") / " + carName), personaId);
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Previous Best Time: " + eventTimeOld + " (#" + recordPlace + ") / " + carName), personaId);
		}
		// Player's best is not changed
		if (recordsEntity != null && recordsEntity.getTimeMS() < eventDataEntity.getEventDurationInMilliseconds() && !recordCaptureFinished) {
			recordCaptureFinished = true;
			int recordPlace = recordsDAO.calcRecordPlace(eventId, userId, false, carClassHash, carVersion);
			String eventTime = timeReadConverter.convertRecord(eventDataEntity.getEventDurationInMilliseconds());
			
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your Current Record: " + eventTime + " (#" + recordPlace + ") / " + recordsEntity.getCarName()), personaId);
		}
	}
}
