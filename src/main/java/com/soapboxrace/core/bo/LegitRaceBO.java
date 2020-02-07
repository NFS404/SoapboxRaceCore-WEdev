package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.ArbitrationPacket;
import com.soapboxrace.jaxb.http.DragArbitrationPacket;
import com.soapboxrace.jaxb.http.PursuitArbitrationPacket;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.http.TeamEscapeArbitrationPacket;

@Stateless
public class LegitRaceBO {

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private SocialBO socialBo;
	
	@EJB
    private OpenFireSoapBoxCli openFireSoapBoxCli;

	public boolean isLegit(Long activePersonaId, ArbitrationPacket arbitrationPacket, EventSessionEntity sessionEntity, boolean isSingle) {
		int minimumTime = 0;
		long eventMinTime = sessionEntity.getEvent().getMinTime();
		String eventName = sessionEntity.getEvent().getName();
		if (eventMinTime != 0) {
			minimumTime = (int) eventMinTime;
		}
		String eventType = null;

		if (arbitrationPacket instanceof PursuitArbitrationPacket) {
			minimumTime = parameterBO.getIntParam("PURSUIT_MINIMUM_TIME");
		    eventType = "Pursuit";
		}
		else if (arbitrationPacket instanceof RouteArbitrationPacket) {
			minimumTime = parameterBO.getIntParam("ROUTE_MINIMUM_TIME");
		    eventType = "Race";
		}
		else if (arbitrationPacket instanceof TeamEscapeArbitrationPacket) {
			minimumTime = parameterBO.getIntParam("TE_MINIMUM_TIME");
	        eventType = "Team Escape";
		}
		else if (arbitrationPacket instanceof DragArbitrationPacket) {
			minimumTime = parameterBO.getIntParam("DRAG_MINIMUM_TIME");
		    eventType = "Drag";
		}

		long eventDuration = arbitrationPacket.getEventDurationInMilliseconds();
//		final long timeDiff = sessionEntity.getEnded() - sessionEntity.getStarted(); // SessionEnded is NULL sometimes, eventDuration time would be more efficient
		boolean legit = eventDuration >= minimumTime;
		boolean finishReasonLegit = true;
		String isSingleText = "MP";
		if (isSingle = true) {
			isSingleText = "SP";
		}
		
		// 0 - quitted from race, 22 - finished, 518 - escaped from SP pursuit, 266 - busted on SP & MP pursuit
		if (arbitrationPacket.getFinishReason() != 0 && arbitrationPacket.getFinishReason() != 266) {
			finishReasonLegit = false;
		}
		if (!legit && !finishReasonLegit) {
			socialBo.sendReport(0L, activePersonaId, 3, String.format(eventType + " (" + eventName + "), abnormal event time (ms, session: " + sessionEntity.getId() + "): %d", eventDuration), (int) arbitrationPacket.getCarId(), 0, 0L);
		}
		if (eventDuration > 10000000) { // 4294967295 is not a vaild race time...
			socialBo.sendReport(0L, activePersonaId, 3, String.format(eventType + ", error/auto-finish (rank: " + arbitrationPacket.getRank() + ")  event time (ms): %d", eventDuration), (int) arbitrationPacket.getCarId(), 0, 0L);
		}
		if (!legit && eventType.contentEquals("Pursuit")) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### To get the reward, you need to stay on Pursuit longer."), activePersonaId);
		}
		if (arbitrationPacket.getHacksDetected() > 0 && sessionEntity.getEvent().getId() != 1000) { // 1000 - Cheat-legal freeroam event
			socialBo.sendReport(0L, activePersonaId, 3, ("Cheat report, during a " + eventType + " (" + eventName + "), " + isSingleText), (int) arbitrationPacket.getCarId(), 0,
					arbitrationPacket.getHacksDetected());
		}
		return legit;
	}
}
