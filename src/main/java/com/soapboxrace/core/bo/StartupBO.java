package com.soapboxrace.core.bo;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.dao.LobbyEntrantDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;

@Startup
@Singleton
public class StartupBO {
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private LobbyDAO lobbyDAO;
	
	@EJB
	private LobbyEntrantDAO lobbyEntrantDAO;
	
	@PostConstruct
    public void init() {
        personaPresenceDAO.forceResetPresence(); // Reset all player presence values, so nobody will be viewed as "online"
        lobbyEntrantDAO.deleteAll(); // Reset all lobbies information, in case if some players were kicked from the game during lobby
        lobbyDAO.deleteAll();
    }
}
