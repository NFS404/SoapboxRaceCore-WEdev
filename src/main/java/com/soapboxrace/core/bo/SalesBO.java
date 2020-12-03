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
	public String saleGen(String saleName, String saleCar1, String saleCar2, String saleCar3, String saleCar4) {
		SalesEntity salesEntity = new SalesEntity();
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
		ProductEntity saleCar1Prod = productDAO.findByCarName(saleCar1);
		float prod1Price = saleCar1Prod.getPrice();
		
		salesEntity.setCar1(saleCar1); // Take the original car's price and level-requirement
		salesEntity.setCar1Cost(prod1Price);
		salesEntity.setCar1Lvl(saleCar1Prod.getLevel());
		saleCar1Prod.setPrice(prod1Price / 2); // 50% off, equal to resell prices
		saleCar1Prod.setSecondaryIcon("On_Sale");
		saleCar1Prod.setLevel(3);
		productDAO.update(saleCar1Prod);
		
		salesEntity.setCar2(null);
		salesEntity.setCar3(null);
		salesEntity.setCar4(null);
		
		if (saleCar2 != null) {
			// System.out.println("saleCar2");
			ProductEntity saleCar2Prod = productDAO.findByCarName(saleCar2);
			float prod2Price = saleCar2Prod.getPrice();
			salesEntity.setCar2(saleCar2); // Take the original car's price and level-requirement
			salesEntity.setCar2Cost(prod2Price);
			salesEntity.setCar2Lvl(saleCar2Prod.getLevel());
			saleCar2Prod.setPrice(prod2Price / 2); // 50% off, equal to resell prices
			saleCar2Prod.setSecondaryIcon("On_Sale");
			saleCar2Prod.setLevel(3);
			productDAO.update(saleCar2Prod);
		}
		
        if (saleCar3 != null) {
        	// System.out.println("saleCar3");
        	ProductEntity saleCar3Prod = productDAO.findByCarName(saleCar3);
        	float prod3Price = saleCar3Prod.getPrice();
			salesEntity.setCar3(saleCar3); // Take the original car's price and level-requirement
			salesEntity.setCar3Cost(prod3Price);
			salesEntity.setCar3Lvl(saleCar3Prod.getLevel());
      		saleCar3Prod.setPrice(prod3Price / 2); // 50% off, equal to resell prices
       		saleCar3Prod.setSecondaryIcon("On_Sale");
       		saleCar3Prod.setLevel(3);
			productDAO.update(saleCar3Prod);
		}
        
        if (saleCar4 != null) {
        	// System.out.println("saleCar4");
        	ProductEntity saleCar4Prod = productDAO.findByCarName(saleCar4);
        	float prod4Price = saleCar4Prod.getPrice();
			salesEntity.setCar4(saleCar4); // Take the original car's price and level-requirement
			salesEntity.setCar4Cost(prod4Price);
			salesEntity.setCar4Lvl(saleCar4Prod.getLevel());
       		saleCar4Prod.setPrice(prod4Price / 2); // 50% off, equal to resell prices
       		saleCar4Prod.setSecondaryIcon("On_Sale");
       		saleCar4Prod.setLevel(3);
			productDAO.update(saleCar4Prod);
		}
        salesDAO.update(salesEntity);
		return "Sale is successfully generated";
	}
	
	@Schedule(dayOfWeek = "TUE", persistent = false)
	public String saleEnd() {
		SalesEntity salesEntity = salesDAO.findById((long) 1); // Currently only 1 sales event at the time is supported
		if (salesEntity == null) {
			return "";
		}
		ProductEntity saleCar1Prod = productDAO.findByCarName(salesEntity.getCar1());
		saleCar1Prod.setLevel(salesEntity.getCar1Lvl());
		saleCar1Prod.setPrice(salesEntity.getCar1Cost());
		saleCar1Prod.setSecondaryIcon("");
		productDAO.update(saleCar1Prod);
		
		if (salesEntity.getCar2() != null) {
			ProductEntity saleCar2Prod = productDAO.findByCarName(salesEntity.getCar2());
			saleCar2Prod.setLevel(salesEntity.getCar2Lvl());
			saleCar2Prod.setPrice(salesEntity.getCar2Cost());
			saleCar2Prod.setSecondaryIcon("");
			productDAO.update(saleCar2Prod);
		}
        if (salesEntity.getCar3() != null) {
        	ProductEntity saleCar3Prod = productDAO.findByCarName(salesEntity.getCar3());
			saleCar3Prod.setLevel(salesEntity.getCar3Lvl());
			saleCar3Prod.setPrice(salesEntity.getCar3Cost());
			saleCar3Prod.setSecondaryIcon("");
			productDAO.update(saleCar3Prod);
		}
        if (salesEntity.getCar4() != null) {
        	ProductEntity saleCar4Prod = productDAO.findByCarName(salesEntity.getCar4());
			saleCar4Prod.setLevel(salesEntity.getCar4Lvl());
			saleCar4Prod.setPrice(salesEntity.getCar4Cost());
			saleCar4Prod.setSecondaryIcon("");
			productDAO.update(saleCar4Prod);
		}
		
        salesDAO.delete(salesEntity); // Removes the sales entry
		return "";
	}
}
