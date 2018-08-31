package org.eclipse.lsp4xml.contentmodel.participants.codeactions;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;

public class CVCComplexType23CodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, XMLDocument document, List<CodeAction> codeActions) {
		// Remove
		CodeAction removeContentAction = new CodeAction("Remove content");
		removeContentAction.setKind(CodeActionKind.QuickFix);
		removeContentAction.setDiagnostics(Arrays.asList(diagnostic));
		TextEdit edit = new TextEdit(range, "");
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(
				document.getTextDocument().getUri(), document.getTextDocument().getVersion());

		WorkspaceEdit workspaceEdit = new WorkspaceEdit(
				Arrays.asList(new TextDocumentEdit(versionedTextDocumentIdentifier, Arrays.asList(edit))));
		removeContentAction.setEdit(workspaceEdit);
		codeActions.add(removeContentAction);
	}

}
