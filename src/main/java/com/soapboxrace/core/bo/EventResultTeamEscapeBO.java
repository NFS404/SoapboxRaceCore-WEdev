package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppEvent;
import com.soapboxrace.jaxb.http.Accolades;
import com.soapboxrace.jaxb.http.ArrayOfTeamEscapeEntrantResult;
import com.soapboxrace.jaxb.http.ExitPath;
import com.soapboxrace.jaxb.http.TeamEscapeArbitrationPacket;
import com.soapboxrace.jaxb.http.TeamEscapeEntrantResult;
import com.soapboxrace.jaxb.http.TeamEscapeEventResult;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeTeamEscapeEntrantResult;
import com.soapboxrace.jaxb.xmpp.XMPP_TeamEscapeEntrantResultType;

@Stateless
public class EventResultTeamEscapeBO {

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private EventDataDAO eventDataDao;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private RewardTeamEscapeBO rewardTeamEscapeBO;

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

	public TeamEscapeEventResult handleTeamEscapeEnd(EventSessionEntity eventSessionEntity, Long activePersonaId,
			TeamEscapeArbitrationPacket teamEscapeArbitrationPacket) {
		Long eventSessionId = eventSessionEntity.getId();
		eventSessionEntity.setEnded(System.currentTimeMillis());

		eventSessionDao.update(eventSessionEntity);

		XMPP_TeamEscapeEntrantResultType xmppTeamEscapeResult = new XMPP_TeamEscapeEntrantResultType();
		xmppTeamEscapeResult.setEventDurationInMilliseconds(teamEscapeArbitrationPacket.getEventDurationInMilliseconds());
		xmppTeamEscapeResult.setEventSessionId(eventSessionId);
		xmppTeamEscapeResult.setFinishReason(teamEscapeArbitrationPacket.getFinishReason());
		xmppTeamEscapeResult.setPersonaId(activePersonaId);

		XMPP_ResponseTypeTeamEscapeEntrantResult teamEscapeEntrantResultResponse = new XMPP_ResponseTypeTeamEscapeEntrantResult();
		teamEscapeEntrantResultResponse.setTeamEscapeEntrantResult(xmppTeamEscapeResult);

		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);

		EventDataEntity eventDataEntity = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
		// XKAYA's arbitration exploit fix
		if (eventDataEntity.getArbitration()) {
			System.out.println("WARINING - XKAYA's arbitration exploit attempt, driver: " + personaEntity.getName());
			return null;
		}
		int currentEventId = eventDataEntity.getEvent().getId();
		achievementsBO.applyAirTimeAchievement(teamEscapeArbitrationPacket, personaEntity);
		achievementsBO.applyPursuitCostToState(teamEscapeArbitrationPacket, personaEntity);
		achievementsBO.applyTeamEscape(teamEscapeArbitrationPacket, personaEntity);
		eventDataEntity.setAlternateEventDurationInMilliseconds(teamEscapeArbitrationPacket.getAlternateEventDurationInMilliseconds());
		eventDataEntity.setBustedCount(teamEscapeArbitrationPacket.getBustedCount());
		eventDataEntity.setCarId(teamEscapeArbitrationPacket.getCarId());
		eventDataEntity.setCopsDeployed(teamEscapeArbitrationPacket.getCopsDeployed());
		eventDataEntity.setCopsDisabled(teamEscapeArbitrationPacket.getCopsDisabled());
		eventDataEntity.setCopsRammed(teamEscapeArbitrationPacket.getCopsRammed());
		eventDataEntity.setCostToState(teamEscapeArbitrationPacket.getCostToState());
		eventDataEntity.setDistanceToFinish(teamEscapeArbitrationPacket.getDistanceToFinish());
		eventDataEntity.setEventDurationInMilliseconds(teamEscapeArbitrationPacket.getEventDurationInMilliseconds());
		eventDataEntity.setEventModeId(eventDataEntity.getEvent().getEventModeId());
		eventDataEntity.setFinishReason(teamEscapeArbitrationPacket.getFinishReason());
		eventDataEntity.setFractionCompleted(teamEscapeArbitrationPacket.getFractionCompleted());
		eventDataEntity.setHacksDetected(teamEscapeArbitrationPacket.getHacksDetected());
		eventDataEntity.setInfractions(teamEscapeArbitrationPacket.getInfractions());
		eventDataEntity.setLongestJumpDurationInMilliseconds(teamEscapeArbitrationPacket.getLongestJumpDurationInMilliseconds());
		eventDataEntity.setNumberOfCollisions(teamEscapeArbitrationPacket.getNumberOfCollisions());
		eventDataEntity.setPerfectStart(teamEscapeArbitrationPacket.getPerfectStart());
		eventDataEntity.setRank(teamEscapeArbitrationPacket.getRank());
		eventDataEntity.setPersonaId(activePersonaId);
		eventDataEntity.setRoadBlocksDodged(teamEscapeArbitrationPacket.getRoadBlocksDodged());
		eventDataEntity.setSpikeStripsDodged(teamEscapeArbitrationPacket.getSpikeStripsDodged());
		eventDataEntity.setSumOfJumpsDurationInMilliseconds(teamEscapeArbitrationPacket.getSumOfJumpsDurationInMilliseconds());
		eventDataEntity.setTopSpeed(teamEscapeArbitrationPacket.getTopSpeed());
		boolean speedBugChance = eventResultBO.speedBugChance(personaEntity.getUser().getLastLogin());
		eventDataEntity.setSpeedBugChance(speedBugChance);
		eventDataDao.update(eventDataEntity);

		ArrayOfTeamEscapeEntrantResult arrayOfTeamEscapeEntrantResult = new ArrayOfTeamEscapeEntrantResult();
		// +1 to play count for this track, MP
		if (eventDataEntity.getRank() == 1 && arrayOfTeamEscapeEntrantResult.getTeamEscapeEntrantResult().size() > 1) {
			EventEntity eventEntity = eventDAO.findById(currentEventId);
			eventEntity.setFinishCount(eventEntity.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity);
			personaDAO.update(personaEntity);
		}
		// +1 to play count for this track, SP (No default SP TEs)
		if (arrayOfTeamEscapeEntrantResult.getTeamEscapeEntrantResult().size() < 2) {
			EventEntity eventEntity = eventDAO.findById(currentEventId);
			eventEntity.setFinishCount(eventEntity.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity);
			personaDAO.update(personaEntity);
			EventDataEntity eventDataEntitySP = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
			eventDataEntitySP.setIsSingle(true);
			eventDataDao.update(eventDataEntitySP);
		}
		boolean oneGetAway = false;
		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			TeamEscapeEntrantResult teamEscapeEntrantResult = new TeamEscapeEntrantResult();
			teamEscapeEntrantResult.setDistanceToFinish(racer.getDistanceToFinish());
			teamEscapeEntrantResult.setEventDurationInMilliseconds(racer.getEventDurationInMilliseconds());
			teamEscapeEntrantResult.setEventSessionId(eventSessionId);
			teamEscapeEntrantResult.setFinishReason(racer.getFinishReason());
			if (racer.getFinishReason() == 22) {
				oneGetAway = true;
			}
			teamEscapeEntrantResult.setFractionCompleted(racer.getFractionCompleted());
			teamEscapeEntrantResult.setPersonaId(racer.getPersonaId());
			teamEscapeEntrantResult.setRanking(racer.getRank());
			arrayOfTeamEscapeEntrantResult.getTeamEscapeEntrantResult().add(teamEscapeEntrantResult);

			if (!racer.getPersonaId().equals(activePersonaId)) {
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendTeamEscapeEnd(teamEscapeEntrantResultResponse);
				if (teamEscapeArbitrationPacket.getRank() == 1) {
					xmppEvent.sendEventTimingOut(eventSessionId);
				}
			}
		}
		if (oneGetAway && teamEscapeArbitrationPacket.getRank() == eventDataDao.getRacers(eventSessionId).size()) {
			for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
				Long personaId = racer.getPersonaId();
				PersonaEntity personaEntityGetAway = personaDAO.findById(personaId);
				achievementsBO.applyTeamEscapeGetAway(personaEntityGetAway);
			}
		}

		TeamEscapeEventResult teamEscapeEventResult = new TeamEscapeEventResult();
		if (arrayOfTeamEscapeEntrantResult.getTeamEscapeEntrantResult().size() < 2) {
			// For now, you can't get the rewards on SP team escapes ;)
			System.out.println("Player " + personaEntity.getName() + " has tried to finish Team Escape on SP mode.");
		}
		else {
			teamEscapeEventResult.setAccolades(rewardTeamEscapeBO.getTeamEscapeAccolades(activePersonaId, teamEscapeArbitrationPacket, eventSessionEntity));
		}
		teamEscapeEventResult.setDurability(carDamageBO.updateDamageCar(activePersonaId, teamEscapeArbitrationPacket, teamEscapeArbitrationPacket.getNumberOfCollisions()));
		teamEscapeEventResult.setEntrants(arrayOfTeamEscapeEntrantResult);
		teamEscapeEventResult.setEventId(currentEventId);
		teamEscapeEventResult.setEventSessionId(eventSessionId);
		teamEscapeEventResult.setExitPath(ExitPath.EXIT_TO_FREEROAM);
		teamEscapeEventResult.setInviteLifetimeInMilliseconds(0);
		teamEscapeEventResult.setLobbyInviteId(0);
		teamEscapeEventResult.setPersonaId(activePersonaId);
		return teamEscapeEventResult;
	}
}
