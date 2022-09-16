/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.validation;

import org.eclipse.lemminx.extensions.contentmodel.participants.RNGErrorCode;
import org.junit.jupiter.api.Test;
import static org.eclipse.lemminx.XMLAssert.d;
import static org.eclipse.lemminx.XMLAssert.testDiagnosticsFor;

/**
 * Test validating XML files against RelaxNG schemas
 *
 * @author datho7561
 */
public class RelaxNGValidationTest {

	@Test
	public void relaxNGWithBrokenRNG() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/rng/broken.rng\"?>\r\n" + //
				"<root><item /></root>";
		testDiagnosticsFor(xml, d(1, 17, 52, RNGErrorCode.InvalidRelaxNG));
	}

	@Test
	public void relaxNGWithMissingAttributeValue() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/rng/dressSize.rng\"?>\r\n" + //
				"<dress size=\"\"></dress>";
		testDiagnosticsFor(xml, d(2, 7, 11, RNGErrorCode.BadAttribute),
				d(2, 23, 23, RNGErrorCode.IncompleteContentModel));
	}

	@Test
	public void relaxNGWithBadAttributeValue() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/rng/dressSize.rng\"?>\r\n" + //
				"<dress size=\"sandwich\"></dress>";
		testDiagnosticsFor(xml, d(2, 7, 11, RNGErrorCode.BadAttribute),
				d(2, 31, 31, RNGErrorCode.IncompleteContentModel));
	}

	@Test
	public void relaxNGWithUndefinedAttribute() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/rng/dressSize.rng\"?>\r\n" + //
				"<dress size=\"small\" asdf=\"\"></dress>";
		testDiagnosticsFor(xml, d(2, 20, 24, RNGErrorCode.BadAttribute),
				d(2, 36, 36, RNGErrorCode.IncompleteContentModel));
	}

	@Test
	public void relaxNGWithUndefinedElement() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/rng/dressSize.rng\"?>\r\n" + //
				"<dress size=\"small\"><pant></pant></dress>";
		testDiagnosticsFor(xml, d(2, 21,25, RNGErrorCode.BadTagName));
	}

	@Test
	public void relaxNGWithInvalidElementContent() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/rng/dressSize.rng\"?>\r\n" + //
				"<dress size=\"small\">pant</dress>";
		testDiagnosticsFor(xml, d(2, 20,24, RNGErrorCode.BadText));
	}

	@Test
	public void relaxNGElementTextContentDoesntMatchType() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/rng/dressSize.rng\"?>\r\n" + //
				"<dress><size>pant</size></dress>";
		// FIXME: only the innermost error should be reported
		testDiagnosticsFor(xml, d(2, 13,17, RNGErrorCode.BadText),
				d(2, 7, 24, RNGErrorCode.BadText));
	}

	@Test
	public void relaxNGWithValidContent1() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/rng/dressSize.rng\"?>\r\n" + //
				"<dress size=\"medium\"></dress>";
		testDiagnosticsFor(xml);
	}

	@Test
	public void relaxNGWithValidContent2() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/rng/dressSize.rng\"?>\r\n" + //
				"<dress size=\"-42\"></dress>";
		testDiagnosticsFor(xml);
	}

	@Test
	public void relaxNGWithValidContent3() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/rng/dressSize.rng\"?>\r\n" + //
				"<dress><size>medium</size></dress>";
		testDiagnosticsFor(xml);
	}

	@Test
	public void relaxNGWithValidContent4() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/rng/dressSize.rng\"?>\r\n" + //
				"<dress><size>9000000</size></dress>";
		testDiagnosticsFor(xml);
	}

}
