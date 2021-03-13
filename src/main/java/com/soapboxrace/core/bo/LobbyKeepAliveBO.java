package com.soapboxrace.core.bo;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.LobbyEntrantEntity;

@Startup
@Singleton
public class LobbyKeepAliveBO {

	@EJB
	private LobbyDAO lobbyDao;

	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private LobbyBO lobbyBO;
	
	@Resource
    private TimerService timerService;

	public void searchPriorityTimer(Long personaId, int carClassHash, int raceFilter, boolean isSClassFilterActive, int priorityTimer) {
	    TimerConfig timerConfig = new TimerConfig();
	    String[] infoArray = new String[4];
	    infoArray[0] = personaId.toString();
	    infoArray[1] = String.valueOf(carClassHash);
	    infoArray[2] = String.valueOf(raceFilter);
	    infoArray[3] = String.valueOf(isSClassFilterActive);
	    timerConfig.setInfo(infoArray);
	    timerService.createSingleActionTimer(priorityTimer, timerConfig);
	}
	
	@Timeout
	public void searchPriorityTimeout(Timer timer) {
		String[] infoArray = (String[]) timer.getInfo();
		int searchStage = 3;
		lobbyBO.joinFastLobby(Long.getLong(infoArray[0]), Integer.parseInt(infoArray[1]), Integer.parseInt(infoArray[2]), 
				Boolean.getBoolean(infoArray[3]), searchStage); // personaId, carClassHash, raceFilter, isSClassFilterActive
	}
	
	// What reason for this? Is this necessary for lobbies?
	@Schedule(second = "*/20", minute = "*", hour = "*", persistent = false)
	public void run() {
		List<LobbyEntity> findAllOpen = lobbyDao.findAllOpen();
		if (findAllOpen != null) {
			for (LobbyEntity lobbyEntity : findAllOpen) {
				List<LobbyEntrantEntity> entrants = lobbyEntity.getEntrants();
				if (entrants != null) {
					for (LobbyEntrantEntity lobbyEntrantEntity : entrants) {
						lobbyBO.sendJoinMsg(lobbyEntrantEntity.getPersona().getPersonaId(), entrants);
					}
				}
			}
		}
	}

}
