package com.soapboxrace.core.jpa;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

/**
 * Заезды с наилудшим временем
 * @author Vadimka
 */
@Entity
@Table(name = "EVENT_DATA")
@NamedNativeQueries({
	@NamedNativeQuery(
			name="BestTimeRaceEntity.best",
//			query = "SELECT " + 
//					"	d.id id, " + 
//					"	cc.name car_name, " + 
//					"	p.name user_name, " + 
//					"	p.iconindex user_iconid, " + 
//					"	cc.carclasshash car_class, " + 
//					"	d.eventdurationinmilliseconds race_time, " + 
//					"	d.perfectstart perfect_start, " + 
//					"	d.topspeed max_speed, " + 
//					"	d.hacksdetected hacks_level " + 
//					"FROM " + 
//					"	event_data d, " + 
//					"	carslot cs, " + 
//					"	customcar cc, " + 
//					"	persona p " + 
//					"WHERE " + 
//					"	d.carid=cs.id AND d.carid = cc.id AND p.id = cs.personaid AND d.eventid = :eventid " + 
//					"ORDER BY d.eventdurationinmilliseconds",

			query = "SELECT " + 
					"	d.id id, " + 
					"	cc.name car_name, " + 
					"	p.name user_name, " + 
					"	p.iconindex user_iconid, " + 
					"	cc.carclasshash car_class, " + 
					"	d.eventdurationinmilliseconds race_time, " + 
					"	d.perfectstart perfect_start, " + 
					"	d.topspeed max_speed, " + 
					"	d.hacksdetected hacks_level, " + 
					"	d.numberofcollisions collision, " + 
					"	e.ended ended " +
					"FROM " + 
					"	event_data d, " + 
					"	customcar cc, " + 
					"	persona p, " + 
					"	event_session e " +
					"WHERE " + 
					"	d.carid = cc.id AND p.id = d.personaid AND d.eventsessionid = e.id AND d.eventid = :eventid " + 
					"ORDER BY d.eventdurationinmilliseconds",
			resultClass = BestTimeRaceEntity.class
		),
	@NamedNativeQuery(
			name="BestTimeRaceEntity.findByPersonaName",
			query = "SELECT " + 
					"	d.id id, " + 
					"	cc.name car_name, " + 
					"	p.name user_name, " + 
					"	p.iconindex user_iconid, " + 
					"	cc.carclasshash car_class, " + 
					"	d.eventdurationinmilliseconds race_time, " + 
					"	d.perfectstart perfect_start, " + 
					"	d.topspeed max_speed, " + 
					"	d.hacksdetected hacks_level, " + 
					"	d.numberofcollisions collision, " + 
					"	e.ended ended " +
					"FROM " + 
					"	event_data d, " + 
					"	customcar cc, " + 
					"	persona p, " + 
					"	event_session e " +
					"WHERE " + 
					"	d.carid = cc.id AND p.id = d.personaid AND d.eventsessionid = e.id AND d.eventid = :eventid AND p.name LIKE :pname " + 
					"ORDER BY d.eventdurationinmilliseconds",
			resultClass = BestTimeRaceEntity.class
		)
})
public class BestTimeRaceEntity {
	@Id
	@Column(name="id")
	private int id;
//	@Column(name="user_name")
	private String user_name;
//	@Column(name="user_iconid")
	private int user_iconid;
//	@Column(name="car_name")
	private String car_name;
//	@Column(name="car_class")
	private int car_class;
//	@Column(name="race_time")
	private BigInteger race_time;
//	@Column(name="perfect_start")
	private int perfect_start;
//	@Column(name="max_speed")
	private double max_speed;
//	@Column(name="hacks_level")
	private int hacks_level;
//	@Column(name="collision")
	private int collision;
	private long ended;
	/**
	 * Получить имя профиля
	 */
	public String getUserName() {
		return user_name;
	}
	/**
	 * Получить иконку профиля
	 */
	public int getUserIconId() {
		return user_iconid;
	}
	/**
	 * Получить название машины, на которой ехал профиль
	 */
	public String getCarName() {
		return car_name;
	}
	/**
	 * Получить хэш класса машины
	 */
	public int getCarClass() {
		return car_class;
	}
	/**
	 * Получить время, за которое была проехана трасса в милисекундах
	 */
	public BigInteger getRaceTime() {
		return race_time;
	}
	/**
	 * Был ли быстрый старт
	 */
	public boolean isPerfectStart() {
		if (perfect_start != 0) return true;
		else return false;
	}
	/**
	 * Получить максимальную скорость
	 */
	public double getMaxSpeed() {
		return max_speed;
	}
	/**
	 * Получить уровень возможного читерства
	 */
	public int getHacksLevel() {
		return hacks_level;
	}
	/**
	 * Получить столкновения
	 */
	public int getCollisions() {
		return collision;
	}
	/**
	 * Дата окончания заезда
	 */
	public long getdate() {
		return ended;
	}
}
