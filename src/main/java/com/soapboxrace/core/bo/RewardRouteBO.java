package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.RewardVO;
import com.soapboxrace.core.bo.PersonaBO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.SkillModRewardType;
import com.soapboxrace.jaxb.http.Accolades;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.http.ArrayOfRouteEntrantResult;
import com.soapboxrace.jaxb.http.OwnedCarTrans;

@Stateless
public class RewardRouteBO extends RewardBO {

	@EJB
	private PersonaDAO personaDao;

	@EJB
	private LegitRaceBO legitRaceBO;
	
	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private ParameterBO parameterBO;

	public Accolades getRouteAccolades(Long activePersonaId, RouteArbitrationPacket routeArbitrationPacket, EventSessionEntity eventSessionEntity, ArrayOfRouteEntrantResult arrayOfRouteEntrantResult, int isDropableMode) {
		boolean isSingle = false;
		if (arrayOfRouteEntrantResult.getRouteEntrantResult().size() < 2) {
			isSingle = true;
		}
		EventEntity eventEntity = eventSessionEntity.getEvent();
		
		// Interceptor events doesn't have a legit time checks due to force time limits
		if (eventEntity.getEventModeId() != 100 && !legitRaceBO.isLegit(activePersonaId, routeArbitrationPacket, eventSessionEntity, isSingle)) {
			return new Accolades();
		}
		
		PersonaEntity personaEntity = personaDao.findById(activePersonaId);
		RewardVO rewardVO = getRewardVO(personaEntity);
		OwnedCarTrans defaultCar = personaBO.getDefaultCar(activePersonaId);

		setBaseRewardRace(personaEntity, eventEntity, routeArbitrationPacket, rewardVO, arrayOfRouteEntrantResult);
		setRankReward(eventEntity, routeArbitrationPacket, rewardVO);
		setPerfectStartReward(eventEntity, routeArbitrationPacket.getPerfectStart(), rewardVO);
		setClassBonusReward(eventEntity, defaultCar.getCustomCar().getCarClassHash(), rewardVO);
		setTopSpeedReward(eventEntity, routeArbitrationPacket.getTopSpeed(), rewardVO);
		setSkillMultiplierReward(personaEntity, rewardVO, SkillModRewardType.SOCIALITE);
		setMultiplierReward(eventEntity, rewardVO);

		applyRaceReward(rewardVO.getRep(), rewardVO.getCash(), personaEntity);
		boolean isTeamRace = false;
		if (eventSessionEntity.getTeam2Check() && parameterBO.getIntParam("TEAM_CURRENTSEASON") != 0) {
			isTeamRace = true;
		}
		return getAccolades(personaEntity, routeArbitrationPacket, rewardVO, isDropableMode, isTeamRace);
	}

}
