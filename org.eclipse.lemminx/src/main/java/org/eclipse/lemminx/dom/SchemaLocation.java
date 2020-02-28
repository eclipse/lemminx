/**
 *  Copyright (c) 2018 Angelo ZERR.
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
package org.eclipse.lemminx.dom;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 * The declared "xsi:schemaLocation"
 */
public class SchemaLocation {

	private final Map<String, String> schemaLocationValuePairs;

	private final DOMAttr attr;

	public SchemaLocation(String base, DOMAttr attr) {
		this.attr = attr;
		this.schemaLocationValuePairs = new HashMap<>();
		String value = attr.getValue();
		StringTokenizer st = new StringTokenizer(value);
		do {
			String namespaceURI = st.hasMoreTokens() ? st.nextToken() : null;
			String locationHint = st.hasMoreTokens() ? st.nextToken() : null;
			if (namespaceURI == null || locationHint == null)
				break;
			schemaLocationValuePairs.put(namespaceURI, locationHint);
		} while (true);
	}

	public String getLocationHint(String namespaceURI) {
		return schemaLocationValuePairs.get(namespaceURI);
	}

	public DOMAttr getAttr() {
		return attr;
	}

}
