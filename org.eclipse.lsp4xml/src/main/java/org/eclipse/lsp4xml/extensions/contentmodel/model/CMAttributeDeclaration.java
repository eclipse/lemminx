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
 * Content model element which abstracts attribute declaration from a given
 * grammar (XML Schema, DTD).
 */
public interface CMAttributeDeclaration {

	/**
	 * Returns the declared element name.
	 * 
	 * @return the declared element name.
	 */
	String getName();
	
	String getDefaultValue();

	Collection<String> getEnumerationValues(); 
	
	String getDocumentation();

	/**
	 * Returns true if the attribute is required and false otherwise.
	 * 
	 * @return true if the attribute is required and false otherwise.
	 */
	boolean isRequired();
}
