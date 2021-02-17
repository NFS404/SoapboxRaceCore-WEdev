package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "VISUALPART")
@NamedQueries({ @NamedQuery(name = "VisualPartEntity.deleteByCustomCar", //
		query = "DELETE FROM VisualPartEntity obj WHERE obj.customCar = :customCar"), //
                @NamedQuery(name = "VisualPartEntity.deleteHiddenItems", //
        query = "DELETE FROM VisualPartEntity obj WHERE obj.customCar = :customCar AND (obj.slotHash = 1398141924 OR obj.slotHash = -1909727572 "
        		+ "OR obj.slotHash = -1649588878 OR obj.slotHash = 1469703691 OR obj.slotHash = -1515028479 OR obj.slotHash = 1403499614)"), //
                @NamedQuery(name = "VisualPartEntity.findCopLightsPart", //
        query = "SELECT obj FROM VisualPartEntity obj WHERE obj.customCar = :customCar AND obj.partHash = 1672122654"), //
                @NamedQuery(name = "VisualPartEntity.isBodykitInstalled", //
        query = "SELECT obj FROM VisualPartEntity obj WHERE obj.customCar = :customCar AND obj.slotHash = -966088147"), //
                @NamedQuery(name = "VisualPartEntity.isSpoilerInstalled", //
        query = "SELECT obj FROM VisualPartEntity obj WHERE obj.customCar = :customCar AND obj.slotHash = -918850563"), //
                @NamedQuery(name = "VisualPartEntity.isLowkitInstalled", //
        query = "SELECT obj FROM VisualPartEntity obj WHERE obj.customCar = :customCar AND obj.slotHash = -2126743923"), //
})
public class VisualPartEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private int partHash;
	private int slotHash;

	@ManyToOne
	@JoinColumn(name = "customCarId", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_VISUALPART_CUSTOMCAR"))
	private CustomCarEntity customCar;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getPartHash() {
		return partHash;
	}

	public void setPartHash(int partHash) {
		this.partHash = partHash;
	}

	public int getSlotHash() {
		return slotHash;
	}

	public void setSlotHash(int slotHash) {
		this.slotHash = slotHash;
	}

	public CustomCarEntity getCustomCar() {
		return customCar;
	}

	public void setCustomCar(CustomCarEntity customCar) {
		this.customCar = customCar;
	}

}
