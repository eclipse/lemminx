/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.dom;

import java.util.concurrent.CancellationException;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.commons.TextDocumentVersionChecker;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test to stop parse of {@link DOMDocument} when current version is not
 * synchronized with the version of {@link TextDocument}.
 * 
 * @author Angelo ZERR
 *
 */
public class DOMDocumentVersionCheckerTest {

	@Test
	public void stopDOMParsing() {

		TextDocument textDocument = createTextDocument();
		int version = 1;
		textDocument.setVersion(version);

		// text document version and checker version are synchronized -> parse should be
		// done
		CancellationException ex = null;
		TextDocumentVersionChecker checker = new TextDocumentVersionChecker(textDocument, version);
		try {
			DOMParser.getInstance().parse(textDocument, null, true, checker);
		} catch (CancellationException e) {
			ex = e;
		}
		Assert.assertNull(ex);

		// text document version and checker version are NOT synchronized -> parse
		// should be stopped
		ex = null;
		version = 2;
		textDocument.setVersion(version);
		try {
			DOMParser.getInstance().parse(textDocument, null, true, checker);
		} catch (CancellationException e) {
			ex = e;
		}
		Assert.assertNotNull(ex);

		// text document version and checker version are synchronized -> parse should be
		// done
		ex = null;
		checker = new TextDocumentVersionChecker(textDocument, version);
		try {
			DOMParser.getInstance().parse(textDocument, null, true, checker);
		} catch (CancellationException e) {
			ex = e;
		}
		Assert.assertNull(ex);
	}

	@Test
	public void positionAt() throws BadLocationException {
		TextDocument textDocument = createTextDocument();
		int version = 1;
		textDocument.setVersion(version);

		TextDocumentVersionChecker checker = new TextDocumentVersionChecker(textDocument, version);
		DOMDocument document = DOMParser.getInstance().parse(textDocument, null, true, checker);

		// text document version and checker version are synchronized -> call of
		// positionAt (which uses line tracker) should work
		CancellationException ex = null;
		try {
			document.positionAt(0);
		} catch (CancellationException e) {
			ex = e;
		}
		Assert.assertNull(ex);

		// text document version and checker version are NOT synchronized -> call of
		// positionAt (which uses line tracker) should NOT work
		ex = null;
		textDocument.setVersion(++version);
		try {
			document.positionAt(0);
		} catch (CancellationException e) {
			ex = e;
		}
		Assert.assertNotNull(ex);
	}

	private static TextDocument createTextDocument() {
		return new TextDocument("<root />", "nasa.xml");
	}
}
