package com.soapboxrace.core.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.codec.digest.DigestUtils;

import com.soapboxrace.core.dao.InventoryDAO;
import com.soapboxrace.core.dao.InventoryItemDAO;
import com.soapboxrace.core.dao.LevelRepDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ProductDAO;
import com.soapboxrace.core.jpa.ProductEntity;
import com.soapboxrace.jaxb.http.LuckyDrawItem;

@Stateless
public class DropBO {
	@EJB
	private PersonaDAO personaDao;

	@EJB
	private InventoryDAO inventoryDao;

	@EJB
	private InventoryItemDAO inventoryItemDao;

	@EJB
	private LevelRepDAO levelRepDao;

	@EJB
	private ProductDAO productDao;

	public ProductEntity getRandomProductItem(String eventMode, int isDropableMode, boolean isTeamRace) {
		// Put "POWERUP" for power-ups drop, disabled for WEv2 - Hypercycle
		Random random = new Random();
		int number = 0;
		String productTypeChosen = "";
		if (eventMode != null && eventMode.equalsIgnoreCase("thunt")) {
			String[] productTypeArr = { "SKILLMODPART", "PERFORMANCEPART" };
			number = random.nextInt(productTypeArr.length);
			productTypeChosen = productTypeArr[number];
		}
		else {
			String[] productTypeArr = { "VISUALPART", "SKILLMODPART", "PERFORMANCEPART" };
			number = random.nextInt(productTypeArr.length);
			productTypeChosen = productTypeArr[number];
		}
		// isDropableMode values: 1 - default drop, 2 - default + rare items, 3 - weak drop
		return productDao.getRandomDrop(productTypeChosen, isDropableMode, isTeamRace);
	}

	public LuckyDrawItem copyProduct2LuckyDraw(ProductEntity productEntity) {
		LuckyDrawItem luckyDrawItem = new LuckyDrawItem();
		luckyDrawItem.setDescription(productEntity.getProductTitle());
		luckyDrawItem.setHash(productEntity.getHash());
		luckyDrawItem.setIcon(productEntity.getIcon());
		luckyDrawItem.setRemainingUseCount(productEntity.getUseCount());
		luckyDrawItem.setResellPrice(productEntity.getResalePrice());
		luckyDrawItem.setVirtualItem(DigestUtils.md5Hex(productEntity.getHash().toString()));
		luckyDrawItem.setVirtualItemType(productEntity.getProductType());
		luckyDrawItem.setWasSold(false);
		return luckyDrawItem;
	}
}
