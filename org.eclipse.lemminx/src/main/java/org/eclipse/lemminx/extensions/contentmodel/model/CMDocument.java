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

import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lsp4j.LocationLink;

/**
 * Content model document which abstracts element declaration from a given
 * grammar (XML Schema, DTD).
 */
public interface CMDocument {

	/**
	 * Returns true if the model document defines the given namespace and false
	 * otherwise.
	 * 
	 * @param namespaceURI
	 * @return true if the model document defines the given namespace and false
	 *         otherwise.
	 */
	boolean hasNamespace(String namespaceURI);

	/**
	 * Returns the elements declaration of the model document root.
	 * 
	 * @return the elements declaration of the model document root.
	 */
	Collection<CMElementDeclaration> getElements();

	/**
	 * Returns the declared element which matches the given XML element and null
	 * otherwise.
	 * 
	 * @param element the XML element
	 * @return the declared element which matches the given XML element and null
	 *         otherwise.
	 */
	CMElementDeclaration findCMElement(DOMElement element, String namespace);

	/**
	 * Returns the root URI of the model document.
	 * 
	 * @return the root URI of the model document.
	 */
	String getURI();

	/**
	 * Returns the location of the type definition of the given node.
	 * 
	 * @param node the node
	 * @return the location of the type definition of the given node.
	 */
	LocationLink findTypeLocation(DOMNode node);

	/**
	 * Returns true if the content model document is dirty and false otherwise.
	 * 
	 * @return true if the content model document is dirty and false otherwise.
	 */
	boolean isDirty();
}
