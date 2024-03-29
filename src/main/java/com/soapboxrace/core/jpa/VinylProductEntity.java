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
@Table(name = "VINYLPRODUCT")
@NamedQueries({ //
		@NamedQuery(name = "VinylProductEntity.findByProductId", //
				query = "SELECT obj FROM VinylProductEntity obj " //
						+ "WHERE obj.productId = :productId"), //
		@NamedQuery(name = "VinylProductEntity.findByHash", query = "SELECT obj FROM VinylProductEntity obj " //
				+ "WHERE obj.hash = :hash"),
		@NamedQuery(name = "VinylProductEntity.findByCategoryLevelEnabled", //
				query = "SELECT obj FROM VinylProductEntity obj " //
						+ "WHERE obj.category = :category " //
						+ "AND :minLevel >= obj.minLevel " //
						+ "AND (premium = false OR premium = :premium) " //
						+ "AND obj.enabled = :enabled") //
})
public class VinylProductEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String bundleItems;
	private String categoryId;
	private String currency;
	private String description;
	private int durationMinute;
	private int hash;
	private String icon;
	private int level;
	private String longDescription;
	private int price;
	private int priority;
	private String productId;
	private String productTitle;
	private String productType;
	private String secondaryIcon;
	private int useCount;
	private String visualStyle;
	private String webIcon;
	private String webLocation;

	@ManyToOne
	@JoinColumn(name = "parentCategoryId", referencedColumnName = "idcategory", foreignKey = @ForeignKey(name = "FK_VINYLPRODUCT_CATEGORY"))
	private CategoryEntity category;

	private String categoryName;
	private boolean enabled;
	private int minLevel;
	private boolean premium;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBundleItems() {
		return bundleItems;
	}

	public void setBundleItems(String bundleItems) {
		this.bundleItems = bundleItems;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDurationMinute() {
		return durationMinute;
	}

	public void setDurationMinute(int durationMinute) {
		this.durationMinute = durationMinute;
	}

	public int getHash() {
		return hash;
	}

	public void setHash(int hash) {
		this.hash = hash;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductTitle() {
		return productTitle;
	}

	public void setProductTitle(String productTitle) {
		this.productTitle = productTitle;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getSecondaryIcon() {
		return secondaryIcon;
	}

	public void setSecondaryIcon(String secondaryIcon) {
		this.secondaryIcon = secondaryIcon;
	}

	public int getUseCount() {
		return useCount;
	}

	public void setUseCount(int useCount) {
		this.useCount = useCount;
	}

	public String getVisualStyle() {
		return visualStyle;
	}

	public void setVisualStyle(String visualStyle) {
		this.visualStyle = visualStyle;
	}

	public String getWebIcon() {
		return webIcon;
	}

	public void setWebIcon(String webIcon) {
		this.webIcon = webIcon;
	}

	public String getWebLocation() {
		return webLocation;
	}

	public void setWebLocation(String webLocation) {
		this.webLocation = webLocation;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public CategoryEntity getCategory() {
		return category;
	}

	public void setCategory(CategoryEntity category) {
		this.category = category;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	public boolean isPremium() {
		return premium;
	}

	public void setPremium(boolean premium) {
		this.premium = premium;
	}

}
