package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.RewardVO;
import com.soapboxrace.core.bo.PersonaBO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.SkillModRewardType;
import com.soapboxrace.jaxb.http.Accolades;
import com.soapboxrace.jaxb.http.EnumRewardType;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
import com.soapboxrace.jaxb.http.TeamEscapeArbitrationPacket;

@Stateless
public class RewardTeamEscapeBO extends RewardBO {

	@EJB
	private PersonaDAO personaDao;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private LegitRaceBO legitRaceBO;
	
	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private EventDataDAO eventDataDAO;

	public Accolades getTeamEscapeAccolades(Long activePersonaId, TeamEscapeArbitrationPacket teamEscapeArbitrationPacket,
			EventSessionEntity eventSessionEntity, int isDropableMode) {
		int finishReason = teamEscapeArbitrationPacket.getFinishReason();
		if (!legitRaceBO.isLegit(activePersonaId, teamEscapeArbitrationPacket, eventSessionEntity, false) || finishReason != 22) {
			return new Accolades();
		}

		float bustedCount = teamEscapeArbitrationPacket.getBustedCount();
		bustedCount++;

		EventEntity eventEntity = eventSessionEntity.getEvent();
		PersonaEntity personaEntity = personaDao.findById(activePersonaId);
		RewardVO rewardVO = getRewardVO(personaEntity);
		OwnedCarTrans defaultCar = personaBO.getDefaultCar(activePersonaId);
		
		setBaseReward(personaEntity, eventEntity, teamEscapeArbitrationPacket, rewardVO);
		setRankReward(eventEntity, teamEscapeArbitrationPacket, rewardVO);
		
		Float bustedBaseRep = rewardVO.getBaseRep() / bustedCount;
		Float bustedBaseCash = rewardVO.getBaseCash() / bustedCount;
//		System.out.println("before: " + bustedBaseCash);
		
//		int finishedRacers = eventDataDAO.countTEFinishers(eventSessionEntity.getId()).intValue();
//		if (finishedRacers < 2) { // At least 2 racers should finish to get the full rewards
//			System.out.println("TRIGGERED");
//			bustedBaseRep = (float) (bustedBaseRep * 0.6);
//			bustedBaseCash = (float) (bustedBaseCash * 0.6);
//			System.out.println("after: " + bustedBaseCash);
//		}
		
		rewardVO.setBaseRep(bustedBaseRep.intValue());
		rewardVO.setBaseCash(bustedBaseCash.intValue());

		setPerfectStartReward(eventEntity, teamEscapeArbitrationPacket.getPerfectStart(), rewardVO);
		setClassBonusReward(eventEntity, defaultCar.getCustomCar().getCarClassHash(), rewardVO);
		setPursuitParamReward(teamEscapeArbitrationPacket.getCopsDeployed(), EnumRewardType.COP_CARS_DEPLOYED, rewardVO);
		setPursuitParamReward(teamEscapeArbitrationPacket.getCopsDisabled(), EnumRewardType.COP_CARS_DISABLED, rewardVO);
		setPursuitParamReward(teamEscapeArbitrationPacket.getCopsRammed(), EnumRewardType.COP_CARS_RAMMED, rewardVO);
		setPursuitParamReward(teamEscapeArbitrationPacket.getCostToState(), EnumRewardType.COST_TO_STATE, rewardVO);
		setPursuitParamReward(teamEscapeArbitrationPacket.getEventDurationInMilliseconds(), EnumRewardType.PURSUIT_LENGTH, rewardVO);
		setPursuitParamReward(teamEscapeArbitrationPacket.getInfractions(), EnumRewardType.INFRACTIONS, rewardVO);
		setPursuitParamReward(teamEscapeArbitrationPacket.getRoadBlocksDodged(), EnumRewardType.ROADBLOCKS_DODGED, rewardVO);
		setPursuitParamReward(teamEscapeArbitrationPacket.getSpikeStripsDodged(), EnumRewardType.SPIKE_STRIPS_DODGED, rewardVO);

		setTopSpeedReward(eventEntity, teamEscapeArbitrationPacket.getTopSpeed(), rewardVO);
		setSkillMultiplierReward(personaEntity, rewardVO, SkillModRewardType.BOUNTY_HUNTER);
		setMultiplierReward(eventEntity, rewardVO);

		applyRaceReward(rewardVO.getRep(), rewardVO.getCash(), personaEntity);
//		System.out.println("### TE test: player " + personaEntity.getName() + "has requested TE accolades, finishers: " + finishedRacers + ", cash amount: " + rewardVO.getCash());
		return getAccolades(personaEntity, teamEscapeArbitrationPacket, rewardVO, isDropableMode, false, false);
	}
}
