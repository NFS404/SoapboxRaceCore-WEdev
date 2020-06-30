package com.soapboxrace.core.bo;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.dao.LobbyEntrantDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.LobbyEntrantEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.core.xmpp.XmppLobby;
import com.soapboxrace.jaxb.http.ArrayOfLobbyEntrantInfo;
import com.soapboxrace.jaxb.http.LobbyCountdown;
import com.soapboxrace.jaxb.http.LobbyEntrantAdded;
import com.soapboxrace.jaxb.http.LobbyEntrantInfo;
import com.soapboxrace.jaxb.http.LobbyEntrantRemoved;
import com.soapboxrace.jaxb.http.LobbyInfo;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
import com.soapboxrace.jaxb.xmpp.XMPP_LobbyInviteType;

@Stateless
public class LobbyBO {

	@EJB
	private EventDAO eventDao;

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private PersonaDAO personaDao;

	@EJB
	private TokenSessionDAO tokenDAO;

	@EJB
	private LobbyDAO lobbyDao;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private LobbyEntrantDAO lobbyEntrantDao;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private OpenFireRestApiCli openFireRestApiCli;
	
	@EJB
	private TokenSessionBO tokenSessionBO;

	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private TeamsDAO teamsDao;
	
	@EJB
	private LobbyCountdownBO lobbyCountdownBO;

	// Checking the division of selected car - Hypercycle
	public String carDivision(int carClassHash) {
		if (carClassHash == 872416321 || carClassHash == 415909161 || carClassHash == 1866825865) {
			return "edc";
		}
		if (carClassHash == 0 || carClassHash == 1337) {
			return "npc";
		}
		return "bas";
	}
		
	public void joinFastLobby(String securityToken, Long personaId, int carClassHash, int raceFilter, int physicsProfileHash) {
		// List<LobbyEntity> lobbys = lobbyDao.findAllOpen(carClassHash);
//		System.out.println("MM START Time: " + System.currentTimeMillis());
		boolean isModCar = false;
		if (physicsProfileHash == 202813212 || physicsProfileHash == -840317713 || physicsProfileHash == -845093474) {
			isModCar = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### ModCars is restricted from events."), personaId);
		}
		if (!isModCar) {
			List<LobbyEntity> lobbys = lobbyDao.findAllMPLobbies(carClassHash, raceFilter);
			if (lobbys.isEmpty() && parameterBO.getBoolParam("RACENOW_RANDOMRACES")) {
				PersonaEntity personaEntity = personaDao.findById(personaId);
				createRandomLobby(securityToken, personaEntity);
			}
			PersonaEntity personaEntity = personaDao.findById(personaId);
			joinLobby(personaEntity, lobbys);
		}
	}

	public void joinQueueEvent(Long personaId, int eventId, int carClassHash) {
		PersonaEntity personaEntity = personaDao.findById(personaId);
//		String carDivision = this.carDivision(carClassHash);
		List<LobbyEntity> lobbys = lobbyDao.findByEventStarted(eventId);
		if (lobbys.size() == 0) {
			createLobby(personaEntity, eventId, false);
		} else {
			joinLobby(personaEntity, lobbys);
		}
	}

	public void createPrivateLobby(Long personaId, int eventId, int carClassHash) {
		List<Long> listOfPersona = openFireRestApiCli.getAllPersonaByGroup(personaId);
		if (!listOfPersona.isEmpty()) {
			PersonaEntity personaEntity = personaDao.findById(personaId);
//			String carDivision = this.carDivision(carClassHash);
			createLobby(personaEntity, eventId, true);

			LobbyEntity lobbys = lobbyDao.findByEventAndPersona(eventId, personaId);
			if (lobbys != null) {
				XMPP_LobbyInviteType lobbyInviteType = new XMPP_LobbyInviteType();
				lobbyInviteType.setEventId(eventId);
				lobbyInviteType.setInvitedByPersonaId(personaId);
				lobbyInviteType.setInviteLifetimeInMilliseconds(60);
				lobbyInviteType.setPrivate(true);
				lobbyInviteType.setLobbyInviteId(lobbys.getId());

				for (Long idPersona : listOfPersona) {
					if (!idPersona.equals(personaId)) {
						XmppLobby xmppLobby = new XmppLobby(idPersona, openFireSoapBoxCli);
						xmppLobby.sendLobbyInvite(lobbyInviteType);
					}
				}
			}
		}
	}

	private void createLobby(PersonaEntity personaEntity, int eventId, Boolean isPrivate) {
		EventEntity eventEntity = new EventEntity();
		eventEntity.setId(eventId);

		LobbyEntity lobbyEntity = new LobbyEntity();
		lobbyEntity.setEvent(eventEntity);
		lobbyEntity.setIsPrivate(isPrivate);
		lobbyEntity.setPersonaId(personaEntity.getPersonaId());
//		lobbyEntity.setCarDivision(carDivision);
		lobbyDao.insert(lobbyEntity);

		sendJoinEvent(personaEntity.getPersonaId(), lobbyEntity);
		lobbyCountdownBO.scheduleLobbyStart(lobbyEntity);
	}
	
	private void createRandomLobby(String securityToken, PersonaEntity personaEntity) {
		EventEntity eventEntity = new EventEntity();
		eventEntity.setId(randomEventId(securityToken)); 

		LobbyEntity lobbyEntity = new LobbyEntity();
		lobbyEntity.setEvent(eventEntity);
		lobbyEntity.setIsPrivate(false);
		lobbyEntity.setPersonaId(personaEntity.getPersonaId());
		lobbyDao.insert(lobbyEntity);

//		System.out.println("MM RANDOM END Time: " + System.currentTimeMillis());
		sendJoinEvent(personaEntity.getPersonaId(), lobbyEntity);
		openFireSoapBoxCli.send(XmppChat.createSystemMessage("### New MP race is created."), personaEntity.getPersonaId());
		lobbyCountdownBO.scheduleLobbyStart(lobbyEntity);
	}
	
	// Random generated eventId, with carClassHash and isEnabled checks - Hypercycle
	// FIXME predefined eventId arrays
	private int randomEventId(String securityToken) {
		Random rand = new Random();
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		OwnedCarTrans defaultCar = personaBO.getDefaultCar(activePersonaId);
		int playerRaceFilter = defaultCar.getCustomCar().getRaceFilter();
		
		int[] eventIdArray = { 3,9,10,13,14,15,16,17,18,19,20,21,23,24,
				27,28,29,30,33,34,35,36,37,41,45,47,49,50,53,54,
				55,56,57,61,71,72,76,78,79,80,81,83,85,97,100,103,108,116,
				120,125,131,134,145,146,147,148,158,165,168,184,194,212,
				222,227,228,235,252,269,276,277,279,280,287,290,291,292,296,
				297,298,303,304,308,309,313,314,317,337,338,357,
				359,360,362,366,367,368,370,375,376,377,392,393,499,500,501,502,503,
				504,506,507,509,510,511,513,515,516,517,521,525,526,529,530,531,532,
				533,534,535,600,1100,1101,1102,1103,1104,1105,1106,1107,1108,1109,1110,1111,
				1112,1113,1114,1115,1116,1117,1118,1119,1120,1121,1122,1123,1124,1125,
				1126,1127,1128,1129,1130,1131,1132,1133,1134,1135};
	    int randomId = eventIdArray[rand.nextInt(eventIdArray.length)];
		
		EventEntity eventEntity = eventDao.findById(randomId);
		    if (!eventEntity.getIsEnabled()) {
		    //	System.out.println("randomEventId value is not available - REPEAT");
	        	return randomEventId(securityToken);
	        }
		    if (playerRaceFilter == 1 && (eventEntity.getEventModeId() == 19 || eventEntity.getEventModeId() == 24)) {
			//    	System.out.println("randomEventId value is not available by race filter (P2P) - REPEAT");
		        	return randomEventId(securityToken);
		        }
		    if (playerRaceFilter == 2 && (eventEntity.getEventModeId() == 4 || eventEntity.getEventModeId() == 9 || eventEntity.getEventModeId() == 24)) {
		    //	System.out.println("randomEventId value is not available by race filter (Drag) - REPEAT");
	        	return randomEventId(securityToken);
	        }
		    if (playerRaceFilter == 3 && eventEntity.getEventModeId() == 24) {
		    //	System.out.println("randomEventId value is not available by race filter (Race) - REPEAT");
	        	return randomEventId(securityToken);
	        }
		    if (playerRaceFilter == 4 && (eventEntity.getEventModeId() == 4 || eventEntity.getEventModeId() == 9 || eventEntity.getEventModeId() == 19)) {
		    //	System.out.println("randomEventId value is not available by race filter (Pursuit) - REPEAT");
	        	return randomEventId(securityToken);
	        }
		    if (eventEntity.getCarClassHash() != 607077938 && (defaultCar.getCustomCar().getCarClassHash() != eventEntity.getCarClassHash())) {
		    //	System.out.println("randomEventId value is restricted for player - REPEAT");
	        	return randomEventId(securityToken);
	        }
		    //  System.out.println("randomEventId generation DONE, value is " + randomId);
		    return randomId;
	}

	// FIXME I'm not sure how the server will react on lobby-list, where all lobbies is full...
	private void joinLobby(PersonaEntity personaEntity, List<LobbyEntity> lobbys) {
		LobbyEntity lobbyEntity = null;
		LobbyEntity lobbyEntityEmpty = null;
		for (LobbyEntity lobbyEntityTmp : lobbys) {
			int maxEntrants = lobbyEntityTmp.getEvent().getMaxPlayers();
			List<LobbyEntrantEntity> lobbyEntrants = lobbyEntityTmp.getEntrants();
			int entrantsSize = lobbyEntrants.size();
			if (entrantsSize < maxEntrants) {
				if (lobbyEntityEmpty == null) { // In case of empty lobby-list, player will got the first empty lobby
					lobbyEntityEmpty = lobbyEntityTmp;
				}
				if (entrantsSize > 0) {
					lobbyEntity = lobbyEntityTmp;
					if (!isPersonaInside(personaEntity.getPersonaId(), lobbyEntrants)) {
						LobbyEntrantEntity lobbyEntrantEntity = new LobbyEntrantEntity();
						lobbyEntrantEntity.setPersona(personaEntity);
						lobbyEntrantEntity.setLobby(lobbyEntity);
						lobbyEntrants.add(lobbyEntrantEntity);
					}
					break;
				}
			}
		}
		if (lobbyEntity != null) {
//			System.out.println("MM END Time: " + System.currentTimeMillis());
			sendJoinEvent(personaEntity.getPersonaId(), lobbyEntity);
		}
		if (lobbyEntity == null && lobbyEntityEmpty != null) { // If all lobbies on the search is empty, player will got the first created empty lobby
//			System.out.println("MM END Time: " + System.currentTimeMillis());
			sendJoinEvent(personaEntity.getPersonaId(), lobbyEntityEmpty);
		}
	}

	private boolean isPersonaInside(Long personaId, List<LobbyEntrantEntity> lobbyEntrants) {
		for (LobbyEntrantEntity lobbyEntrantEntity : lobbyEntrants) {
			Long entrantPersonaId = lobbyEntrantEntity.getPersona().getPersonaId();
			if (Objects.equals(entrantPersonaId, personaId)) {
				return true;
			}
		}
		return false;
	}

	private void sendJoinEvent(Long personaId, LobbyEntity lobbyEntity) {
		int eventId = lobbyEntity.getEvent().getId();
		Long lobbyId = lobbyEntity.getId();

		XMPP_LobbyInviteType xMPP_LobbyInviteType = new XMPP_LobbyInviteType();
		xMPP_LobbyInviteType.setEventId(eventId);
		xMPP_LobbyInviteType.setLobbyInviteId(lobbyId);

		XmppLobby xmppLobby = new XmppLobby(personaId, openFireSoapBoxCli);
		xmppLobby.sendLobbyInvite(xMPP_LobbyInviteType);
	}

	public LobbyInfo acceptinvite(Long personaId, Long lobbyInviteId) {
		LobbyEntity lobbyEntity = lobbyDao.findById(lobbyInviteId);
		EventEntity eventEntity = lobbyEntity.getEvent();
		int eventId = eventEntity.getId();

		LobbyCountdown lobbyCountdown = new LobbyCountdown();
		lobbyCountdown.setLobbyId(lobbyInviteId);
		lobbyCountdown.setEventId(eventId);
		lobbyCountdown.setLobbyCountdownInMilliseconds(lobbyEntity.getLobbyCountdownInMilliseconds());
		lobbyCountdown.setLobbyStuckDurationInMilliseconds(7500);

		ArrayOfLobbyEntrantInfo arrayOfLobbyEntrantInfo = new ArrayOfLobbyEntrantInfo();
		List<LobbyEntrantInfo> lobbyEntrantInfo = arrayOfLobbyEntrantInfo.getLobbyEntrantInfo();

		List<LobbyEntrantEntity> entrants = lobbyEntity.getEntrants();
		sendJoinMsg(personaId, entrants);
		boolean personaInside = false;
		
		for (LobbyEntrantEntity lobbyEntrantEntity : entrants) {
			Long teamRacerPersona = lobbyEntrantEntity.getPersona().getPersonaId();
			LobbyEntrantInfo LobbyEntrantInfo = new LobbyEntrantInfo();
			LobbyEntrantInfo.setPersonaId(teamRacerPersona);
			LobbyEntrantInfo.setLevel(lobbyEntrantEntity.getPersona().getLevel());
			LobbyEntrantInfo.setGridIndex(lobbyEntrantEntity.getGridIndex());
			lobbyEntrantInfo.add(LobbyEntrantInfo);
			if (teamRacerPersona.equals(personaId)) {
				personaInside = true;
			}
			
		}
		if (!personaInside) {
			LobbyEntrantEntity lobbyEntrantEntity = new LobbyEntrantEntity();
			PersonaEntity personaEntity = personaDao.findById(personaId);
			lobbyEntrantEntity.setPersona(personaEntity);
			lobbyEntrantEntity.setLobby(lobbyEntity);
			lobbyEntrantEntity.setGridIndex(entrants.size());
			lobbyEntity.getEntrants().add(lobbyEntrantEntity);
			lobbyDao.update(lobbyEntity);
			LobbyEntrantInfo LobbyEntrantInfo = new LobbyEntrantInfo();
			LobbyEntrantInfo.setPersonaId(lobbyEntrantEntity.getPersona().getPersonaId());
			LobbyEntrantInfo.setLevel(lobbyEntrantEntity.getPersona().getLevel());
			LobbyEntrantInfo.setGridIndex(lobbyEntrantEntity.getGridIndex());
			lobbyEntrantInfo.add(LobbyEntrantInfo);
		}

		LobbyInfo lobbyInfoType = new LobbyInfo();
		lobbyInfoType.setCountdown(lobbyCountdown);
		lobbyInfoType.setEntrants(arrayOfLobbyEntrantInfo);
		lobbyInfoType.setEventId(eventId);
		lobbyInfoType.setLobbyInviteId(lobbyInviteId);
		lobbyInfoType.setLobbyId(lobbyInviteId);
		
		// 2 teams can be inside of one race - Hypercycle
		// FIXME team 1 almost never knows it's opponent + team can't exit - no 'exit' event for team
		// carClass 0 = open races for all classes
		boolean teamIsAssigned = false;
		Long teamRacerPersona = personaId;
		PersonaEntity personaEntityRacer = personaDao.findById(teamRacerPersona);
		TeamsEntity racerTeamEntity = personaEntityRacer.getTeam();
		if (racerTeamEntity != null && racerTeamEntity.getActive() && parameterBO.getIntParam("TEAM_CURRENTSEASON") > 0
				&& eventEntity.getCarClassHash() != 607077938) { // 0 means there is no active team racing
			int serverCarClass = parameterBO.getIntParam("CLASSBONUS_CARCLASSHASH");
			OwnedCarTrans defaultCar = personaBO.getDefaultCar(personaId);
			int playerCarClass = defaultCar.getCustomCar().getCarClassHash();
			if (serverCarClass == playerCarClass || serverCarClass == 0) {
				Long team1id = lobbyEntity.getTeam1Id();
				Long team2id = lobbyEntity.getTeam2Id();
				String team1Name = "";
				String team2Name = "";
				Long racerTeamId = racerTeamEntity.getTeamId();
				if (team1id == racerTeamId) {
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your team joined as #1."), teamRacerPersona);
				}
				if (team2id == racerTeamId) {
					team1Name = teamsDao.findById(team1id).getTeamName();
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your team joined as #2. First team is " + team1Name), teamRacerPersona);
				}
				if (team1id == null && !teamIsAssigned) {
					lobbyEntity.setTeam1Id(racerTeamId);
					teamIsAssigned = true;
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your team joined as #1."), teamRacerPersona);
				}
				if (team1id != racerTeamId && team2id == null && !teamIsAssigned) {
					lobbyEntity.setTeam2Id(racerTeamId);
					teamIsAssigned = true;
					team1Name = teamsDao.findById(team1id).getTeamName();
					team2Name = racerTeamEntity.getTeamName();
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your team joined as #2. First team is " + team1Name), teamRacerPersona);
					
					for (LobbyEntrantEntity lobbyEntrantEntity : entrants) {
						PersonaEntity entrantPersona = lobbyEntrantEntity.getPersona();
						TeamsEntity teamsEntity1 = entrantPersona.getTeam();
						if (teamsEntity1 != null && teamsEntity1.getTeamId() == team1id) {
							openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Second team is " + team2Name), entrantPersona.getPersonaId());
						}
					}
				}
				if (team1id != racerTeamId && team2id != racerTeamId && !teamIsAssigned) {
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your team is not participate on that event."), teamRacerPersona);
				}
			}
		}
		return lobbyInfoType;
	}

	public void sendJoinMsg(Long personaId, List<LobbyEntrantEntity> lobbyEntrants) {
		for (LobbyEntrantEntity lobbyEntrantEntity : lobbyEntrants) {
			LobbyEntrantAdded lobbyEntrantAdded = new LobbyEntrantAdded();
			if (!Objects.equals(personaId, lobbyEntrantEntity.getPersona().getPersonaId())) {
				lobbyEntrantAdded.setHeat(1);
				lobbyEntrantAdded.setLevel(lobbyEntrantEntity.getPersona().getLevel());
				lobbyEntrantAdded.setPersonaId(personaId);
				lobbyEntrantAdded.setLobbyId(lobbyEntrantEntity.getLobby().getId());
				XmppLobby xmppLobby = new XmppLobby(lobbyEntrantEntity.getPersona().getPersonaId(), openFireSoapBoxCli);
				xmppLobby.sendJoinMsg(lobbyEntrantAdded);
			}
		}
	}

	public void deleteLobbyEntrant(Long personaId, Long lobbyId) {
		PersonaEntity personaEntity = personaDao.findById(personaId);
		lobbyEntrantDao.deleteByPersona(personaEntity);
		updateLobby(personaId, lobbyId);
	}

	private void updateLobby(Long personaId, Long lobbyId) {
		LobbyEntity lobbyEntity = lobbyDao.findById(lobbyId);
		List<LobbyEntrantEntity> listLobbyEntrantEntity = lobbyEntity.getEntrants();
		for (LobbyEntrantEntity entity : listLobbyEntrantEntity) {
			LobbyEntrantRemoved lobbyEntrantRemoved = new LobbyEntrantRemoved();
			if (!Objects.equals(entity.getPersona().getPersonaId(), personaId)) {
				lobbyEntrantRemoved.setPersonaId(personaId);
				lobbyEntrantRemoved.setLobbyId(lobbyId);
				XmppLobby xmppLobby = new XmppLobby(entity.getPersona().getPersonaId(), openFireSoapBoxCli);
				xmppLobby.sendExitMsg(lobbyEntrantRemoved);
			}
		}
	}
}
