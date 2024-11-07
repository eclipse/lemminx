/*******************************************************************************
* Copyright (c) 2024 Christoph Läubrich and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Christoph Läubrich - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.services.extensions.completion.ICompletionParticipant;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.junit.jupiter.api.Test;
import org.w3c.dom.CDATASection;

/**
 * XML completion tests which uses the {@link ICompletionParticipant}
 */
public class XMLCompletionExtensionTest extends AbstractCacheBasedTest {

	private static final CompletionItem HELLO_WORLD_ITEM = XMLAssert.c("World", "World");

	private final class TestCompletionParticipant implements ICompletionParticipant {
		@Override
		public void onXMLContent(ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker)
				throws Exception {
			if ("hello".equals(request.getCurrentTag())) {
				response.addCompletionAttribute(HELLO_WORLD_ITEM);
			} else {
				DOMNode node = request.getNode();
				if (node instanceof CDATASection) {
					DOMElement element = node.getParentElement();
					String tagName = element.getTagName();
					if ("hello".equals(tagName)) {
						response.addCompletionAttribute(HELLO_WORLD_ITEM);
					}
				}
			}
		}

		@Override
		public void onTagOpen(ICompletionRequest completionRequest, ICompletionResponse completionResponse,
				CancelChecker cancelChecker) throws Exception {
		}

		@Override
		public void onDTDSystemId(String valuePrefix, ICompletionRequest request, ICompletionResponse response,
				CancelChecker cancelChecker) throws Exception {
		}

		@Override
		public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response,
				CancelChecker cancelChecker) throws Exception {
		}

		@Override
		public void onAttributeName(boolean generateValue, ICompletionRequest request, ICompletionResponse response,
				CancelChecker cancelChecker) throws Exception {
		}
	}

	/**
	 * Test that
	 * {@link ICompletionParticipant#onXMLContent(ICompletionRequest, ICompletionResponse, CancelChecker)}
	 * is called for a simple content tag
	 * 
	 * @throws BadLocationException
	 */
	@Test
	public void testSimpleCompletion() throws BadLocationException {
		XMLLanguageService service = new XMLLanguageService();
		service.registerCompletionParticipant(new TestCompletionParticipant());
		XMLAssert.testCompletionFor(service, "<hello>|</hello>", (String) null, null, null, null, true,
				HELLO_WORLD_ITEM);
	}

	/**
	 * Test that
	 * {@link ICompletionParticipant#onXMLContent(ICompletionRequest, ICompletionResponse, CancelChecker)}
	 * is called for a CDATA content tag
	 * 
	 * @throws BadLocationException
	 */
	@Test
	public void testCDATACompletion() throws BadLocationException {
		XMLLanguageService service = new XMLLanguageService();
		service.registerCompletionParticipant(new TestCompletionParticipant());
		// check cursor is at the end of cdata section
		XMLAssert.testCompletionFor(service, "<hello><![CDATA[ |]]></hello>", (String) null, null, null, null, true,
				HELLO_WORLD_ITEM);
		// check cursor is at start of section
		XMLAssert.testCompletionFor(service, "<hello><![CDATA[| ]]></hello>", (String) null, null, null, null, true,
				HELLO_WORLD_ITEM);
		// check cursor is inside of section
		XMLAssert.testCompletionFor(service, "<hello><![CDATA[ | ]]></hello>", (String) null, null, null, null, true,
				HELLO_WORLD_ITEM);
		// check cursor is inside completely empty section
		XMLAssert.testCompletionFor(service, "<hello><![CDATA[|]]></hello>", (String) null, null, null, null, true,
				HELLO_WORLD_ITEM);
	}

}
