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

/**
 * 
 * The declared "xsi:noNamespaceSchemaLocation"
 */
public class NoNamespaceSchemaLocation {

	private final String location;

	public NoNamespaceSchemaLocation(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

}
