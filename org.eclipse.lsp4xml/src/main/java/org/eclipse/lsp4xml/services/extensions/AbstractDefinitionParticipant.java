package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;

public abstract class AbstractDefinitionParticipant implements IDefinitionParticipant {

	@Override
	public void findDefinition(DOMDocument document, Position position, List<LocationLink> locations,
			CancelChecker cancelChecker) {
		if (!match(document)) {
			return;
		}
		try {
			int offset = document.offsetAt(position);
			DOMNode node = document.findNodeAt(offset);
			if (node != null) {
				findDefinition(node, position, offset, locations, cancelChecker);
			}
		} catch (BadLocationException e) {

		}
	}

	protected abstract boolean match(DOMDocument document);

	protected abstract void findDefinition(DOMNode node, Position position, int offset, List<LocationLink> locations,
			CancelChecker cancelChecker);

}
