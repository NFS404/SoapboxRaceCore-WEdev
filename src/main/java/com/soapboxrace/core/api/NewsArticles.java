package com.soapboxrace.core.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.jaxb.http.ArrayOfNewsArticleTrans;
import com.soapboxrace.jaxb.http.NewsArticleTrans;

@Path("/NewsArticles")
public class NewsArticles {

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public ArrayOfNewsArticleTrans newsArticles() {
		ArrayOfNewsArticleTrans arrayOfNewsArticleTrans = new ArrayOfNewsArticleTrans();
		NewsArticleTrans newsArticleTrans = new NewsArticleTrans();
		
		String shortText = "TXT_NEWS_RARE_ITEM_WON_STREAK_SHORT"; // Example
		shortText = "TXT_NEWS_WEV2_TEST_SHORT"; // Can be loaded from the server
		String longText = "TXT_NEWS_RARE_ITEM_WON_STREAK"; // Example
		longText = "TXT_NEWS_WEV2_TEST_FULL"; // Needs to be in locale files
		String parameters = "11871723|DRIVERX682|23,-1151617747,381|0"; // Example
		parameters = ""; // Interactive string data
		newsArticleTrans.setExpiryTime(null);
		newsArticleTrans.setFilters(1); // 1 - Me, 2 - Friends
		newsArticleTrans.setIconType(4); // 1 - Gray Car, 2 - Green Car (Freeroam), 3 - Friends, 4 - Me (Single)
		newsArticleTrans.setLongTextHALId(longText);
		newsArticleTrans.setNewsId(1);
		newsArticleTrans.setParameters(parameters);
		newsArticleTrans.setPersonaId(0); // If differs from 0, the avatar pic will be displayed only for the reciever
		newsArticleTrans.setShortTextHALId(shortText);
		newsArticleTrans.setSticky(0); // 0 - gray text color, 1 - yellow text color
		newsArticleTrans.setTimestamp(System.currentTimeMillis());
		newsArticleTrans.setType(20);
	    
		arrayOfNewsArticleTrans.getNewsArticleTrans().add(newsArticleTrans);
		return arrayOfNewsArticleTrans;
	}
}
