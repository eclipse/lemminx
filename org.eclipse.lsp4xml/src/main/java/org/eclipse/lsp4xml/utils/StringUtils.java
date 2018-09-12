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
}
