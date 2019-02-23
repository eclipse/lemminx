/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.settings;

import java.util.Objects;

import org.eclipse.lsp4j.FormattingOptions;

/**
 * This class is the root of all formatting settings. It is necessary to update
 * this class for any new additions.
 * 
 * All defaults should be set here to eventually be overridden if needed.
 */
public class XMLFormattingOptions extends FormattingOptions {
	// All possible keys
	private static final String SPLIT_ATTRIBUTES = "splitAttributes";
	private static final String JOIN_CDATA_LINES = "joinCDATALines";
	private static final String FORMAT_COMMENTS = "formatComments";
	private static final String JOIN_COMMENT_LINES = "joinCommentLines";
	private static final String ENABLED = "enabled";
	private static final String SPACE_BEFORE_EMPTY_CLOSE_TAG = "spaceBeforeEmptyCloseTag";
	private static final String QUOTATIONS = "quotations";

	// Values for QUOTATIONS
	public static final String DOUBLE_QUOTES_VALUE = "doubleQuotes";
	public static final String SINGLE_QUOTES_VALUE = "singleQuotes";
	enum Quotations {
		doubleQuotes, singleQuotes
	}	
	private static final String PRESERVE_EMPTY_CONTENT = "preserveEmptyContent";

	public XMLFormattingOptions() {
		this(false);
	}

	/**
	 * Create an XMLFormattingOptions instance with the option to initialize
	 * default values for all supported settings. 
	 */
	public XMLFormattingOptions(boolean initializeDefaults) {
		if (initializeDefaults) {
			initializeDefaultSettings();
		}
	}

	/** 
	 * Necessary: Initialize default values in case client does not provide one 
	 */
	public void initializeDefaultSettings() {
		this.setSplitAttributes(false);
		this.setJoinCDATALines(false);
		this.setFormatComments(true);
		this.setJoinCommentLines(false);
		this.setEnabled(true);
		this.setSpaceBeforeEmptyCloseTag(true);
		this.setQuotations(DOUBLE_QUOTES_VALUE);
		this.setPreserveEmptyContent(false);
	}

	public XMLFormattingOptions(int tabSize, boolean insertSpaces, boolean initializeDefaultSettings) {
		super(tabSize, insertSpaces);
		if(initializeDefaultSettings) {
			initializeDefaultSettings();
		}
	}

	public XMLFormattingOptions(int tabSize, boolean insertSpaces) {
		this(tabSize, insertSpaces, true);
	}

	public XMLFormattingOptions(FormattingOptions options, boolean initializeDefaultSettings) {
		if(initializeDefaultSettings) {
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

	public void setQuotations(final String quotations) {
		this.putString(XMLFormattingOptions.QUOTATIONS, quotations);
	}

	/**
	 * Returns the value of the format.quotations preference.
	 * 
	 * If invalid or null, the default is {@link XMLFormattingOptions#DOUBLE_QUOTES_VALUE}.
	 */
	public String getQuotations() {
		final String value = this.getString(XMLFormattingOptions.QUOTATIONS);
		if ((value != null) && isValidQuotations()) {
			return value;
		} else {
			this.setQuotations(XMLFormattingOptions.DOUBLE_QUOTES_VALUE);
			return DOUBLE_QUOTES_VALUE;// default
		}
	}

	/**
	 * If the quotations preference is a valid option.
	 * 
	 * Keep up to date with new preferences.
	 * @return
	 */
	private boolean isValidQuotations() {
		final String value = this.getString(XMLFormattingOptions.QUOTATIONS);
		return SINGLE_QUOTES_VALUE.equals(value) || DOUBLE_QUOTES_VALUE.equals(value);
	}

	/**
	 * Checks if {@code quotation} equals the current value for {@code format.quotations}.
	 * @param quotation 
	 * @return
	 */
	public boolean isQuotations(String quotation) {
		String value = getQuotations();
		return Objects.equals(value, quotation);
	}

	public void setPreserveEmptyContent(final boolean spaceBeforeEmptyCloseTag) {
		this.putBoolean(XMLFormattingOptions.PRESERVE_EMPTY_CONTENT, Boolean.valueOf(spaceBeforeEmptyCloseTag));
	}

	public boolean isPreserveEmptyContent() {
		final Boolean value = this.getBoolean(XMLFormattingOptions.PRESERVE_EMPTY_CONTENT);
		if ((value != null)) {
			return (value).booleanValue();
		} else {
			return true;
		}
	}

	public XMLFormattingOptions merge(FormattingOptions formattingOptions) {
		formattingOptions.entrySet().stream().forEach(entry -> {
			String key = entry.getKey();
			if(!key.equals("tabSize") && !key.equals("insertSpaces")) {
				this.put(entry.getKey(), entry.getValue());	
			} 
			else {
				this.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}
		);
		return this;
	}

	public static XMLFormattingOptions create(FormattingOptions options, FormattingOptions sharedFormattingOptions) {
		return new XMLFormattingOptions(options).merge(sharedFormattingOptions);
	}

}