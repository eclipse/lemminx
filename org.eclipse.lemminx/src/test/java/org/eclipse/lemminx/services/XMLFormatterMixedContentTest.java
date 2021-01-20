/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.settings.SharedSettings;
import org.junit.jupiter.api.Test;

public class XMLFormatterMixedContentTest {

	@Test
	public void testMixedContentWithTrimTrailing() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimTrailingWhitespace(true);

		String xml = //
				"<aaa>\n" + //
				"  aaa!<aaa />\n" + //
				"</aaa>\n   ";

		String expected = //
				"<aaa>\n" + //
				"  aaa!\n" + //
				"  <aaa />\n" + //
				"</aaa>";

		XMLAssert.assertFormat(xml, expected, settings);
	}

	@Test
	public void testMixedContentWithTrimTrailing2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimTrailingWhitespace(true);

		String xml = //
				"<aaa>\n" + //
				"  aaa!<aaa />aaa!<!-- Comment -->\n" + //
				"</aaa>\n   ";

		String expected = //
				"<aaa>\n" + //
				"  aaa!\n" + //
				"  <aaa />\n" + //
				"  aaa! <!-- Comment -->\n" + //
				"</aaa>";

		XMLAssert.assertFormat(xml, expected, settings);
	}

	@Test
	public void testMixedContentWithTrimTrailing3() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimTrailingWhitespace(true);

		String xml = //
				"<aaa>\n" + //
				"  I believe <code>this</code> use case <b>should</b> be addressed, but in a <em>future</em> PR.\n" + //
				"</aaa>";

		String expected = //
				"<aaa>\n" + //
				"  I believe\n" + //
				"  <code>this</code>\n" + //
				"  use case\n" + //
				"  <b>should</b>\n" + //
				"  be addressed, but in a\n" + //
				"  <em>future</em>\n" + //
				"  PR.\n" + //
				"</aaa>";

		XMLAssert.assertFormat(xml, expected, settings);
	}

	@Test
	public void testMixedContentWithTrimTrailing4() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimTrailingWhitespace(true);

		String xml = //
				"<aaa>\n" + //
				"  content a <bbb> content b <ccc />  content c   </bbb>content d\n" + //
				"</aaa>";

		String expected = //
				"<aaa>\n" + //
				"  content a\n" + //
				"  <bbb>\n" + //
				"    content b\n" + //
				"    <ccc />\n" + //
				"    content c\n" + //
				"  </bbb>\n" + //
				"  content d\n" + //
				"</aaa>";

		XMLAssert.assertFormat(xml, expected, settings);
	}

	@Test
	public void testMixedContentWithTrimTrailing5() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimTrailingWhitespace(true);

		String xml = //
				"<aaa>\n" + //
				"  content a\n" + //
				"  <bbb>\n" + //
				"    content b\n" + //
				"    <ccc />\n" + //
				"    content c\n" + //
				"  </bbb>\n" + //
				"  content d\n" + //
				"</aaa>";

		XMLAssert.assertFormat(xml, xml, settings);
	}

}
