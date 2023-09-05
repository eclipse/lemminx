package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions;

import java.util.List;


import org.eclipse.lsp4j.Range;
import org.eclipse.lemminx.dom.DOMDocument;
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
		
	
		// Replace with "appinfo"
		CodeAction replaceAction_appinfo = CodeActionFactory.replace("Replace with \"appinfo\"", diagnosticRange,
				"xs:appinfo",
				document.getTextDocument(), diagnostic);
		
		// Replace with "documentation"
		CodeAction replaceAction_documentation = CodeActionFactory.replace("Replace with \"documentation\"", diagnosticRange,
				"xs:documentation",
				document.getTextDocument(), diagnostic);

				

		codeActions.add(replaceAction_appinfo);
		codeActions.add(replaceAction_documentation);
		

		
	}

}