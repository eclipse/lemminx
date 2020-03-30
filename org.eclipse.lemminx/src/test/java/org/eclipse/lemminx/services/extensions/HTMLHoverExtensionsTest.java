/**
 *  Copyright (c) 2018 Angelo ZERR
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
package org.eclipse.lemminx.services.extensions;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.junit.jupiter.api.Test;

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
		assertHover("<html>hover|Text</html>", "hoverText", 6);
		assertHover("<html>h|overText</html>", "hoverText", 6);
		assertHover("<html> |</html>", " ", 6);
	};

	private static void assertHover(String value) throws BadLocationException {
		assertHover(value, null, null);
	}

	private static void assertHover(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {
		XMLAssert.assertHover(new HTMLLanguageService(), value, null, null, expectedHoverLabel, expectedHoverOffset);
	}

	private static class HTMLLanguageService extends XMLLanguageService {

		public HTMLLanguageService() {
			// Register HTML hover participant in the language service.
			super.registerHoverParticipant(new HTMLHoverParticipant());
		}

		class HTMLHoverParticipant extends HoverParticipantAdapter {

			@Override
			public String onTag(IHoverRequest request) {
				String tag = request.getCurrentTag();
				String tagLabel = request.isOpen() ? "<" + tag + ">" : "</" + tag + ">";
				return tagLabel;
			}
			@Override
			public String onText(IHoverRequest request) throws Exception {
				return request.getNode().getTextContent();
			}
		}
	}
}
