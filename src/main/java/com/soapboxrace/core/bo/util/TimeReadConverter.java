package com.soapboxrace.core.bo.util;

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
		if (secondsO < 10) {
			finalStrSec = String.format("%d:0%d", minutesO, secondsO);
		}
		else {
			finalStrSec = String.format("%d:%d", minutesO, secondsO);
		}
		
		// Milliseconds
		if (msO < 100 && msO > 9) {
			finalStrMS = ".0" + msO;
		}
		if (msO < 10) {
			finalStrMS = ".00" + msO;
		}
		else {
			finalStrMS = "." + msO;
		}
		String finalStr = finalStrSec.concat(finalStrMS);
		return finalStr;
	}
}
