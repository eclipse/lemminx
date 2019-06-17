package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;

public abstract class AbstractDefinitionParticipant implements IDefinitionParticipant {

	@Override
	public final void findDefinition(DOMDocument document, Position position, List<Location> locations) {
		if (!match(document, position)) {
			return;
		}
		try {
			int offset = document.offsetAt(position);
			DOMNode node = document.findNodeAt(offset);
			if (node != null) {
				DOMAttr attr = node.findAttrAt(offset);
				findDefinition(attr != null ? attr : node, position, locations);
			}
		} catch (BadLocationException e) {

		}
	}

	protected abstract boolean match(DOMDocument document, Position position);

	protected abstract void findDefinition(DOMNode node, Position position, List<Location> locations);

}
