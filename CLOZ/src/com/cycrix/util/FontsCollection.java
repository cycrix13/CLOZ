package com.cycrix.util;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FontsCollection {
	public static final String[] fontNameArr = new String[] {
		"font1.ttf",
		"font2.ttc",
		"font3.ttf",
	};
	
	public static HashMap<String, Typeface> fonts = new HashMap<String, Typeface>();
	
	public static void init(Context ct) {
		for (String fontName : fontNameArr) {
			Typeface font = Typeface.createFromAsset(ct.getAssets(), "fonts/" + fontName);
			fonts.put(fontName, font);
		}
	}
	
	public static Typeface getFont(String name) {
		return fonts.get(name);
	}
	
	public static void setFont(View v) {
		
		if (v instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) v;
			for (int i = 0; i < group.getChildCount(); i++)
				setFont(group.getChildAt(i));
		}
		
		if (v instanceof TextView) {
			TextView txt = (TextView) v;
						
			Typeface defaultFont = txt.getTypeface();
			
			int defaultStyle = 0;
			if (defaultFont != null) {
				defaultStyle = defaultFont.getStyle();
			}

			if (txt.getContentDescription() == null)
				return; 
			
			String fontName = txt.getContentDescription().toString();
			
			if (!fontName.startsWith("font:"))
				return;
			
			fontName = fontName.substring(5);
			
			Typeface font = FontsCollection.getFont(fontName);
			if (font != null)
				txt.setTypeface(font, defaultStyle);
		}
	}
}
