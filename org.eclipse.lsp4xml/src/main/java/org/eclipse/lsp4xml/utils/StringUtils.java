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
		if (value == null) {
			return false;
		}
		char c;
		int end = value.length();
		int index = 0;
		while (index < end) {
			c = value.charAt(index);
			if (Character.isWhitespace(c) == false) {
				return false;
			}
			index++;
		}
		return true;
	}

	/**
	 * Normalizes the whitespace characters of a given string and applies it to the
	 * given string builder.
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
		while (i < value.length() && Character.isWhitespace(c)) {
			i++;
			c = val[i];
		}

		return value.substring(i, len);
	}

	/**
	 * Given a string that is only whitespace, this will return the amount of
	 * newline characters.
	 * 
	 * If the newLineCounter becomes > newLineLimit, then the value of newLineLimit
	 * is always returned.
	 * 
	 * @param text
	 * @param isWhitespace
	 * @param delimiter
	 * @return
	 */
	public static int getNumberOfNewLines(String text, boolean isWhitespace, String delimiter, int newLineLimit) {
		if (!isWhitespace) {
			return 0;
		}

		int newLineCounter = 0;
		boolean delimiterHasTwoCharacters = delimiter.length() == 2;
		for (int i = 0; newLineCounter <= newLineLimit && i < text.length(); i++) {
			String c;
			if (delimiterHasTwoCharacters) {
				if (i + 1 < text.length()) {
					c = text.substring(i, i + 2);
					if (delimiter.equals(c)) {
						newLineCounter++;
						i++; // skip the second char of the delimiter
					}
				}
			} else {
				c = String.valueOf(text.charAt(i));
				if (delimiter.equals(c)) {
					newLineCounter++;
				}
			}
		}
		return newLineCounter;
	}

	/**
	 * Given a string will give back a non null string that is either the given
	 * string, or an empty string.
	 * 
	 * @param text
	 * @return
	 */
	public static String getDefaultString(String text) {
		if (text != null) {
			return text;
		}
		return "";
	}

	/**
	 * Traverses backwards from the endOffset until it finds a whitespace character.
	 * 
	 * The offset of the character after the whitespace is returned.
	 * 
	 * (text = "abcd efg|h", endOffset = 8) -> 5
	 * 
	 * 
	 * @param text
	 * @param endOffset non-inclusive
	 * @return Start offset directly after the first whitespace.
	 */
	public static int getOffsetAfterWhitespace(String text, int endOffset) {
		if (text == null || endOffset <= 0 || endOffset > text.length()) {
			return -1;
		}

		char c = text.charAt(endOffset - 1);
		int i = endOffset;

		if (!Character.isWhitespace(c)) {
			while (!Character.isWhitespace(c)) {
				i--;
				if (i <= 0) {
					break;
				}
				c = text.charAt(i - 1);

			}
			return i;
		}
		return -1;
	}

	public static String cleanPathForWindows(String pathString) {
		if (pathString.startsWith("/")) {
			if (pathString.length() > 3) {
				char letter = pathString.charAt(1);
				char colon = pathString.charAt(2);
				if (Character.isLetter(letter) && ':' == colon) {
					pathString = pathString.substring(1);
				}
			}

		}
		pathString = pathString.replace("/", "\\");
		return pathString;
	}

	public static String escapeBackticks(String text) {
		int i = text.length() - 1;
		StringBuilder b = new StringBuilder(text);
		while (i >= 0) {
			char c = text.charAt(i);
			if (c == '`') {
				b.insert(i, "\\");
			}
			i--;
		}
		return b.toString();
	}

	public static boolean isTagOutsideOfBackticks(String text) {
		int i = 0;
		boolean inBacktick = false;
		while (i < text.length()) {
			char c = text.charAt(i);
			if (c == '`') {
				if (inBacktick) {
					inBacktick = false;
				} else {
					inBacktick = true;
				}
			} else if (c == '<') {
				i++;
				while (i < text.length()) {
					c = text.charAt(i);
					if (c == '`') {
						i--;
						break;
					}
					if (c == '>') {
						if (!inBacktick) {
							return true;
						}
						break;
					}
					i++;
				}
			}

			i++;
		}
		return false;

	}

	public static int findExprBeforeAt(String text, String expr, int offset) {
		if (offset <= 0) {
			return -1;
		}
		expr = expr.toUpperCase();
		int startOffset = -1;
		char first = expr.charAt(0);
		int length = Math.min(offset, expr.length());
		int i = 0;
		for (i = 1; i <= length; i++) {
			if (Character.toUpperCase(text.charAt(offset - i)) == first) {
				startOffset = offset - i;
				break;
			}
		}
		if (startOffset == -1) {
			return -1;
		}
		for (int j = 0; j < i; j++) {
			if (Character.toUpperCase(text.charAt(startOffset + j)) != expr.charAt(j)) {
				return -1;
			}
		}
		return startOffset - 1;
	}

}
