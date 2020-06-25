package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.PromoCodeBO;
import com.soapboxrace.core.bo.SalesBO;

@Path("/PromoCode")
public class PromoCode {

	@EJB
	private PromoCodeBO bo;

	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private SalesBO salesBO;

	@POST
	@Path("/createPromoCode")
	@Produces(MediaType.TEXT_HTML)
	public String createPromoCode(@FormParam("promoCodeToken") String promoCodeToken, @FormParam("codeType") String codeType) {
		if (parameterBO.getStrParam("PROMO_CODE_TOKEN").equals(promoCodeToken) && codeType != null) {
			return bo.createPromoCode(codeType);
		}
		if (parameterBO.getStrParam("PROMO_CODE_TOKEN").equals(promoCodeToken) && codeType == null) {
			return "ERROR: Code type is not selected, please try again";
		}
		return "ERROR: invalid token (not a staff? quit right now, hacker)";
	}

	@POST
	@Path("/usePromoCode")
	@Produces(MediaType.TEXT_HTML)
	public String usePromoCode(@FormParam("promoCode") String promoCode, @FormParam("email") String email, @FormParam("password") String password, @FormParam("nickname") String nickname, @FormParam("token") String token) {
		if (token != null && (promoCode.isEmpty() || nickname.isEmpty())) {
			return "ERROR: empty nickname or code";
		}
		if (token == null && (promoCode.isEmpty() || email.isEmpty() || password.isEmpty() || nickname.isEmpty())) {
			return "ERROR: empty email, password, nickname or code";
		}
		if (token != null && !parameterBO.getStrParam("PROMO_CODE_TOKEN").equals(token)) {
			return "ERROR: invalid token (not a staff? quit right now, hacker)";
		}
		return bo.usePromoCode(promoCode, email, password, nickname, token);
	}
	
	@POST
	@Path("/useDebug")
	@Produces(MediaType.TEXT_HTML)
	public String useDebug(@FormParam("adminToken") String adminToken, @FormParam("premiumType") String premiumType, @FormParam("extraMoney") String extraMoney, @FormParam("nickname") String nickname, @FormParam("timeYear") String timeYear, @FormParam("timeMonth") String timeMonth, @FormParam("timeDay") String timeDay) {
		if (adminToken != null && !parameterBO.getStrParam("ADMIN_TOKEN").equals(adminToken)) {
			return "ERROR: invalid token";
		}
		return bo.useDebug(premiumType, extraMoney, nickname, timeYear, timeMonth, timeDay);
	}
	
	@POST
	@Path("/saleGen")
	@Produces(MediaType.TEXT_HTML)
	public String saleGen(@FormParam("saleManagerToken") String saleManagerToken, @FormParam("saleTime") String saleTime, @FormParam("saleCar1") String saleCar1, @FormParam("saleCar2") String saleCar2, @FormParam("saleCar3") String saleCar3, @FormParam("saleCar4") String saleCar4, @FormParam("saleName") String saleName) {
		if (saleManagerToken != null && !parameterBO.getStrParam("SALE_MANAGERTOKEN").equals(saleManagerToken)) {
			return "ERROR: invalid token";
		}
		return salesBO.saleGen(saleTime, saleName, saleCar1, saleCar2, saleCar3, saleCar4);
	}
}
