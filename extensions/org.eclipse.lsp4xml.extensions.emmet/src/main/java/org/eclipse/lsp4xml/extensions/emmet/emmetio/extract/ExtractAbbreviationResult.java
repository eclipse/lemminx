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

public class ExtractAbbreviationResult {

	private final String abbreviation;

	private final int location;

	private final int start;

	private final int end;

	public ExtractAbbreviationResult(String abbreviation, int location, int start, int end) {
		super();
		this.abbreviation = abbreviation;
		this.location = location;
		this.start = start;
		this.end = end;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public int getLocation() {
		return location;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

}
