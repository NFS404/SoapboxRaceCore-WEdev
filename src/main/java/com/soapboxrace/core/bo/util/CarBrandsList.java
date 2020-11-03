package com.soapboxrace.core.bo.util;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.AchievementBrandsDAO;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.jpa.AchievementBrandsEntity;

@Stateless
public class CarBrandsList {
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private AchievementBrandsDAO achievementBrandsDAO;

	public Integer[] getBrandInfo(int carHash, Long personaId) {
		AchievementBrandsEntity achievementBrandsEntity = achievementBrandsDAO.findById(personaId);
		String brandName = carClassesDAO.findByHash(carHash).getManufactor();
		int brandAchievementDefId = 0;
		int winsCount = 0;
		Integer[] brandInfo = new Integer[2];
		switch (brandName) {
		case "ALFA ROMEO":
			brandAchievementDefId = 28;
			winsCount = achievementBrandsEntity.getAlfaRomeoWins() + 1;
			achievementBrandsEntity.setAlfaRomeoWins(winsCount);
			break;
		case "ASTON MARTIN":
			brandAchievementDefId = 29;
			winsCount = achievementBrandsEntity.getAstonMartinWins() + 1;
			achievementBrandsEntity.setAstonMartinWins(winsCount);
			break;
		case "AUDI":
			brandAchievementDefId = 30;
			winsCount = achievementBrandsEntity.getAudiWins() + 1;
			achievementBrandsEntity.setAudiWins(winsCount);
			break;
		}
		achievementBrandsDAO.update(achievementBrandsEntity);
		brandInfo[0] = brandAchievementDefId;
		brandInfo[1] = winsCount;
		return brandInfo;
	}

}

    //	TRAFFIC(0), //
	//	ALFA_ROMEO(1), //
	//	ARIEL(2), //
	//	ASTON_MARTIN(3), //
	//	AUDI(4), //
	//	BAC(5), //
	//	BENTLEY(6), //
	//	BMW(7), //
	//	BUGATTI(8), //
	//	BUICK(9), //
	//	CADILLAC(10), //
	//	CATERHAM(11), //
	//	CHEVROLET(12), //
	//	CHRYSLER(13), //
	//	CITROEN(14), //
	//	DODGE(15), //
	//	FORD(16), //
	//	FORD_SHELBY(17), //
	//	FERRARI(18), //
	//	FIAT(19), //
	//	GTA(20), //
	//	GUMPERT(21), //
	//	HENNESSEY(22), //
	//	HOLDEN(23), //
	//	HONDA(24), //
	//	HUMMER(25), //
	//	INFINITI(26), //
	//	ITALDESIGN(27), //
	//	JAGUAR(28), //
	//	JEEP(29), //
	//	KOENIGSEGG(30), //
	//	LADA(31), //
	//	LAMBORGHINI(32), //
	//	LANCIA(33), //
	//	LEXUS(34), //
	//	LOTUS(35), //
	//	MARUSSIA(36), //
	//	MASERATI(37), //
	//	MAZDA(38), //
	//	MCLAREN(39), //
	//	MERCEDESBENZ(40), //
	//	MITSUBISHI(41), //
	//	NFS(42), //
	//	NISSAN(43), //
	//	OPEL(44), //
	//	PAGANI(45), //
	//	PEUGEOT(46), //
	//	PLYMOUTH(47), //
	//	PONTIAC(48), //
	//	PORSCHE(49), //
	//	RENAULT(50), //
	//	SCION(51), //
	//	SEAT(52), //
	//	SHELBY(53), //
	//	SMART(54), //
	//	SRT(55), //
	//	SUBARU(56), //
	//	TESLA(57), //
	//	TOYOTA(58), //
	//	VAUXHALL(59), //
	//	VOLKSWAGEN(60), //
	//	VOLVO(61); //