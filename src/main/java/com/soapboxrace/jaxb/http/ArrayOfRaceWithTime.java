package com.soapboxrace.jaxb.http;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Список заездов
 * @author Vadimka
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfRaceWithTime", propOrder = {
	"races",
	"countRaces",
	"raceName",
	"raceType",
	"raceClass"
})
public class ArrayOfRaceWithTime {

	@XmlElement(name = "Count")
	private BigInteger countRaces;

	@XmlElement(name = "RaceName")
	private String raceName;

	@XmlElement(name = "RaceType")
	private int raceType;

	@XmlElement(name = "RaceClass")
	private int raceClass;

	@XmlElement(name = "Race")
	private List<Race> races;
	
	public ArrayOfRaceWithTime() {
		races = new ArrayList<Race>();
		raceName = "";
		raceType = 0;
		raceClass = 0;
	}
	
	public void setCount(BigInteger count) {
		countRaces = count;
	}
	public void set(
			String raceName,
			int raceType,
			int raceClass
			) {
		this.raceName = raceName;
		this.raceType = raceType;
		this.raceClass = raceClass;
	}
	public void add(
			String username,
			int iconid,
			String carname,
			int carclasshash,
			BigInteger racetime,
			double maxspeed,
			boolean perfectStart,
			boolean useHacks,
			int collisions,
			String date,
			boolean isCarVersionVaild
		) {
		races.add(new Race(username, iconid, carname, carclasshash, racetime, maxspeed, perfectStart, useHacks, collisions, date, isCarVersionVaild));
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "Race", propOrder = {
		"userName",
		"userIconId",
		"carName",
		"carClass",
		"raceTime",
		"maxSpeed",
		"perfectStart",
		"useHacks",
		"collisions",
		"date",
		"isCarVersionVaild"
	})
	public static class Race {
		@XmlElement(name = "UserName")
		private String userName;
		@XmlElement(name = "UserIconId")
		private int userIconId;
		@XmlElement(name = "CarName")
		private String carName;
		@XmlElement(name = "CarClass")
		private String carClass;
		@XmlElement(name = "RaceTime")
		private BigInteger raceTime;
		@XmlElement(name = "MaxSpeed")
		private double maxSpeed;
		@XmlElement(name = "PerfectStart")
		private boolean perfectStart;
		@XmlElement(name = "MaybeHacks")
		private boolean useHacks;
		@XmlElement(name = "Collisions")
		private int collisions;
		@XmlElement(name = "Date")
		private String date;
		@XmlElement(name = "IsCarVersionVaild")
		private boolean isCarVersionVaild;
		
		protected Race(
				String username,
				int iconid,
				String carname,
				int carclasshash,
				BigInteger racetime,
				double maxspeed,
				boolean perfectStart,
				boolean useHacks,
				int collisions,
				String date,
				boolean isCarVersionVaild
			) {
			userName = username;
			userIconId = iconid;
			carName = carname;
			raceTime = racetime;
			maxSpeed = maxspeed;
			this.perfectStart = perfectStart;
			this.useHacks = useHacks;
			this.date = date;
			this.collisions = collisions;
			this.isCarVersionVaild = isCarVersionVaild;
			switch(carclasshash) {
			case 872416321:
				carClass = "E";
				break;
			case 415909161:
				carClass = "D";
				break;
			case 1866825865:
				carClass = "C";
				break;
			case -406473455:
				carClass = "B";
				break;
			case -405837480:
				carClass = "A";
				break;
			case -2142411446:
				carClass = "S";
				break;
			case 607077938 :
				carClass = "OR";
				break;
			case 1337 :
				carClass = "NPC";
				break;
			default:
				carClass = "";
				break;
			}
		}
	}
	
}
