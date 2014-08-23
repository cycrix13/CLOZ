package com.jorgebs.cloz;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;

import android.net.ParseException;

public class CustomComparator implements Comparator<Looks> {
	@Override
	public int compare(Looks o1, Looks o2) {
		try {
			String dateStr1 = "";
			JSONArray dateArr1 = new JSONArray(o1.date);
			for (int i = 0; i < dateArr1.length(); i++) {
				dateStr1 += dateArr1.getString(i);
				if (i < dateArr1.length() - 1) {
					dateStr1 += "-";
				}
			}

			String dateStr2 = "";
			JSONArray dateArr2 = new JSONArray(o2.date);
			for (int i = 0; i < dateArr2.length(); i++) {
				dateStr2 += dateArr2.getString(i);
				if (i < dateArr2.length() - 1) {
					dateStr2 += "-";
				}
			}

			SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
			try {
				Date date1 = format.parse(dateStr1);
				Date date2 = format.parse(dateStr2);
				return date2.compareTo(date1);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		} catch (Exception e) {
		}

		return 0;
	}
}