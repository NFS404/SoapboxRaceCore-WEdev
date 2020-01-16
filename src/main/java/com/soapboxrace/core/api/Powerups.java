package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.bo.AchievementsBO;
import com.soapboxrace.core.bo.InventoryBO;
import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.PersonaBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.jaxb.xmpp.XMPP_PowerupActivatedType;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypePowerupActivated;

@Path("/powerups")
public class Powerups {

	@EJB
	private TokenSessionBO tokenBO;

	@EJB
	private InventoryBO inventoryBO;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private AchievementsBO achievementsBO;

	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private EventSessionDAO eventSessionDao;
	
	@EJB
	private PersonaDAO personaDAO;

	@POST
	@Path("/activated/{powerupHash}")
	@Produces(MediaType.APPLICATION_XML)
	public String activated(@HeaderParam("securityToken") String securityToken, @PathParam(value = "powerupHash") Integer powerupHash,
			@QueryParam("targetId") Long targetId, @QueryParam("receivers") String receivers, @QueryParam("eventSessionId") Long eventSessionId) {
		Long activePersonaId = tokenBO.getActivePersonaId(securityToken);

		if (parameterBO.getBoolParam("POWERUPS_ENABLED")) {
			// TeamNOS - if race has been randomly started without NOS, team players wouldn't be able to use it, but others will be able
//			PersonaEntity personaEntityTeam = personaDAO.findById(activePersonaId);
//			if (personaEntityTeam.getTeam() != null && eventSessionId != 0) {
//				EventSessionEntity eventSessionEntity = eventSessionDao.findById(eventSessionId);
//				if (!eventSessionEntity.getTeamNOS() && powerupHash == -1681514783) {
//					return "";
//				}
//			}
			XMPP_ResponseTypePowerupActivated powerupActivatedResponse = new XMPP_ResponseTypePowerupActivated();
			XMPP_PowerupActivatedType powerupActivated = new XMPP_PowerupActivatedType();
			powerupActivated.setId(Long.valueOf(powerupHash));
			powerupActivated.setTargetPersonaId(targetId);
			powerupActivated.setPersonaId(activePersonaId);
			powerupActivatedResponse.setPowerupActivated(powerupActivated);
			// achievementsBO.broadcastUITest(personaEntityTeam);
			openFireSoapBoxCli.send(powerupActivatedResponse, activePersonaId);
			for (String receiver : receivers.split("-")) {
				Long receiverPersonaId = Long.valueOf(receiver);
				if (receiverPersonaId > 10 && !activePersonaId.equals(receiverPersonaId)) {
					openFireSoapBoxCli.send(powerupActivatedResponse, receiverPersonaId);
				}
		    }
		}

		if (!inventoryBO.hasItem(activePersonaId, powerupHash)) {
//			System.out.println(String.format("Persona %d doesn't have powerup %d", activePersonaId, powerupHash));
			return "";
		}
		if (parameterBO.getBoolParam("ENABLE_POWERUP_DECREASE")) {
			inventoryBO.decrementUsage(activePersonaId, powerupHash);
		}
		PersonaEntity personaEntity = personaBO.getPersonaById(activePersonaId);
		achievementsBO.applyPowerupAchievement(personaEntity);
		return "";
	}
}
