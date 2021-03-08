package com.soapboxrace.core.bo;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.soapboxrace.core.dao.PersonaPresenceDAO;

@Startup
@Singleton
public class StartupBO {
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@PostConstruct
    public void init() {
        personaPresenceDAO.forceResetPresence(); // Reset all player presence values, so nobody will be viewed as "online"
    }
}
