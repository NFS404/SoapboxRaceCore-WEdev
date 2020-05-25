package com.soapboxrace.core.bo;

import java.time.LocalDateTime;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CarSlotEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.OwnedCarEntity;
import com.soapboxrace.jaxb.http.DragArbitrationPacket;
import com.soapboxrace.jaxb.http.DragEventResult;
import com.soapboxrace.jaxb.http.PursuitArbitrationPacket;
import com.soapboxrace.jaxb.http.PursuitEventResult;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.http.RouteEventResult;
import com.soapboxrace.jaxb.http.TeamEscapeArbitrationPacket;
import com.soapboxrace.jaxb.http.TeamEscapeEventResult;

@Stateless
public class EventResultBO {

	@EJB
	private EventResultRouteBO eventResultRouteBO;

	@EJB
	private EventResultDragBO eventResultDragBO;

	@EJB
	private EventResultTeamEscapeBO eventResultTeamEscapeBO;

	@EJB
	private EventResultPursuitBO eventResultPursuitBO;
	
	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private CarClassesDAO carClassesDAO;

	public PursuitEventResult handlePursitEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, PursuitArbitrationPacket pursuitArbitrationPacket,
			Boolean isBusted) {
		return eventResultPursuitBO.handlePursitEnd(eventSessionEntity, activePersonaId, pursuitArbitrationPacket, isBusted);
	}

	public RouteEventResult handleRaceEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, RouteArbitrationPacket routeArbitrationPacket, Long eventEnded) {
		return eventResultRouteBO.handleRaceEnd(eventSessionEntity, activePersonaId, routeArbitrationPacket, eventEnded);
	}

	public DragEventResult handleDragEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, DragArbitrationPacket dragArbitrationPacket, Long eventEnded) {
		return eventResultDragBO.handleDragEnd(eventSessionEntity, activePersonaId, dragArbitrationPacket, eventEnded);
	}

	public TeamEscapeEventResult handleTeamEscapeEnd(EventSessionEntity eventSessionEntity, Long activePersonaId,
			TeamEscapeArbitrationPacket teamEscapeArbitrationPacket) {
		return eventResultTeamEscapeBO.handleTeamEscapeEnd(eventSessionEntity, activePersonaId, teamEscapeArbitrationPacket);
	}
	
	// after 2 hours of playing, NFSW's time system can glitch sometimes, giving a possible player advantage
	// so server will save this value is player was logged for 2 hours and more
	public boolean speedBugChance (LocalDateTime lastLogin) {
		boolean speedBugChance = false;
		if (lastLogin.plusHours(2).isBefore(LocalDateTime.now()) ) {
			speedBugChance = true;
		}
		return speedBugChance;
	}
	
	public int carVersionCheck(Long personaId) {
		CarSlotEntity carSlotEntity = personaBO.getDefaultCarEntity(personaId);
		OwnedCarEntity ownedCarEntity = carSlotEntity.getOwnedCar();
		CustomCarEntity customCarEntityVer = ownedCarEntity.getCustomCar();
		
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(customCarEntityVer.getPhysicsProfileHash());
		return carClassesEntity.getCarVersion();
	}
	
	public String getCarClassLetter(int carClassHash) {
		String letter = "";
		switch(carClassHash) {
		case 872416321:
			letter = "E";
			break;
		case 415909161:
			letter = "D";
			break;
		case 1866825865:
			letter = "C";
			break;
		case -406473455:
			letter = "B";
			break;
		case -405837480:
			letter = "A";
			break;
		case -2142411446:
			letter = "S";
			break;
		case 607077938 :
			letter = "OR";
			break;
		default:
			letter = "NPC";
			break;
		}
		return letter;
	}
	
	// Drift-Spec can be modified on the client side, but we don't want to let modders participate on the events
	// FIXME Test code
	public boolean modCarCheck(Long personaId) {
		CarSlotEntity carSlotEntity = personaBO.getDefaultCarEntity(personaId);
		OwnedCarEntity ownedCarEntity = carSlotEntity.getOwnedCar();
		CustomCarEntity customCarEntity = ownedCarEntity.getCustomCar();
		int carPhysicsHash = customCarEntity.getPhysicsProfileHash();
		boolean isModCar = false;
		if (carPhysicsHash == 202813212 || carPhysicsHash == -840317713 || carPhysicsHash == -845093474 || carPhysicsHash == -133221572 || carPhysicsHash == -409661256) {
			isModCar = true;
		}
		return isModCar;
	}

}
