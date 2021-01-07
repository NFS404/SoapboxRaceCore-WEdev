package com.soapboxrace.core.api;

import java.net.URI;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.SessionBO;
import com.soapboxrace.core.dao.BanDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.jaxb.http.ChatServer;

@Path("/Session")
public class Session {

	@Context
	UriInfo uri;

	@EJB
	private SessionBO bo;

	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private BanDAO banDAO;
	
	@EJB
	private UserDAO userDAO;

	@GET
	@Path("/GetChatInfo")
	@Produces(MediaType.APPLICATION_XML)
	public ChatServer getChatInfo(@HeaderParam("securityToken") String securityToken, @HeaderParam("userId") Long userId) {
		ChatServer chatServer = new ChatServer();
		String xmppIp = parameterBO.getStrParam("XMPP_IP");
		boolean chatBanned = false; // FIXME This stuff is not working yet, xmpp address swap is failing for others
//		BanEntity chatBanEntity = banDAO.findByUser(userDAO.findById(userId));
//		if (chatBanEntity != null && chatBanEntity.getType().contentEquals("CHAT_BAN")) {
//			chatBanned = true;
//		}
		if (chatBanned || "127.0.0.1".equals(parameterBO.getStrParam("XMPP_IP"))) {
			URI myUri = uri.getBaseUri();
			xmppIp = myUri.getHost();
//			if (chatBanned) { // Disable the access to public chats for chat-banned players
//				bo.chatBanMessageTimer(securityToken);
//			}
		}
		chatServer.setIp(xmppIp);
		chatServer.setPort(parameterBO.getIntParam("XMPP_PORT"));
		chatServer.setPrefix("sbrw");
		chatServer.setRooms(bo.getAllChatRoom());
		return chatServer;
	}
}
