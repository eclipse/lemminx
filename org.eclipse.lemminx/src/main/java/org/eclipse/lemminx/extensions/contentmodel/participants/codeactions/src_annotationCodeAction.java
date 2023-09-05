package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions;

import java.util.List;


import org.eclipse.lsp4j.Range;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lemminx.commons.CodeActionFactory;

public class src_annotationCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) {
		Diagnostic diagnostic = request.getDiagnostic();
		DOMDocument document = request.getDocument();
		Range diagnosticRange = diagnostic.getRange();
		String codeActionText;

		// Attempt to get tag name
		try {
			int startOffset = document.offsetAt(diagnosticRange.getStart()) + 1;
			DOMNode node = document.findNodeAt(startOffset);
			DOMElement element = (DOMElement) node;
			String tagName = element.getTagName();
			codeActionText = "Replace '" + tagName + "' with ";
		} catch (BadLocationException e) {
			codeActionText = "Replace with ";
		}
		
	
		// Replace with "appinfo"
		CodeAction replaceAction_appinfo = CodeActionFactory.replace(codeActionText + "'xs:appinfo'", diagnosticRange,
				"xs:appinfo",
				document.getTextDocument(), diagnostic);
		
		// Replace with "documentation"
		CodeAction replaceAction_documentation = CodeActionFactory.replace(codeActionText + "'xs:documentation'", diagnosticRange,
				"xs:documentation",
				document.getTextDocument(), diagnostic);

				

		codeActions.add(replaceAction_appinfo);
		codeActions.add(replaceAction_documentation);
		

		
	}

}