package org.eclipse.lemminx.services.snippets;

import java.util.Map;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.ICompletionRequest;

public class XmlModelSnippetContext implements IXMLSnippetContext {

	public static IXMLSnippetContext DEFAULT_CONTEXT = new PrologSnippetContext();

	@Override
	public boolean isMatch(ICompletionRequest request, Map<String, String> model) {
		DOMNode node = request.getNode();
		int offset = request.getOffset();
		if ((node.isComment() || node.isDoctype()) && offset < node.getEnd()) {
			// completion was triggered inside comment or doctype
			return false;
		}
		DOMDocument document = request.getXMLDocument();
		DOMElement documentElement = document.getDocumentElement();
		if (documentElement != null && documentElement.getTagName() != null) {
			return offset <= documentElement.getStart();
		}
		// TODO: ensure it's not triggered before xml declaratioon
		return true;

		//TODO: add more pseudo attributes (e.g. type and schemaspacens) in json snippet
	}
	
}