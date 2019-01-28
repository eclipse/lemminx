/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.dom;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.URI.MalformedURIException;

/**
 * 
 * The declared "xsi:noNamespaceSchemaLocation"
 */
public class NoNamespaceSchemaLocation {

	private final String documentURI;

	private final DOMAttr attr;

	public NoNamespaceSchemaLocation(String documentURI, DOMAttr attr) {
		this.documentURI = documentURI;
		this.attr = attr;
	}

	public DOMAttr getAttr() {
		return attr;
	}

	/**
	 * Returns the location declared in the attribute value of
	 * "xsi:noNamespaceSchemaLocation"
	 * 
	 * @return the location declared in the attribute value of
	 *         "xsi:noNamespaceSchemaLocation"
	 */
	public String getLocation() {
		return attr.getValue();
	}

	/**
	 * Returns the expanded system location
	 * 
	 * @return the expanded system location
	 */
	public String getResolvedLocation() {
		return getResolvedLocation(documentURI, getLocation());
	}

	private String getResolvedLocation(String documentURI, String location) {
		try {
			return XMLEntityManager.expandSystemId(location, documentURI, false);
		} catch (MalformedURIException e) {
			return location;
		}
	}

}
