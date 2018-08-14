/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.junit.Before;
import org.junit.Test;

/**
 * XML diagnostics services tests
 *
 */
public class XMLDiagnosticsTest {
	private List<Diagnostic> expectedDiagnostics;
	private final CancelChecker monitor = new CancelChecker(){
		@Override
		public void checkCanceled() {}};


	@Before
	public void startup() {
		expectedDiagnostics = new DiagnosticArrayList();
	}
	private static CancelChecker NULL_MONITOR = new CancelChecker() {

		@Override
		public void checkCanceled() {
			// Do nothing
		}
	};

	@Test
	public void testErrorAttributeNameOnly() {
		String text = "<team wrong></team>";
		setExpectedDiagnostic(XMLDiagnosticMessages.ATTRIBUTE_NO_EQUALS, null);
		assertValidation(text);
	}
	
	@Test
	public void testErrorAttributeNoClosingQuote() {
		String text = "<team wrong=\" ></team>";
		setExpectedDiagnostic(XMLDiagnosticMessages.MISSING_CLOSED_QUOTE, null);
		assertValidation(text);
	}

	@Test
	public void testErrorAttrobuteNoQuotes() {
		String text = "<team wrong= ></team>";
		setExpectedDiagnostic(XMLDiagnosticMessages.ATTRIBUTE_MISSING_VALUE, null);
		assertValidation(text);
	}

	@Test
	public void testErrorElementOpenTag() {
		String text = "<team </team>";
		setExpectedDiagnostic(XMLDiagnosticMessages.TAG_NOT_CLOSED, null);
		setExpectedDiagnostic(XMLDiagnosticMessages.MISSING_START_TAG, null);
		assertValidation(text);
	}

	@Test
	public void testErrorElementOpenTagNoSpace() {
		String text = "<team</team>";
		setExpectedDiagnostic(XMLDiagnosticMessages.TAG_NOT_CLOSED, null);
		setExpectedDiagnostic(XMLDiagnosticMessages.MISSING_START_TAG, null);
		assertValidation(text);
	}

	@Test
	public void testErrorContentBeforeRoot() {
		String text = "text in prolog <team></team>";
		setExpectedDiagnostic(XMLDiagnosticMessages.CONTENT_BEFORE_OPEN_TAG, null);
		assertValidation(text);
	}

	@Test
	public void testErrorContentBeforeProlog() {
		String text = "text in prolog <?xml ?><team></team>";
		setExpectedDiagnostic(XMLDiagnosticMessages.CONTENT_BEFORE_PROLOG, null);
		setExpectedDiagnostic(XMLDiagnosticMessages.CONTENT_BEFORE_OPEN_TAG, null);
		assertValidation(text);
	}

	@Test
	public void testErrorEndTagMissingClosingBracket() {
		String text = "<team></team";
		setExpectedDiagnostic(XMLDiagnosticMessages.TAG_NOT_CLOSED, null);
		setExpectedDiagnostic(XMLDiagnosticMessages.MISSING_END_TAG, null);
		assertValidation(text);
	}

	@Test
	public void testErrorContentOutsideRootTag() {
		String text = "<team></team> trailing text";
		setExpectedDiagnostic(XMLDiagnosticMessages.CONTENT_OUTSIDE_OF_ROOT_TAG, null);
		assertValidation(text);
	}

	@Test
	public void testErrorElementUnterminated() {
		String text = new StringBuilder("<Id>\r\n" + //
				"          <OrgId\r\n" + //
				"            <Othr>\r\n" + //
				"              <Id> 222010012</Id>\r\n" + //
				"            </Othr>\r\n" + //
				"          </OrgId>\r\n" + //
				"        </Id>") //
						.toString();
		setExpectedDiagnostic(XMLDiagnosticMessages.TAG_NOT_CLOSED, null);
		setExpectedDiagnostic(XMLDiagnosticMessages.MISSING_START_TAG, null);
		assertValidation(text);
	}

	// @Test
	// public void testErrorMissingQuoteAttribute() {
	// 	String text = "<root a=\"b c=\"d\"></root>";
	// 	setExpectedDiagnostic(text, null);
	// 	assertValidation(text);
	// }

	// @Test	
	// public void testNoErrorCommentOutsideRoot() {
	// 	String text = "<!-- COMMENT_TEXT --!> <root></root>";
	// 	setExpectedDiagnostic(text, null);
	// 	assertValidation(text);
	// }

	// @Test
	// public void testNoErrorPrologOutsideRoot() {
	// 	String text = "<?xml a=\"b\" c=\"d\"?><root></root>";
	// 	setExpectedDiagnostic(text, null);
	// 	assertValidation(text);	
	// }

	

	//------Tools--------------------------------------------
	public void assertValidation(String text) {
		TextDocument textDocument = new TextDocument(text, "test:uri");
		XMLExtensionsRegistry registry = new XMLExtensionsRegistry();
		XMLDiagnostics d = new XMLDiagnostics(registry);
		d.setDocument(textDocument);
		DiagnosticArrayList diagnostics = new DiagnosticArrayList();
		
		try {
			d.doBasicDiagnostics(textDocument, diagnostics, monitor);
		} catch (BadLocationException e) {
			fail("Bad Location in diagnostic test");
		}

		assertDiagnostics(diagnostics);
	}

	private void assertDiagnostics(List<Diagnostic> diagnostics) {
		assertEquals(expectedDiagnostics.size(), diagnostics.size());
		for (Diagnostic d : expectedDiagnostics) {
			assertTrue(diagnostics.contains(d));	
		}
		assertEquals(0,diagnostics.size());
	}

	public void setExpectedDiagnostic(String message, Range range) {
		Diagnostic d = new Diagnostic();
		d.setMessage(message);
		if(range != null) {
			d.setRange(range);
		}
		expectedDiagnostics.add(d);

	}

	private class DiagnosticArrayList extends ArrayList<Diagnostic>{
		
		@Override
		public boolean contains(Object o) {
			Diagnostic expected = (Diagnostic) o;
			Iterator it = this.iterator();
			while(it.hasNext()) {
				Diagnostic d = (Diagnostic) it.next();
				if(expected.getMessage().equals(d.getMessage())) {
					Range r = expected.getRange();
					if(r != null) {
						if(r.equals(d.getRange())) {
							it.remove();
							return true;
						}
						
						continue;
					}
					it.remove();
					return true;
				}
				
			}
			return false;
		}

	}
	

}
