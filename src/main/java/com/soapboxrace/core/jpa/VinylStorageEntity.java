package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "VINYLSTORAGE")
@NamedQueries({ //
        @NamedQuery(name = "VinylStorageEntity.findByCode", //
		        query = "SELECT obj FROM VinylStorageEntity obj WHERE obj.code = :code "),//
        @NamedQuery(name = "VinylStorageEntity.deleteAllVinyls", //
                query = "DELETE FROM VinylStorageEntity obj WHERE obj.userId = :userId "),//
})
public class VinylStorageEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String code;
	private String vinylTrans;
	private String paintTrans;
	private int carHash;
	private int appliedCount;
	private Long userId;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getVinylTrans() {
		return vinylTrans;
	}

	public void setVinylTrans(String vinylTrans) {
		this.vinylTrans = vinylTrans;
	}
	
	public String getPaintTrans() {
		return paintTrans;
	}

	public void setPaintTrans(String paintTrans) {
		this.paintTrans = paintTrans;
	}
	
	public int getCarHash() {
		return carHash;
	}

	public void setCarHash(int carHash) {
		this.carHash = carHash;
	}
	
	public int getAppliedCount() {
		return appliedCount;
	}

	public void setAppliedCount(int appliedCount) {
		this.appliedCount = appliedCount;
	}
	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
