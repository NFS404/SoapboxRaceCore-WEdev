package com.soapboxrace.core.bo;

import java.time.LocalDate;
import java.util.stream.Stream;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.TimeReadConverter;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventMissionsDAO;
import com.soapboxrace.core.dao.ParameterDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventMissionsEntity;
import com.soapboxrace.core.jpa.ParameterEntity;
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
	private ParameterDAO parameterDAO;
	
	@EJB
	private ParameterBO parameterBO;
	
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
			achievementsBO.applyDailySeries(personaEntity, eventEntity.getId());
		}
		else {
			isDone = false; // No Rewards, since it's a replay
		}
		achievementsBO.broadcastUICustom(activePersonaId, message, "MISSIONRESULTMODE", 5);
		return isDone;
	}
	
	// Daily challenge races rotation
	// Array contains the list of eventIds
	@Schedule(dayOfWeek = "*", persistent = false)
	public String dailySeriesRotation() {
		if (parameterBO.getBoolParam("DAILYSERIES_ROTATION")) {
			ParameterEntity parameterEntity = parameterDAO.findById("DAILYSERIES_CURRENTID");
			String dailySeriesStr = parameterBO.getStrParam("DAILYSERIES_SCHEDULE");
			if (dailySeriesStr == null) {
				System.out.println("### DailySeriesRotation is not defined!");
				return "";
			}
			String[] dailySeriesArray = dailySeriesStr.split(",");
			if (dailySeriesArray.length < 2) {
				System.out.println("### DailySeriesRotation should contain 2 events or more.");
				return "";
			}
			int[] dailySeriesIntArray = Stream.of(dailySeriesArray).mapToInt(Integer::parseInt).toArray();
			int currentArrayId = Integer.parseInt(parameterEntity.getValue());
			int currentEventId = dailySeriesIntArray[currentArrayId];
				
			updateEventStatus(currentEventId, false); // Disable previous event
			
			currentArrayId++;
			if (currentArrayId >= dailySeriesIntArray.length) {currentArrayId = 0;} // Reset the rotation
			currentEventId = dailySeriesIntArray[currentArrayId];
			
			updateEventStatus(currentEventId, true); // Enable new event
			
			parameterEntity.setValue(String.valueOf(currentArrayId));
			parameterDAO.update(parameterEntity);
		}
		return "";
	}
	
	private void updateEventStatus (int eventId, boolean isEnabled) {
		EventEntity event = eventDAO.findById(eventId);
		event.setIsEnabled(isEnabled);
		eventDAO.update(event);
	}
	
}
