package com.soapboxrace.core.api.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * RW User-Agent parser
 * @author Vadimka
 */
public abstract class RWACUserAgent {
	private static final String UPDATE_INFO_URL = "https://raw.githubusercontent.com/VadimkaG/NFSWlauncher/master/version.txt";
	/**
	 * Получить данные о RacingWorld из User-Agent
	 * @param userAgent Проверяемый User-Agent
	 * @return Получает массив. Если проверка не успешна - вернет null
	 * Элементы полученного массива:
	 *  0 - Эмулируемый браузер
	 *  1 - Название системы
	 *  2 - Архитектура системы
	 *  3 - Название лаунчера
	 *  4 - Версия лаунчера
	 */
	public static String[] ParseUserAgent(String userAgent) {
		int index = userAgent.indexOf("(");
		if (index == -1) return null;
		
		String browser = userAgent.substring(0,index-1);
		
		int indexEnd = userAgent.indexOf(")",index);
		if (indexEnd == -1) return null;
		String OSdataRAW = userAgent.substring(index+1,indexEnd);
		String[] OSdata = OSdataRAW.split(" ");
		if (OSdata.length < 2) return null;
		
		String OSarch = OSdata[OSdata.length-1];
		String OSname = OSdataRAW.substring(0,OSdataRAW.length()-OSarch.length()-1);
		
		if (userAgent.length() < indexEnd+2) return null;
		String launcherDataRAW = userAgent.substring(indexEnd+2);
		String[] launcherData = launcherDataRAW.split(" ");
		if (launcherData.length < 2) return null;
		
		String launcherVersion = launcherData[launcherData.length-1];
		String launcherName = launcherDataRAW.substring(0,launcherDataRAW.length()-launcherVersion.length()-1);
		
		if (launcherVersion.contains(" ")) return null;
		if (!launcherVersion.contains(".")) return null;
		
		String[] out = {
				browser,
				OSname,
				OSarch,
				launcherName,
				launcherVersion
		};
		return out;
	}
	/**
	 * Получить последнюю версию лаунчера RacingWorld
	 * Функция использует загрузки данных по HTTP
	 * Постоянное использывание функции может замедлить работу программы
	 * Желательно запускать проверку в отдельном потоке
	 */
	public static String getRWVersionLatest() {
		try {
			URL url = new URL(UPDATE_INFO_URL);
			HttpURLConnection conn =(HttpURLConnection) url.openConnection();
			conn.connect();
			if ((conn.getResponseCode()) != 200) {
				//System.out.println("Припопытке загрузки информации о версии был получен код "+responseCode);
				return null;
			}
			
			// Загружаем данные
			InputStream is = conn.getInputStream();
			StringBuilder response;
			try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
				response = new StringBuilder();
				String line;
				while ((line = rd.readLine()) != null) {
					response.append(line);
				}
			}
			is.close();
			String str = response.toString();
			if (str == null) {
				//System.out.println("Припопытке загрузки информации о версии данных не получено");
				return null;
			}
			String[] info = str.split(";");
			if (info.length >= 2) {
				return info[0];
			} else {
				//System.out.println("Не верная информации об версии");
				return null;
			}
			// ================
			
			
		} catch (Exception e) {
			//System.out.println("Ошибка загрузки информации о версии");
			return null;
		}
	}
	/**
	 * Может ли быть такая версия у лаунчера?
	 * @param launcherVersion Версия лаунчера Racing World
	 * @return
	 */
	public static boolean validateVersion(String launcherVersion) {
		int len = launcherVersion.length();
		if (len > 20) return false;
		String[] strs = launcherVersion.split("\\.");
		if (strs.length < 2) return false;
		return true;
	}
	/**
	 * Релизная ли это версия
	 * @param launcherVersion Версия лаунчера Racing World
	 * @return
	 */
	public static boolean isRWVersionRelease(String launcherVersion) {
		launcherVersion = cutExe(launcherVersion);
		String[] strs = launcherVersion.split("\\.");
		if (strs.length < 1) return false;
		for (String str : strs) {
			if (!str.chars().allMatch(Character::isDigit)) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Вырезать _exe из версии лаунчера
	 * @param launcherVersion Версия лаунчера Racing World
	 * @return
	 */
	public static String cutExe(String launcherVersion) {
		int len = launcherVersion.length();
		if (len > 4 && launcherVersion.substring(len-4, len).equalsIgnoreCase("_exe"))
			return launcherVersion.substring(0,len-4);
		else return launcherVersion;
	}
	/**
	 * Проверка является ли лаунчер exe версией
	 * @param launcherVersion Версия лаунчера Racing World
	 */
	public static boolean isRWVersionExe(String launcherVersion) {
		int len = launcherVersion.length();
		if (len < 4) return false;
		if (launcherVersion.substring(len-4, len).equalsIgnoreCase("_exe"))
			return true;
		else return false;
	}
	/**
	 * Проверить последняя ли версия RacingWorld
	 * @param launcherVersion Версия лаунчера Racing World
	 */
	public static boolean isRWLatestVersion(String launcherVersion) {
		if (cutExe(getRWVersionLatest()).equalsIgnoreCase(cutExe(launcherVersion))) return true;
		else return true;
	}
	/**
	 * Больше ли заданная версия версия лаунчера
	 * @param launcherVersion Версия лаунчера Racing World
	 * @param version Версия с которой нужно сравнить
	 * @return
	 */
	public static boolean biggerRWVersion(String launcherVersion, String version) {
		launcherVersion = cutExe(launcherVersion);
		if (launcherVersion.equalsIgnoreCase(version)) return false;
		String[] strsLauncher = launcherVersion.split("\\.");
		String[] strs = version.split("\\.");
		int count;
		if (strs.length > strsLauncher.length) count = strs.length;
		else count = strsLauncher.length;
		for(int i = 0; i < count; i++) {
			String il;
			if (i > strsLauncher.length-1) il = "0";
			else il = strsLauncher[i];
			String ii;
			if (i > strs.length-1) ii = "0";
			else ii = strs[i];
			if (il.equalsIgnoreCase(ii)) continue;
			boolean isLauncherDigit = il.chars().allMatch(Character::isDigit);
			boolean isDigit = ii.chars().allMatch(Character::isDigit);
			if (!isLauncherDigit && !isDigit) continue;
			if (!isLauncherDigit && isDigit) return false;
			if (isLauncherDigit && !isDigit) return true;
			if (Integer.valueOf(il) > Integer.valueOf(ii)) return true;
			else return false;
		}
		return false;
	}
	/**
	 * Меньше ли заданная версия версия лаунчера
	 * @param launcherVersion Версия лаунчера Racing World
	 * @param version Версия с которой нужно сравнить
	 * @return
	 */
	public static boolean lowerRWVersion(String launcherVersion, String version) {
		launcherVersion = cutExe(launcherVersion);
		if (launcherVersion.equalsIgnoreCase(version)) return false;
		String[] strsLauncher = launcherVersion.split("\\.");
		String[] strs = version.split("\\.");
		int count;
		if (strs.length > strsLauncher.length) count = strs.length;
		else count = strsLauncher.length;
		for(int i = 0; i < count; i++) {
			String il;
			if (i > strsLauncher.length-1) il = "0";
			else il = strsLauncher[i];
			String ii;
			if (i > strs.length-1) ii = "0";
			else ii = strs[i];
			if (il.equalsIgnoreCase(ii)) continue;
			boolean isLauncherDigit = il.chars().allMatch(Character::isDigit);
			boolean isDigit = ii.chars().allMatch(Character::isDigit);
			if (!isLauncherDigit && !isDigit) continue;
			if (!isLauncherDigit && isDigit) return true;
			if (isLauncherDigit && !isDigit) return false;
			if (Integer.valueOf(il) < Integer.valueOf(ii)) return true;
			else return false;
		}
		return false;
	}
	/**
	 * Равняются ли сравниваемые версии
	 * @param launcherVersion Версия лаунчера Racing World
	 * @param version Версия с которой нужно сравнить
	 * @return
	 */
	public static boolean equalsRWVersion(String launcherVersion, String version) {
		launcherVersion = cutExe(launcherVersion);
		if (launcherVersion.equalsIgnoreCase(version)) return true;
		else return false;
	}

}
