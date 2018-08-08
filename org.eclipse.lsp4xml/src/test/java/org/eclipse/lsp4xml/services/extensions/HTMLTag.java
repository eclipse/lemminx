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
package org.eclipse.lsp4xml.services.extensions;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
					"The input element represents a typed data field, usually with a form control to allow the user to edit the data.", //
					"dir", //
					"disabled", //
					"onmousemove", //
					"size", //
					"src", //
					"style", //
					"tabindex", //
					"type:t"));

	private static final Map<String, String[]> valueSets;

	static {
		valueSets = new HashMap<>();
		valueSets.put("t", new String[] { "text", "checkbox" });
	}

	private final String tag;
	private final String label;
	private final String[] attributes;

	public HTMLTag(String tag, String label, String... attributes) {
		this.tag = tag;
		this.label = label;
		this.attributes = attributes;
	}

	public String getTag() {
		return tag;
	}

	public String getLabel() {
		return label;
	}

	public String[] getAttributes() {
		return attributes;
	}

	private static HTMLTag t(String tag, String label, String... attributes) {
		return new HTMLTag(tag, label, attributes);
	}

	public static HTMLTag getHTMLTag(String tag) {
		Optional<HTMLTag> htmlTag = HTML_TAGS.stream().filter(t -> t.getTag().equals(tag)).findFirst();
		return htmlTag.isPresent() ? htmlTag.get() : null;
	}

	public static String[] getAttributeValues(String type) {
		return valueSets.get(type);
	}

}
