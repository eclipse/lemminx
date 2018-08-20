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
package org.eclipse.lsp4xml.contentmodel.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4xml.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.utils.XMLBuilder;

/**
 * XML generator used to generate an XML fragment with formatting from a given
 * element declaration (XML Schema element declaration, DTD element, etc).
 */
public class XMLGenerator {

	private final FormattingOptions formattingOptions;
	private final String whitespacesIndent;
	private final String lineDelimiter;
	private final boolean canSupportSnippets;

	/**
	 * XML generator constructor.
	 * 
	 * @param formattingOptions  the formatting options (uses spaces or tabs for
	 *                           indentation, etc)
	 * @param whitespacesIndent  the whitespaces to use to indent XML children
	 *                           elements.
	 * @param lineDelimiter      the line delimiter to use when several XML elements
	 *                           must be generated.
	 * @param canSupportSnippets true if snippets can be supported and false
	 *                           otherwise.
	 */
	public XMLGenerator(FormattingOptions formattingOptions, String whitespacesIndent, String lineDelimiter,
			boolean canSupportSnippets) {
		this.formattingOptions = formattingOptions;
		this.whitespacesIndent = whitespacesIndent;
		this.lineDelimiter = lineDelimiter;
		this.canSupportSnippets = canSupportSnippets;
	}

	/**
	 * Returns the XML generated from the given element declaration.
	 * 
	 * @param elementDeclaration
	 * @return the XML generated from the given element declaration.
	 */
	public String generate(CMElementDeclaration elementDeclaration) {
		XMLBuilder xml = new XMLBuilder(formattingOptions, whitespacesIndent, lineDelimiter);
		generate(elementDeclaration, 0, 0, xml, new ArrayList<CMElementDeclaration>());
		return xml.toString();
	}

	private int generate(CMElementDeclaration elementDeclaration, int level, int snippetIndex, XMLBuilder xml,
			List<CMElementDeclaration> generatedElements) {
		if (generatedElements.contains(elementDeclaration)) {
			return snippetIndex;
		}
		generatedElements.add(elementDeclaration);
		if (level > 0) {
			xml.linefeed();
			xml.indent(level);
		}
		xml.startElement(elementDeclaration.getName(), false);
		Collection<CMElementDeclaration> children = elementDeclaration.getElements();
		if (children.size() > 0) {
			xml.closeStartElement();
			level++;
			for (CMElementDeclaration child : children) {
				snippetIndex = generate(child, level, snippetIndex, xml, generatedElements);
			}
			level--;
			xml.linefeed();
			xml.indent(level);
			xml.endElement(elementDeclaration.getName());
		} else if (elementDeclaration.isEmpty()) {
			xml.endElement();
		} else {
			xml.closeStartElement();
			if (canSupportSnippets) {
				snippetIndex++;
				xml.addContent("$" + snippetIndex);				
			}
			xml.endElement(elementDeclaration.getName());
		}
		return snippetIndex;
	}

}
