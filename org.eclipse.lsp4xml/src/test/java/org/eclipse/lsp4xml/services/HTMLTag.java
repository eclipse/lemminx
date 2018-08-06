/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import java.util.Arrays;
import java.util.Collection;

/**
 * HTML tags
 *
 */
public class HTMLTag {

	public static final Collection<HTMLTag> HTML_TAGS = Arrays.asList(t("div",
			"The div element has no special meaning at all. It represents its children. It can be used with the class, lang, and title attributes to mark up semantics common to a group of consecutive elements."), //
			t("header",
					"The header element represents introductory content for its nearest ancestor sectioning content or sectioning root element. A header typically contains a group of introductory or navigational aids. When the nearest ancestor sectioning content or sectioning root element is the body element, then it applies to the whole page."), //
			t("html", "The html element represents the root of an HTML document."), //
			t("h1", "The h1 element represents a section heading."), //
			t("iframe", "The iframe element represents a nested browsing context."), //
			t("input",
					"The input element represents a typed data field, usually with a form control to allow the user to edit the data."));

	private final String tag;
	private final String label;

	public HTMLTag(String tag, String label) {
		this.tag = tag;
		this.label = label;
	}

	public String getTag() {
		return tag;
	}

	public String getLabel() {
		return label;
	}

	private static HTMLTag t(String tag, String label) {
		return new HTMLTag(tag, label);
	}
}
