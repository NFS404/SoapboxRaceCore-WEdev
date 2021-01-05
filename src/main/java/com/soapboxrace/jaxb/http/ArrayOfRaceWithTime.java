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
 * @author Vadimka, Hypercycle
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfRaceWithTime", propOrder = {
	"races",
	"countRaces",
	"raceName",
	"raceType",
	"raceClass",
	"eventClass",
	"trainingId",
	"baseEvent"
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
	
	@XmlElement(name = "EventClass")
	private String eventClass;
	
	@XmlElement(name = "TrainingId")
	private int trainingId;
	
	@XmlElement(name = "BaseEvent")
	private int baseEvent;

	@XmlElement(name = "Race")
	private List<Race> races;
	
	public ArrayOfRaceWithTime() {
		races = new ArrayList<Race>();
		raceName = "";
		raceType = 0;
		raceClass = 0;
		eventClass = "all";
		trainingId = 0;
		baseEvent = 0;
	}
	
	public void setCount(BigInteger count) {
		countRaces = count;
	}
	public void set(
			String raceName,
			int raceType,
			int raceClass,
			String eventClass,
			int trainingId,
			int baseEvent
			) {
		this.raceName = raceName;
		this.raceType = raceType;
		this.raceClass = raceClass;
		this.eventClass = eventClass;
		this.trainingId = trainingId;
		this.baseEvent = baseEvent;
	}
	public Race add(
			int id,
			String playerName,
			int playerIconId,
			String carName,
			int carClassHash,
			int raceTime,
			int raceAltTime,
			int srvTime,
			int airTime,
			int lapTime,
			float maxSpeed,
			float avgSpeed,
			boolean perfectStart,
			boolean isSingle,
			String date,
			boolean isCarVersionVaild,
			int pNos,
			int pSlingshot,
			int pOneMoreLap,
			int pReady,
			int pMagnet,
			int pShield,
			int pEvade,
			int pJuggernaut,
			int pRunFlatTires,
			int pCooldown,
			int pTeamEvade,
			int pTeamSlingshot,
			int rating,
			boolean hasBodykit,
			boolean hasSpoiler,
			boolean hasLowkit
		) {
		 Race race = new Race(id, playerName, playerIconId, carName, carClassHash, raceTime, raceAltTime, srvTime, airTime, lapTime, maxSpeed, avgSpeed, perfectStart,
	                isSingle, date, isCarVersionVaild, pNos, pSlingshot, pOneMoreLap, pReady, pMagnet, pShield, pEvade, pJuggernaut, pRunFlatTires, 
	                pCooldown, pTeamEvade, pTeamSlingshot, rating, hasBodykit, hasSpoiler, hasLowkit);
	     races.add(race);
	     return race;
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "Race", propOrder = {
		"id",
		"playerName",
		"playerIconId",
		"carName",
		"carClass",
		"raceTime",
		"raceAltTime",
		"srvTime",
		"airTime",
		"lapTime",
		"maxSpeed",
		"avgSpeed",
		"perfectStart",
		"isSingle",
		"date",
		"isCarVersionVaild",
		"pNos",
		"pSlingshot",
		"pOneMoreLap",
		"pReady",
		"pMagnet",
		"pShield",
		"pEvade",
		"pJuggernaut",
		"pRunFlatTires",
		"pCooldown",
		"pTeamEvade",
		"pTeamSlingshot",
		"rating",
		"perfArray",
		"skillArray",
		"hasBodykit",
		"hasSpoiler",
		"hasLowkit",
	})
	public static class Race {
		@XmlElement(name = "id")
		private int id;
		@XmlElement(name = "PlayerName")
		private String playerName;
		@XmlElement(name = "PlayerIconId")
		private int playerIconId;
		@XmlElement(name = "CarName")
		private String carName;
		@XmlElement(name = "CarClass")
		private String carClass;
		@XmlElement(name = "RaceTime")
		private int raceTime;
		@XmlElement(name = "RaceAltTime")
		private int raceAltTime;
		@XmlElement(name = "SrvTime")
		private int srvTime;
		@XmlElement(name = "AirTime")
		private int airTime;
		@XmlElement(name = "LapTime")
		private int lapTime;
		@XmlElement(name = "MaxSpeed")
		private double maxSpeed;
		@XmlElement(name = "AvgSpeed")
		private double avgSpeed;
		@XmlElement(name = "PerfectStart")
		private boolean perfectStart;
		@XmlElement(name = "IsSingle")
		private boolean isSingle;
		@XmlElement(name = "Date")
		private String date;
		@XmlElement(name = "IsCarVersionVaild")
		private boolean isCarVersionVaild;
		@XmlElement(name = "PNos")
		private int pNos;
		@XmlElement(name = "PSlingshot")
		private int pSlingshot;
		@XmlElement(name = "POneMoreLap")
		private int pOneMoreLap;
		@XmlElement(name = "PReady")
		private int pReady;
		@XmlElement(name = "PMagnet")
		private int pMagnet;
		@XmlElement(name = "PShield")
		private int pShield;
		@XmlElement(name = "PEvade")
		private int pEvade;
		@XmlElement(name = "PJuggernaut")
		private int pJuggernaut;
		@XmlElement(name = "PRunFlatTires")
		private int pRunFlatTires;
		@XmlElement(name = "PCooldown")
		private int pCooldown;
		@XmlElement(name = "PTeamEvade")
		private int pTeamEvade;
		@XmlElement(name = "PTeamSlingshot")
		private int pTeamSlingshot;
		@XmlElement(name = "CarRating")
		private int rating;
		@XmlElement(name = "PerfArray")
        private List<PerfItem> perfArray = new ArrayList<PerfItem>();
        public void addPerfArray(String name, String icon) {
            perfArray.add(new PerfItem(name,icon));
        }
        public List<PerfItem> getPerfArray() {
            return perfArray;
        }
        
        @XmlElement(name = "SkillArray")
        private List<SkillItem> skillArray = new ArrayList<SkillItem>();
        public void addSkillArray(String name, String icon) {
            skillArray.add(new SkillItem(name,icon));
        }
        public List<SkillItem> getSkillArray() {
            return skillArray;
        }
        
		@XmlElement(name = "HasBodykit")
		private boolean hasBodykit;
		@XmlElement(name = "HasSpoiler")
		private boolean hasSpoiler;
		@XmlElement(name = "HasLowkit")
		private boolean hasLowkit;
		
		protected Race(
				int id,
				String playerName,
				int playerIconId,
				String carName,
				int carClassHash,
				int raceTime,
				int raceAltTime,
				int srvTime,
				int airTime,
				int lapTime,
				float maxSpeed,
				float avgSpeed,
				boolean perfectStart,
				boolean isSingle,
				String date,
				boolean isCarVersionVaild,
				int pNos,
				int pSlingshot,
				int pOneMoreLap,
				int pReady,
				int pMagnet,
				int pShield,
				int pEvade,
				int pJuggernaut,
				int pRunFlatTires,
				int pCooldown,
				int pTeamEvade,
				int pTeamSlingshot,
				int rating,
				boolean hasBodykit,
				boolean hasSpoiler,
				boolean hasLowkit
			) {
			this.id = id;
			this.playerName = playerName;
			this.playerIconId = playerIconId;
			this.carName = carName;
			this.raceTime = raceTime;
			this.raceAltTime = raceAltTime;
			this.srvTime = srvTime;
			this.airTime = airTime;
			this.lapTime = lapTime;
			this.maxSpeed = maxSpeed;	
			this.avgSpeed = avgSpeed;	
			this.perfectStart = perfectStart;
			this.isSingle = isSingle;
			this.date = date;
			this.isCarVersionVaild = isCarVersionVaild;
			this.pNos = pNos;
			this.pSlingshot = pSlingshot;
			this.pOneMoreLap = pOneMoreLap;
			this.pReady = pReady;
			this.pMagnet = pMagnet;
			this.pShield = pShield;
			this.pEvade = pEvade;
			this.pJuggernaut = pJuggernaut;
			this.pRunFlatTires = pRunFlatTires;
			this.pCooldown = pCooldown;
			this.pTeamEvade = pTeamEvade;
			this.pTeamSlingshot = pTeamSlingshot;
			this.rating = rating;
			this.hasBodykit = hasBodykit;
			this.hasSpoiler = hasSpoiler;
			this.hasLowkit = hasLowkit;
			
			switch(carClassHash) {
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
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "PerfItem", propOrder = {
        "name",
        "icon",
    })
    public static class PerfItem {
        @XmlElement(name = "name")
        private String name;
        @XmlElement(name = "icon")
        private String icon;
        
        protected PerfItem(
                String name,
                String icon
            ) {
            this.name = name;
            this.icon = icon; 
        }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SkillItem", propOrder = {
        "name",
        "icon",
    })
    public static class SkillItem {
        @XmlElement(name = "name")
        private String name;
        @XmlElement(name = "icon")
        private String icon;
        
        protected SkillItem(
                String name,
                String icon
            ) {
            this.name = name;
            this.icon = icon;
        }
    }
}
