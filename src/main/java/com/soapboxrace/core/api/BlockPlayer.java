/*
 * This file is part of the Soapbox Race World core source code.
 */

package com.soapboxrace.core.api;
import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.SocialBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.jpa.UserEntity;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/blockplayer")
public class BlockPlayer {

    @EJB
    private SocialBO socialBO;
    
    @EJB
    private TokenSessionBO tokenSessionBO;
    
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_XML)
    public Response blockPlayer(@HeaderParam("securityToken") String securityToken, @QueryParam("otherPersonaId") Long otherPersonaId) {
    	UserEntity userEntity = tokenSessionBO.getUser(securityToken);
        return Response.ok().entity(socialBO.blockPlayer(userEntity.getId(), tokenSessionBO.getActivePersonaId(securityToken), otherPersonaId)).build();
    }
}