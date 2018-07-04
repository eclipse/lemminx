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

import org.eclipse.lsp4xml.model.Node;

/**
 * Content model document which abstracts element declaration from a given
 * grammar (XML Schema, DTD).
 */
public interface CMDocument {

	/**
	 * Returns the declared element which matches the given XML element and null
	 * otherwise.
	 * 
	 * @param element the XML element
	 * @return the declared element which matches the given XML element and null
	 *         otherwise.
	 */
	CMElement findCMElement(Node element);

}
