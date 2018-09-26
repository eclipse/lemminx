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
package org.eclipse.lsp4xml.services.extensions;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Test;

/**
 * XML hover tests which uses the {@link IHoverParticipant} to emulate HTML
 * language.
 *
 */
public class HTMLHoverExtensionsTest {

	@Test
	public void testSingle() throws BadLocationException {
		assertHover("|<html></html>");
		assertHover("<|html></html>", "<html>", 1);
		assertHover("<h|tml></html>", "<html>", 1);
		assertHover("<htm|l></html>", "<html>", 1);
		assertHover("<html|></html>", "<html>", 1);
		assertHover("<html>|</html>");
		assertHover("<html><|/html>");
		assertHover("<html></|html>", "</html>", 8);
		assertHover("<html></h|tml>", "</html>", 8);
		assertHover("<html></ht|ml>", "</html>", 8);
		assertHover("<html></htm|l>", "</html>", 8);
		assertHover("<html></html|>", "</html>", 8);
		assertHover("<html></html>|");
	};

	private static void assertHover(String value) throws BadLocationException {
		assertHover(value, null, null);
	}

	private static void assertHover(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {
		XMLAssert.assertHover(new HTMLLanguageService(), value, null, expectedHoverLabel, expectedHoverOffset);
	}

	private static class HTMLLanguageService extends XMLLanguageService {

		public HTMLLanguageService() {
			// Register HTML hover participant in the language service.
			super.registerHoverParticipant(new HTMLHoverParticipant());
		}

		class HTMLHoverParticipant extends HoverParticipantAdapter {

			@Override
			public Hover onTag(IHoverRequest request) {
				String tag = request.getCurrentTag();
				String tagLabel = request.isOpen() ? "<" + tag + ">" : "</" + tag + ">";
				MarkupContent content = new MarkupContent();
				content.setValue(tagLabel);
				return new Hover(content, request.getTagRange());
			}
		}
	}
}
