package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.EventPowerupsDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.jpa.EventPowerupsEntity;
import com.soapboxrace.jaxb.xmpp.XMPP_PowerupActivatedType;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypePowerupActivated;

@Stateless
public class EventPowerupsBO {
	
	@EJB
	private PersonaDAO personaDao;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private EventPowerupsDAO eventPowerupsDAO;

	public String recordPowerups(int powerupHash, Long userId, Long eventDataId) {
		EventPowerupsEntity eventPowerupsEntity = eventPowerupsDAO.findByEventDataId(eventDataId);
		if (eventPowerupsEntity == null) {
			return "";
		}
		
		switch(powerupHash) {
		case -1681514783: // NOS Shot
			eventPowerupsEntity.setNosShot(eventPowerupsEntity.getNosShot() + 1);
			break;
		case 2236629: // Slingshot
			eventPowerupsEntity.setSlingshot(eventPowerupsEntity.getSlingshot() + 1);
			break;
		case 1627606782: // One More Lap / Ghost (WEv2)
			eventPowerupsEntity.setOneMoreLap(eventPowerupsEntity.getOneMoreLap() + 1);
			break;
		case 957701799: // Ready
			eventPowerupsEntity.setReady(eventPowerupsEntity.getReady() + 1);
			break;
		case -364944936: // Shield
			eventPowerupsEntity.setShield(eventPowerupsEntity.getShield() + 1);
			break;
		case 125509666: // Traffic Magnet
			eventPowerupsEntity.setTrafficMagnet(eventPowerupsEntity.getTrafficMagnet() + 1);
			break;
		case 1805681994: // Juggernaut
			eventPowerupsEntity.setJuggernaut(eventPowerupsEntity.getJuggernaut() + 1);
			break;
		case -611661916: // Emergency Evade
			eventPowerupsEntity.setEmergencyEvade(eventPowerupsEntity.getEmergencyEvade() + 1);
			break;
		case -1564932069: // Team Emergency Evade
			eventPowerupsEntity.setTeamEmergencyEvade(eventPowerupsEntity.getTeamEmergencyEvade() + 1);
			break;
		case -537557654: // Run Flat Tires
			eventPowerupsEntity.setRunFlatTires(eventPowerupsEntity.getRunFlatTires() + 1);
			break;
		case -1692359144: // Instant Cooldown
			eventPowerupsEntity.setInstantCooldown(eventPowerupsEntity.getInstantCooldown() + 1);
			break;
		case 1113720384: // Team Slingshot
			eventPowerupsEntity.setTeamSlingshot(eventPowerupsEntity.getTeamSlingshot() + 1);
			break;
		}
		eventPowerupsDAO.update(eventPowerupsEntity);
		return "";
	}
	
	public boolean isPowerupsUsed(EventPowerupsEntity eventPowerupsEntity) {
		boolean isPowerupsUsed = false;
		if (eventPowerupsEntity.getNosShot() != 0 || eventPowerupsEntity.getSlingshot() != 0 || eventPowerupsEntity.getOneMoreLap() != 0 || 
				eventPowerupsEntity.getTrafficMagnet() != 0 || eventPowerupsEntity.getTrafficMagnet() != 0 || eventPowerupsEntity.getJuggernaut() != 0 ||
				eventPowerupsEntity.getTeamEmergencyEvade() != 0 || eventPowerupsEntity.getRunFlatTires() != 0 || 
				eventPowerupsEntity.getInstantCooldown() != 0 || eventPowerupsEntity.getTeamSlingshot() != 0 
				|| eventPowerupsEntity.getEmergencyEvade() != 0 ) {
			// eventPowerupsEntity.getShield() != 0 || eventPowerupsEntity.getReady() != 0
			isPowerupsUsed = true;
		}
		return isPowerupsUsed;
	}
	
	public XMPP_ResponseTypePowerupActivated powerupResponse(Integer powerupHash, Long targetId, Long activePersonaId) {
		XMPP_ResponseTypePowerupActivated powerupActivatedResponse = new XMPP_ResponseTypePowerupActivated();
		XMPP_PowerupActivatedType powerupActivated = new XMPP_PowerupActivatedType();
		powerupActivated.setId(Long.valueOf(powerupHash));
		powerupActivated.setTargetPersonaId(targetId);
		powerupActivated.setPersonaId(activePersonaId);
		powerupActivatedResponse.setPowerupActivated(powerupActivated);
		return powerupActivatedResponse;
	}
	
}
