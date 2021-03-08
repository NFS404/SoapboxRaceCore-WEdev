package com.soapboxrace.core.bo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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

import com.soapboxrace.core.bo.util.StringListConverter;
import com.soapboxrace.core.bo.util.TimeReadConverter;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.dao.OwnedCarDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.VisualPartDAO;
import com.soapboxrace.core.jpa.EventEntity;
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
	
	@EJB
	private TimeReadConverter timeReadConverter;
	
	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private OwnedCarDAO ownedCarDAO;
	
	@EJB
	private VisualPartDAO visualPartDAO;
	
	@EJB
	private StringListConverter stringListConverter;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private AchievementsBO achievementsBO;
	
	@EJB
	private TeamsDAO teamsDAO;
	
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
		EventEntity eventEntity = lobbyEntity.getEvent();
		XMPP_LobbyLaunchedType lobbyLaunched = new XMPP_LobbyLaunchedType();
		Entrants entrantsType = new Entrants();
		List<LobbyEntrantInfo> lobbyEntrantInfo = entrantsType.getLobbyEntrantInfo();
		XMPP_CryptoTicketsType xMPP_CryptoTicketsType = new XMPP_CryptoTicketsType();
		List<XMPP_P2PCryptoTicketType> p2pCryptoTicket = xMPP_CryptoTicketsType.getP2PCryptoTicket();
		int i = 0;
		byte numOfRacers = (byte) entrants.size();
		EventSessionEntity eventSessionEntity = new EventSessionEntity();
		eventSessionEntity.setStarted(System.currentTimeMillis());
		eventSessionEntity.setEvent(eventEntity);
		eventSessionEntity.setTeam1Id(lobbyEntity.getTeam1Id());
		eventSessionEntity.setTeam2Id(lobbyEntity.getTeam2Id());
		
		Long team1Id = eventSessionEntity.getTeam1Id();
		Long team2Id = eventSessionEntity.getTeam2Id();
		eventSessionEntity.setTeamNOS(false); // False by default
		// TeamNOS - if race has been randomly started without PUs, team players wouldn't be able to use it, but others will be able
		// Permanently disabled due to the community request
		eventSessionDao.insert(eventSessionEntity);
		String udpRaceIp = parameterBO.getStrParam("UDP_RACE_IP");
		
		boolean isInterceptorEvent = eventEntity.getEventModeId() == 100 ? true : false;
		String timeLimit = "!pls fix!";
		List<Long> personaCops = new ArrayList<Long>();
		List<Long> personaRacers = new ArrayList<Long>();
		if (isInterceptorEvent) {
			timeLimit = timeReadConverter.convertRecord(eventEntity.getTimeLimit());
		}
		
		for (LobbyEntrantEntity lobbyEntrantEntity : entrants) {
			// eventDataEntity.setIsSinglePlayer(false);
			PersonaEntity entrantPersona = lobbyEntrantEntity.getPersona();
			Long personaId = entrantPersona.getPersonaId();
			
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
			lobbyEntrantInfoType.setLevel(entrantPersona.getLevel());
			lobbyEntrantInfoType.setHeat(1);
			lobbyEntrantInfoType.setGridIndex(i++);
			lobbyEntrantInfoType.setState(LobbyEntrantState.UNKNOWN);
			if (isInterceptorEvent) {
				// If player has a Cop Lights item - this player is a Cop
				if (visualPartDAO.findCopLightsPart(personaBO.getDefaultCarEntity(personaId).getOwnedCar().getCustomCar()) != null) {
					personaCops.add(personaId);
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You are the Cop - hold the racers until " + timeLimit + "!"), personaId);
				}
				else { // If not - player is a Racer
					personaRacers.add(personaId);
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You are the Racer - finish until " + timeLimit + " to win!\n" +
					"## Power-Ups is disabled for Racers."), personaId);
				}
			}
			if ("127.0.0.1".equals(udpRaceIp)) {
				TokenSessionEntity tokenEntity = tokenDAO.findByUserId(lobbyEntrantEntity.getPersona().getUser().getId());
				lobbyEntrantInfoType.setUdpRaceHostIp(tokenEntity.getClientHostIp());
			}
			lobbyEntrantInfo.add(lobbyEntrantInfoType);
			
			if (entrantPersona.getTeam() != null && team2Id != null) {
				Long playerTeamId = entrantPersona.getTeam().getTeamId();
				if (playerTeamId.equals(team1Id) || playerTeamId.equals(team2Id)) { // Get the opponent team name and make a notification
					String opponentTeamName = "!pls fix!";
					if (entrantPersona.getTeam().getTeamId().equals(team2Id)) {
						opponentTeamName = teamsDAO.findById(team1Id).getTeamName();
					}
					else {opponentTeamName = teamsDAO.findById(team2Id).getTeamName();}
					achievementsBO.broadcastUICustom(personaId, opponentTeamName, "TEAMRACEMODE", 4);
					personaPresenceDAO.updateDisablePU(personaId, true); // Disable Power-Ups for Team Racing player
			    }
			}
//			if (entrantPersona.getTeam() != null && team2NOS != null) {
//				String puStatus = "TXT_WEV3_BASEANNOUNCER_TEAMPU_ON";
//				if (!teamNOS) {puStatus = "TXT_WEV3_BASEANNOUNCER_TEAMPU_OFF";}
//				achievementsBO.broadcastUICustom(personaId, puStatus, "TEAMPUMODE", 4);
//			}
		}
		if (isInterceptorEvent) {
			if (!personaCops.isEmpty() && !personaRacers.isEmpty()) {	
				eventSessionEntity.setPersonaCops(stringListConverter.listToStr(personaCops));
				eventSessionEntity.setPersonaRacers(stringListConverter.listToStr(personaRacers));
				eventSessionDao.update(eventSessionEntity);
				String playersList = "### Cops: " + stringListConverter.interceptorPersonaChatList(personaCops) + "\n"
						+ "## Racers: " + stringListConverter.interceptorPersonaChatList(personaRacers);
				for (LobbyEntrantEntity lobbyEntrantEntity : entrants) {
					Long entrantId = lobbyEntrantEntity.getPersona().getPersonaId();
					openFireSoapBoxCli.send(XmppChat.createSystemMessage(playersList), entrantId);
					if (personaRacers.contains(entrantId)) { // Give a "racer" tag to presence, so racer wouldn't be able to use PUs
						personaPresenceDAO.updateDisablePU(entrantId, true); // Disable Power-Ups for Racer player
					}
				}
			}
			else {
				for (LobbyEntrantEntity lobbyEntrantEntity : entrants) {
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Not enough Cops or Racers to begin - cancelled."), lobbyEntrantEntity.getPersona().getPersonaId());
				}
				return; // Cancel the event
			}
		}
		
		XMPP_EventSessionType xMPP_EventSessionType = new XMPP_EventSessionType();
		ChallengeType challengeType = new ChallengeType();
		challengeType.setChallengeId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		challengeType.setPattern("FFFFFFFFFFFFFFFF");
		challengeType.setLeftSize(14);
		challengeType.setRightSize(50);

		xMPP_EventSessionType.setEventId(eventEntity.getId());
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