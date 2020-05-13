/**
 *  Copyright (c) 201r(0,8,0, 12)-2020 Angelo ZERR
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

import static org.eclipse.lemminx.XMLAssert.r;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.Range;
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
		assertHover("<|html></html>", "<html>", r(0, 1, 0, 5));
		assertHover("<h|tml></html>", "<html>", r(0, 1, 0, 5));
		assertHover("<htm|l></html>", "<html>", r(0, 1, 0, 5));
		assertHover("<html|></html>", "<html>", r(0, 1, 0, 5));
		assertHover("<html>|</html>");
		assertHover("<html><|/html>");
		assertHover("<html></|html>", "</html>", r(0, 8, 0, 12));
		assertHover("<html></h|tml>", "</html>", r(0, 8, 0, 12));
		assertHover("<html></ht|ml>", "</html>", r(0, 8, 0, 12));
		assertHover("<html></htm|l>", "</html>", r(0, 8, 0, 12));
		assertHover("<html></html|>", "</html>", r(0, 8, 0, 12));
		assertHover("<html></html>|");
		assertHover("<html>hover|Text</html>", "hoverText", r(0, 6, 0, 15));
		assertHover("<html>h|overText</html>", "hoverText", r(0, 6, 0, 15));
		assertHover("<html> |</html>", " ", r(0, 6, 0, 7));
	};

	private static void assertHover(String value) throws BadLocationException {
		assertHover(value, null, null);
	}

	private static void assertHover(String value, String expectedHoverLabel, Range expectedHoverOffset)
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
