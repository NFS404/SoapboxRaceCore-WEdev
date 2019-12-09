package com.soapboxrace.core.bo;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.BanDAO;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ReportDAO;
import com.soapboxrace.core.dao.TreasureHuntDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.BanEntity;
import com.soapboxrace.core.jpa.BestTimeRaceEntity;
import com.soapboxrace.core.jpa.CarNameEntity;
import com.soapboxrace.core.jpa.ClassCountEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.MostPopularEventEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PersonaTopRaceEntity;
import com.soapboxrace.core.jpa.PersonaTopTreasureHunt;
import com.soapboxrace.core.jpa.ProfileIconEntity;
import com.soapboxrace.core.jpa.TreasureHuntEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.jaxb.http.ArrayOfCarClassHash;
import com.soapboxrace.jaxb.http.ArrayOfCarNameTop;
import com.soapboxrace.jaxb.http.ArrayOfEvents;
import com.soapboxrace.jaxb.http.ArrayOfPersonaBase;
import com.soapboxrace.jaxb.http.ArrayOfProfileIcon;
import com.soapboxrace.jaxb.http.ArrayOfRaceWithTime;
import com.soapboxrace.jaxb.http.ChangePassword;
import com.soapboxrace.jaxb.http.MostPopularRaces;
import com.soapboxrace.jaxb.http.MostPopularRaces.Race;
import com.soapboxrace.jaxb.http.PersonaBase;
import com.soapboxrace.jaxb.http.PersonaInfo;
import com.soapboxrace.jaxb.http.PersonaPremiumInfo;
import com.soapboxrace.jaxb.http.TopProfileRaces;
import com.soapboxrace.jaxb.http.TopProfileRaces.ProfileDataRaces;
import com.soapboxrace.jaxb.http.TopProfileScore;
import com.soapboxrace.jaxb.http.TopProfileScore.ProfileDataScore;
import com.soapboxrace.jaxb.http.TopProfileTreasureHunt;
import com.soapboxrace.jaxb.http.TopProfileTreasureHunt.ProfileDataTreasureHunt;

/**
 * Класс для запросов в базу для REST API
 * @author Vadimka
 */
@Stateless
public class RestApiBO {
	
	// ============== Утилиты выборки ================
	
	/**
	 * Объект заросов в базу по тематике машин
	 */
	@EJB
	private CustomCarDAO carDAO;
	/**
	 * Объект заросов в базу по тематике заездов
	 */
	@EJB
	private EventDAO eventDAO;
	/**
	 * Объект заросов в базу по тематике заездов
	 */
	@EJB
	private EventDataDAO eventDataDAO;
	/**
	 * Объект заросов в базу по тематике профиля
	 */
	@EJB
	private PersonaDAO personaDAO;
	/**
	 * Объект заросов в базу по тематике банов профиля
	 */
	@EJB
	private BanDAO banDAO;
	/**
	 * Объект заросов в базу по тематике репорта
	 */
	@EJB
	private ReportDAO reportDAO;
	/**
	 * Объект заросов в базу по тематике поиска кристаликов
	 */
	@EJB
	private TreasureHuntDAO diamondDAO;
	/**
	 * Объект заросов в базу по тематике аккаунта
	 */
	@EJB
	private UserDAO userDAO;
	
	// ================= Функции выборки ================
	
	/**
	 * Получает топ профилей по очкам
	 * @param onPage - Из скольки профилей будет состоять топ
	 * @return TopProfileScore
	 */
	public TopProfileScore getTopScores(int onPage) {
		if (onPage > 300) onPage = 300;
		TopProfileScore arrayOfProfileData = new TopProfileScore();
		List<PersonaEntity> listOfProfile = personaDAO.getTopScore(onPage);
		for (PersonaEntity personaEntity : listOfProfile) {
			arrayOfProfileData.add(new ProfileDataScore(
					personaEntity.getName(),
					personaEntity.getIconIndex(),
					personaEntity.getScore()
				));
		}
		return arrayOfProfileData;
	}
	/**
	 * Получает то профилей по количеству трас, в которых участвовал профиль
	 * @param onPage - Из скольки профилей будет состоять топ
	 * @return TopProfileRaces
	 */
	public TopProfileRaces getTopRacers(int onPage) {
		if (onPage > 300) onPage = 300;
		TopProfileRaces arrayOfProfileData = new TopProfileRaces();
		List<PersonaTopRaceEntity> profiles = personaDAO.getTopRacers(onPage);
		for (PersonaTopRaceEntity profile : profiles) {
			arrayOfProfileData.add(new ProfileDataRaces(profile.getName(), profile.getIcon(), profile.getEventData()));
		}
		return arrayOfProfileData;
	}
	/**
	 * Получает топ профилей по количеству дней, за которые собраны кристалики
	 * @param onPage - Из скольки профилей будет состоять топ
	 * @author Vadimka
	 */
	public TopProfileTreasureHunt TopProfileTreasureHunt(int onPage) {
		if (onPage > 300) onPage = 300;
		TopProfileTreasureHunt arrayOfProfileData = new TopProfileTreasureHunt();
		List<PersonaTopTreasureHunt> listOfProfile = personaDAO.getTopTreasureHunt(onPage);
		for (PersonaTopTreasureHunt persona : listOfProfile) {
			arrayOfProfileData.add(new ProfileDataTreasureHunt(
					persona.getName(),
					persona.getIcon(),
					persona.getTreasureHunt()
				));
		}
		return arrayOfProfileData;
	}
	/**
	 * Популярные трассы на режимах
	 * @param onPage - Из скольки профилей будет состоять топ
	 */
	public MostPopularRaces MostPopularRaces() {
		MostPopularRaces arrayOfRaces = new MostPopularRaces();
		List<MostPopularEventEntity> races = eventDataDAO.findTopRaces();
		for (MostPopularEventEntity race : races) {
			switch(race.getEventModeId()) {
			// Круг
			case 4:
				arrayOfRaces.setCircuit(new Race(race.getName(), race.getClassHash(), race.getCount()));
				break;
			// Спринт
			case 9:
				arrayOfRaces.setSprint(new Race(race.getName(), race.getClassHash(), race.getCount()));
				break;
			// Драг
			case 19:
				arrayOfRaces.setDrag(new Race(race.getName(), race.getClassHash(), race.getCount()));
				break;
			// Погоня
			case 12:
				arrayOfRaces.setPursuit(new Race(race.getName(), race.getClassHash(), race.getCount()));
				break;
			// Спасение командой
			case 24:
				arrayOfRaces.setTeamEscape(new Race(race.getName(), race.getClassHash(), race.getCount()));
				break;
			}
		}
		return arrayOfRaces;
	}
	/**
	 * Популярные классы машин
	 * @param onPage - Из скольки профилей будет состоять топ
	 */
	public ArrayOfCarClassHash getPopularCarClass(int onPage) {
		if (onPage > 300) onPage = 300;
		ArrayOfCarClassHash list = new ArrayOfCarClassHash();
		List<ClassCountEntity> classes = carDAO.getPopularCarsByClass(onPage);
		for (ClassCountEntity classCountEntity : classes) {
			list.add(classCountEntity.getClassHash(), classCountEntity.getCount());
		}
		return list;
	}
	/**
	 * Популярные иконки профиля
	 * @param onPage - сколько позиций вывести на странице
	 */
	public ArrayOfProfileIcon getPopularProfileIcons(int onPage) {
		if (onPage > 300) onPage = 300;
		ArrayOfProfileIcon icons = new ArrayOfProfileIcon();
		List<ProfileIconEntity> list = personaDAO.getPopularIcons(onPage);
		for (ProfileIconEntity profileIconEntity : list) {
			icons.add(profileIconEntity.getIconid(), profileIconEntity.getCount());
		}
		return icons;
	}
	/**
	 * Популярные имена машин
	 * @param onPage - сколько позиций вывести на странице
	 */
	public ArrayOfCarNameTop getTopCarName(int onPage) {
		if (onPage > 300) onPage = 300;
		ArrayOfCarNameTop names = new ArrayOfCarNameTop();
		List<CarNameEntity> list = carDAO.getTopCarName(onPage);
		for (CarNameEntity car : list) {
			names.add(car.getCount(), car.getName());
		}
		return names;
	}
	/**
	 * Получить список лучших заездов по времени
	 * @param page - Номер страницы
	 * @param onPage - Сколько позиций на странице
	 */
	public ArrayOfRaceWithTime getTopTimeRace(int eventid, int page, int onPage) {
		if (onPage > 300) onPage = 300;
		ArrayOfRaceWithTime list = new ArrayOfRaceWithTime();
		list.setCount(eventDataDAO.countBestTime(eventid));
		EventEntity event = eventDAO.findById(eventid);
		if (event != null)
			list.set(
					event.getName(),
					event.getEventModeId(),
					event.getCarClassHash()
				);
		boolean isHacks;
		SimpleDateFormat sf = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (BestTimeRaceEntity race : eventDataDAO.bestTime(eventid, page, onPage)) {
			switch (race.getHacksLevel()) {
				case 8:
				case 40:
					isHacks = true;
					break;
				default:
					isHacks = false;
					break;
			}
			list.add(
					race.getUserName(), 
					race.getUserIconId(), 
					race.getCarName(), 
					race.getCarClass(), 
					race.getRaceTime(), 
					race.getMaxSpeed(), 
					race.isPerfectStart(),
					isHacks,
					race.getCollisions(),
					sf.format(new Date(race.getdate()))
				);
		}
		return list;
	}
	/**
	 * Получить список лучших заездов по времени
	 * Фильтрация по имени профиля
	 * @param page - Номер страницы
	 * @param onPage - Сколько позиций на странице
	 */
	public ArrayOfRaceWithTime getTopTimeRaceByPersona(int eventid, String personaName, int page, int onPage) {
		if (onPage > 300) onPage = 300;
		ArrayOfRaceWithTime list = new ArrayOfRaceWithTime();
		list.setCount(eventDataDAO.countBestTimeByPersona(eventid, personaName));
		EventEntity event = eventDAO.findById(eventid);
		if (event != null)
			list.set(
					event.getName(),
					event.getEventModeId(),
					event.getCarClassHash()
				);
		for (BestTimeRaceEntity race : eventDataDAO.bestTimeByPersona(eventid, personaName, page, onPage)) {
			final boolean isHacks;
			switch (race.getHacksLevel()) {
				case 8:
				case 40:
					isHacks = true;
					break;
				default:
					isHacks = false;
					break;
			}
			list.add(
					race.getUserName(), 
					race.getUserIconId(), 
					race.getCarName(), 
					race.getCarClass(), 
					race.getRaceTime(), 
					race.getMaxSpeed(), 
					race.isPerfectStart(),
					isHacks,
					race.getCollisions(),
					""
				);
		}
		return list;
	}
	/**
	 * Список всех эвентов
	 */
	public ArrayOfEvents getRaces(boolean all) {
		ArrayOfEvents list = new ArrayOfEvents();
		list.setCount(eventDAO.countAll(all));
		List<EventEntity> races = null;
		if (all)
			races = eventDAO.findAll();
		else
			races = eventDAO.findAllEnabled();
		for (EventEntity race : races) {
			list.add(race.getId(), race.getName(), race.getCarClassHash(), String.valueOf(race.getEventModeId()));
		}
		return list;
	}
	/**
	 * Получить постраничный список профилей
	 * @param page - страница
	 * @param onPage - количество профилей на странице
	 * @return
	 */
	public ArrayOfPersonaBase getPersonas(int page, int onPage) {
		ArrayOfPersonaBase list = new ArrayOfPersonaBase();
		List<PersonaEntity> personas = personaDAO.getAllPaged((page-1) * onPage, onPage);
		for (PersonaEntity personaEntity : personas) {
			PersonaBase persona = new PersonaBase();
			persona.setBadges(null);
			persona.setIconIndex(personaEntity.getIconIndex());
			persona.setLevel(personaEntity.getLevel());
			persona.setMotto(personaEntity.getMotto());
			persona.setName(personaEntity.getName());
			persona.setPersonaId(personaEntity.getPersonaId());
			persona.setPresence(0);
			persona.setScore(personaEntity.getScore());
			persona.setUserId(personaEntity.getUser().getId());
			list.getPersonaBase().add(persona);
		}
		return list;
	}
	/**
	 * Получить информацию о профиле
	 * @param username - имя профиля
	 */
	public PersonaInfo getPersonaInfo(String username) {
		
		// =============== Persona ===============
		PersonaEntity persona = personaDAO.findByName(username);
		if (persona == null) return null;
		
		// =============== User ===============
		UserEntity user = persona.getUser();
		if (user == null) return null;
		
		// =============== Premium ===============
		LocalDate premiumEnds = user.getPremiumDate();
		if (premiumEnds != null) premiumEnds.plusDays(186);
		
		// =============== Ban ===============
		BanEntity ban = banDAO.findByUser(user);
		boolean isBan = false;
		String banReason = "";
		LocalDateTime banEnds = null;
		if (ban != null) {
			isBan = true;
			banReason = ban.getReason();
			banEnds = ban.getEndsAt();
		}
		
		// =============== Diamods ===============
		int countDiamondsDay = 0;
		TreasureHuntEntity diamond = diamondDAO.findById(persona.getPersonaId());
		if (diamond != null && !diamond.getIsStreakBroken()) {
			countDiamondsDay = diamond.getStreak();
		}
		
		// =============== PersonaInfo ===============
		PersonaInfo personaInfo = new PersonaInfo(
				persona.getName(), 
				persona.getLevel(), 
				persona.getCash(), 
				persona.getIconIndex(), 
				persona.getCreated(), 
				user.getCreated(), 
				persona.getScore(), 
				personaDAO.getCurrentCar(persona.getPersonaId()),
				countDiamondsDay,
				isBan, 
				banReason, 
				banEnds, 
				eventDataDAO.countHackRacesByPersona(persona.getPersonaId()),
				reportDAO.countReportsOnPersona(persona.getPersonaId())
			);
		
		return personaInfo;
	}
	/**
	 * Получить информацию о премиуме
	 * @param username
	 * @return
	 */
	public PersonaPremiumInfo getPersonaPremiumInfo(String username, String password, String email) {
		
		// =============== Persona ===============
		PersonaEntity persona = personaDAO.findByName(username);
		if (persona == null) return new PersonaPremiumInfo();
		
		// =============== User ===============
		UserEntity user = persona.getUser();
		if (user == null) return new PersonaPremiumInfo();
		
		// =============== Premium ===============
		LocalDate premiumEnds = user.getPremiumDate();
		if (premiumEnds != null) premiumEnds = premiumEnds.plusDays(186);
		
		PersonaPremiumInfo premiumInfo;
		
		if (email.equalsIgnoreCase(user.getEmail()) && password.equals(user.getPassword()))
			premiumInfo = new PersonaPremiumInfo(
					username, 
					user.getExtraMoney(),
					user.isPremium(),
					user.getPremiumType(), 
					premiumEnds
				);
		else
			premiumInfo = new PersonaPremiumInfo();
		return premiumInfo;
	}
	/**
	 * Смена пароля
	 * @param email - email аккаунта
	 * @param password - Пароль аккаунта
	 * @param newPassword - Новый пароль аккаунта
	 * @return
	 */
	public ChangePassword changePassword(String email, String password, String newPassword) {
		UserEntity user = userDAO.findByEmail(email);
		if (user == null || !user.getPassword().equalsIgnoreCase(password)) return new ChangePassword(false, "Incorrect login or password");
		user.setPassword(newPassword);
		userDAO.update(user);
		return new ChangePassword(true, "Password changed");
	}
}
