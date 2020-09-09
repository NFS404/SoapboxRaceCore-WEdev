package com.soapboxrace.core.api;

import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.bo.util.TimeReadConverter;
import com.soapboxrace.core.dao.NewsArticlesDAO;
import com.soapboxrace.core.jpa.NewsArticlesEntity;
import com.soapboxrace.jaxb.http.ArrayOfNewsArticleTrans;
import com.soapboxrace.jaxb.http.NewsArticleTrans;

@Path("/NewsArticles")
public class NewsArticles {

	@EJB
	private NewsArticlesDAO newsArticlesDAO;
	
	@EJB
	private TimeReadConverter timeReadConverter;
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	// In-game news feed system
	public ArrayOfNewsArticleTrans newsArticles() {
		ArrayOfNewsArticleTrans arrayOfNewsArticleTrans = new ArrayOfNewsArticleTrans();
		List<NewsArticlesEntity> newsCommonList = newsArticlesDAO.loadCommon();
		for (NewsArticlesEntity newsArticle : newsCommonList) {
			NewsArticleTrans newsArticleTrans = new NewsArticleTrans();
			newsArticleTrans.setNewsId(newsArticle.getId());
			newsArticleTrans.setExpiryTime(null); // Needs testing
			newsArticleTrans.setFilters(newsArticle.getFilters());
			newsArticleTrans.setIconType(newsArticle.getIconType());
			newsArticleTrans.setLongTextHALId(newsArticle.getLongTextHALId());
			newsArticleTrans.setParameters(newsArticle.getParameters());
			newsArticleTrans.setPersonaId(newsArticle.getPersonaId());
			newsArticleTrans.setShortTextHALId(newsArticle.getShortTextHALId());
			newsArticleTrans.setSticky(newsArticle.getSticky());
			newsArticleTrans.setTimestamp(timeReadConverter.DateTimeToTicksC(newsArticle.getTimeStamp()));
			newsArticleTrans.setType(newsArticle.getType());
			
			arrayOfNewsArticleTrans.getNewsArticleTrans().add(newsArticleTrans);
		}
		return arrayOfNewsArticleTrans;
		
//		String shortText = "TXT_NEWS_RARE_ITEM_WON_STREAK_SHORT"; // Example
//		shortText = "TXT_NEWS_WEV2_BONUSCLASS_A_SHORT"; // Can be loaded from the server
//		String longText = "TXT_NEWS_RARE_ITEM_WON_STREAK"; // Example
//		longText = "TXT_NEWS_WEV2_BONUSCLASS_A_FULL"; // Needs to be in locale files
//		String parameters = "11871723|NILZAO|23,-1151617747,381|0"; // Example
		// Parameters syntax (11871723|NILZAO|23,-1151617747,381|0):
		// Example line: [0:l4] just won a [1:l5] from completing a Treasure Hunt! Check your map and play now! [2:l1] / [3:l9]
		// 11871723|NILZAO|23: Player persona nickname (l4 with unknown hash and number)
		// -1151617747: Item hash (l5)
		// 381|0: ??? Send a PM action, open a map action (l1, l9, unknown values, not used)
		// Other call values: [0:g] - gifts, [0:l0] - ??? shop content, [0:l2] - local player profile, [0:c] - car, [0:e] - event, [0:d] - event record, speed or level
		// Use "0" parameter if your string didn't contain any of interactive strings
//		parameters = "0"; // Interactive string data
//		newsArticleTrans.setExpiryTime(null);
//		newsArticleTrans.setFilters(1); // 0 - None, 1 - Me, 2 - Friends, 4 - System, 8 - Crew
//		newsArticleTrans.setIconType(4); // 1 - Gray Car, 2 - Green Car (Freeroam), 3 - Friends, 4 - Me (Single)
//		newsArticleTrans.setLongTextHALId(longText);
//		newsArticleTrans.setNewsId(1);
//		newsArticleTrans.setParameters(parameters);
//		newsArticleTrans.setPersonaId(0); // If differs from 0, the avatar pic will be displayed only for the reciever
//		newsArticleTrans.setShortTextHALId(shortText);
//		newsArticleTrans.setSticky(0); // 0 - gray text color, 1 - yellow text color
//		newsArticleTrans.setTimestamp(System.currentTimeMillis());
//		newsArticleTrans.setType(20);
	}
}
