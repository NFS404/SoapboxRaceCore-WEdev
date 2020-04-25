package com.soapboxrace.core.bo.util;

import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;

@Stateless
public class TimeReadConverter {
	
	// Taken from https://www.skptricks.com/2018/09/convert-milliseconds-into-days-hours-minutes-seconds-in-java.html
	public String convertRecord(Long ms) {
		long minutesO = TimeUnit.MILLISECONDS.toMinutes(ms)
		  - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms));
		long secondsO = TimeUnit.MILLISECONDS.toSeconds(ms)
		  - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms));
		long msO = TimeUnit.MILLISECONDS.toMillis(ms)
		  - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(ms));
		  
		return String.format("%d:%d.%d", minutesO, secondsO, msO);
	}
}
