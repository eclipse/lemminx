/**
 *  Copyright (c) 2018 Red Hat, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Nikolas Komonen <nikolaskomonen@gmail.com>, Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lsp4xml.utils;

/**
 * String utilities.
 *
 */
public class StringUtils {

	private StringUtils() {

	}

	/**
	 * Returns the result of normalize space of the given string.
	 * 
	 * @param str
	 * @return the result of normalize space of the given string.
	 */
	public static String normalizeSpace(String str) {
		StringBuilder b = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if (Character.isWhitespace(c)) {
				if (i <= 0 || Character.isWhitespace(str.charAt(i - 1)))
					continue;
				b.append(' ');
				continue;
			}
			b.append(c);
		}
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

	/**
	 * Trims whitespace from the right
	 * @param str
	 * @param startOffset The offset where the search should begin
	 * @return
	 */
	public static String rTrimOffset(String str, int startOffset) {
		if(str == null ) {
			return null;
		}
		int i;
		for (i = startOffset; i >= 0; i--) {
			char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
				break;	
			}
		}
		return str.substring(0,i + 1);
	}

	/**
	 * Trims whitespace from the left
	 * @param str
	 * @return
	 */
	public static String lTrim(String str) {
		if(str == null ) {
			return null;
		}
		int i;
		for (i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
				break;	
			}
		}
		return str.substring(i);
	}

	
	/**
	 * Remove beginning newlines
	 * @param str
	 * @return
	 */
	public static String removeBeginningNewLines(String str, char delimiter) {
		if(str == null ) {
			return null;
		}
		int lastNonNewlineWhitespace = 0;
		int i;
		for (i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			
			if (!Character.isWhitespace(c)) {
				if(lastNonNewlineWhitespace == 0) {
					lastNonNewlineWhitespace = i;
				}
				break;	
			}
			if(c == delimiter) {
				lastNonNewlineWhitespace = 0;
			} else {
				if(lastNonNewlineWhitespace == 0) {
					lastNonNewlineWhitespace = i;
				}
				
			}
		}
		return str.substring(lastNonNewlineWhitespace);
	}
}
