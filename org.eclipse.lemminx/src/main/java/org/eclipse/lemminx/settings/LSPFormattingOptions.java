/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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

import java.util.LinkedHashMap;

import org.eclipse.lsp4j.FormattingOptions;

/**
 * Value-object describing what options formatting should use.
 * 
 * <p>
 * This class redefines the LSP4J {@link FormattingOptions} without extending
 * {@link LinkedHashMap} (public class FormattingOptions extends
 * LinkedHashMap<String, Either3<String, Number, Boolean>> {).
 * </p>
 * 
 * <p>
 * FormattingOptions can support only String, Number, Boolean, but not Array of
 * String. It is the reason why LemMinX redefines LSPFormattingOptions.
 * </p>
 */
public class LSPFormattingOptions {

	/**
	 * Size of a tab in spaces.
	 */
	private int tabSize;

	/**
	 * Prefer spaces over tabs.
	 */
	private boolean insertSpaces;

	/**
	 * Trim trailing whitespace on a line.
	 *
	 * @since 3.15.0
	 */
	private boolean trimTrailingWhitespace;

	/**
	 * Insert a newline character at the end of the file if one does not exist.
	 *
	 * @since 3.15.0
	 */
	private boolean insertFinalNewline;

	/**
	 * Trim all newlines after the final newline at the end of the file.
	 *
	 * @since 3.15.0
	 */
	private boolean trimFinalNewlines;

	public int getTabSize() {
		return tabSize;
	}

	public void setTabSize(int tabSize) {
		this.tabSize = tabSize;
	}

	public boolean isInsertSpaces() {
		return insertSpaces;
	}

	public void setInsertSpaces(boolean insertSpaces) {
		this.insertSpaces = insertSpaces;
	}

	public boolean isTrimTrailingWhitespace() {
		return trimTrailingWhitespace;
	}

	public void setTrimTrailingWhitespace(boolean trimTrailingWhitespace) {
		this.trimTrailingWhitespace = trimTrailingWhitespace;
	}

	public boolean isInsertFinalNewline() {
		return insertFinalNewline;
	}

	public void setInsertFinalNewline(boolean insertFinalNewline) {
		this.insertFinalNewline = insertFinalNewline;
	}

	public boolean isTrimFinalNewlines() {
		return trimFinalNewlines;
	}

	public void setTrimFinalNewlines(boolean trimFinalNewlines) {
		this.trimFinalNewlines = trimFinalNewlines;
	}

}
