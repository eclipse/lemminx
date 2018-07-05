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
package org.eclipse.lsp4xml.contentmodel;

import java.util.Collection;

/**
 * Content model element which abstracts element declaration from a given
 * grammar (XML Schema, DTD).
 */
public interface CMElement {

	/**
	 * Returns the declared element name.
	 * 
	 * @return the declared element name.
	 */
	String getName();

	/**
	 * Returns the attributes of this declared element.
	 * 
	 * @return the attributes element of this declared element.
	 */
	Collection<CMAttribute> getAttributes();

	/**
	 * Returns the children declared element of this declared element.
	 * 
	 * @return the children declared element of this declared element.
	 */
	Collection<CMElement> getElements();

	/**
	 * Returns the declared element which matches the given XML tag name / namespace
	 * and null otherwise.
	 * 
	 * @param tag
	 * @param namespace
	 * @return the declared element which matches the given XML tag name / namespace
	 *         and null otherwise.
	 */
	CMElement findCMElement(String tag, String namespace);

	/**
	 * Returns the documentation of the declared element.
	 * 
	 * @return the documentation of the declared element.
	 */
	String getDocumentation();
}
