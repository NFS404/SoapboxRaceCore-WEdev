package com.soapboxrace.core.bo;

import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.RewardVO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.SkillModRewardType;
import com.soapboxrace.jaxb.http.Accolades;
import com.soapboxrace.jaxb.http.EnumRewardType;
import com.soapboxrace.jaxb.http.PursuitArbitrationPacket;

@Stateless
public class RewardPursuitBO extends RewardBO {

	@EJB
	private PersonaDAO personaDao;

	@EJB
	private LegitRaceBO legitRaceBO;

	public Accolades getPursuitAccolades(Long activePersonaId, PursuitArbitrationPacket pursuitArbitrationPacket, EventSessionEntity eventSessionEntity,
			Boolean isBusted, int isDropableMode) {
		if (!legitRaceBO.isLegit(activePersonaId, pursuitArbitrationPacket, eventSessionEntity, true) || isBusted) {
			return new Accolades();
		}
		EventEntity eventEntity = eventSessionEntity.getEvent();
		PersonaEntity personaEntity = personaDao.findById(activePersonaId);
		RewardVO rewardVO = getRewardVO(personaEntity);

		setPursuitRewards(personaEntity, eventEntity, pursuitArbitrationPacket, rewardVO);

		Random random = new Random();
		pursuitArbitrationPacket.setRank(random.nextInt(4));
		applyRaceReward(rewardVO.getRep(), rewardVO.getCash(), personaEntity);
		return getAccolades(personaEntity, pursuitArbitrationPacket, rewardVO, isDropableMode, false, false);
	}

	public void setPursuitRewards(PersonaEntity personaEntity, EventEntity eventEntity, PursuitArbitrationPacket pursuitArbitrationPacket, RewardVO rewardVO) {
		setBaseReward(personaEntity, eventEntity, pursuitArbitrationPacket, rewardVO);
		setPursuitParamReward(pursuitArbitrationPacket.getCopsDeployed(), EnumRewardType.COP_CARS_DEPLOYED, rewardVO);
		setPursuitParamReward(pursuitArbitrationPacket.getCopsDisabled(), EnumRewardType.COP_CARS_DISABLED, rewardVO);
		setPursuitParamReward(pursuitArbitrationPacket.getCopsRammed(), EnumRewardType.COP_CARS_RAMMED, rewardVO);
		setPursuitParamReward(pursuitArbitrationPacket.getCostToState(), EnumRewardType.COST_TO_STATE, rewardVO);
		setPursuitParamReward(pursuitArbitrationPacket.getEventDurationInMilliseconds(), EnumRewardType.PURSUIT_LENGTH, rewardVO);
		setPursuitParamReward(pursuitArbitrationPacket.getHeat(), EnumRewardType.HEAT_LEVEL, rewardVO);
		setPursuitParamReward(pursuitArbitrationPacket.getInfractions(), EnumRewardType.INFRACTIONS, rewardVO);
		setPursuitParamReward(pursuitArbitrationPacket.getRoadBlocksDodged(), EnumRewardType.ROADBLOCKS_DODGED, rewardVO);
		setPursuitParamReward(pursuitArbitrationPacket.getSpikeStripsDodged(), EnumRewardType.SPIKE_STRIPS_DODGED, rewardVO);

		setTopSpeedReward(eventEntity, pursuitArbitrationPacket.getTopSpeed(), rewardVO);
		setSkillMultiplierReward(personaEntity, rewardVO, SkillModRewardType.BOUNTY_HUNTER);
		setMultiplierReward(eventEntity, rewardVO);
	}
}
