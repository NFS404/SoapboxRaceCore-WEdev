package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.AchievementRankDAO;
import com.soapboxrace.core.dao.AchievementStateDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ProductDAO;
import com.soapboxrace.core.dao.PromoCodeDAO;
import com.soapboxrace.core.dao.SalesDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.ProductEntity;
import com.soapboxrace.core.jpa.SalesEntity;

@Stateless
public class SalesBO {

	@EJB
	private PromoCodeDAO promoCodeDao;

	@EJB
	private UserDAO userDao;
	
	@EJB
	private PersonaDAO personaDao;
	
	@EJB
	private AchievementStateDAO achievementStateDao;
	
	@EJB
	private AchievementRankDAO achievementRankDao;
	
	@EJB
	private ProductDAO productDAO;
	
	@EJB
	private SalesDAO salesDAO;
	
	@EJB
	private ParameterBO parameterBO;
	
	// Car sales generator
	public String saleGen(String saleTime, String saleName, String saleCar1, String saleCar2, String saleCar3, String saleCar4) {
		SalesEntity salesEntity = new SalesEntity();
		salesEntity.setSaleTime(saleTime);
		salesEntity.setId((long) 1); // JPA Entity must have IDs, but it's not necessary there

		if (saleCar1 == null || saleCar1.contentEquals("")) {
			return "ERROR: no cars selected";
		}
		if (saleCar2.contentEquals("")) {
			saleCar2 = null;
		}
		if (saleCar3.contentEquals("")) {
			saleCar3 = null;
		}
		if (saleCar4.contentEquals("")) {
			saleCar4 = null;
		}
		ProductEntity saleCar1Prod = productDAO.findByLongDesc(saleCar1);
		saleCar1Prod.setPrice(saleCar1Prod.getPrice() / 2); // 50% off, equal to resell prices
		saleCar1Prod.setSecondaryIcon("On_Sale");
		saleCar1Prod.setLevel(3);
		productDAO.update(saleCar1Prod);
		salesEntity.setCar1(saleCar1);
		salesEntity.setCar2(null);
		salesEntity.setCar3(null);
		salesEntity.setCar4(null);
		
		if (saleCar2 != null) {
			System.out.println("saleCar2");
			ProductEntity saleCar2Prod = productDAO.findByLongDesc(saleCar2);
			saleCar2Prod.setPrice(saleCar2Prod.getPrice() / 2); // 50% off, equal to resell prices
			saleCar2Prod.setSecondaryIcon("On_Sale");
			saleCar2Prod.setLevel(3);
			productDAO.update(saleCar2Prod);
			salesEntity.setCar2(saleCar2);
		}
		
        if (saleCar3 != null) {
        	System.out.println("saleCar3");
        	ProductEntity saleCar3Prod = productDAO.findByLongDesc(saleCar3);
      		saleCar3Prod.setPrice(saleCar3Prod.getPrice() / 2); // 50% off, equal to resell prices
       		saleCar3Prod.setSecondaryIcon("On_Sale");
       		saleCar3Prod.setLevel(3);
			productDAO.update(saleCar3Prod);
			salesEntity.setCar3(saleCar3);
		}
        
        if (saleCar4 != null) {
        	System.out.println("saleCar4");
        	ProductEntity saleCar4Prod = productDAO.findByLongDesc(saleCar4);
       		saleCar4Prod.setPrice(saleCar4Prod.getPrice() / 2); // 50% off, equal to resell prices
       		saleCar4Prod.setSecondaryIcon("On_Sale");
       		saleCar4Prod.setLevel(3);
			productDAO.update(saleCar4Prod);
			salesEntity.setCar4(saleCar4);
		}
        salesDAO.update(salesEntity);
		return "Sale is successfully generated";
	}
	
	@Schedule(dayOfWeek = "MON", persistent = false)
	public void saleEnd() {
		//
	}
}
