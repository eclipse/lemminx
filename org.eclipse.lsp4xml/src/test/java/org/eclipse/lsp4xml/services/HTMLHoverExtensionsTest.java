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

import java.util.List;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.extensions.IHoverParticipant;
import org.eclipse.lsp4xml.extensions.IHoverRequest;
import org.eclipse.lsp4xml.extensions.XMLExtensionAdapter;
import org.eclipse.lsp4xml.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.internal.parser.XMLParser;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.junit.Assert;
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

	private void assertHover(String value) throws BadLocationException {
		assertHover(value, null, null);
	}

	private void assertHover(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {
		int offset = value.indexOf("|");
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, "test://test/test.html");

		Position position = document.positionAt(offset);

		// Register HTML hover plugin in the language service.
		XMLExtensionsRegistry registry = new XMLExtensionsRegistry();
		registry.registerExtension(new HTMLHoverPlugin());
		XMLLanguageService ls = new XMLLanguageService(registry);
		XMLDocument htmlDoc = XMLParser.getInstance().parse(document);

		Hover hover = ls.doHover(htmlDoc, position);
		if (expectedHoverLabel == null) {
			Assert.assertNull(hover);
		} else {
			String actualHoverLabel = getHoverLabel(hover);
			Assert.assertEquals(expectedHoverLabel, actualHoverLabel);
			Assert.assertNotNull(hover.getRange());
			Assert.assertNotNull(hover.getRange().getStart());
			Assert.assertEquals(expectedHoverOffset.intValue(), hover.getRange().getStart().getCharacter());
		}
	}

	private static String getHoverLabel(Hover hover) {
		Either<List<Either<String, MarkedString>>, MarkupContent> contents = hover.getContents();
		if (contents == null) {
			return null;
		}
		return contents.getRight().getValue();
	}

	class HTMLHoverPlugin extends XMLExtensionAdapter implements IHoverParticipant {

		@Override
		public Hover onTag(IHoverRequest request) {
			String tag = request.getCurrentTag();
			String tagLabel = request.isOpen() ? "<" + tag + ">" : "</" + tag + ">";
			MarkupContent content = new MarkupContent();
			content.setValue(tagLabel);
			return new Hover(content, request.getTagRange());
		}

		@Override
		public IHoverParticipant getHoverParticipant() {
			return this;
		}
	}
}
