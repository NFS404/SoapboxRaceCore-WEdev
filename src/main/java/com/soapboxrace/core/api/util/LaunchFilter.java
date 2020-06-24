package com.soapboxrace.core.api.util;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.soapboxrace.core.bo.AuthenticationBO;
import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.jaxb.login.LoginStatusVO;

@LauncherChecks
@Provider
@Priority(Priorities.AUTHORIZATION)
public class LaunchFilter implements ContainerRequestFilter {
	
	// Название лаунчера от Vadimka
	public static final String RacingWorldTitle = "Racing World";
	// Версия лаунчера от Vadimka
	public static final String RacingWorldVersion = "0.11.2";

	// Название лаунчера от faost.wilde
	public static final String WEXTitle = "WEX";
	// Версия лаунчера от faost.wilde
	public static final String WEXVersion = "2.0.4";
	
	// Сообщение о некорректном лаунчере
	public static final String msg_InvalidLauncher = "Invalid launcher. Please, get the latest WEX, RW or SBRW launcher.";
	// Не верная версия у лаунчера
	public static final String msg_OldVersionlauncher = "Your launcher version is not supported. Please, update your launcher to latest version.";
	// Вы заблокированы на сервере
	public static final String msg_YouAreBanned = "You are banned up to %TIME% by reason: %REASON%.";
	
	String[] UserAgentData = null;

	@EJB
	private AuthenticationBO authenticationBO;

	@EJB
	private ParameterBO parameterBO;

	@Context
	private HttpServletRequest sr;
	
	public LaunchFilter() {}
	
	public LaunchFilter(ParameterBO param) {
		parameterBO = param;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String userAgent = requestContext.getHeaderString("User-Agent");
		String xUserAgent = requestContext.getHeaderString("X-UserAgent");
		LoginStatusVO loginStatusVO = new LoginStatusVO(0L, "", false);
		boolean system_found = false;
		if (isRWSystem(userAgent) && xUserAgent == null) {
			system_found = true;
			if (!checkVersionRW()) {
				loginStatusVO.setDescription(msg_OldVersionlauncher);
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(loginStatusVO).build());
			}
		}
		
		else if (isSBRWSystem(xUserAgent)) {
			system_found = true;
			if (!checkVersionSBRW()) {
				loginStatusVO.setDescription(msg_OldVersionlauncher);
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(loginStatusVO).build());
			}
		}
		
		if (
				!system_found
				&&
				(
					parameterBO.getBoolParam("RWAC_LAUNCHER_PROTECTION")
					||
					parameterBO.getBoolParam("ENABLE_METONATOR_LAUNCHER_PROTECTION")
				)
			) {
			loginStatusVO.setDescription(msg_InvalidLauncher);
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(loginStatusVO).build());
		}
		
		if (isBanned(requestContext)) return;
	}
	/**
	 * Использывать ли систему RW?
	 * @param userAgent - User-Agent, используемый Racing World
	 * @author Vadimka
	 */
	public boolean isRWSystem(String userAgent) {
		if (!parameterBO.getBoolParam("RWAC_LAUNCHER_PROTECTION")) return false;
		UserAgentData = RWACUserAgent.ParseUserAgent(userAgent);
		if (UserAgentData == null) return false;
		if (
				!UserAgentData[3].equalsIgnoreCase(RacingWorldTitle)
				&&
				!UserAgentData[3].equalsIgnoreCase(WEXTitle)
			) return false;
		return true;
	}
	/**
	 * Проверка версии лаунчера, через систему RW
	 * @author Vadimka
	 */
	private boolean checkVersionRW() {
		if (UserAgentData == null) return false;
		if (UserAgentData[3].equalsIgnoreCase(RacingWorldTitle) && RWACUserAgent.lowerRWVersion(UserAgentData[4], parameterBO.getStrParam("LAUNCHER_RW_MINVERSION"))) return false;
		else if (UserAgentData[3].equalsIgnoreCase(WEXTitle) && RWACUserAgent.lowerRWVersion(UserAgentData[4], parameterBO.getStrParam("LAUNCHER_WEX_MINVERSION"))) return false;
		return true;
	}
	/**
	 * Использывать ли систему SBRW?
	 * @param requestContext
	 */
	private boolean isSBRWSystem(String xUserAgent) {
		if (!parameterBO.getBoolParam("ENABLE_METONATOR_LAUNCHER_PROTECTION"))
			return false;
		
		UserAgentData = xUserAgent.split(" ", 3);
		if (
				UserAgentData.length < 3
				||
				!UserAgentData[0].equalsIgnoreCase("GameLauncherReborn")
				||
				!UserAgentData[2].equalsIgnoreCase("WinForms (+https://github.com/SoapboxRaceWorld/GameLauncher_NFSW)")
			) {
			return false;
		}
		return true;
		
		// FIXME Awful version check, should use maven's versioning maybe
		// 9.9.9.9 - SBRW Profile Exporter
		//if (xUserAgent != null && ((xUserAgent.equalsIgnoreCase("GameLauncherReborn " + parameterBO.getStrParam("LAUNCHER_SBRW_VERSION") + " WinForms (+https://github.com/worldunitedgg/GameLauncher_NFSW)")) ||
		//		(xUserAgent.equalsIgnoreCase("GameLauncherReborn 9.9.9.9 WinForms (+https://github.com/worldunitedgg/GameLauncher_NFSW)"))))
		//	return true;
		//String xUserAgent = requestContext.getHeaderString("X-UserAgent");
		// TODO Сделать проверку xUserAgent
		//return false;
	}
	/**
	 * Проверка версии лаунчера, через систему SBRW
	 * @param requestContext
	 */
	private boolean checkVersionSBRW() {
		if (RWACUserAgent.lowerRWVersion(UserAgentData[1], parameterBO.getStrParam("LAUNCHER_SBRW_VERSION")))
			return false;
		return true;
	}
	/**
	 * Заблокирован ли пользователь
	 * @param requestContext
	 */
	// FIXME Ban message can not be displayed
	public boolean isBanned(ContainerRequestContext requestContext) {
		LoginStatusVO checkIsBanned = authenticationBO.checkIsBannedAccount(sr.getParameter("email"));
		if (checkIsBanned != null && checkIsBanned.getBan() != null) {
			checkIsBanned.setDescription(msg_YouAreBanned.replace("%TIME%",checkIsBanned.getBan().getExpires()).replace("%REASON%",checkIsBanned.getBan().getReason()));
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(checkIsBanned).build());
			return true;
		}
		return false;
	}
	
	/* =============================== OLD SBRW CODE =======================================
	
	private boolean isSpecificLaunhcerHeader(ContainerRequestContext requestContext) {
		String gameLauncherHeader = parameterBO.getStrParam("GAME_LAUNCHER_HEADER");
		if (!gameLauncherHeader.isEmpty()) {
			String[] split = gameLauncherHeader.split(";");
			String header = split[0];
			String value = split[1];
			String headerString = requestContext.getHeaderString(header);
			if (headerString == null || !headerString.equals(value)) {
				LoginStatusVO loginStatusVO = new LoginStatusVO(0L, "", false);
				loginStatusVO.setDescription("Invalid launcher version.");
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(loginStatusVO).build());
				return false;
			}
		}
		return true;
	}

	private boolean isMinLauncherVersion(ContainerRequestContext requestContext) {
		int intParam = parameterBO.getIntParam("LAUNCHER_MIN_VERSION");
		if (intParam > 0) {
			String headerString = requestContext.getHeaderString("X-GameLauncherVersion");
			if (headerString == null || headerString.trim().isEmpty()) {
				return false;
			}
			int valueOf = Integer.valueOf(headerString);
			if (intParam > valueOf) {
				return false;
			}
		}
		return true;
	}

	private boolean isValidLauncher(ContainerRequestContext requestContext) {
		if (!parameterBO.getBoolParam("ENABLE_METONATOR_LAUNCHER_PROTECTION")) {
			return true;
		}
		String hwid = requestContext.getHeaderString("X-HWID");
		String userAgent = requestContext.getHeaderString("User-Agent");
		String gameLauncherHash = requestContext.getHeaderString("X-GameLauncherHash");

		if ((userAgent == null || !userAgent.equals("GameLauncher (+https://github.com/SoapboxRaceWorld/GameLauncher_NFSW)"))
				|| (hwid == null || hwid.trim().isEmpty()) || (gameLauncherHash == null || gameLauncherHash.trim().isEmpty())) {
			LoginStatusVO loginStatusVO = new LoginStatusVO(0L, "", false);
			loginStatusVO.setDescription("Invalid launcher version.");
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(loginStatusVO).build());
			return false;
		}
		return true;
	}*/
}
