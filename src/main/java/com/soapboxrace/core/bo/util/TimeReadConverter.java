package com.soapboxrace.core.bo.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;

@Stateless
public class TimeReadConverter {
	
	public String convertRecord(Long ms) {
		long minutesO = TimeUnit.MILLISECONDS.toMinutes(ms)
		  - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms));
		long secondsO = TimeUnit.MILLISECONDS.toSeconds(ms)
		  - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms));
		long msO = TimeUnit.MILLISECONDS.toMillis(ms)
		  - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(ms));
		  
		String finalStrSec = "";
		String finalStrMS = "";
		
		// Seconds
		if (secondsO < 10) {finalStrSec = String.format("%d:0%d", minutesO, secondsO);}
		else {finalStrSec = String.format("%d:%d", minutesO, secondsO);}
		
		// Milliseconds
		if (msO < 100 && msO > 9) {finalStrMS = ".0" + msO;}
		if (msO < 10) {finalStrMS = ".00" + msO;}
		if (msO > 99) {finalStrMS = "." + msO;}
		
		String finalStr = finalStrSec.concat(finalStrMS);
		return finalStr;
	}
	
	public Long DateTimeToTicksC(LocalDateTime localDateTime) {
		long epochTicks = 621355968000000000L; // Game uses C# ticks for the news times
		ZoneId zone = ZoneId.of(TimeZone.getDefault().getID());
		ZoneOffset zoneOffset = zone.getRules().getOffset(localDateTime);
		Long ticks = localDateTime.toEpochSecond(zoneOffset) * 10000000 + epochTicks;
		return ticks;
	}
}
