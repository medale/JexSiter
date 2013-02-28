package org.medale.exsiter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GitShell {

	public static String getDateTag(Date date) {
		String datePattern = "yyyyMMMdd";
		SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
		return formatter.format(date);
	}
}
