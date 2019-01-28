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
package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.dom.DOMDocument;

/**
 * Definition participant API.
 *
 */
public interface IDefinitionParticipant {

	/**
	 * Find definition.
	 * 
	 * @param document
	 * @param position 
	 * @param locations
	 */
	void findDefinition(DOMDocument document, Position position, List<Location> locations);

}
