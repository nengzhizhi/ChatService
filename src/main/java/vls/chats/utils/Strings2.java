package vls.chats.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

/**
 * @author Eric Gohn
 *
 */
public class Strings2 {
	public static String format(String template, @Nullable Object... args) {
		template = String.valueOf(template); // null -> "null"

		// start substituting the arguments into the '%s' placeholders
		StringBuilder builder = new StringBuilder(template.length() + 16
				* args.length);
		int templateStart = 0;
		int i = 0;
		while (i < args.length) {
			int placeholderStart = template.indexOf("%s", templateStart);
			if (placeholderStart == -1) {
				break;
			}
			builder.append(template.substring(templateStart, placeholderStart));
			builder.append(args[i++]);
			templateStart = placeholderStart + 2;
		}
		builder.append(template.substring(templateStart));

		// if we run out of placeholders, append the extra args in square braces
		if (i < args.length) {
			builder.append(" [");
			builder.append(args[i++]);
			while (i < args.length) {
				builder.append(", ");
				builder.append(args[i++]);
			}
			builder.append(']');
		}

		return builder.toString();
	}

	public static String hiddenTel(String tel) {
		if (tel == null) {
			return null;
		}
		return tel.replaceFirst("(?<=\\d{3})\\d{4}(?=\\d{4})", "****");
	}

	/**
	 * Check whether given string is null, empty or white space.
	 * 
	 * @param value
	 *            The string to be checked.
	 * @return <code>true</code> if <code>value</code> is null, empty, or white
	 *         space; Otherwise <code>false</code>.
	 */
	public static boolean isNullOrWhiteSpace(String value) {
		if (value == null)
			return true;
		return value.trim().equals("");
	}

	public static int parseInt(String str, int defaultValue) {
		if (Strings.isNullOrEmpty(str)) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static long parseLong(String str, long defaultValue) {
		if (Strings.isNullOrEmpty(str)) {
			return defaultValue;
		}
		try {
			return Long.parseLong(str);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static String toLowerCase(String str) {
		if (str == null) {
			return null;
		}
		return str.toLowerCase();
	}

	public static String trim(String str) {
		return str != null ? str.trim() : null;
	}
	public static String SHA1(String message) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			byte[] result = digest.digest(message.getBytes());
			StringBuffer sb = new StringBuffer();
			for (byte b : result) {
				sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

}