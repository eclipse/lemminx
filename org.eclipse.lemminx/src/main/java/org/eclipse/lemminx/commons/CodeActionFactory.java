/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

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
	public static CodeAction insert(String title, Position position, String insertText, TextDocumentItem document,
			Diagnostic diagnostic) {
		CodeAction insertContentAction = new CodeAction(title);
		insertContentAction.setKind(CodeActionKind.QuickFix);
		insertContentAction.setDiagnostics(Arrays.asList(diagnostic));
		TextEdit edit = new TextEdit(new Range(position, position), insertText);
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(
				document.getUri(), document.getVersion());

		TextDocumentEdit textDocumentEdit = new TextDocumentEdit(versionedTextDocumentIdentifier, Collections.singletonList(edit));
		WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)));

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

		TextDocumentEdit textDocumentEdit = new TextDocumentEdit(versionedTextDocumentIdentifier, Collections.singletonList(edit));
		WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)));

		insertContentAction.setEdit(workspaceEdit);
		return insertContentAction;
	}

	public static CodeAction replaceAt(String title, String replaceText, TextDocumentItem document,
			Diagnostic diagnostic, Collection<Range> ranges) {
		CodeAction insertContentAction = new CodeAction(title);
		insertContentAction.setKind(CodeActionKind.QuickFix);
		insertContentAction.setDiagnostics(Arrays.asList(diagnostic));
		
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(
				document.getUri(), document.getVersion());
		ArrayList<TextEdit> edits = new ArrayList<TextEdit>();
		for (Range range : ranges) {
			TextEdit edit = new TextEdit(range, replaceText);
			edits.add(edit);
		}
		TextDocumentEdit textDocumentEdit = new TextDocumentEdit(versionedTextDocumentIdentifier, edits);
		WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)));

		insertContentAction.setEdit(workspaceEdit);
		return insertContentAction;
	}
}
