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

public class ElementUnterminatedCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, XMLDocument document, List<CodeAction> codeActions) {
		// Add close '>'
		CodeAction closeAction = new CodeAction("Close element");
		closeAction.setKind(CodeActionKind.QuickFix);
		closeAction.setDiagnostics(Arrays.asList(diagnostic));
		TextEdit edit = new TextEdit(new Range(range.getEnd(), range.getEnd()), ">");
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(
				document.getTextDocument().getUri(), document.getTextDocument().getVersion());

		WorkspaceEdit workspaceEdit = new WorkspaceEdit(
				Arrays.asList(new TextDocumentEdit(versionedTextDocumentIdentifier, Arrays.asList(edit))));
		closeAction.setEdit(workspaceEdit);
		codeActions.add(closeAction);

		// Add Auto close '/>'
		CodeAction autoCloseAction = new CodeAction("Auto-close element");
		autoCloseAction.setKind(CodeActionKind.QuickFix);
		autoCloseAction.setDiagnostics(Arrays.asList(diagnostic));

		edit = new TextEdit(new Range(range.getEnd(), range.getEnd()), "/>");
		
		workspaceEdit = new WorkspaceEdit(
				Arrays.asList(new TextDocumentEdit(versionedTextDocumentIdentifier, Arrays.asList(edit))));		
		autoCloseAction.setEdit(workspaceEdit);

		codeActions.add(autoCloseAction);
	}

}
