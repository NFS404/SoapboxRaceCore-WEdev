package com.soapboxrace.core.bo;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventPowerupsDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.NewsArticlesDAO;
import com.soapboxrace.core.dao.ParameterDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventPowerupsEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.NewsArticlesEntity;
import com.soapboxrace.core.jpa.ParameterEntity;
import com.soapboxrace.core.jpa.PersonaEntity;

@Stateless
public class EventBO {

	@EJB
	private EventDAO eventDao;

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private EventDataDAO eventDataDao;
	
	@EJB
	private EventPowerupsDAO eventPowerupsDao;

	@EJB
	private PersonaDAO personaDao;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private ParameterDAO parameterDAO;
	
	@EJB
	private EventResultBO eventResultBO;
	
	@EJB
	private NewsArticlesDAO newsArticlesDAO;
	
	@EJB
	private UserDAO userDao;

	public List<EventEntity> availableAtLevel(Long personaId) {
		PersonaEntity personaEntity = personaDao.findById(personaId);
		return eventDao.findByRotation(personaEntity.getLevel());
	}

	public Long createEventDataSession(Long personaId, Long eventSessionId, Long eventTimer) {
		EventSessionEntity eventSessionEntity = findEventSessionById(eventSessionId);
		EventDataEntity eventDataEntity = new EventDataEntity();
		eventDataEntity.setPersonaId(personaId);
		eventDataEntity.setEventSessionId(eventSessionId);
		eventDataEntity.setEvent(eventSessionEntity.getEvent());
		eventDataEntity.setServerEventDuration(eventTimer); // Temp value of the event timer (current system time)
		eventDataDao.insert(eventDataEntity);
		return eventDataEntity.getId();
	}
	
	public void createEventPowerupsSession(Long personaId, Long eventDataId) {
		EventPowerupsEntity eventPowerupsEntity = new EventPowerupsEntity();
		eventPowerupsEntity.setEventData(eventDataId);
		eventPowerupsDao.insert(eventPowerupsEntity);
	}

	public EventSessionEntity createEventSession(int eventId) {
		EventEntity eventEntity = eventDao.findById(eventId);
		if (eventEntity == null) {
			return null;
		}
		EventSessionEntity eventSessionEntity = new EventSessionEntity();
		eventSessionEntity.setEvent(eventEntity);
		eventSessionEntity.setStarted(System.currentTimeMillis());
		eventSessionEntity.setTeamNOS(true); // Temporal value
		eventSessionDao.insert(eventSessionEntity);
		return eventSessionEntity;
	}
	
	// Change the current events list (every week)
	// If ROTATION_COUNT defined as 1, server will not change it (set all event's rotation ids to 1)
	// Rotation on "0" - track is always enable, "999" - not used
	// To display more than #3 rotation news (in-game), a new locale strings should be created
	@Schedule(dayOfWeek = "MON", persistent = false)
	public String eventRotation() {
		int rotationCount = parameterBO.getIntParam("ROTATION_COUNT");
		if (rotationCount == 1) {
			return "";
		}
		ParameterEntity parameterEntity = parameterDAO.findById("ROTATIONID");
		int rotationCur = Integer.valueOf(parameterEntity.getValue()) + 1;
		if (rotationCur > rotationCount) {
			rotationCur = 1;
		}
		parameterEntity.setValue(String.valueOf(rotationCur));
		parameterDAO.update(parameterEntity);
		
		NewsArticlesEntity newsRotation = newsArticlesDAO.findByName("ROTATION");
		newsRotation.setShortTextHALId("TXT_NEWS_WEV2_ROTATION_" + rotationCur + "_SHORT");
		newsRotation.setLongTextHALId("TXT_NEWS_WEV2_ROTATION_" + rotationCur + "_FULL");
		newsArticlesDAO.update(newsRotation);
		
		// Reset money send limits
		userDao.resetMoneySendLimit();
		return "";
	}
	
	// Change the current reward-bonus (and team-racing) class
	// Array structure: Sunday,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday
	// "NP" class parameter means no power-ups day, "0" disables class bonus
	@Schedule(dayOfWeek = "*", persistent = false)
	public String bonusClassRotation() {
		ParameterEntity parameterEntity = parameterDAO.findById("CLASSBONUS_CARCLASSHASH");
		String bonusClassStr = parameterBO.getStrParam("CLASSBONUS_SCHEDULE");
		String[] bonusClassArray = bonusClassStr.split(",");
		if (bonusClassArray.length != 7) {
			System.out.println("### BonusClassRotation is not defined or not vaild!");
			parameterEntity.setValue("0"); // No selected car class
			parameterDAO.update(parameterEntity);
			return "";
		}
		int dayOfWeekInt = (new GregorianCalendar().get(Calendar.DAY_OF_WEEK)) - 1; // Calendar have "1 - 7" numbers, while we need "0 - 6"
		String todayClass = bonusClassArray[dayOfWeekInt];
		parameterEntity.setValue(String.valueOf(eventResultBO.getCarClassInt(todayClass)));
		parameterDAO.update(parameterEntity);
		
		if (todayClass.contentEquals("NP")) {
			ParameterEntity parameterPUEntity = parameterDAO.findById("POWERUPS_NOPUDAY");
			parameterPUEntity.setValue("true");
			parameterDAO.update(parameterPUEntity);
			
			NewsArticlesEntity newsWednesday = newsArticlesDAO.findByName("NOPOWERUPSDAY");
			newsWednesday.setIsEnabled(true);
			newsArticlesDAO.update(newsWednesday);
			
			NewsArticlesEntity newsBonusClass = newsArticlesDAO.findByName("BONUSCLASS");
			newsBonusClass.setIsEnabled(false);
			newsArticlesDAO.update(newsBonusClass);
		}
		if (!todayClass.contentEquals("NP")) {
			ParameterEntity parameterPUEntity = parameterDAO.findById("POWERUPS_NOPUDAY");
			parameterPUEntity.setValue("false");
			parameterDAO.update(parameterPUEntity);
			
			NewsArticlesEntity newsWednesday = newsArticlesDAO.findByName("NOPOWERUPSDAY");
			newsWednesday.setIsEnabled(false);
			newsArticlesDAO.update(newsWednesday);
		}
		if (todayClass.contentEquals("0")) {
			NewsArticlesEntity newsBonusClass = newsArticlesDAO.findByName("BONUSCLASS");
			newsBonusClass.setIsEnabled(false);
			newsArticlesDAO.update(newsBonusClass);
		}
		if (!todayClass.contentEquals("0") && !todayClass.contentEquals("NP")) {
			NewsArticlesEntity newsBonusClass = newsArticlesDAO.findByName("BONUSCLASS");
			newsBonusClass.setShortTextHALId("TXT_NEWS_WEV2_BONUSCLASS_" + todayClass + "_SHORT");
			newsBonusClass.setLongTextHALId("TXT_NEWS_WEV2_BONUSCLASS_" + todayClass + "_FULL");
			newsBonusClass.setIsEnabled(true);
			newsArticlesDAO.update(newsBonusClass);
		}
		return "";
	}
	
	public EventSessionEntity findEventSessionById(Long id) {
		return eventSessionDao.findById(id);
	}
}
