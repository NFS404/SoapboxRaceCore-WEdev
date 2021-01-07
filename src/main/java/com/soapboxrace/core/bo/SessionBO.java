package com.soapboxrace.core.bo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import com.soapboxrace.core.dao.BanDAO;
import com.soapboxrace.core.dao.ChatRoomDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.BanEntity;
import com.soapboxrace.core.jpa.ChatRoomEntity;
import com.soapboxrace.core.jpa.TokenSessionEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.ArrayOfChatRoom;
import com.soapboxrace.jaxb.http.ChatRoom;

@Stateless
public class SessionBO {

	@EJB
	private ChatRoomDAO chatRoomDao;
	
	@EJB
    private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private TokenSessionDAO tokenDAO;
	
	@EJB
	private BanDAO banDAO;
	
	@EJB
	private UserDAO userDAO;
	
	public static final DateTimeFormatter banEndFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	
	@Resource
    private TimerService timerService;
	
	public ArrayOfChatRoom getAllChatRoom() {
		List<ChatRoomEntity> chatRoomList = chatRoomDao.findAll();
		ArrayOfChatRoom arrayOfChatRoom = new ArrayOfChatRoom();
		for(ChatRoomEntity entity : chatRoomList) {
			ChatRoom chatRoom = new ChatRoom();
			chatRoom.setChannelCount(entity.getAmount());
			chatRoom.setLongName(entity.getLongName());
			chatRoom.setShortName(entity.getShortName());
			arrayOfChatRoom.getChatRoom().add(chatRoom);
		}
		return arrayOfChatRoom;
	}
	
	// We need to send the chat-ban message later than the game loading
	public void chatBanMessageTimer (String securityToken) {
		TimerConfig timerConfig = new TimerConfig();
	    timerConfig.setInfo(securityToken);
	    timerService.createSingleActionTimer(60000, timerConfig);
	}
	
	@Timeout
	public String chatBanMessageTimer (Timer timer) {
		// We get the TokenSession later, since on this moment player gets to the gameplay...
		String securityToken = (String) timer.getInfo();
		TokenSessionEntity tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		if (tokenSessionEntity == null) {
			return ""; // If player is quit already, stop the loop
		}
		Long activePersonaId = tokenSessionEntity.getActivePersonaId();
		if (activePersonaId == 0) {
			chatBanMessageTimer(securityToken); // Start the timer again, until player gets to the persona
		}
		else {
			BanEntity chatBanEntity = banDAO.findByUser(userDAO.findById(tokenSessionEntity.getUserId()));
			LocalDateTime blockDateTime = chatBanEntity.getEndsAt();
			String blockDate = (blockDateTime == null ? null : banEndFormatter.format(blockDateTime));
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This account has been banned from chats, until " + blockDate + "."), activePersonaId);
		}
		return "";
	}

}
