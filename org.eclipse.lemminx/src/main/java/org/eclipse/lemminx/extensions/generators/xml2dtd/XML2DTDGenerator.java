/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.generators.xml2dtd;

import java.util.Collection;
import java.util.Map;

import org.eclipse.lemminx.extensions.generators.AbstractXML2GrammarGenerator;
import org.eclipse.lemminx.extensions.generators.AttributeDeclaration;
import org.eclipse.lemminx.extensions.generators.Cardinality;
import org.eclipse.lemminx.extensions.generators.ElementDeclaration;
import org.eclipse.lemminx.extensions.generators.Grammar;
import org.eclipse.lemminx.utils.XMLBuilder;

/**
 * File Generator implementation to generate DTD from a given XML source.
 * 
 */
public class XML2DTDGenerator extends AbstractXML2GrammarGenerator<DTDGeneratorSettings> {

	@Override
	protected void generate(Grammar grammar, DTDGeneratorSettings settings, XMLBuilder dtd) {
		for (ElementDeclaration element : grammar.getElements()) {
			boolean hasCharacterContent = element.hasCharacterContent();

			// <!ELEMENT
			dtd.startDTDElementDecl();
			dtd.addParameter(element.getName());

			Collection<ElementDeclaration> children = element.getElements();
			if (children.isEmpty()) {
				if (hasCharacterContent) {
					dtd.addContent(" (#PCDATA)");
				} else {
					dtd.addContent(" EMPTY");
				}
			} else {
				// There are children
				if (!hasCharacterContent) {

					boolean sequenced = element.getChildrenProperties().isSequenced();
					if (sequenced) {
						// All elements have the same child elements in the same sequence
						dtd.addContent(" (");
						boolean first = true;
						for (Map.Entry<String, Cardinality> elementInfo : element.getChildrenProperties()
								.getCardinalities().entrySet()) {
							if (!first) {
								dtd.addContent(",");
							}
							first = false;
							dtd.addContent(elementInfo.getKey());
							Cardinality cardinality = elementInfo.getValue();
							if (cardinality.getMin() == 0 && cardinality.getMax() == 1) {
								// ? 0-1
								dtd.addContent("?");
							} else if (cardinality.getMin() == 0 && cardinality.getMax() > 1) {
								// * 0-n
								dtd.addContent("*");
							} else if (cardinality.getMax() > 1) {
								// + 1-n
								dtd.addContent("+");
							}
						}
						dtd.addContent(")");

					} else {
						// the children don't always appear in the same sequence;
						dtd.addContent(" (");
						boolean first = true;
						for (ElementDeclaration elementInfo : children) {
							if (!first) {
								dtd.addContent("|");
							}
							first = false;
							dtd.addContent(elementInfo.getName());
						}
						dtd.addContent(")*");
					}
				} else {
					// Mixed content
					dtd.addContent("(#PCDATA");
					if (hasCharacterContent) {
						for (ElementDeclaration elementInfo : children) {
							dtd.addContent("|");
							dtd.addContent(elementInfo.getName());
						}
					}
					dtd.addContent(")*");
				}
			}
			dtd.closeStartElement();

			// <!ATTLIST
			Collection<AttributeDeclaration> attributes = element.getAttributes();
			if (!attributes.isEmpty()) {
				dtd.startDTDAttlistDecl();
				dtd.addParameter(element.getName());
				for (AttributeDeclaration attribute : attributes) {
					dtd.addParameter(attribute.getName());
					dtd.addParameter("CDATA");
					dtd.addParameter("#IMPLIED");
				}
				dtd.closeStartElement();
			}
		}
	}

	@Override
	protected String getFileExtension() {
		return "dtd";
	}

	@Override
	protected boolean isFlat() {
		return true;
	}
}
