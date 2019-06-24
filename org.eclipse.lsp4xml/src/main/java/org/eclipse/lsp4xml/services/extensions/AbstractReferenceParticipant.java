/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;

/**
 * Abstract class for reference participant.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractReferenceParticipant implements IReferenceParticipant {

	@Override
	public void findReference(DOMDocument document, Position position, ReferenceContext context,
			List<Location> locations, CancelChecker cancelChecker) {
		if (!match(document)) {
			return;
		}
		try {
			int offset = document.offsetAt(position);
			DOMNode node = document.findNodeAt(offset);
			if (node != null) {
				findReferences(node, position, offset, context, locations, cancelChecker);
			}
		} catch (BadLocationException e) {

		}
	}

	/**
	 * Returns true if the reference support is applicable for the given document
	 * and false otherwise.
	 * 
	 * @param document
	 * @return true if the reference support is applicable for the given document
	 *         and false otherwise.
	 */
	protected abstract boolean match(DOMDocument document);

	/**
	 * Find the references
	 * 
	 * @param node
	 * @param position
	 * @param offset
	 * @param locations
	 * @param cancelChecker
	 */
	protected abstract void findReferences(DOMNode node, Position position, int offset, ReferenceContext context,
			List<Location> locations, CancelChecker cancelChecker);

}
