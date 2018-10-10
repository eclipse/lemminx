package org.eclipse.lsp4xml.extensions.contentmodel.participants;

import java.util.List;

import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.NoNamespaceSchemaLocation;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.IDocumentLinkParticipant;

public class ContentModelDocumentLinkParticipant implements IDocumentLinkParticipant {

	@Override
	public void findDocumentLinks(XMLDocument document, List<DocumentLink> links) {
		NoNamespaceSchemaLocation noNamespaceSchemaLocation = document.getNoNamespaceSchemaLocation();
		if (noNamespaceSchemaLocation != null) {
			try {
				String location = noNamespaceSchemaLocation.getLocation();
				Node attrValue = noNamespaceSchemaLocation.getAttr().getNodeAttrValue();
				Position start = document.positionAt(attrValue.getStart() + 1);
				Position end = document.positionAt(attrValue.getEnd() - 1);
				links.add(new DocumentLink(new Range(start, end), location));
			} catch (BadLocationException e) {
				// Do nothing
			}
		}
	}

}
