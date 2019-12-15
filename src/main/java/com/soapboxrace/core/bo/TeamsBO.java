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
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.FriendListEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;

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
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private DiscordWebhook discordBot;

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
		if (parameterBO.getBoolParam("DISCORD_ONLINECOUNT")) {
			List<TeamsEntity> teamsList = teamsDao.findAllTeams();
			String messageAppend = "";
			for (TeamsEntity team : teamsList) {
				messageAppend = messageAppend.concat("\n:busts_in_silhouette: **" + team.getTeamName() + "** - **" +
			team.getPlayersCount() + "P** - " + team.getTeamPoints() + ":small_orange_diamond:");
			}
			String message = ":heavy_minus_sign:"
	        		+ "\n:city_sunset: **|** Season 1 (*19.12.2019 : 19.01.2020*)"
	        		+ "\n:military_medal: **|** Текущая статистика команд / Current team stats:\n"
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
