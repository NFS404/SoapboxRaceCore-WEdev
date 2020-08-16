package com.soapboxrace.core.bo;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.LobbyEntrantEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.TokenSessionEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.core.xmpp.XmppLobby;
import com.soapboxrace.jaxb.http.Entrants;
import com.soapboxrace.jaxb.http.LobbyEntrantInfo;
import com.soapboxrace.jaxb.http.LobbyEntrantState;
import com.soapboxrace.jaxb.xmpp.ChallengeType;
import com.soapboxrace.jaxb.xmpp.XMPP_CryptoTicketsType;
import com.soapboxrace.jaxb.xmpp.XMPP_EventSessionType;
import com.soapboxrace.jaxb.xmpp.XMPP_LobbyLaunchedType;
import com.soapboxrace.jaxb.xmpp.XMPP_P2PCryptoTicketType;

@Singleton
@Lock(LockType.READ)
public class LobbyCountdownBO {
	
	@EJB
	private LobbyDAO lobbyDao;
	
	@EJB
	private EventSessionDAO eventSessionDao;
	
	@EJB
	private TokenSessionDAO tokenDAO;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private PersonaDAO personaDAO;
	
	@Resource
    private TimerService timerService;

	// Using some code parts from SBRW (WU.gg) code branch
	public void scheduleLobbyStart(LobbyEntity lobbyEntity) {
	    TimerConfig timerConfig = new TimerConfig();
	    timerConfig.setInfo(lobbyEntity.getId());
	    timerService.createSingleActionTimer(parameterBO.getIntParam("LOBBY_TIME"), timerConfig);
	}
	
	@Timeout
	public void eventInit(Timer timer) {
		Long lobbyId = (Long) timer.getInfo();
		LobbyEntity lobbyEntity = lobbyDao.findById(lobbyId);
		List<LobbyEntrantEntity> entrants = lobbyEntity.getEntrants();
		if (entrants.size() < 2 || entrants.size() >= 8) {
			for (LobbyEntrantEntity poorPlayer : entrants) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Too low or too many players in this lobby - cancelled."), poorPlayer.getPersona().getPersonaId());
			}
			return;
		}
		Collections.sort(entrants);
		XMPP_LobbyLaunchedType lobbyLaunched = new XMPP_LobbyLaunchedType();
		Entrants entrantsType = new Entrants();
		List<LobbyEntrantInfo> lobbyEntrantInfo = entrantsType.getLobbyEntrantInfo();
		XMPP_CryptoTicketsType xMPP_CryptoTicketsType = new XMPP_CryptoTicketsType();
		List<XMPP_P2PCryptoTicketType> p2pCryptoTicket = xMPP_CryptoTicketsType.getP2PCryptoTicket();
		int i = 0;
		byte numOfRacers = (byte) entrants.size();
		EventSessionEntity eventSessionEntity = new EventSessionEntity();
		eventSessionEntity.setStarted(System.currentTimeMillis());
		eventSessionEntity.setEvent(lobbyEntity.getEvent());
		eventSessionEntity.setTeam1Id(lobbyEntity.getTeam1Id());
		eventSessionEntity.setTeam2Id(lobbyEntity.getTeam2Id());
		
		Long team2NOSTest = eventSessionEntity.getTeam2Id();
		boolean teamNOS = true;
		// TeamNOS - if race has been randomly started without NOS, team players wouldn't be able to use it, but others will be able
		if (team2NOSTest != null) {
			SecureRandom randNOS = new SecureRandom();
			teamNOS = randNOS.nextBoolean();
			eventSessionEntity.setTeamNOS(teamNOS);
		}
		eventSessionDao.insert(eventSessionEntity);
		String udpRaceIp = parameterBO.getStrParam("UDP_RACE_IP");
		for (LobbyEntrantEntity lobbyEntrantEntity : entrants) {
			// eventDataEntity.setIsSinglePlayer(false);
			Long personaId = lobbyEntrantEntity.getPersona().getPersonaId();
			// eventDataEntity.setPersonaId(personaId);
			byte gridIndex = (byte) i;
			byte[] helloPacket = { 10, 11, 12, 13 };
			ByteBuffer byteBuffer = ByteBuffer.allocate(48);
			byteBuffer.put(gridIndex);
			byteBuffer.put(helloPacket);
			byteBuffer.putInt(eventSessionEntity.getId().intValue());
			byteBuffer.put(numOfRacers);
			byteBuffer.putInt(personaId.intValue());
			byte[] cryptoTicketBytes = byteBuffer.array();
			String relayCrypotTicket = Base64.getEncoder().encodeToString(cryptoTicketBytes);
			tokenDAO.updateRelayCrytoTicketByPersonaId(personaId, relayCrypotTicket);

			XMPP_P2PCryptoTicketType p2pCryptoTicketType = new XMPP_P2PCryptoTicketType();
			p2pCryptoTicketType.setPersonaId(personaId);
			p2pCryptoTicketType.setSessionKey("AAAAAAAAAAAAAAAAAAAAAA==");
			p2pCryptoTicket.add(p2pCryptoTicketType);

			LobbyEntrantInfo lobbyEntrantInfoType = new LobbyEntrantInfo();
			lobbyEntrantInfoType.setPersonaId(personaId);
			lobbyEntrantInfoType.setLevel(lobbyEntrantEntity.getPersona().getLevel());
			lobbyEntrantInfoType.setHeat(1);
			lobbyEntrantInfoType.setGridIndex(i++);
			lobbyEntrantInfoType.setState(LobbyEntrantState.UNKNOWN);
			if ("127.0.0.1".equals(udpRaceIp)) {
				TokenSessionEntity tokenEntity = tokenDAO.findByUserId(lobbyEntrantEntity.getPersona().getUser().getId());
				lobbyEntrantInfoType.setUdpRaceHostIp(tokenEntity.getClientHostIp());
			}
			lobbyEntrantInfo.add(lobbyEntrantInfoType);
			
			PersonaEntity personaEntityTeam = personaDAO.findById(personaId);
			if (personaEntityTeam.getTeam() != null && team2NOSTest != null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Team NOS on this race: " + teamNOS), personaId);
			}
		}
		XMPP_EventSessionType xMPP_EventSessionType = new XMPP_EventSessionType();
		ChallengeType challengeType = new ChallengeType();
		challengeType.setChallengeId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		challengeType.setPattern("FFFFFFFFFFFFFFFF");
		challengeType.setLeftSize(14);
		challengeType.setRightSize(50);

		xMPP_EventSessionType.setEventId(lobbyEntity.getEvent().getId());
		xMPP_EventSessionType.setChallenge(challengeType);
		xMPP_EventSessionType.setSessionId(eventSessionEntity.getId());
		lobbyLaunched.setNewRelayServer(true);
		lobbyLaunched.setLobbyId(lobbyEntity.getId());
		lobbyLaunched.setUdpRelayHost(udpRaceIp);
		lobbyLaunched.setUdpRelayPort(parameterBO.getIntParam("UDP_RACE_PORT"));
		lobbyLaunched.setEntrants(entrantsType);
		lobbyLaunched.setEventSession(xMPP_EventSessionType);

		XmppLobby xmppLobby = new XmppLobby(0L, openFireSoapBoxCli);
		xmppLobby.sendRelay(lobbyLaunched, xMPP_CryptoTicketsType);
	}
}