package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.soapboxrace.core.bo.SocialBO;
import com.soapboxrace.core.bo.TokenSessionBO;

@Path("/getblockersbyusers")
public class GetBlockersByUsers {

	@EJB
    private SocialBO socialBO;

    @EJB
    private TokenSessionBO tokenSessionBO;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getBlockedUserList(@HeaderParam("userId") Long userId, @HeaderParam("securityToken") String securityToken) {
        Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
        return Response.ok().entity(socialBO.getBlockersByUsers(activePersonaId)).build();
    }
}
