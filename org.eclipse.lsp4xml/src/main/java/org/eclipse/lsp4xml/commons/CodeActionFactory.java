/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.commons;

import java.util.Arrays;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;

/**
 * Factory for simple {@link CodeAction}
 *
 */
public class CodeActionFactory {

	/**
	 * Create a CodeAction to remove the content from the given range.
	 * 
	 * @param title
	 * @param range
	 * @param document
	 * @param diagnostic
	 * @return
	 */
	public static CodeAction remove(String title, Range range, TextDocumentItem document, Diagnostic diagnostic) {
		return replace(title, range, "", document, diagnostic);
	}

	/**
	 * Create a CodeAction to insert a new content at the end of the given range.
	 * 
	 * @param title
	 * @param range
	 * @param insertText
	 * @param document
	 * @param diagnostic
	 * @return
	 */
	public static CodeAction insert(String title, Range range, String insertText, TextDocumentItem document,
			Diagnostic diagnostic) {
		CodeAction insertContentAction = new CodeAction(title);
		insertContentAction.setKind(CodeActionKind.QuickFix);
		insertContentAction.setDiagnostics(Arrays.asList(diagnostic));
		TextEdit edit = new TextEdit(new Range(range.getEnd(), range.getEnd()), insertText);
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(
				document.getUri(), document.getVersion());

		WorkspaceEdit workspaceEdit = new WorkspaceEdit(
				Arrays.asList(new TextDocumentEdit(versionedTextDocumentIdentifier, Arrays.asList(edit))));
		insertContentAction.setEdit(workspaceEdit);
		return insertContentAction;
	}

	public static CodeAction replace(String title, Range range, String replaceText, TextDocumentItem document,
			Diagnostic diagnostic) {
		CodeAction insertContentAction = new CodeAction(title);
		insertContentAction.setKind(CodeActionKind.QuickFix);
		insertContentAction.setDiagnostics(Arrays.asList(diagnostic));
		TextEdit edit = new TextEdit(range, replaceText);
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(
				document.getUri(), document.getVersion());

		WorkspaceEdit workspaceEdit = new WorkspaceEdit(
				Arrays.asList(new TextDocumentEdit(versionedTextDocumentIdentifier, Arrays.asList(edit))));
		insertContentAction.setEdit(workspaceEdit);
		return insertContentAction;
	}
}
