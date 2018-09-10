/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
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

	private final String location;

	public NoNamespaceSchemaLocation(String xmlDocumentURI, String location) {
		this.location = getLocation(xmlDocumentURI, location);
	}

	private String getLocation(String xmlDocumentURI, String location) {
		try {
			return XMLEntityManager.expandSystemId(location, xmlDocumentURI, false);
		} catch (MalformedURIException e) {
			return location;
		}
	}

	public String getLocation() {
		return location;
	}

}
