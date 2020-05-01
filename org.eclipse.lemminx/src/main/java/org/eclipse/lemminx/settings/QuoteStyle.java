/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.settings;

/**
 * Quote style (single quotes, double quotes)
 */
public enum QuoteStyle {
	singleQuotes("single"), doubleQuotes("double");

	private String text;

	QuoteStyle(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static QuoteStyle fromString(String text) {
		for (QuoteStyle style : QuoteStyle.values()) {
			if (style.getText().equalsIgnoreCase(text)) {
				return style;
			}
		}
		return null;
	}
}