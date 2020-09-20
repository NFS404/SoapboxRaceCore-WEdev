package com.soapboxrace.core.bo.util;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.lang3.StringUtils;

import com.soapboxrace.core.dao.PersonaDAO;

@Stateless
public class PersonaListConverter {
	
	@EJB
	private PersonaDAO personaDAO;

	public String interceptorPersonaList(List<Long> personas) {
		if (personas.isEmpty()) {
			return null;
		}
		String list = StringUtils.join(personas, ',');
		return list;
	}
	
	public String interceptorPersonaChatList(List<Long> personas) {
		if (personas.isEmpty()) {
			return "N/A";
		}
		StringBuilder sbArray = new StringBuilder();
		for (Long personaId : personas)
		{
			sbArray.append(personaDAO.findById(personaId).getName());
			sbArray.append(" ");
		}
		return sbArray.toString();
	}
	
	public Long[] StrToLongList(String[] strList) {
		Long[] longList = new Long[strList.length];
		for (int i = 0; i < strList.length; i++)
			longList[i] = Long.parseLong(strList[i]);
		return longList;
	}

}
