/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.d;

import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.DTDErrorCode;
import org.junit.Test;

/**
 * DTD doctype diagnostics services tests
 *
 */
public class DTDDoctypeDiagnosticsTest {

	@Test
	public void MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL() throws Exception {
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<!DOCTYPE student [\r\n" + //
				"  <!ELEMENT \r\n" + // <-- error
				"]>\r\n" + //
				"<student />";
		XMLAssert.testDiagnosticsFor(xml, d(2, 4, 11, DTDErrorCode.MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL));
	}

	@Test
	public void ElementDeclUnterminated() throws Exception {
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<!DOCTYPE student [\r\n" + //
				"  <!ELEMENT student (surname,id)>\r\n" + //
				"  <!ELEMENT surname (#PCDATA)\r\n" + // <- error
				"]>\r\n" + //
				"<student />";
		XMLAssert.testDiagnosticsFor(xml, d(3, 29, 30, DTDErrorCode.ElementDeclUnterminated));
	}

	@Test
	public void MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN() throws Exception {
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<!DOCTYPE student [\r\n" + //
				"  <!ELEMENT surname\r\n" + // <- error
				"]>\r\n" + //
				"<student />";
		XMLAssert.testDiagnosticsFor(xml,
				d(2, 19, 20, DTDErrorCode.MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN));
	}

}
