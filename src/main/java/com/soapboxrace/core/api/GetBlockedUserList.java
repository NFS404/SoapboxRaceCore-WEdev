package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.soapboxrace.core.bo.SocialBO;

@Path("/getblockeduserlist")
public class GetBlockedUserList {
	
	@EJB
    private SocialBO socialBO;

	@GET
	@Produces(MediaType.APPLICATION_XML)
	 public Response getBlockedUserList(@HeaderParam("userId") Long userId) {
        return Response.ok().entity(socialBO.getBlockedUserList(userId)).build();
    }
}
