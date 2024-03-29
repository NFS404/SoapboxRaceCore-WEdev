package com.soapboxrace.core.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CAR_CLASSES")
public class CarClassesEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "store_name")
	private String storeName;
	@Column(name = "product_id")
	private String productId;
	@Column(name = "hash")
	private Integer hash;
	@Column(name = "full_name")
	private String fullName;
	@Column(name = "manufactor")
	private String manufactor;
	@Column(name = "model")
	private String model;
	@Column(name = "modelSmall")
	private String modelSmall;
	@Column(name = "ts_stock")
	private Integer tsStock;
	@Column(name = "ts_var1")
	private Integer tsVar1;
	@Column(name = "ts_var2")
	private Integer tsVar2;
	@Column(name = "ts_var3")
	private Integer tsVar3;
	@Column(name = "ac_stock")
	private Integer acStock;
	@Column(name = "ac_var1")
	private Integer acVar1;
	@Column(name = "ac_var2")
	private Integer acVar2;
	@Column(name = "ac_var3")
	private Integer acVar3;
	@Column(name = "ha_stock")
	private Integer haStock;
	@Column(name = "ha_var1")
	private Integer haVar1;
	@Column(name = "ha_var2")
	private Integer haVar2;
	@Column(name = "ha_var3")
	private Integer haVar3;
	@Column(name = "carversion")
	private Integer carVersion;
	@Column(name = "quickraceallowed")
	private boolean quickRaceAllowed;
	@Column(name = "lootboxtype")
	private Integer lootboxType;
	@Column(name = "israre")
	private boolean isRare;
	@Column(name = "basemodel")
	private String baseModel;

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getStoreName() {
		return this.storeName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return this.fullName;
	}

	public void setManufactor(String manufactor) {
		this.manufactor = manufactor;
	}

	public String getManufactor() {
		return this.manufactor;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getModel() {
		return this.model;
	}
	
	public void setModelSmall(String modelSmall) {
		this.modelSmall = modelSmall;
	}

	public String getModelSmall() {
		return this.modelSmall;
	}

	public void setTsStock(Integer tsStock) {
		this.tsStock = tsStock;
	}

	public Integer getTsStock() {
		return this.tsStock;
	}

	public void setTsVar1(Integer tsVar1) {
		this.tsVar1 = tsVar1;
	}

	public Integer getTsVar1() {
		return this.tsVar1;
	}

	public void setTsVar2(Integer tsVar2) {
		this.tsVar2 = tsVar2;
	}

	public Integer getTsVar2() {
		return this.tsVar2;
	}

	public void setTsVar3(Integer tsVar3) {
		this.tsVar3 = tsVar3;
	}

	public Integer getTsVar3() {
		return this.tsVar3;
	}

	public void setAcStock(Integer acStock) {
		this.acStock = acStock;
	}

	public Integer getAcStock() {
		return this.acStock;
	}

	public void setAcVar1(Integer acVar1) {
		this.acVar1 = acVar1;
	}

	public Integer getAcVar1() {
		return this.acVar1;
	}

	public void setAcVar2(Integer acVar2) {
		this.acVar2 = acVar2;
	}

	public Integer getAcVar2() {
		return this.acVar2;
	}

	public void setAcVar3(Integer acVar3) {
		this.acVar3 = acVar3;
	}

	public Integer getAcVar3() {
		return this.acVar3;
	}

	public void setHaStock(Integer haStock) {
		this.haStock = haStock;
	}

	public Integer getHaStock() {
		return this.haStock;
	}

	public void setHaVar1(Integer haVar1) {
		this.haVar1 = haVar1;
	}

	public Integer getHaVar1() {
		return this.haVar1;
	}

	public void setHaVar2(Integer haVar2) {
		this.haVar2 = haVar2;
	}

	public Integer getHaVar2() {
		return this.haVar2;
	}

	public void setHaVar3(Integer haVar3) {
		this.haVar3 = haVar3;
	}

	public Integer getHaVar3() {
		return this.haVar3;
	}

	public Integer getHash() {
		return hash;
	}

	public void setHash(Integer hash) {
		this.hash = hash;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}
	
	public int getCarVersion() {
		return carVersion;
	}

	public void setCarVersion(int carVersion) {
		this.carVersion = carVersion;
	}
	
	public boolean getQuickRaceAllowed() {
		return quickRaceAllowed;
	}

	public void setQuickRaceAllowed(boolean quickRaceAllowed) {
		this.quickRaceAllowed = quickRaceAllowed;
	}
	
	public int getLootboxType() {
		return lootboxType;
	}

	public void setLootboxType(int lootboxType) {
		this.lootboxType = lootboxType;
	}
	
	public boolean isRare() {
		return isRare;
	}

	public void setIsRare(boolean setIsRare) {
		this.isRare = setIsRare;
	}
	
	public String getBaseModel() {
		return baseModel;
	}
	
	public void setBaseModel(String baseModel) {
		this.baseModel = baseModel;
	}
}
