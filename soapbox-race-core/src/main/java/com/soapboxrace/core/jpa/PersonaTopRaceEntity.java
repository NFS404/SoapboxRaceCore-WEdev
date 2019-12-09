package com.soapboxrace.core.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

/**
 * Статистика: Сколько заедов проехал профиль
 * @author Vadimka
 */
@Entity
@Table(name = "PERSONA")
@NamedNativeQuery(
	name="top",
	query="SELECT * from (SELECT "
			+ "p.id id,"
			+ "p.name persona_name, "
			+ "p.iconindex iconindex, "
			+ "Count(*) AS countEventData "
		+ "FROM "
			+ "persona p "
		+ "INNER JOIN event_data e ON p.id=e.personaid "
		+ "GROUP BY p.id"
		+ ") s ORDER BY countEventData DESC",
	resultClass = PersonaTopRaceEntity.class
	)
public class PersonaTopRaceEntity {

	@Id
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "persona_name", nullable = true)
	private String name;
	@Column(name = "iconindex", nullable = true)
	private int iconIndex;
	@Column(name = "countEventData", nullable = true)
	private int countEventData;
	
	public String getName() {
		return name;
	}
	public int getIcon() {
		return iconIndex;
	}
	public int getEventData() {
		return countEventData;
	}
}
