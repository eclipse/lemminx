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

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.xerces.util.URI.MalformedURIException;

/**
 * 
 * The declared "xsi:schemaLocation"
 */
public class SchemaLocation {

	private final Map<String, String> schemaLocationValuePairs;

	public SchemaLocation(String base, String value) {
		this.schemaLocationValuePairs = new HashMap<>();
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

	/**
	 * Given a schema URI, this will return true if the given URI
	 * matches the defined path in xsi:schemaLocation.
	 */
	public boolean usesSchema(Path rootPath, Path xsdPath) {
		if (rootPath == null || xsdPath == null) {
			return false;
		}

		for (String value : schemaLocationValuePairs.values()) {
			String valueWithoutSchema = URI.create(value).normalize().getPath();
			Path currentSchemaURI = Paths.get(valueWithoutSchema);
			if(!currentSchemaURI.isAbsolute()) {
				currentSchemaURI = rootPath.resolve(currentSchemaURI);
			}
			
			if(xsdPath.equals(currentSchemaURI)) {
				return true;
			}
		}
		return false;
	}

}
