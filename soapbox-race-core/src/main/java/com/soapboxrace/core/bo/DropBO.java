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

	public ProductEntity getRandomProductItem(String eventMode) {
		// Put "POWERUP" for power-ups drop, disabled for WEv2 - Hypercycle
		String[] productTypeArr = { "VISUALPART", "SKILLMODPART", "PERFORMANCEPART" };
		Random random = new Random();
		int number = 0;
		if (eventMode != null && eventMode.equalsIgnoreCase("thunt")) {
			number = random.nextInt((productTypeArr.length - 1) + 1);
		}
		else {
			number = random.nextInt(productTypeArr.length);
		}
		return productDao.getRandomDrop(productTypeArr[number]);
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
