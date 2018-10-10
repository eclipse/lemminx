package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4xml.dom.XMLDocument;

public interface IDocumentLinkParticipant {

	void findDocumentLinks(XMLDocument document, List<DocumentLink> links);

}
