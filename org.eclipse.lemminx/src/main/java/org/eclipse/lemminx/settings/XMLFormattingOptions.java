/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.settings;

import org.eclipse.lsp4j.FormattingOptions;

/**
 * This class is the root of all formatting settings. It is necessary to update
 * this class for any new additions.
 * 
 * All defaults should be set here to eventually be overridden if needed.
 */
public class XMLFormattingOptions extends FormattingOptions {

	public static final String DEFAULT_QUOTATION = "\"";
	public static final int DEFAULT_PRESERVER_NEW_LINES = 2;
	public static final int DEFAULT_TAB_SIZE = 2;
	public static final EnforceQuoteStyle DEFAULT_ENFORCE_QUOTE_STYLE = EnforceQuoteStyle.preferred;
	public static final QuoteStyle DEFAULT_QUOTE_STYLE = QuoteStyle.doubleQuotes;

	// All possible keys
	private static final String SPLIT_ATTRIBUTES = "splitAttributes";
	private static final String JOIN_CDATA_LINES = "joinCDATALines";
	private static final String FORMAT_COMMENTS = "formatComments";
	private static final String JOIN_COMMENT_LINES = "joinCommentLines";
	private static final String ENABLED = "enabled";
	private static final String SPACE_BEFORE_EMPTY_CLOSE_TAG = "spaceBeforeEmptyCloseTag";
	private static final String JOIN_CONTENT_LINES = "joinContentLines";
	private static final String PRESERVED_NEWLINES = "preservedNewlines";
	private static final String TRIM_FINAL_NEWLINES = "trimFinalNewlines";
	private static final String ENFORCE_QUOTE_STYLE = "enforceQuoteStyle";
	private static final String QUOTE_STYLE = "quoteStyle";

	enum Quotations {
		doubleQuotes, singleQuotes
	}

	private static final String PRESERVE_EMPTY_CONTENT = "preserveEmptyContent";

	/**
	 * Options for formatting empty elements.
	 * 
	 * <ul>
	 * <li>{@link #expand} : expand empty elements. With this option the following
	 * XML:
	 * 
	 * <pre>
	 * {@code
	 * <example />
	 * }
	 * </pre>
	 * 
	 * will be formatted to :
	 * 
	 * <pre>
	 * {@code
	 * <example><example>
	 * }
	 * </pre>
	 * 
	 * </li>
	 * <li>{@link #collapse} : collapse empty elements. With this option the
	 * following XML:
	 * 
	 * <pre>
	 * {@code
	 * <example></example>
	 * }
	 * </pre>
	 * 
	 * will be formatted to :
	 * 
	 * <pre>
	 * {@code
	 * <example />
	 * }
	 * </pre>
	 * 
	 * </li>
	 * <li>{@link #ignore} : keeps the original XML content for empty elements.
	 * </li>
	 * </ul>
	 * 
	 */
	public static enum EmptyElements {
		expand, collapse, ignore;
	}

	private static final String EMPTY_ELEMENTS = "emptyElements";

	public XMLFormattingOptions() {
		this(false);
	}

	/**
	 * Create an XMLFormattingOptions instance with the option to initialize default
	 * values for all supported settings.
	 */
	public XMLFormattingOptions(boolean initializeDefaults) {
		if (initializeDefaults) {
			initializeDefaultSettings();
		}
	}

	/**
	 * Necessary: Initialize default values in case client does not provide one
	 */
	private void initializeDefaultSettings() {
		super.setTabSize(DEFAULT_TAB_SIZE);
		super.setInsertSpaces(true);
		this.setSplitAttributes(false);
		this.setJoinCDATALines(false);
		this.setFormatComments(true);
		this.setJoinCommentLines(false);
		this.setJoinContentLines(false);
		this.setEnabled(true);
		this.setSpaceBeforeEmptyCloseTag(true);
		this.setPreserveEmptyContent(false);
		this.setPreservedNewlines(DEFAULT_PRESERVER_NEW_LINES);
		this.setEmptyElement(EmptyElements.ignore);
		this.setQuoteStyle(DEFAULT_QUOTE_STYLE);
	}

	public XMLFormattingOptions(int tabSize, boolean insertSpaces, boolean initializeDefaultSettings) {
		if (initializeDefaultSettings) {
			initializeDefaultSettings();
		}
		super.setTabSize(tabSize);
		super.setInsertSpaces(insertSpaces);
	}

	public XMLFormattingOptions(int tabSize, boolean insertSpaces) {
		this(tabSize, insertSpaces, true);
	}

	public XMLFormattingOptions(FormattingOptions options, boolean initializeDefaultSettings) {
		if (initializeDefaultSettings) {
			initializeDefaultSettings();
		}
		merge(options);
	}

	public XMLFormattingOptions(FormattingOptions options) {
		this(options, true);
	}

	public boolean isSplitAttributes() {
		final Boolean value = this.getBoolean(XMLFormattingOptions.SPLIT_ATTRIBUTES);
		if ((value != null)) {
			return (value).booleanValue();
		} else {
			return false;
		}
	}

	public void setSplitAttributes(final boolean splitAttributes) {
		this.putBoolean(XMLFormattingOptions.SPLIT_ATTRIBUTES, Boolean.valueOf(splitAttributes));
	}

	public boolean isJoinCDATALines() {
		final Boolean value = this.getBoolean(XMLFormattingOptions.JOIN_CDATA_LINES);
		if ((value != null)) {
			return (value).booleanValue();
		} else {
			return false;
		}
	}

	public void setJoinCDATALines(final boolean joinCDATALines) {
		this.putBoolean(XMLFormattingOptions.JOIN_CDATA_LINES, Boolean.valueOf(joinCDATALines));
	}

	public boolean isFormatComments() {
		final Boolean value = this.getBoolean(XMLFormattingOptions.FORMAT_COMMENTS);
		if ((value != null)) {
			return (value).booleanValue();
		} else {
			return false;
		}
	}

	public void setFormatComments(final boolean formatComments) {
		this.putBoolean(XMLFormattingOptions.FORMAT_COMMENTS, Boolean.valueOf(formatComments));
	}

	public boolean isJoinCommentLines() {
		final Boolean value = this.getBoolean(XMLFormattingOptions.JOIN_COMMENT_LINES);
		if ((value != null)) {
			return (value).booleanValue();
		} else {
			return false;
		}
	}

	public void setJoinCommentLines(final boolean joinCommentLines) {
		this.putBoolean(XMLFormattingOptions.JOIN_COMMENT_LINES, Boolean.valueOf(joinCommentLines));
	}

	public boolean isJoinContentLines() {
		final Boolean value = this.getBoolean(XMLFormattingOptions.JOIN_CONTENT_LINES);
		if ((value != null)) {
			return (value).booleanValue();
		} else {
			return false;
		}
	}

	public void setJoinContentLines(final boolean joinContentLines) {
		this.putBoolean(XMLFormattingOptions.JOIN_CONTENT_LINES, Boolean.valueOf(joinContentLines));
	}

	public boolean isEnabled() {
		final Boolean value = this.getBoolean(XMLFormattingOptions.ENABLED);
		if ((value != null)) {
			return (value).booleanValue();
		} else {
			return false;
		}
	}

	public void setEnabled(final boolean enabled) {
		this.putBoolean(XMLFormattingOptions.ENABLED, Boolean.valueOf(enabled));
	}

	public void setSpaceBeforeEmptyCloseTag(final boolean spaceBeforeEmptyCloseTag) {
		this.putBoolean(XMLFormattingOptions.SPACE_BEFORE_EMPTY_CLOSE_TAG, Boolean.valueOf(spaceBeforeEmptyCloseTag));
	}

	public boolean isSpaceBeforeEmptyCloseTag() {
		final Boolean value = this.getBoolean(XMLFormattingOptions.SPACE_BEFORE_EMPTY_CLOSE_TAG);
		if ((value != null)) {
			return (value).booleanValue();
		} else {
			return true;
		}
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

	public void setPreserveEmptyContent(final boolean preserveEmptyContent) {
		this.putBoolean(XMLFormattingOptions.PRESERVE_EMPTY_CONTENT, Boolean.valueOf(preserveEmptyContent));
	}

	public boolean isPreserveEmptyContent() {
		final Boolean value = this.getBoolean(XMLFormattingOptions.PRESERVE_EMPTY_CONTENT);
		if ((value != null)) {
			return (value).booleanValue();
		} else {
			return true;
		}
	}

	public void setPreservedNewlines(final int preservedNewlines) {
		this.putNumber(XMLFormattingOptions.PRESERVED_NEWLINES, preservedNewlines);
	}

	public int getPreservedNewlines() {
		final Number value = this.getNumber(XMLFormattingOptions.PRESERVED_NEWLINES);
		if ((value != null)) {
			return value.intValue();
		} else {
			return 2;
		}
	}

	public void setEmptyElement(EmptyElements emptyElement) {
		this.putString(XMLFormattingOptions.EMPTY_ELEMENTS, emptyElement.name());
	}

	public EmptyElements getEmptyElements() {
		String value = this.getString(XMLFormattingOptions.EMPTY_ELEMENTS);
		if ((value != null)) {
			try {
				return EmptyElements.valueOf(value);
			} catch (Exception e) {
			}
		}
		return EmptyElements.ignore;
	}

	/**
	 * Returns the value of trimFinalNewlines.
	 * 
	 * If the trimFinalNewlines does not exist, defaults to true.
	 */
	@Override
	public boolean isTrimFinalNewlines() {
		final Boolean value = this.getBoolean(TRIM_FINAL_NEWLINES);
		return (value == null) ? true: value;

	public void setEnforceQuoteStyle(EnforceQuoteStyle enforce) {
		this.putString(XMLFormattingOptions.ENFORCE_QUOTE_STYLE, enforce.name());
	}

	public EnforceQuoteStyle getEnforceQuoteStyle() {
		String value = this.getString(XMLFormattingOptions.ENFORCE_QUOTE_STYLE);
		EnforceQuoteStyle enforceStyle = null;
		
		try {
			enforceStyle = value == null ? null : EnforceQuoteStyle.valueOf(value);
		} catch (IllegalArgumentException e) {
			return DEFAULT_ENFORCE_QUOTE_STYLE;
		}
		
		return enforceStyle == null ? DEFAULT_ENFORCE_QUOTE_STYLE : enforceStyle;
	}

	public void setQuoteStyle(QuoteStyle style) {
		this.putString(QUOTE_STYLE, style.getText());
	}

	public QuoteStyle getQuoteStyle() {
		String style = this.getString(QUOTE_STYLE);
		QuoteStyle value = QuoteStyle.fromString(style);
		return value == null ? DEFAULT_QUOTE_STYLE : value;
	}

	public XMLFormattingOptions merge(FormattingOptions formattingOptions) {
		formattingOptions.entrySet().stream().forEach(entry -> {
			String key = entry.getKey();
			if (!key.equals("tabSize") && !key.equals("insertSpaces")) {
				this.put(entry.getKey(), entry.getValue());
			} else {
				this.putIfAbsent(entry.getKey(), entry.getValue());
			}
		});
		return this;
	}

	public static XMLFormattingOptions create(FormattingOptions options, FormattingOptions sharedFormattingOptions) {
		return new XMLFormattingOptions(options).merge(sharedFormattingOptions);
	}

}
