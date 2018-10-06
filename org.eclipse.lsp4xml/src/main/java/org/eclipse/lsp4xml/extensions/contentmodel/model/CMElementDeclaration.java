/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.model;

import java.util.Collection;

/**
 * Content model element which abstracts element declaration from a given
 * grammar (XML Schema, DTD).
 */
public interface CMElementDeclaration {

	/**
	 * Returns the declared element name.
	 * 
	 * @return the declared element name.
	 */
	String getName();

	/**
	 * Returns the declared element name with the given prefix.
	 * 
	 * @return the declared element name with the given prefix.
	 */
	String getName(String prefix);

	/**
	 * Returns the attributes of this declared element.
	 * 
	 * @return the attributes element of this declared element.
	 */
	Collection<CMAttributeDeclaration> getAttributes();

	/**
	 * Returns the children declared element of this declared element.
	 * 
	 * @return the children declared element of this declared element.
	 */
	Collection<CMElementDeclaration> getElements();

	/**
	 * Returns the declared element which matches the given XML tag name / namespace
	 * and null otherwise.
	 * 
	 * @param tag
	 * @param namespace
	 * @return the declared element which matches the given XML tag name / namespace
	 *         and null otherwise.
	 */
	CMElementDeclaration findCMElement(String tag, String namespace);

	/**
	 * Returns the declared attribute which match the given name and null otherwise.
	 * 
	 * @param attributeName
	 * @return the declared attribute which match the given name and null otherwise.
	 */
	CMAttributeDeclaration findCMAttribute(String attributeName);

	/**
	 * Returns the documentation of the declared element.
	 * 
	 * @return the documentation of the declared element.
	 */
	String getDocumentation();

	/**
	 * Returns true if the element cannot contains element children or text content
	 * and false otherwise.
	 * 
	 * @return true if the element cannot contains element children or text content
	 *         and false otherwise.
	 */
	boolean isEmpty();

	 Collection<String> getEnumerationValues();
}
