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
	name="PersonaTopTreasureHunt.top",
	query="SELECT "
			+ "p.id id,"
			+ "p.name persona_name, "
			+ "p.iconindex iconindex, "
			+ "streak AS treasureHunt "
		+ "FROM "
			+ "persona p "
		+ "WHERE "
			+ "p.isHidden = false "
		+ "INNER JOIN treasure_hunt e ON p.id=e.personaid "
		+ "ORDER BY treasureHunt DESC",
	resultClass = PersonaTopTreasureHunt.class
	)
public class PersonaTopTreasureHunt {

	@Id
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "persona_name", nullable = true)
	private String name;
	@Column(name = "iconindex", nullable = true)
	private int iconIndex;
	@Column(name = "treasureHunt", nullable = true)
	private int treasurehunt;
	
	public String getName() {
		return name;
	}
	public int getIcon() {
		return iconIndex;
	}
	public int getTreasureHunt() {
		return treasurehunt;
	}
}
