package com.soapboxrace.core.bo;

import com.soapboxrace.core.api.util.MiscUtils;
import com.soapboxrace.core.dao.BanDAO;
import com.soapboxrace.core.dao.HardwareInfoDAO;
import com.soapboxrace.core.dao.PersonaDAO;
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

    public void sendCommand(Long personaId, Long abuserPersonaId, String command) {
        CommandInfo commandInfo = CommandInfo.parse(command);
        PersonaEntity personaEntity = personaDao.findById(abuserPersonaId);
        PersonaEntity personaEntityAdmin = personaDao.findById(personaId);
        
        String bannedPlayer = personaEntity.getName();
        String adminPlayer = personaEntityAdmin.getName();

        if (personaEntity == null)
            return;

        switch (commandInfo.action) {
            case BAN:
                if (banDAO.findByUser(personaEntity.getUser()) != null) {
                    openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is already banned."), personaId);
                    break;
                }

                sendBan(personaEntity, commandInfo.timeEnd, commandInfo.reason);
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is banned."), personaId);
                String message = ":heavy_minus_sign:"
                		+ "\n:hammer: **|** Nгрок **" + bannedPlayer + "** был забанен модератором **" + adminPlayer + "**. Помянем его."
                		+ "\n:hammer: **|** Player **" + bannedPlayer + "** was banned by moderator **" + adminPlayer + "**. Remember him.";
        		discordBot.sendMessage(message);
        		
                break;
            case BAN_F:
                if (banDAO.findByUser(personaEntity.getUser()) != null) {
                    openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is already banned."), personaId);
                    break;
                }

                sendBan(personaEntity, commandInfo.timeEnd, commandInfo.reason);
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is banned forever."), personaId);
                String messageF = ":heavy_minus_sign:"
                		+ "\n:hammer: **|** Nгрок **" + bannedPlayer + "** был забанен модератором **" + adminPlayer + "**. Помянем его."
                		+ "\n:hammer: **|** Player **" + bannedPlayer + "** was banned by moderator **" + adminPlayer + "**. Remember him.";
        		discordBot.sendMessage(messageF);
        		
                break;
            case KICK:
                sendKick(personaEntity.getUser().getId(), personaEntity.getPersonaId());
                System.out.println("Player " + personaEntity.getName() + " was kicked, by " + adminPlayer);
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Tactical kick is deployed."), personaId);
                break;
            case UNBAN:
            	banDAO.unbanUser(personaEntity.getUser());
				HardwareInfoEntity hardwareInfoEntity = hardwareInfoDAO.findByUserId(personaEntity.getUser().getId());
				// FIXME Sometimes appears with proper unban sequence
				// FIXME Error 500 when user got a new account
				if (!hardwareInfoEntity.isBanned()) {
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This user is not banned. For now."), personaId);
				}
				if (hardwareInfoEntity != null) {
					hardwareInfoEntity.setBanned(false);
					hardwareInfoDAO.update(hardwareInfoEntity);
				}
                if (hardwareInfoEntity == null) {
                	openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Can't find HW entry for user - maybe user created a new account?"), personaId);
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
        sendKick(userEntity.getId(), personaEntity.getPersonaId());

        HardwareInfoEntity hardwareInfoEntity = hardwareInfoDAO.findByUserId(userEntity.getId());

        if (hardwareInfoEntity != null) {
            hardwareInfoEntity.setBanned(true);
            hardwareInfoDAO.update(hardwareInfoEntity);
        }
    }

    private void sendKick(Long userId, Long personaId) {
        openFireSoapBoxCli.send("<NewsArticleTrans><ExpiryTime><", personaId);
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
            UNKNOWN
        }
    }
}