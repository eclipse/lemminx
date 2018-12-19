/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr. - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.lsp4xml.dom;

public class LineIndentInfo {

	private final String lineDelimiter;
	private final String whitespacesIndent;

	public LineIndentInfo(String lineDelimiter, String whitespacesIndent) {
		this.lineDelimiter = lineDelimiter;
		this.whitespacesIndent = whitespacesIndent;
	}
	
	public String getLineDelimiter() {
		return lineDelimiter;
	}
	
	public String getWhitespacesIndent() {
		return whitespacesIndent;
	}

}
