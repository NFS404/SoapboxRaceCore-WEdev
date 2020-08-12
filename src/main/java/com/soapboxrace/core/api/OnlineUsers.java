package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.dao.ServerInfoDAO;
import com.soapboxrace.core.jpa.ServerInfoEntity;

@Path("/OnlineUsers")
public class OnlineUsers {

	@EJB
	private ServerInfoDAO serverInfoDAO;

	@GET
	@Path("/getOnline")
	@Produces(MediaType.APPLICATION_JSON)
	// FIXME Where is it even used?...
	public int onlineUsers() {
		System.out.println("getOnline ACTION");
//		ServerInfoEntity serverInfoEntity = serverInfoDAO.findInfo();
//		return serverInfoEntity.getOnlineNumber();
		return 0;
	}

}
