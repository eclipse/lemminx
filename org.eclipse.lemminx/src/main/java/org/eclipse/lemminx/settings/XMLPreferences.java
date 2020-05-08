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
 * XML Preferences
 *
 */
public class XMLPreferences {
	
	public static final QuoteStyle DEFAULT_QUOTE_STYLE = QuoteStyle.doubleQuotes;

	private QuoteStyle quoteStyle;

	public XMLPreferences() {
		this.quoteStyle = DEFAULT_QUOTE_STYLE;
	}

	/**
	 * Returns the actual quotation value as a char.
	 * 
	 * Either a {@code '} or {@code "}.
	 * 
	 * Defaults to {@code "}.
	 */
	public char getQuotationAsChar() {
		QuoteStyle style = getQuoteStyle();
		return QuoteStyle.doubleQuotes.equals(style) ? '\"' : '\'';
	}

	/**
	 * Returns the actual quotation value as a String.
	 * 
	 * Either a {@code '} or {@code "}.
	 * 
	 * Defaults to {@code "}.
	 */
	public String getQuotationAsString() {
		return Character.toString(getQuotationAsChar());
	}

	/**
	 * Sets the quote style
	 * 
	 * @param quoteStyle
	 */
	public void setQuoteStyle(QuoteStyle quoteStyle) {
		this.quoteStyle = quoteStyle;
	}

	/**
	 * Returns the quote style
	 * 
	 * @return
	 */
	public QuoteStyle getQuoteStyle() {
		return this.quoteStyle;
	}

	public void merge(XMLPreferences newPreferences) {
		this.setQuoteStyle(newPreferences.getQuoteStyle());
	}
}
