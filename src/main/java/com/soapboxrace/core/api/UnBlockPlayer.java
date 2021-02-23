/*
 * This file is part of the Soapbox Race World core source code.
 */

package com.soapboxrace.core.api;

import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.SocialBO;
import com.soapboxrace.core.bo.TokenSessionBO;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/unblockplayer")
public class UnBlockPlayer {

    @EJB
    private TokenSessionBO tokenSessionBO;

    @EJB
    private SocialBO socialBO;

    @GET
    @Secured
    @Produces(MediaType.APPLICATION_XML)
    public Response unblockPlayer(@HeaderParam("userId") Long userId, @HeaderParam("securityToken") String securityToken, @QueryParam("otherPersonaId") Long otherPersonaId) {
        if (!tokenSessionBO.verifyToken(userId, securityToken)) {
        	return Response.ok().build();
        }
    	return Response.ok().entity(socialBO.unblockPlayer(userId, tokenSessionBO.getActivePersonaId(securityToken), otherPersonaId)).build();
    }
}