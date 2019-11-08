/**
 *  Copyright (c) 2018 Angelo ZERR and Liferay Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Seiphon Wang <seiphon.wang@liferay.com>
 */
package org.eclipse.lsp4xml.extensions.contentmodel;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.URI.MalformedURIException;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Test;

public class DTDHoverExtensionsTest {

	@Test
	public void testTagHover() throws BadLocationException, MalformedURIException {
		String dtdURI = getDTDFileURI("liferay-service-builder_7_2_0.dtd");
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<!DOCTYPE service-builder PUBLIC \"-//Liferay//DTD Service Builder 7.2.0//EN\" \"http://www.liferay.com/dtd/liferay-service-builder_7_2_0.dtd\">"
				+ "<service-builder dependency-injector=\"ds\" package-path=\"testSB\"></servi|ce-builder>";
		assertHover(xml,"The service-builder element is the root of the deployment descriptor for" + //
				" a Service Builder descriptor that is used to generate services available to" +
				" portlets. The Service Builder saves the developer time by generating Spring" +
				" utilities, SOAP utilities, and Hibernate persistence classes to ease the" +
				" development of services."
						+ //
						System.lineSeparator() + //
						System.lineSeparator() + "Source: [liferay-service-builder_7_2_0.dtd](" + dtdURI + ")",
				206);
	}

	@Test
	public void testAttributeNameHover() throws BadLocationException, MalformedURIException {
		String dtdURI = getDTDFileURI("liferay-service-builder_7_2_0.dtd");
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<!DOCTYPE service-builder PUBLIC \"-//Liferay//DTD Service Builder 7.2.0//EN\" \"http://www.liferay.com/dtd/liferay-service-builder_7_2_0.dtd\">"
				+ "<service-builder dependency-injector=\"ds\" pa|ckage-path=\"testSB\"></service-builder>";
		assertHover(xml,
				"The package-path value specifies the package of the generated code."
						+ //
						System.lineSeparator() + //
						System.lineSeparator() + "Source: [liferay-service-builder_7_2_0.dtd](" + dtdURI + ")",
				null);
	}

	private static void assertHover(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {
		XMLAssert.assertHover(new XMLLanguageService(), value, "src/test/resources/catalogs/catalog-liferay.xml", null,
				expectedHoverLabel, expectedHoverOffset);
	}

	private static String getDTDFileURI(String dtdURI) throws MalformedURIException {
		return XMLEntityManager.expandSystemId("dtd/" + dtdURI, "src/test/resources/test.xml", true).replace("///",
				"/");
	}
}
