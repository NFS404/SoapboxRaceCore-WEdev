package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.ServerInfoDAO;
import com.soapboxrace.core.jpa.ServerInfoEntity;

@Stateless
public class GetServerInformationBO {

	@EJB
	private ServerInfoDAO serverInfoDAO;

	@EJB
	private OnlineUsersBO onlineUsersBO;

	@EJB
	private ParameterBO parameterBO;

	public ServerInfoEntity getServerInformation() {
		ServerInfoEntity serverInfoEntity = serverInfoDAO.findInfo();
		String ticketToken = parameterBO.getStrParam("TICKET_TOKEN");
		if (ticketToken != null && !ticketToken.equals("null")) {
			serverInfoEntity.setRequireTicket(true);
		}
		serverInfoEntity.setServerVersion("sbrw_wevolved");
		Integer maxOnlinePlayers = parameterBO.getIntParam("MAX_ONLINE_PLAYERS");
		serverInfoEntity.setMaxOnlinePlayers(maxOnlinePlayers);
		return serverInfoEntity;
	}

}
