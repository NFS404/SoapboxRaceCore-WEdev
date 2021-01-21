package com.soapboxrace.jaxb.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Выдаёт токен или код ошибки от API
 * @author Hypercycle
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "APITokenResultsList", propOrder = {
	"apiTokenResultData"
})
public class APITokenResult {

	@XmlElement(name = "apiTokenResult", nillable = true)
	protected List<APITokenEntity> apiTokenResultData;
	
	public APITokenResult() {
		apiTokenResultData = new ArrayList<APITokenEntity>();
	}
	/**
	 * Добавить профиль в список
	 * @param profile - объект профиля
	 */
	public void add(APITokenEntity apiTokenInfo) {
		apiTokenResultData.add(apiTokenInfo);
	}
	/**
	 * Профиль с именем, иконкой и количеством очков
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "apiTokenResultEntity", propOrder = {
		"token",
		"errorCode",
	})
	public static class APITokenEntity {
		@XmlElement(name = "Token")
		protected String token;
		@XmlElement(name = "ErrorCode")
		protected int errorCode;
		public APITokenEntity(String token, int errorCode) {
			this.token = token;
			this.errorCode = errorCode;
		}
		/**
		 * Получить имя профиля
		 */
		public String getToken() {
			return token;
		}
		/**
		 * Получить ID иконки
		 */
		public int getErrorCode() {
			return errorCode;
		}
	}
}
