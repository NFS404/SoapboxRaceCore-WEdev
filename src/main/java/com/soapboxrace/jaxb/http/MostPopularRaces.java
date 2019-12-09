package com.soapboxrace.jaxb.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Хранит список профилей с количеством дней, которые он подряд собирал кристалики
 * @author Vadimka
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TopRaces", propOrder = {
	"circuit",
	"sprint",
	"drag",
	"pursuit",
	"team_escape"
})
public class MostPopularRaces {
	@XmlElement(name = "Circuit")
	protected Race circuit;
	@XmlElement(name = "Sprint")
	protected Race sprint;
	@XmlElement(name = "Drag")
	protected Race drag;
	@XmlElement(name = "Pursuit")
	protected Race pursuit;
	@XmlElement(name = "TeamEscape")
	protected Race team_escape;
	
	/**
	 * Установить круговой заезд
	 * @param race - заезд
	 */
	public void setCircuit(Race race) {
		circuit = race;
	}
	/**
	 * Получить круговой заезд
	 * @return MostPopularRaces.Race
	 */
	public Race getCircuit() {
		return circuit;
	}
	/**
	 * Установить заезд типа Спринт
	 * @param race - заезд
	 */
	public void setSprint(Race race) {
		sprint = race;
	}
	/**
	 * Получить заезд типа спринт
	 * @return MostPopularRaces.Race
	 */
	public Race getSprint() {
		return sprint;
	}
	/**
	 * Установить заезд типа драг
	 * @param race - заезд
	 */
	public void setDrag(Race race) {
		drag = race;
	}
	/**
	 * Получить заезд типа драг
	 * @return MostPopularRaces.Race
	 */
	public Race getDrag() {
		return drag;
	}
	/**
	 * Установить заезд типа погоня
	 * @param race - заезд
	 */
	public void setPursuit(Race race) {
		pursuit = race;
	}
	/**
	 * Получить заезд типа погоня
	 * @return MostPopularRaces.Race
	 */
	public Race getPursuit() {
		return pursuit;
	}
	/**
	 * Установить заезд типа Спасение командой
	 * @param race - заезд
	 */
	public void setTeamEscape(Race race) {
		team_escape = race;
	}
	/**
	 * Получить заезд типа спасение командой
	 * @return MostPopularRaces.Race
	 */
	public Race getTeamEscape() {
		return team_escape;
	}
	/**
	 * Трасса
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "race", propOrder = {
		"name",
		"carClass",
		"played"
	})
	public static class Race {
		@XmlElement(name = "Name")
		protected String name;
		@XmlElement(name = "CarClass")
		protected String carClass;
		@XmlElement(name = "Played")
		protected int played;
		public Race(String name, int carClass, int countPlayed) {
			this.name = name;
			this.played = countPlayed;
			switch(carClass) {
			case 872416321:
				this.carClass = "E";
				break;
			case 415909161:
				this.carClass = "D";
				break;
			case 1866825865:
				this.carClass = "C";
				break;
			case -406473455:
				this.carClass = "B";
				break;
			case -405837480:
				this.carClass = "A";
				break;
			case -2142411446:
				this.carClass = "S";
				break;
			case 607077938 :
				this.carClass = "OR";
				break;
			case 1337 :
				this.carClass = "NPC";
				break;
			default:
				this.carClass = "";
				break;
			}
		}
		public Race(String name, String clas) {
			this.name = name;
			this.carClass = clas;
		}
		/**
		 * Получить имя трассы
		 */
		public String getName() {
			return name;
		}
		/**
		 * Получить Клас трассы
		 */
		public String getCarClass() {
			return carClass;
		}
		/**
		 * Сколько гонок на этой трассе
		 */
		public int getPlayed() {
			return played;
		}
	}
}
