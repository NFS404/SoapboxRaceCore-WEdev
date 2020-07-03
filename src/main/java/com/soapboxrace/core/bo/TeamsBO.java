package com.soapboxrace.core.bo;

import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.mail.Session;

import org.apache.commons.codec.digest.DigestUtils;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.OwnedCarTrans;

@Stateless
public class TeamsBO {

	@EJB
	private TeamsDAO teamsDao;

	@EJB
	private UserDAO userDao;
	
	@EJB
	private PersonaDAO personaDao;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private EventSessionDAO eventSessionDAO;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private EventDataDAO eventDataDao;
	
	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private EventSessionDAO eventSessionDao;
	
	@EJB
	private AchievementsBO achievementsBO;

	@Resource(mappedName = "java:jboss/mail/Gmail")
	private Session mailSession;

	public String teamJoin(String teamName, String email, String password, String nickname) {
		UserEntity userEntity = checkLogin(email, password);
		if (userEntity == null) {
			return "ERROR: invalid email or password";
		}
		PersonaEntity personaEntity = personaDao.findByName(nickname);
		if (personaEntity == null) {
			return "ERROR: wrong nickname";
		}
		if (personaEntity.getLevel() < 30) {
			return "ERROR: minimum level for teams is 30";
		}
		TeamsEntity teamsEntity = teamsDao.findByName(teamName);
		if (teamsEntity == null) {
			return "ERROR: wrong team name";
		}
		if (!teamsEntity.getOpenEntry()) {
			return "ERROR: this team is invite-only, leader of the team must invite you";
		}
		if (personaEntity.getTeam() == teamsEntity) {
			return "ERROR: you already on this team lol";
		}
		if (personaEntity.getTeam() != null) {
			return "ERROR: you already on another team, traitor";
		}
		if (teamsEntity.getPlayersCount() >= 8) {
			return "ERROR: this team is full";
		}
		personaEntity.setTeam(teamsEntity);
		teamsEntity.setPlayersCount(teamsEntity.getPlayersCount() + 1);
		personaDao.update(personaEntity);
		teamsDao.update(teamsEntity);
		System.out.println("Player " + nickname + " has joined to team " + teamName);
		return "DONE: you're joined to team " + teamName;
	}
	
	// Teams In-Game interactions
	public void teamJoinIG(PersonaEntity personaEntity, TeamsEntity teamsEntity) {
		String playerName = personaEntity.getName();
		String teamName = teamsEntity.getTeamName();
		personaEntity.setTeam(teamsEntity);
		teamsEntity.setPlayersCount(teamsEntity.getPlayersCount() + 1);
		personaDao.update(personaEntity);
		teamsDao.update(teamsEntity);
		String message = ":heavy_minus_sign:"
        		+ "\n:inbox_tray: **|** Nгрок **" + playerName + "** вступает в команду **" + teamName + "**."
        		+ "\n:inbox_tray: **|** Player **" + playerName + "** has joined to team **" + teamName + "**.";
		discordBot.sendMessage(message, true);
	}
	
	public void teamLeaveIG(PersonaEntity personaEntity, TeamsEntity teamsEntity) {
		String playerName = personaEntity.getName();
		String teamName = teamsEntity.getTeamName();
		personaEntity.setTeam(null);
		teamsEntity.setPlayersCount(teamsEntity.getPlayersCount() - 1);
		personaDao.update(personaEntity);
		teamsDao.update(teamsEntity);
		String message = ":heavy_minus_sign:"
        		+ "\n:outbox_tray: **|** Nгрок **" + playerName + "** покинул команду **" + teamName + "**."
        		+ "\n:outbox_tray: **|** Player **" + playerName + "** left the team **" + teamName + "**.";
		discordBot.sendMessage(message, true);
	}
	
	// Team break, note that the team infos and leader ID stays on DB
	public void teamBreakIG(PersonaEntity personaEntity, TeamsEntity teamsEntity) {
		String teamName = teamsEntity.getTeamName();
		List<PersonaEntity> listOfTeammates = teamsEntity.getListOfTeammates();
		for (PersonaEntity personaEntityTeam : listOfTeammates) {
			personaEntityTeam.setTeam(null);
			personaDao.update(personaEntityTeam);
		}
		teamsEntity.setPlayersCount(0);
		teamsEntity.setActive(false);
		teamsEntity.setOpenEntry(false);
		teamsDao.update(teamsEntity);

		String message = ":heavy_minus_sign:"
        		+ "\n:regional_indicator_f: **|** Команда **" + teamName + "** была распущена лидером."
        		+ "\n:regional_indicator_f: **|** Team **" + teamName + "** is no longer exists.";
		discordBot.sendMessage(message, true);
	}
	
	// Basic 1-ball team race, based on racers ranks (they should be correct on DB...), win 1 min timeout
	// The fastest racer of his team will bring a win on this race, depending on opponent's teams position - Hypercycle
	// carClass 0 = open races for all classes
	public void teamAccoladesBasic(Long eventSessionId) {
		if (parameterBO.getIntParam("TEAM_CURRENTSEASON") > 0) {
			try {
				Thread.sleep(60000);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			System.out.println("### TEAMS: EventSession " + eventSessionId + "has been completed, proceed");
			EventSessionEntity eventSessionEntity = eventSessionDAO.findById(eventSessionId);
			String message = "";
			String messageDebug = "";
//			System.out.println("TEST teamAccoladesBasic sleep, count " + count + ", team1check: " + eventSessionEntity.getTeam1Check() + ", team2check: " + eventSessionEntity.getTeam2Check());
			
			if (eventSessionEntity.getTeam1Check() && eventSessionEntity.getTeam2Check()) {
				int targetCarClass = parameterBO.getIntParam("CLASSBONUS_CARCLASSHASH");
				Long teamWinner = eventSessionEntity.getTeamWinner();
				Long team1 = eventSessionEntity.getTeam1Id();
				Long team2 = eventSessionEntity.getTeam2Id();
				// Placeholder
				String winnerPlayerName = "!pls fix!";
				String winnerTeamName = "!pls fix!";
				String loserTeamName = "!pls fix!";
				int winnerTeamPoints = 0;
				for (EventDataEntity racer : eventDataDao.getRacersRanked(eventSessionId)) {
					PersonaEntity racerEntity = personaDao.findById(racer.getPersonaId());
					TeamsEntity racerTeamEntity = racerEntity.getTeam();
					if (racerTeamEntity != null && teamWinner == null) {
						Long racerTeamId = racerTeamEntity.getTeamId();
						if ((racerTeamId == team1 || racerTeamId == team2)) {
							OwnedCarTrans defaultCar = personaBO.getDefaultCar(racer.getPersonaId());
							if (defaultCar.getCustomCar().getCarClassHash() == targetCarClass || targetCarClass == 0) {
								teamWinner = racerTeamId;
								eventSessionEntity.setTeamWinner(racerTeamId);
								eventSessionDao.update(eventSessionEntity);
								winnerPlayerName = racerEntity.getName();
								if (racerTeamEntity.getTeamId() == team1) {
									loserTeamName = teamsDao.findById(team2).getTeamName();
								}
								if (racerTeamEntity.getTeamId() == team2) {
									loserTeamName = teamsDao.findById(team1).getTeamName();
								}	
								winnerTeamName = racerTeamEntity.getTeamName();
								racerTeamEntity.setTeamPoints(racerTeamEntity.getTeamPoints() + 1);
								winnerTeamPoints = racerTeamEntity.getTeamPoints();
								teamsDao.update(racerTeamEntity);
								achievementsBO.applyTeamRacesWonAchievement(racerEntity);
								
								messageDebug = teamAccoladesDebugTimes(eventSessionId);
								message = ":heavy_minus_sign:"
						        		+ "\n:trophy: **|** Nгрок **" + winnerPlayerName + "** принёс победу своей команде **" + winnerTeamName + "** в заезде против **" + loserTeamName + "** (*итого очков: " + winnerTeamPoints + ", сессия " + eventSessionId + "*)."
						        		+ "\n:trophy: **|** Player **" + winnerPlayerName + "** brought victory to his team **" + winnerTeamName + "** during race against **" + loserTeamName + "** (*points: " + winnerTeamPoints + ", session " + eventSessionId + "*)."
						        		+ "\n" + messageDebug;
								discordBot.sendMessage(message, true);
							}
						}
					}
					if (teamWinner != null) {
						openFireSoapBoxCli.send(XmppChat.createSystemMessage("### " + winnerTeamName + " has won the event! +1P, total: " + winnerTeamPoints + ", session " + eventSessionId), racer.getPersonaId());
				    }
				}
				if (teamWinner == null) {
//					System.out.println("TeamAccolades forfeit end for session " + eventSessionId);
					message = ":heavy_minus_sign:"
			        		+ "\n:thinking: **|** Никто из игроков команд не финишировал за минуту после одиночного гонщика (*сессия " + eventSessionId + "*)."
			        		+ "\n:thinking: **|** Nobody from both teams is finished after lone player on 1 minute (*session " + eventSessionId + "*).";
					discordBot.sendMessage(message, true);
			    }
			}
		}
	}
	
	private String teamAccoladesDebugTimes(Long eventSessionId) {
		String messageDebug = "";
		List<EventDataEntity> listOfRacers = eventDataDao.getRacersRanked(eventSessionId);
		for (EventDataEntity racerDebug : listOfRacers) {
			PersonaEntity racerEntityDebug = personaDao.findById(racerDebug.getPersonaId());
			messageDebug = messageDebug.concat(racerDebug.getRank() + " - " + racerEntityDebug.getName() + " - " + racerDebug.getEventDurationInMilliseconds() + " ms \n");
		}
		return messageDebug;
	}
	
	public void teamEntryIG(boolean openEntryValue, TeamsEntity teamsEntity) {
		teamsEntity.setOpenEntry(openEntryValue);
		teamsDao.update(teamsEntity);
		System.out.println("team changed their entry rule");
	}
	
	public String teamCreate(String teamName, String leaderName, boolean openEntry) {
		teamName = teamName.toUpperCase();
		TeamsEntity teamsEntityCheck = teamsDao.findByName(teamName);
		if (teamsEntityCheck != null) {
			return "ERROR: this team is already exist";
		}
		PersonaEntity personaEntityLeader = personaDao.findByName(leaderName);
		if (personaEntityLeader == null) {
			return "ERROR: wrong leader nickname";
		}
		if (personaEntityLeader.getTeam() != null) {
			return "ERROR: that player is already on another team";
		}
		if (personaEntityLeader.getLevel() < 30) {
			return "ERROR: minimum level of player for teams is 30";
		}
		TeamsEntity teamsEntityNew = new TeamsEntity();
		teamsEntityNew.setTeamName(teamName);
		teamsEntityNew.setLeader(personaEntityLeader);
		String teamLeaderNickname = personaEntityLeader.getName();
		teamsEntityNew.setOpenEntry(openEntry);
		teamsEntityNew.setPlayersCount(1);
		teamsEntityNew.setActive(true);
		teamsEntityNew.setCreated(LocalDateTime.now());
		personaEntityLeader.setTeam(teamsEntityNew);
		teamsEntityNew.setCurrentRank("none");
		teamsEntityNew.setMedals(0);
		personaDao.update(personaEntityLeader);
		teamsDao.insert(teamsEntityNew);
		String message = ":heavy_minus_sign:"
        		+ "\n:newspaper: **|** Создана новая команда: **" + teamName + "**, лидер: **" + teamLeaderNickname + "**."
        		+ "\n:newspaper: **|** New team is created: **" + teamName + "**, leader is: **" + teamLeaderNickname + "**.";
		discordBot.sendMessage(message, true);
		return "DONE: new team " + teamName + " is created";
	}
	
	// Output teams leaderboard every hour into Discord
	@Schedule(hour = "*/1", persistent = false)
	public void teamStatsDiscord() {
		if (parameterBO.getBoolParam("DISCORD_ONLINECOUNT") && parameterBO.getIntParam("TEAM_CURRENTSEASON") > 0) { // Season 0 deactivates team actions
			List<TeamsEntity> teamsList = teamsDao.findAllTeams(); // entire TOP message must fit on Discord's 2,000 symbols limitation
			String seasonText = parameterBO.getStrParam("TEAM_SEASONTEXT");
			String messageAppend = "";
			String teamRank = "";
			String teamInitialRank = "";
			for (TeamsEntity team : teamsList) {
				teamInitialRank = team.getCurrentRank();
				switch (teamInitialRank) {
				case "none":
					teamRank = ":busts_in_silhouette:";
					break;
				case "bronze":
					teamRank = ":third_place:";
					break;
				case "silver":
					teamRank = ":second_place:";
					break;
				case "gold":
					teamRank = ":first_place:";
					break;
				case "elite":
					teamRank = ":medal:";
					break;
				}	
				messageAppend = messageAppend.concat("\n" + teamRank + " **" + team.getTeamName() + "** - **" +
			team.getPlayersCount() + "P** - " + team.getTeamPoints() + ":small_orange_diamond: - **" + team.getMedals() + "** :trident:");
			}
			String message = ":heavy_minus_sign:"
	        		+ "\n:city_sunset: **|** " + seasonText
	        		+ "\n:military_medal: **|** ТОП-15 команд / TOP-15 team stats:\n"
	        		+ messageAppend;
			
			discordBot.sendMessage(message, true);
		}
	}

	private UserEntity checkLogin(String email, String password) {
		password = (DigestUtils.sha1Hex(password));
		if (email != null && !email.isEmpty() && !password.isEmpty()) {
			UserEntity userEntity = userDao.findByEmail(email);
			if (userEntity != null) {
				if (password.equals(userEntity.getPassword())) {
					return userEntity;
				}
			}
		}
		return null;
	}
}
