package com.soapboxrace.core.xmpp;

import com.soapboxrace.jaxb.xmpp.XMPP_EventTimedOutType;
import com.soapboxrace.jaxb.xmpp.XMPP_EventTimingOutType;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeDragEntrantResult;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeEventTimedOut;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeEventTimingOut;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeRouteEntrantResult;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeTeamEscapeEntrantResult;

public class XmppEvent {

	private long personaId;

	private OpenFireSoapBoxCli openFireSoapBoxCli;

	public XmppEvent(long personaId, OpenFireSoapBoxCli openFireSoapBoxCli) {
		this.personaId = personaId;
		this.openFireSoapBoxCli = openFireSoapBoxCli;
	}

	public void sendRaceEntrantInfo(XMPP_ResponseTypeRouteEntrantResult routeEntrantResultResponse) {
		openFireSoapBoxCli.send(routeEntrantResultResponse, personaId);
	}

	public void sendTeamEscapeEntrantInfo(XMPP_ResponseTypeTeamEscapeEntrantResult teamEscapeEntrantResultResponse) {
		openFireSoapBoxCli.send(teamEscapeEntrantResultResponse, personaId);
	}

	public void sendDragEntrantInfo(XMPP_ResponseTypeDragEntrantResult dragEntrantResultResponse) {
		openFireSoapBoxCli.send(dragEntrantResultResponse, personaId);
	}

	public void sendEventTimingOut(Long eventSessionId) {
		XMPP_EventTimingOutType eventTimingOut = new XMPP_EventTimingOutType();
		eventTimingOut.setEventSessionId(eventSessionId);
		XMPP_ResponseTypeEventTimingOut eventTimingOutResponse = new XMPP_ResponseTypeEventTimingOut();
		eventTimingOutResponse.setEventTimingOut(eventTimingOut);
		openFireSoapBoxCli.send(eventTimingOutResponse, personaId);
	}
	
	public void sendEventTimedOut(Long eventSessionId) {
		XMPP_EventTimedOutType eventTimedOut = new XMPP_EventTimedOutType();
		eventTimedOut.setEventSessionId(eventSessionId);
		XMPP_ResponseTypeEventTimedOut eventTimedOutResponse = new XMPP_ResponseTypeEventTimedOut();
		eventTimedOutResponse.setEventTimedOut(eventTimedOut);
		openFireSoapBoxCli.send(eventTimedOutResponse, personaId);
	}

}