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
import com.soapboxrace.jaxb.http.DragArbitrationPacket;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
import com.soapboxrace.jaxb.http.ArrayOfDragEntrantResult;

@Stateless
public class RewardDragBO extends RewardBO {

	@EJB
	private PersonaDAO personaDao;

	@EJB
	private LegitRaceBO legitRaceBO;
	
	@EJB
	private PersonaBO personaBO;

	public Accolades getDragAccolades(Long activePersonaId, DragArbitrationPacket dragArbitrationPacket, EventSessionEntity eventSessionEntity, ArrayOfDragEntrantResult arrayOfDragEntrantResult, int isDropableMode) {
		boolean isSingle = false;
		if (arrayOfDragEntrantResult.getDragEntrantResult().size() < 2) {
			isSingle = true;
		}
		if (!legitRaceBO.isLegit(activePersonaId, dragArbitrationPacket, eventSessionEntity, isSingle)) {
			return new Accolades();
		}
		EventEntity eventEntity = eventSessionEntity.getEvent();
		PersonaEntity personaEntity = personaDao.findById(activePersonaId);
		RewardVO rewardVO = getRewardVO(personaEntity);
		OwnedCarTrans defaultCar = personaBO.getDefaultCar(activePersonaId);
		
		setBaseRewardDrag(personaEntity, eventEntity, dragArbitrationPacket, rewardVO, arrayOfDragEntrantResult);
		setRankReward(eventEntity, dragArbitrationPacket, rewardVO);
		setPerfectStartReward(eventEntity, dragArbitrationPacket.getPerfectStart(), rewardVO);
		setClassBonusReward(eventEntity, defaultCar.getCustomCar().getCarClassHash(), rewardVO);
		setTopSpeedReward(eventEntity, dragArbitrationPacket.getTopSpeed(), rewardVO);
		setSkillMultiplierReward(personaEntity, rewardVO, SkillModRewardType.SOCIALITE);
		setMultiplierReward(eventEntity, rewardVO);

		applyRaceReward(rewardVO.getRep(), rewardVO.getCash(), personaEntity);
		return getAccolades(personaEntity, dragArbitrationPacket, rewardVO, isDropableMode, false, false);
	}
}
