package com.cycrix.util;

import java.util.regex.Pattern;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class CyUtils {
	public static final boolean isDebug = true;
	public static final String DOB_FORMAT = "dd/MM/yyyy";

	public static void showError(String message, Exception e, Context ct) {
		try {
			if (isDebug) {
				String messageStr = "";
				if (e != null)
					messageStr = e.toString();
				Builder builder = new Builder(ct);
				builder.setTitle(message);
				builder.setMessage(messageStr + "\n" + e.getStackTrace().toString());
				builder.create().show();
			} else
				Toast.makeText(ct, message, Toast.LENGTH_LONG).show();
		} catch (Exception ex) {

		}
	}
	public static void showToast(String message, Context ct) {
		Toast.makeText(ct, message, Toast.LENGTH_SHORT).show();
	}

	public static String right(String s, int n) {
		try {
			return s.substring(Math.max(s.length() - n, 0));
		} catch (Exception e) {
			return "";
		}
	}
	public static void setHoverEffect(View v) {
		setHoverEffect(v, true);
	}

	public static void setHoverEffect(View v, boolean setOnClick) {

		if (setOnClick)
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {}
			});

		v.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent ev) {
				switch (ev.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					v.getBackground().setColorFilter(0xFF808080, PorterDuff.Mode.MULTIPLY);
					v.invalidate();
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					v.getBackground().clearColorFilter();
					v.invalidate();
					break;
				}
				return false;
			}
		});
	}

	public static String group3digit(int x) {
		StringBuilder builder = new StringBuilder();
		String xStr = Integer.toString(x);

		for (int i = xStr.length() - 1, count = 1; i >= 0; i--, count++) {
			builder.insert(0, xStr.charAt(i));
			if (count % 3 == 0 && i != 0)
				builder.insert(0, ',');
		}

		return builder.toString(); 
	}

	public static String protectSpecialCharacters(String originalUnprotectedString) {
		if (originalUnprotectedString == null) {
			return null;
		}
		boolean anyCharactersProtected = false;

		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < originalUnprotectedString.length(); i++) {
			char ch = originalUnprotectedString.charAt(i);

			boolean controlCharacter = ch < 32;
			boolean unicodeButNotAscii = ch > 126;
			boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>' || ch == '\'' || ch == '\"';

			if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
				stringBuffer.append("&#" + (int) ch + ";");
				anyCharactersProtected = true;
			} else {
				stringBuffer.append(ch);
			}
		}
		if (anyCharactersProtected == false) {
			return originalUnprotectedString;
		}

		return stringBuffer.toString();
	}

	public static int dpToPx(int dp, Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int px = Math.round(dp * ((float) displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));       
		return px;
	}
	
//	private static String[] vieChar = new String[] {
//		"á|à|ả|ã|ạ|ă|ắ|ặ|ằ|ẳ|ẵ|â|ấ|ầ|ẩ|ẫ|ậ|Á|À|Ả|Ã|Ạ|Ă|Ắ|Ặ|Ằ|Ẳ|Ẵ|Â|Ấ|Ầ|Ẩ|Ẫ|Ậ",
//		"đ|Đ",
//		"é|è|ẻ|ẽ|ẹ|ê|ế|ề|ể|ễ|ệ|É|È|Ẻ|Ẽ|Ẹ|Ê|Ế|Ề|Ể|Ễ|Ệ",
//		"í|ì|ỉ|ĩ|ị|Í|Ì|Ỉ|Ĩ|Ị",
//		"ó|ò|ỏ|õ|ọ|ô|ố|ồ|ổ|ỗ|ộ|ơ|ớ|ờ|ở|ỡ|ợ|Ó|Ò|Ỏ|Õ|Ọ|Ô|Ố|Ồ|Ổ|Ỗ|Ộ|Ơ|Ớ|Ờ|Ở|Ỡ|Ợ",
//		"ú|ù|ủ|ũ|ụ|ư|ứ|ừ|ử|ữ|ự|Ú|Ù|Ủ|Ũ|Ụ|Ư|Ứ|Ừ|Ử|Ữ|Ự",
//		"ý|ỳ|ỷ|ỹ|ỵ|Ý|Ỳ|Ỷ|Ỹ|Ỵ"
//	};
	
	private static Pattern[] viePattern = new Pattern[] {
		Pattern.compile("á|à|ả|ã|ạ|ă|ắ|ặ|ằ|ẳ|ẵ|â|ấ|ầ|ẩ|ẫ|ậ|Á|À|Ả|Ã|Ạ|Ă|Ắ|Ặ|Ằ|Ẳ|Ẵ|Â|Ấ|Ầ|Ẩ|Ẫ|Ậ"),
		Pattern.compile("đ|Đ"),
		Pattern.compile("é|è|ẻ|ẽ|ẹ|ê|ế|ề|ể|ễ|ệ|É|È|Ẻ|Ẽ|Ẹ|Ê|Ế|Ề|Ể|Ễ|Ệ"),
		Pattern.compile("í|ì|ỉ|ĩ|ị|Í|Ì|Ỉ|Ĩ|Ị"),
		Pattern.compile("ó|ò|ỏ|õ|ọ|ô|ố|ồ|ổ|ỗ|ộ|ơ|ớ|ờ|ở|ỡ|ợ|Ó|Ò|Ỏ|Õ|Ọ|Ô|Ố|Ồ|Ổ|Ỗ|Ộ|Ơ|Ớ|Ờ|Ở|Ỡ|Ợ"),
		Pattern.compile("ú|ù|ủ|ũ|ụ|ư|ứ|ừ|ử|ữ|ự|Ú|Ù|Ủ|Ũ|Ụ|Ư|Ứ|Ừ|Ử|Ữ|Ự"),
		Pattern.compile("ý|ỳ|ỷ|ỹ|ỵ|Ý|Ỳ|Ỷ|Ỹ|Ỵ")
	};
	
	private static String [] nonVieChar = new String[] {
		"a", "d", "e", "i", "o", "u", "y"};
	
	public static String covertToNonVietnamese(String input) {
		
		for (int i = 0; i < viePattern.length; i++)
			input = viePattern[i].matcher(input).replaceAll(nonVieChar[i]);
		
		return input;
	}
}
