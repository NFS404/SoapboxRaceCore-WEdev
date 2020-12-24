package com.soapboxrace.core.bo;

import java.time.LocalDate;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.TimeReadConverter;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventMissionsDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventMissionsEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.jaxb.http.ArbitrationPacket;

@Stateless
public class EventMissionsBO {
	
	@EJB
	private PersonaDAO personaDao;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private EventMissionsDAO eventMissionsDAO;
	
	@EJB
	private EventDAO eventDAO;
	
	@EJB
	private AchievementsBO achievementsBO;
	
	@EJB
	private TimeReadConverter timeReadConverter;

	public void getEventMissionInfo(EventEntity eventEntity, Long activePersonaId) {
		EventMissionsEntity eventMissionsEntity = eventMissionsDAO.getEventMission(eventEntity);
		LocalDate dailyRaceDate = personaDao.findById(activePersonaId).getDailyRaceDate();
		if (eventMissionsEntity != null) {
			String eventType = eventMissionsEntity.getEventType();
			String timeTarget = "!!!";
			String message = "!pls fix!";
			Long timeLimit = eventEntity.getTimeLimit();
			LocalDate curDate = LocalDate.now();
			switch (eventType) {
			case "TimeAttack":
				timeTarget = timeReadConverter.convertRecord(timeLimit);
				message = timeTarget;
				break;
			case "Race":
				message = "TXT_WEV3_BASEANNOUNCER_RACE_GOAL";
				break;
			case "Escort":
				message = "TXT_WEV3_BASEANNOUNCER_ESCORT_GOAL";
				break;
			}
			achievementsBO.broadcastUICustom(activePersonaId, message, "MISSIONMODE", 5);
			// Daily Race's reward can be given only once per day
			if (dailyRaceDate != null && dailyRaceDate.equals(curDate)) { 
				String messageNoReward = "TXT_WEV3_BASEANNOUNCER_MISSION_REPLAY";
				achievementsBO.broadcastUICustom(activePersonaId, messageNoReward, "MISSIONMODE", 3);
			}
		}
	}
	
	public boolean getEventMissionAccolades(EventEntity eventEntity, EventMissionsEntity eventMissionsEntity, Long activePersonaId,
			ArbitrationPacket arbitrationPacket, int finishReason) {
		PersonaEntity personaEntity = personaDao.findById(activePersonaId);
		LocalDate dailyRaceDate = personaEntity.getDailyRaceDate();
		String eventType = eventMissionsEntity.getEventType();
		String message = "TXT_WEV3_BASEANNOUNCER_MISSIONRESULT_FAIL";
		Long playerTime = arbitrationPacket.getEventDurationInMilliseconds();
		int playerRank = arbitrationPacket.getRank();
		Long timeLimit = eventEntity.getTimeLimit();
		LocalDate curDate = LocalDate.now();
		boolean isDone = false;
		switch (eventType) {
		case "TimeAttack":
			if (finishReason == 22 && playerTime < timeLimit) {
				isDone = true;
				message = "TXT_WEV3_BASEANNOUNCER_MISSIONRESULT_WIN";
			}
			break;
		case "Race":
			if (playerRank == 1) {
				isDone = true;
				message = "TXT_WEV3_BASEANNOUNCER_MISSIONRESULT_WIN";
			}
			break;
		case "Escort":
			if (finishReason == 22 && playerRank == 2 && playerTime < timeLimit) {
				isDone = true;
				message = "TXT_WEV3_BASEANNOUNCER_MISSIONRESULT_WIN";
			}
			break;
		}
		if (isDone && (dailyRaceDate == null || !dailyRaceDate.equals(curDate))) {
			personaEntity.setDailyRaceDate(curDate);
			personaDao.update(personaEntity);
		}
		else {
			isDone = false; // No Rewards, since it's a replay
		}
		achievementsBO.broadcastUICustom(activePersonaId, message, "MISSIONRESULTMODE", 5);
		return isDone;
	}
	
}
