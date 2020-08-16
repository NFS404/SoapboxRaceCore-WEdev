package com.soapboxrace.core.bo;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventPowerupsDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.ParameterDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventPowerupsEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
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
		return "";
	}

	public EventSessionEntity findEventSessionById(Long id) {
		return eventSessionDao.findById(id);
	}

}
