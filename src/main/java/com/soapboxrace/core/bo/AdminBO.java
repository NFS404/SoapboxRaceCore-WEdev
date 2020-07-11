package com.soapboxrace.core.bo;

import com.soapboxrace.core.api.util.MiscUtils;
import com.soapboxrace.core.dao.BanDAO;
import com.soapboxrace.core.dao.HardwareInfoDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.RecordsDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.BanEntity;
import com.soapboxrace.core.jpa.HardwareInfoEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;

import com.soapboxrace.core.bo.util.DiscordWebhook;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.time.LocalDateTime;

@Stateless
public class AdminBO {
    @EJB
    private TokenSessionBO tokenSessionBo;

    @EJB
    private PersonaDAO personaDao;

    @EJB
    private UserDAO userDao;

    @EJB
    private BanDAO banDAO;

    @EJB
    private HardwareInfoDAO hardwareInfoDAO;

    @EJB
    private OpenFireSoapBoxCli openFireSoapBoxCli;
    
    @EJB
	private DiscordWebhook discordBot;
    
    @EJB
    private RecordsDAO recordsDAO;

    public void sendCommand(Long personaId, Long abuserPersonaId, String command) {
        CommandInfo commandInfo = CommandInfo.parse(command);
        PersonaEntity personaEntity = personaDao.findById(abuserPersonaId);
        PersonaEntity personaEntityAdmin = personaDao.findById(personaId);
        
        String bannedPlayer = personaEntity.getName();
        String adminPlayer = personaEntityAdmin.getName();
        UserEntity userEntity = personaEntity.getUser();

        if (personaEntity == null)
            return;

        switch (commandInfo.action) {
            case BAN:
                if (banDAO.findByUser(userEntity) != null) {
                    openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is already banned."), personaId);
                    break;
                }
                recordsDAO.banRecords(userEntity);
                sendBan(personaEntity, commandInfo.timeEnd, commandInfo.reason);
                userDao.ignoreHWBanDisable(userEntity.getId());
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is banned."), personaId);
                String message = ":heavy_minus_sign:"
                		+ "\n:hammer: **|** Nгрок **" + bannedPlayer + "** был забанен модератором **" + adminPlayer + "**. Помянем его."
                		+ "\n:hammer: **|** Player **" + bannedPlayer + "** was banned by moderator **" + adminPlayer + "**. Remember him.";
        		discordBot.sendMessage(message);
        		
                break;
            case BAN_F:
                if (banDAO.findByUser(userEntity) != null) {
                    openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is already banned."), personaId);
                    break;
                }
                recordsDAO.banRecords(userEntity);
                sendBan(personaEntity, commandInfo.timeEnd, commandInfo.reason);
                userDao.ignoreHWBanDisable(userEntity.getId());
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is banned forever."), personaId);
                String messageF = ":heavy_minus_sign:"
                		+ "\n:hammer: **|** Nгрок **" + bannedPlayer + "** был забанен модератором **" + adminPlayer + "**. Помянем его."
                		+ "\n:hammer: **|** Player **" + bannedPlayer + "** was banned by moderator **" + adminPlayer + "**. Remember him.";
        		discordBot.sendMessage(messageF);
        		
                break;
            case KICK:
                sendKick(personaEntity.getPersonaId());
                System.out.println("Player " + personaEntity.getName() + " was kicked, by " + adminPlayer);
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Tactical kick is deployed."), personaId);
                break;
            case IGNORE_HW:
                userDao.ignoreHWBan(userEntity.getId());
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This user is allowed to play with banned HW entrys."), personaId);
                break;
            case UNBAN:
            	banDAO.unbanUser(userEntity);
            	recordsDAO.unbanRecords(userEntity);
				HardwareInfoEntity hardwareInfoEntity = hardwareInfoDAO.findByUserId(userEntity.getId());
				if (hardwareInfoEntity == null) {
                	openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Unable to find HW entry for user - maybe user created a new account?"), personaId);
                	HardwareInfoEntity hardwareInfoEntityCheck = hardwareInfoDAO.findByHardwareHash(userEntity.getGameHardwareHash());
                	UserEntity bannedGuyEntity = userDao.findById(hardwareInfoEntityCheck.getUserId());
                	if (bannedGuyEntity.getPassword().equals(userEntity.getPassword()) || bannedGuyEntity.getIpAddress().equals(userEntity.getIpAddress())) {
                		openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This guy have other accounts (password or IP is the same)."), personaId);
                	}
				}
				if (!hardwareInfoEntity.isBanned()) {
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Some of user HW entrys was not banned at all."), personaId);
				}
				if (hardwareInfoEntity != null) {
					hardwareInfoEntity.setBanned(false);
					hardwareInfoDAO.update(hardwareInfoEntity);
				}
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is unbanned."), personaId);
                break;
            default:
                break;
        }
    }

    // How to use: player report > /ban [time] <reason> (example: /ban 28d14h Any Reason With Spaces)
    // Taken from Apex sources, by HeyItsLeo
    private void sendBan(PersonaEntity personaEntity, LocalDateTime endsOn, String reason) {
        UserEntity userEntity = personaEntity.getUser();
        BanEntity banEntity = new BanEntity();
        banEntity.setUserEntity(userEntity);
        banEntity.setEndsAt(endsOn);
        banEntity.setReason(reason);
        banEntity.setType("EMAIL_BAN");
        banEntity.setData(userEntity.getEmail());
        banDAO.insert(banEntity);
        userDao.update(userEntity);
        sendKick(personaEntity.getPersonaId());

        HardwareInfoEntity hardwareInfoEntity = hardwareInfoDAO.findByUserId(userEntity.getId());

        if (hardwareInfoEntity != null) {
            hardwareInfoEntity.setBanned(true);
            hardwareInfoDAO.update(hardwareInfoEntity);
        }
    }

    private void sendKick(Long personaId) {
        openFireSoapBoxCli.send("<NewsArticleTrans><ExpiryTime><", personaId);
    }
    
    public String renamePersonaAdmin(String nickname, String newNickname) {
    	PersonaEntity personaEntity = personaDao.findByName(nickname);
    	if (personaEntity == null) {
    		return "ERROR: wrong nickname";
    	}
    	personaEntity.setName("newNickname");
    	personaDao.update(personaEntity);
    	
    	sendKick(personaEntity.getPersonaId());
    	System.out.println("### Player nickname of "+ nickname +" has been changed to "+ newNickname +".");
		return "Player's nickname has been changed.";
	}

    private static class CommandInfo {
        public CommandInfo.CmdAction action;
        public String reason;
        public LocalDateTime timeEnd;

        public static CommandInfo parse(String cmd) {
            cmd = cmd.replaceFirst("/", "");

            String[] split = cmd.split(" ");
            CommandInfo.CmdAction action;
            CommandInfo info = new CommandInfo();

            switch (split[0].toLowerCase().trim()) {
                case "ban":
                    action = CmdAction.BAN;
                    break;
                case "ban_f":
                    action = CmdAction.BAN_F;
                    break;
                case "ignore_hw":
                    action = CmdAction.IGNORE_HW;
                    break;
                case "kick":
                    action = CmdAction.KICK;
                    break;
                case "unban":
                    action = CmdAction.UNBAN;
                    break;
                default:
                    action = CmdAction.UNKNOWN;
                    break;
            }

            info.action = action;

            switch (action) {
                case BAN: {
                    LocalDateTime endTime = null;
                    String reason = null;

                    if (split.length >= 2) {
                        long givenTime = MiscUtils.lengthToMiliseconds(split[1]);
                        if (givenTime != 0) {
                            endTime = LocalDateTime.now().plusSeconds(givenTime / 1000);
                            info.timeEnd = endTime;

                            if (split.length > 2) {
                                reason = MiscUtils.argsToString(split, 2, split.length);
                            }
                        } 
                    }

                    info.reason = reason;
                    break;
                }
                case BAN_F: {
                    String reason = MiscUtils.argsToString(split, 1, split.length);
                    LocalDateTime endTime = LocalDateTime.of(3000, 01, 01, 01, 23, 45); // WEv2 ban until Futurama starts
                    info.timeEnd = endTime;
                    info.reason = reason;
                    break;
                }
            }

            return info;
        }

        public enum CmdAction {
            KICK,
            BAN,
            BAN_F,
            UNBAN,
            IGNORE_HW,
            UNKNOWN
        }
    }
}