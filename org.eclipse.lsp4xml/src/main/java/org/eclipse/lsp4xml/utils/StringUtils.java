/**
 *  Copyright (c) 2018 Red Hat, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Nikolas Komonen <nikolaskomonen@gmail.com>, Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lsp4xml.utils;

import java.util.Arrays;
import java.util.Collection;

/**
 * String utilities.
 *
 */
public class StringUtils {

	public static final String[] EMPTY_STRING = new String[0];

	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final Collection<String> TRUE_FALSE_ARRAY = Arrays.asList(TRUE, FALSE);

	private StringUtils() {
	}

	public static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	// Utilities class.
	public static boolean isQuote(char c) {
		return c == '\'' || c == '"';
	}

	public static boolean isWhitespace(String value) {
		if(value == null) {
			return false;
		}
		char c;
		int end = value.length();
		int index = 0;
		while(index < end) {
			c = value.charAt(index);
			if(Character.isWhitespace(c) == false) {
				return false;
			}
			index++;
		}
		return true;
	}

	/**
	 * Normalizes the whitespace characters of a given string and applies it
	 * to the given string builder.
	 * 
	 * @param str
	 * @return the result of normalize space of the given string.
	 */
	public static void normalizeSpace(String str, StringBuilder b) {
		String space = "";
		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if (Character.isWhitespace(c)) {
				if (i == 0 || Character.isWhitespace(str.charAt(i - 1))) {
					continue;
				}
				space = " ";
				continue;
			}
			b.append(space);
			space = "";
			b.append(c);
		}
	}

	/**
	 * Returns the result of normalize space of the given string.
	 * 
	 * @param str
	 * @return the result of normalize space of the given string.
	 */
	public static String normalizeSpace(String str) {
		StringBuilder b = new StringBuilder(str.length());
		normalizeSpace(str, b);
		return b.toString();
	}

	/**
	 * Returns the start whitespaces of the given line text.
	 * 
	 * @param lineText
	 * @return the start whitespaces of the given line text.
	 */
	public static String getStartWhitespaces(String lineText) {
		StringBuilder whitespaces = new StringBuilder();
		char[] chars = lineText.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (Character.isWhitespace(c)) {
				whitespaces.append(c);
			} else {
				break;
			}
		}
		return whitespaces.toString();
	}

	public static void trimNewLines(String value, StringBuilder s) {
		int len = value.length();
		int st = 0;
		char[] val = value.toCharArray();

		// left trim
		boolean hasNewLine = false;
		int start = 0;
		while ((st < len) && (Character.isWhitespace(val[st]))) {
			if (val[st] == '\r' || val[st] == '\n') {
				hasNewLine = true;
			} else if (hasNewLine) {
				break;
			}
			st++;
		}
		if (hasNewLine) {
			start = st;
			// adjust offset with \r\n
			if (st > 0 && st < len && val[st - 1] == '\r' && val[st] == '\n') {
				start++;
			}
		}

		// right trim
		hasNewLine = false;
		int end = len;
		while ((st < len) && (Character.isWhitespace(val[len - 1]))) {
			if (val[len - 1] == '\r' || val[len - 1] == '\n') {
				hasNewLine = true;
			} else if (hasNewLine) {
				break;
			}
			len--;
		}
		if (hasNewLine) {
			end = len;
			// adjust offset with \r\n
			if (val[len - 1] == '\r' && val[len] == '\n') {
				end--;
			}
		}
		s.append(value, start, end);
	}

	public static String trimNewLines(String value) {
		StringBuilder s = new StringBuilder();
		trimNewLines(value, s);
		return s.toString();
	}

	public static String lTrim(String value) {
		int len = value.length();
		int i = 0;
		char[] val = value.toCharArray();
		char c = val[i];

		// left trim
		while(i < value.length() && Character.isWhitespace(c)) {
			i++;
			c = val[i];
		}

		return value.substring(i, len);
	}

	

}
