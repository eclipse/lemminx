/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.settings;

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
	private static final String JOIN_CONTENT_LINES = "joinContentLines";
	private static final String ENABLED = "enabled";

	public XMLFormattingOptions() {
		this(false);
	}

	public XMLFormattingOptions(boolean defaultValue) {
		if (defaultValue) {
			initializeDefaultSettings();
		}
	}

	private void initializeDefaultSettings() {
		this.setSplitAttributes(false);
		this.setJoinCDATALines(false);
		this.setFormatComments(true);
		this.setJoinCommentLines(false);
		this.setJoinContentLines(false);
		this.setEnabled(true);
	}

	public XMLFormattingOptions(int tabSize, boolean insertSpaces) {
		super(tabSize, insertSpaces);
	}

	public XMLFormattingOptions(FormattingOptions options) {
		merge(options);
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

	public XMLFormattingOptions merge(FormattingOptions formattingOptions) {
		formattingOptions.entrySet().stream().forEach(entry -> //
		this.putIfAbsent(entry.getKey(), entry.getValue()) //
		);
		return this;
	}

	public static XMLFormattingOptions create(FormattingOptions options, FormattingOptions sharedFormattingOptions) {
		return new XMLFormattingOptions(options).merge(sharedFormattingOptions);
	}

}