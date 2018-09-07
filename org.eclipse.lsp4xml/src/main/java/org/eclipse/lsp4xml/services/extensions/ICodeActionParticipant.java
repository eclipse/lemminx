package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.dom.XMLDocument;

public interface ICodeActionParticipant {

	void doCodeAction(Diagnostic diagnostic, Range range, XMLDocument document, List<CodeAction> codeActions);

}
