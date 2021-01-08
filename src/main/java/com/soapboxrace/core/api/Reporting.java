package com.soapboxrace.core.api;

import java.io.InputStream;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.AdminBO;
import com.soapboxrace.core.bo.DiscordBO;
import com.soapboxrace.core.bo.HardwareInfoBO;
import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.HardwareInfoEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.jaxb.http.HardwareInfo;
import com.soapboxrace.jaxb.util.JAXBUtility;

@Path("/Reporting")
public class Reporting {

	@EJB
	private HardwareInfoBO hardwareInfoBO;

	@EJB
	private TokenSessionBO tokenBO;

	@EJB
	private UserDAO userDAO;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private AdminBO adminBO;
	
	@EJB
	private DiscordBO discordBO;
	
	@Context
	private HttpServletRequest sr;

	@POST
	@Secured
	@Path("/SendHardwareInfo")
	@Produces(MediaType.APPLICATION_XML)
	public String sendHardwareInfo(InputStream is, @HeaderParam("securityToken") String securityToken) {
		HardwareInfo hardwareInfo = JAXBUtility.unMarshal(is, HardwareInfo.class);
		HardwareInfoEntity hardwareInfoEntity = hardwareInfoBO.save(hardwareInfo);
		UserEntity user = tokenBO.getUser(securityToken);
		user.setGameHardwareHash(hardwareInfoEntity.getHardwareHash());
		userDAO.update(user);
		return "";
	}

	@POST
	@Secured
	@Path("/SendUserSettings")
	@Produces(MediaType.APPLICATION_XML)
	public String sendUserSettings() {
		return "";
	}

	@GET
	@Secured
	@Path("/SendMultiplayerConnect")
	@Produces(MediaType.APPLICATION_XML)
	public String sendMultiplayerConnect(@QueryParam("personaId") Long personaId, @QueryParam("netErrorCode") Long netErrorCode) {
		discordBO.outputNetErrorInfo(personaId, netErrorCode);
		return "";
	}

	@GET
	@Secured
	@Path("/SendClientPingTime")
	@Produces(MediaType.APPLICATION_XML)
	public String sendClientPingTime(@QueryParam("personaId") Long personaId, @QueryParam("pingTime") Long pingTime) {
		System.out.println("/SendClientPingTime: " + sr.getQueryString());
		return "";
	}

	@GET
	@Secured
	@Path("/LoginAnnouncementClicked")
	@Produces(MediaType.APPLICATION_XML)
	public String loginAnnouncementClicked() {
		return "";
	}

	@PUT
	@Path("{path:.*}")
	@Produces(MediaType.APPLICATION_XML)
	public String genericEmptyPut(@PathParam("path") String path) {
		System.out.println("Reporting empty PUT!!!" + path);
		return "";
	}
	
	@POST
	@Path("/renamePersonaAdmin")
	@Produces(MediaType.TEXT_HTML)
	public String createPromoCode(@FormParam("token") String token, @FormParam("nickname") String nickname, @FormParam("newNickname") String newNickname) {
		if (parameterBO.getStrParam("MODERATOR_TOKEN").equals(token) && nickname != null && newNickname != null) {
			return adminBO.renamePersonaAdmin(nickname, newNickname);
		}
		return "ERROR: invalid token (not a staff? quit right now, hacker)";
	}
}
