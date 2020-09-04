package com.soapboxrace.jaxb.http;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Информация о профиле
 * @author Vadimka
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Persona", propOrder = {
	"NAME",
	"LEVEL",
	"CASH",
	"ICONID",
	"DATE_REGISTRED",
	"DATE_REGISTRED_USER",
	"MOTTO",
	"SCORE",
	"CURRENT_CAR",
	"DIAMOND",
	"BAN",
	"BAN_REASON",
	"BAN_ENDS",
	"CHEATDETECTED",
	"PLAYERREPORTS"
})
public class PersonaInfo {
	
	@XmlElement(name = "Name")
	private String NAME;
	
	@XmlElement(name = "Level")
	private int LEVEL;
	
	@XmlElement(name = "Cash")
	private Long CASH;
	
	@XmlElement(name = "IconID")
	private int ICONID;
	
	@XmlElement(name = "DateRegistred")
	private String DATE_REGISTRED;
	
	@XmlElement(name = "DateRegistredUser")
	private String DATE_REGISTRED_USER;
	
	@XmlElement(name = "Motto")
	private String MOTTO;
	
	@XmlElement(name = "Score")
	private int SCORE;
	
	@XmlElement(name = "CurrentCar")
	private String CURRENT_CAR;
	
	@XmlElement(name = "Diamond")
	private int DIAMOND;
	
	@XmlElement(name = "Ban")
	private boolean BAN;
	
	@XmlElement(name = "BanReason")
	private String BAN_REASON;
	
	@XmlElement(name = "BanEnds")
	private String BAN_ENDS;
	
	@XmlElement(name = "CheatDetected")
	private BigInteger CHEATDETECTED;
	
	@XmlElement(name = "PlayerReports")
	private BigInteger PLAYERREPORTS;
	
	/**
	 * Информация о профиле
	 * @param name - Имя
	 * @param level - Уровень
	 * @param cash - Деньги
	 * @param iconid - Иконка профиля
	 * @param dateregistred - Дата регистрации
	 * @param dateregistredu - Дата регистрации аккаунта
	 * @param motto - Подпись игрока
	 * @param score - Количество очков профиля
	 * @param premium - Премиум активен
	 * @param premiumtype - Тип премиума
	 * @param premiumends - Премиус кончается в
	 * @param curcar - Текущая машина
	 * @param diamod - Цепочка сбора алмазов
	 * @param ban - Бан активен
	 * @param banreason - Причина бана
	 * @param banEnds - Бан заканчивается в
	 * @param cheatdetected - Количество гонок с подозрением в читерстве
	 * @param playerreports - Количество репортов на пользователя
	 */
	public PersonaInfo(
			String name, 
			int level, 
			double cash, 
			int iconid, 
			LocalDateTime dateregistred, 
			LocalDateTime dateregistredu, 
			String motto,
			int score, 
			String curcar, 
			int diamod, 
			boolean ban, 
			String banreason, 
			LocalDateTime banEnds, 
			BigInteger cheatdetected, 
			BigInteger playerreports
		) {
		NAME = name;
		LEVEL = level;
		CASH = new Long((int) cash);
		ICONID = iconid;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		DATE_REGISTRED = dateregistred.format(formatter);
		DATE_REGISTRED_USER = dateregistredu.format(formatter);
		MOTTO = motto;
		SCORE = score;
		CURRENT_CAR = curcar;
		DIAMOND = diamod;
		BAN = ban;
		BAN_REASON = banreason;
		if (banEnds != null)
			BAN_ENDS = banEnds.format(formatter);
		else
			BAN_ENDS = null;
		CHEATDETECTED = cheatdetected;
		PLAYERREPORTS = playerreports;
	}
}
