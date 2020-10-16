package com.soapboxrace.core.bo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.StringListConverter;
import com.soapboxrace.core.dao.BanDAO;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.EventCarInfoDAO;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ProductDAO;
import com.soapboxrace.core.dao.RecordsDAO;
import com.soapboxrace.core.dao.ReportDAO;
import com.soapboxrace.core.dao.TreasureHuntDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.BanEntity;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CarNameEntity;
import com.soapboxrace.core.jpa.ClassCountEntity;
import com.soapboxrace.core.jpa.EventCarInfoEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventPowerupsEntity;
import com.soapboxrace.core.jpa.MostPopularEventEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PersonaTopRaceEntity;
import com.soapboxrace.core.jpa.PersonaTopTreasureHunt;
import com.soapboxrace.core.jpa.ProductEntity;
import com.soapboxrace.core.jpa.ProfileIconEntity;
import com.soapboxrace.core.jpa.RecordsEntity;
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
	 * Объект запросов в базу по тематике машин
	 */
	@EJB
	private CustomCarDAO carDAO;
	/**
	 * Объект запросов в базу по тематике заездов
	 */
	@EJB
	private EventDAO eventDAO;
	/**
	 * Объект запросов в базу по тематике заездов
	 */
	@EJB
	private EventDataDAO eventDataDAO;
	/**
	 * Объект запросов в базу по тематике профиля
	 */
	@EJB
	private PersonaDAO personaDAO;
	/**
	 * Объект запросов в базу по тематике банов профиля
	 */
	@EJB
	private BanDAO banDAO;
	/**
	 * Объект запросов в базу по тематике репорта
	 */
	@EJB
	private ReportDAO reportDAO;
	/**
	 * Объект запросов в базу по тематике поиска кристаликов
	 */
	@EJB
	private TreasureHuntDAO diamondDAO;
	/**
	 * Объект запросов в базу по тематике аккаунта
	 */
	@EJB
	private UserDAO userDAO;
	/**
	 * Объект запросов в базу для метода определения версии машины
	 */
	@EJB
	private EventResultBO eventResultBO;
	/**
	 * Объект запросов в базу для получения списков рекордов
	 */
	@EJB
	private RecordsDAO recordsDAO;
	/**
	 * Объект запросов в базу для получения списков рекордов
	 */
	@EJB
	private CarClassesDAO carClassesDAO;
	/**
	 * Объект запросов в базу для получения информации об автомобилях в заезде
	 */
	@EJB
	private EventCarInfoDAO eventCarInfoDAO;
	/**
	 * Объект запросов в базу для получения информации об автомобилях в заезде
	 */
	@EJB
	private StringListConverter stringListConverter;
	/**
	 * Объект запросов в базу для получения предметов магазина
	 */
	@EJB
	private ProductDAO productDAO;
	/**
	 * Объект запросов в базу для выполнения действии по части событии
	 */
	@EJB
	private EventBO eventBO;
	
	
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
	 * Получает топ профилей по количеству трасс, в которых участвовал профиль
	 * @param onPage - Из скольки профилей будет состоять топ
	 * @return TopProfileRaces
	 */
	public TopProfileRaces getTopRacers(int onPage) {
		if (onPage > 300) onPage = 300;
		TopProfileRaces arrayOfProfileData = new TopProfileRaces();
		List<PersonaTopRaceEntity> profiles = personaDAO.getTopRacers(onPage);
		for (PersonaTopRaceEntity profile : profiles) {
			arrayOfProfileData.add(new ProfileDataRaces(profile.getName(), profile.getIcon(), profile.getRacesCount()));
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
				arrayOfRaces.setCircuit(new Race(race.getName(), race.getClassHash(), race.getFinishCount()));
				break;
			// Спринт
			case 9:
				arrayOfRaces.setSprint(new Race(race.getName(), race.getClassHash(), race.getFinishCount()));
				break;
			// Драг
			case 19:
				arrayOfRaces.setDrag(new Race(race.getName(), race.getClassHash(), race.getFinishCount()));
				break;
			// Погоня
			case 12:
				arrayOfRaces.setPursuit(new Race(race.getName(), race.getClassHash(), race.getFinishCount()));
				break;
			// Спасение командой
			case 24:
				arrayOfRaces.setTeamEscape(new Race(race.getName(), race.getClassHash(), race.getFinishCount()));
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
			CarClassesEntity carClassesEntity = carClassesDAO.findByStoreName(car.getName());
			names.add(car.getCount(), carClassesEntity.getModel());
		}
		return names;
	}
	/**
	 * Получить список лучших заездов по времени
	 * @param eventId - Номер трассы
	 * @param powerups - Бонусы true/false
	 * @param carclass - Класс машин
	 * @param page - Номер страницы
	 * @param onPage - Сколько позиций на странице
	 */
	public ArrayOfRaceWithTime getTopTimeRace(int eventid, boolean powerups, String carclass, int page, int onPage) {
		if (onPage > 300) onPage = 300;
		ArrayOfRaceWithTime list = new ArrayOfRaceWithTime();
		int carclasshash = 0;
		switch (carclass) {
		case "E":
			carclasshash = 872416321;
			break;
		case "D":
			carclasshash = 415909161;
			break;
		case "C":
			carclasshash = 1866825865;
			break;
		case "B":
			carclasshash = -406473455;
			break;
		case "A":
			carclasshash = -405837480;
			break;
		case "S":
			carclasshash = -2142411446;
			break;
		case "ALL":
			break;
		}

		if (carclasshash == 0) {
			list.setCount(recordsDAO.countRecordsAll(eventid, powerups));
		}
		else {
			list.setCount(recordsDAO.countRecords(eventid, powerups, carclasshash));
		}
		EventEntity event = eventDAO.findById(eventid);
		if (event != null)
			list.set(
					event.getName(),
					event.getEventModeId(),
					carclasshash,
					eventResultBO.getCarClassLetter(event.getCarClassHash())
				);
		// FIXME Do something with that mess
		if (carclasshash == 0) {
//			ArrayList<Long> userIdList = new ArrayList<>();
			for (RecordsEntity race : recordsDAO.statsEventAll(event, powerups, page, onPage)) {
//				Long userId = race.getUser().getId();
//				if (!userIdList.contains(userId)) {
//				 	userIdList.add(userId);
//				}
//				else {
//					continue;
//				}
				final boolean isCarVersionVaild;
				CarClassesEntity carClassesEntity = carClassesDAO.findByHash(race.getCarPhysicsHash());
				EventPowerupsEntity eventPowerupsEntity = race.getEventPowerups();
				EventCarInfoEntity eventCarInfoEntity = eventCarInfoDAO.findByEventData(race.getEventDataId());
				if (eventCarInfoEntity == null) {
					eventCarInfoEntity = eventBO.createDummyEventCarInfo();
				}
				int serverCarVersionValue = carClassesEntity.getCarVersion();
				if (serverCarVersionValue == race.getCarVersion()) {
					isCarVersionVaild = true;
				}
				else {
					isCarVersionVaild = false;
				}
				
				ArrayOfRaceWithTime.Race raceXml = list.add(
						race.getId().intValue(), 
						race.getPlayerName(),
						race.getPersona().getIconIndex(),
						carClassesEntity.getFullName(),
						race.getCarClassHash(), 
						race.getTimeMS().intValue(), 
						race.getTimeMSAlt().intValue(), 
						race.getTimeMSSrv().intValue(),
						race.getAirTimeMS().intValue(), 
						race.getBestLapTimeMS().intValue(), 
						(float)(race.getTopSpeed() * 3.6), 
						race.getPerfectStart(),
						race.getIsSingle(),
						race.getDate().toString(),
						isCarVersionVaild,
						eventPowerupsEntity.getNosShot(),
						eventPowerupsEntity.getSlingshot(),
						eventPowerupsEntity.getOneMoreLap(),
						eventPowerupsEntity.getReady(),
						eventPowerupsEntity.getTrafficMagnet(),
						eventPowerupsEntity.getShield(),
						eventPowerupsEntity.getEmergencyEvade(),
						eventPowerupsEntity.getJuggernaut(),
						eventPowerupsEntity.getRunFlatTires(),
						eventPowerupsEntity.getInstantCooldown(),
						eventPowerupsEntity.getTeamEmergencyEvade(),
						eventPowerupsEntity.getTeamSlingshot(),
						eventCarInfoEntity.getRating(),
						eventCarInfoEntity.getBodykit(),
						eventCarInfoEntity.getSpoiler(),
						eventCarInfoEntity.getLowkit()
					);

				String perfStrArray = eventCarInfoEntity.getPerfParts();
				if (!perfStrArray.contentEquals("")) {
					Integer[] perfArray = stringListConverter.StrToIntList(perfStrArray.split(","));
					for (int hash : perfArray) {
						ProductEntity perf = productDAO.findByHash(hash);
						raceXml.addPerfArray(
		                        perf.getProductTitle(), // name
		                        perf.getIcon() // icon
		                        );
					}
				}
				String skillStrArray = eventCarInfoEntity.getSkillParts();
				if (!skillStrArray.contentEquals("")) {
					Integer[] skillArray = stringListConverter.StrToIntList(skillStrArray.split(","));
					for (int hash : skillArray) {
						ProductEntity perf = productDAO.findByHash(hash);
						raceXml.addSkillArray(
		                        perf.getProductTitle(), // name
		                        perf.getIcon() // icon
		                        );
					}
				}
			}
		}
		else {
			for (RecordsEntity race : recordsDAO.statsEventClass(event, powerups, carclasshash, page, onPage)) {
				final boolean isCarVersionVaild;
				CarClassesEntity carClassesEntity = carClassesDAO.findByHash(race.getCarPhysicsHash());
				EventPowerupsEntity eventPowerupsEntity = race.getEventPowerups();
				EventCarInfoEntity eventCarInfoEntity = eventCarInfoDAO.findByEventData(race.getEventDataId());
				if (eventCarInfoEntity == null) {
					eventCarInfoEntity = eventBO.createDummyEventCarInfo();
				}
				int serverCarVersionValue = carClassesEntity.getCarVersion();
				if (serverCarVersionValue == race.getCarVersion()) {
					isCarVersionVaild = true;
				}
				else {
					isCarVersionVaild = false;
				}
				ArrayOfRaceWithTime.Race raceXml = list.add(
						race.getId().intValue(), 
						race.getPlayerName(),
						race.getPersona().getIconIndex(),
						carClassesEntity.getFullName(),
						race.getCarClassHash(), 
						race.getTimeMS().intValue(), 
						race.getTimeMSAlt().intValue(), 
						race.getTimeMSSrv().intValue(),
						race.getAirTimeMS().intValue(), 
						race.getBestLapTimeMS().intValue(), 
						(float)(race.getTopSpeed() * 3.6), 
						race.getPerfectStart(),
						race.getIsSingle(),
						race.getDate().toString(),
						isCarVersionVaild,
						eventPowerupsEntity.getNosShot(),
						eventPowerupsEntity.getSlingshot(),
						eventPowerupsEntity.getOneMoreLap(),
						eventPowerupsEntity.getReady(),
						eventPowerupsEntity.getTrafficMagnet(),
						eventPowerupsEntity.getShield(),
						eventPowerupsEntity.getEmergencyEvade(),
						eventPowerupsEntity.getJuggernaut(),
						eventPowerupsEntity.getRunFlatTires(),
						eventPowerupsEntity.getInstantCooldown(),
						eventPowerupsEntity.getTeamEmergencyEvade(),
						eventPowerupsEntity.getTeamSlingshot(),
						eventCarInfoEntity.getRating(),
						eventCarInfoEntity.getBodykit(),
						eventCarInfoEntity.getSpoiler(),
						eventCarInfoEntity.getLowkit()
					);

				String perfStrArray = eventCarInfoEntity.getPerfParts();
				if (!perfStrArray.contentEquals("")) {
					Integer[] perfArray = stringListConverter.StrToIntList(perfStrArray.split(","));
					for (int hash : perfArray) {
						ProductEntity perf = productDAO.findByHash(hash);
						raceXml.addPerfArray(
		                        perf.getProductTitle(), // name
		                        perf.getIcon() // icon
		                        );
					}
				}
				String skillStrArray = eventCarInfoEntity.getSkillParts();
				if (!skillStrArray.contentEquals("")) {
					Integer[] skillArray = stringListConverter.StrToIntList(skillStrArray.split(","));
					for (int hash : skillArray) {
						ProductEntity perf = productDAO.findByHash(hash);
						raceXml.addSkillArray(
		                        perf.getProductTitle(), // name
		                        perf.getIcon() // icon
		                        );
					}
				}
			}
		}
		return list;
	}
	/**
	 * Получить список лучших заездов по времени
	 * Фильтрация по имени профиля
	 * @param eventId - Номер трассы
	 * @param powerups - Бонусы true/false
	 * @param page - Номер страницы
	 * @param personaName - Имя водителя
	 * @param onPage - Сколько позиций на странице
	 */
	public ArrayOfRaceWithTime getTopTimeRaceByPersona(int eventid, boolean powerups, String personaName, int page, int onPage) {
		if (onPage > 300) onPage = 300;
		ArrayOfRaceWithTime list = new ArrayOfRaceWithTime();
		PersonaEntity personaEntity = personaDAO.findByName(personaName);
		if (personaEntity == null) {
			return list;
		}
		UserEntity userEntity = personaEntity.getUser();
		Long userId = userEntity.getId();
		
		list.setCount(recordsDAO.countRecordsPersona(eventid, powerups, userId));
		EventEntity event = eventDAO.findById(eventid);
		if (event != null)
			list.set(
					event.getName(),
					event.getEventModeId(),
					0,
					"all"
				);
		for (RecordsEntity race : recordsDAO.statsEventPersona(event, powerups, userEntity)) {
			final boolean isCarVersionVaild;
			CarClassesEntity carClassesEntity = carClassesDAO.findByHash(race.getCarPhysicsHash());
			EventPowerupsEntity eventPowerupsEntity = race.getEventPowerups();
			EventCarInfoEntity eventCarInfoEntity = eventCarInfoDAO.findByEventData(race.getEventDataId());
			if (eventCarInfoEntity == null) {
				eventCarInfoEntity = eventBO.createDummyEventCarInfo();
			}
			int serverCarVersionValue = carClassesEntity.getCarVersion();
			if (serverCarVersionValue == race.getCarVersion()) {
				isCarVersionVaild = true;
			}
			else {
				isCarVersionVaild = false;
			}
			ArrayOfRaceWithTime.Race raceXml = list.add(
					race.getId().intValue(), 
					race.getPlayerName(),
					race.getPersona().getIconIndex(),
					carClassesEntity.getFullName(),
					race.getCarClassHash(), 
					race.getTimeMS().intValue(), 
					race.getTimeMSAlt().intValue(), 
					race.getTimeMSSrv().intValue(),
					race.getAirTimeMS().intValue(), 
					race.getBestLapTimeMS().intValue(), 
					(float)(race.getTopSpeed() * 3.6), 
					race.getPerfectStart(),
					race.getIsSingle(),
					race.getDate().toString(),
					isCarVersionVaild,
					eventPowerupsEntity.getNosShot(),
					eventPowerupsEntity.getSlingshot(),
					eventPowerupsEntity.getOneMoreLap(),
					eventPowerupsEntity.getReady(),
					eventPowerupsEntity.getTrafficMagnet(),
					eventPowerupsEntity.getShield(),
					eventPowerupsEntity.getEmergencyEvade(),
					eventPowerupsEntity.getJuggernaut(),
					eventPowerupsEntity.getRunFlatTires(),
					eventPowerupsEntity.getInstantCooldown(),
					eventPowerupsEntity.getTeamEmergencyEvade(),
					eventPowerupsEntity.getTeamSlingshot(),
					eventCarInfoEntity.getRating(),
					eventCarInfoEntity.getBodykit(),
					eventCarInfoEntity.getSpoiler(),
					eventCarInfoEntity.getLowkit()
				);

			String perfStrArray = eventCarInfoEntity.getPerfParts();
			if (!perfStrArray.contentEquals("")) {
				Integer[] perfArray = stringListConverter.StrToIntList(perfStrArray.split(","));
				for (int hash : perfArray) {
					ProductEntity perf = productDAO.findByHash(hash);
					raceXml.addPerfArray(
	                        perf.getProductTitle(), // name
	                        perf.getIcon() // icon
	                        );
				}
			}
			String skillStrArray = eventCarInfoEntity.getSkillParts();
			if (!skillStrArray.contentEquals("")) {
				Integer[] skillArray = stringListConverter.StrToIntList(skillStrArray.split(","));
				for (int hash : skillArray) {
					ProductEntity perf = productDAO.findByHash(hash);
					raceXml.addSkillArray(
	                        perf.getProductTitle(), // name
	                        perf.getIcon() // icon
	                        );
				}
			}
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
			races = eventDAO.findAllStats();
		else
			races = eventDAO.findAllEnabledStats();
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
		
		// =============== Diamonds ===============
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
				user.getCreated(),
				persona.getCreated(), 
				persona.getMotto(), 
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
		String premiumType = user.getPremiumType();
		if (premiumType == null) {
			premiumType = "none";
		}
		String premiumTypeFull = "";
	    switch (premiumType) {
	        case "powerup":
    			premiumTypeFull = "Power-Up";
    			break;
	    	case "base":
	    		premiumTypeFull = "Base";
	    		break;
	    	case "plus":
	    		premiumTypeFull = "Plus";
	    		break;
	    	case "full":
	    		premiumTypeFull = "Full";
	    		break;
	    	case "unlim":
	    		premiumTypeFull = "Unlimited";
	    		break;
	    	case "none":
	    		premiumTypeFull = "None";
	    		break;
	    }
		
		PersonaPremiumInfo premiumInfo;
		
		if (email.equalsIgnoreCase(user.getEmail()) && password.equals(user.getPassword()))
			premiumInfo = new PersonaPremiumInfo(
					username, 
					user.getExtraMoney(),
					user.isPremium(),
					premiumTypeFull, 
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
