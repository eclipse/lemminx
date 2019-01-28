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
package org.eclipse.lsp4xml.extensions.emmet.emmetio.extract;

public class EmmetOptions {

	private String syntax;

	private Boolean lookAhead;

	private String prefix;

	public String getSyntax() {
		return syntax;
	}

	public EmmetOptions setSyntax(String syntax) {
		this.syntax = syntax;
		return this;
	}

	public Boolean getLookAhead() {
		return lookAhead;
	}

	public EmmetOptions setLookAhead(Boolean lookAhead) {
		this.lookAhead = lookAhead;
		return this;
	}

	public String getPrefix() {
		return prefix;
	}

	public EmmetOptions setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

}
