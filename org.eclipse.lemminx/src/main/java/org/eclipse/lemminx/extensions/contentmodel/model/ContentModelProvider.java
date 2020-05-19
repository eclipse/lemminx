/**
 *  Copyright (c) 2018 Angelo ZERR
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
package org.eclipse.lemminx.extensions.contentmodel.model;

import java.util.Collection;

import org.eclipse.lemminx.dom.DOMDocument;

/**
 * Content model provider API.
 *
 */
public interface ContentModelProvider {

	/**
	 * Returns the content model provider by using standard association
	 * (xsi:schemaLocation, xsi:noNamespaceSchemaLocation, doctype) an dnull
	 * otherwise.
	 * 
	 * @param document
	 * @param internal 
	 * @return the content model provider by using standard association
	 *         (xsi:schemaLocation, xsi:noNamespaceSchemaLocation, doctype) an dnull
	 *         otherwise.
	 */
	boolean adaptFor(DOMDocument document, boolean internal);

	boolean adaptFor(String uri);

	Collection<String> getSystemIds(DOMDocument xmlDocument, String namespaceURI);

	CMDocument createCMDocument(String key);

	CMDocument createInternalCMDocument(DOMDocument xmlDocument);
}
