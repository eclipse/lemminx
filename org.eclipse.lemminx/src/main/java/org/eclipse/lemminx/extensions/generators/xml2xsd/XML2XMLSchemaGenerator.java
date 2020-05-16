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
package org.eclipse.lemminx.extensions.generators.xml2xsd;

import java.util.Collection;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.extensions.generators.AbstractXML2GrammarGenerator;
import org.eclipse.lemminx.extensions.generators.AttributeDeclaration;
import org.eclipse.lemminx.extensions.generators.Cardinality;
import org.eclipse.lemminx.extensions.generators.ElementDeclaration;
import org.eclipse.lemminx.extensions.generators.Grammar;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lemminx.utils.XMLBuilder;

/**
 * File Generator implementation to generate XML Schema (XSD) from a given XML
 * source.
 * 
 */
public class XML2XMLSchemaGenerator extends AbstractXML2GrammarGenerator<XMLSchemaGeneratorSettings> {

	private static final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";

	private static final String SCHEMA_TARGET_NAMESPACE_ATTR = "targetNamespace";

	private static final String ELEMENT_TYPE_ATTR = "type";

	private static final String ELEMENT_NAME_ATTR = "name";

	private static final String SEQUENCE_ELT = "sequence";

	private static final String COMPLEX_TYPE_ELT = "complexType";

	private static final String COMPLEX_TYPE_MIXED_ATTR = "mixed";

	private static final String SCHEMA_ELT = "schema";

	private static final String ELEMENT_ELT = "element";

	private static final String ATTRIBUTE_ELT = "attribute";

	private static final String ATTRIBUTE_NAME_ATTR = "name";

	private static final String SIMPLE_CONTENT_ELT = "simpleContent";

	private static final String EXTENSION_ELT = "extension";

	private static final String EXTENSION_BASE_ATTR = "base";

	private static final String MAX_OCCURS_ATTR = "maxOccurs";

	private static final String MAX_OCCURS_UNBOUNDED_VALUE = "unbounded";

	private static final String MIN_OCCURS_ATTR = "minOccurs";

	@Override
	protected void generate(Grammar grammar, XMLSchemaGeneratorSettings settings, XMLBuilder schema) {
		String prefix = "xs";
		schema.startPrologOrPI("xml");
		schema.addContent(" version=\"1.0\" encoding=\"UTF-8\"");
		schema.endPrologOrPI();
		// xs:schema
		schema.startElement(prefix, SCHEMA_ELT, false);
		schema.addSingleAttribute(DOMAttr.XMLNS_ATTR + ':' + prefix, XML_SCHEMA_NS, true);
		String targetNamespace = grammar.getDefaultNamespace();
		if (!StringUtils.isEmpty(targetNamespace)) {
			schema.addSingleAttribute(SCHEMA_TARGET_NAMESPACE_ATTR, targetNamespace, true);
		}
		schema.closeStartElement();
		// List of xs:element
		for (ElementDeclaration element : grammar.getElements()) {
			generateXSElement(schema, prefix, element);
		}
		schema.endElement(prefix, SCHEMA_ELT);
	}

	private void generateXSElement(XMLBuilder schema, String prefix, ElementDeclaration elementDecl) {
		Collection<ElementDeclaration> children = elementDecl.getElements();
		Collection<AttributeDeclaration> attributes = elementDecl.getAttributes();
		boolean hasChildren = !children.isEmpty();
		boolean hasAttributes = !attributes.isEmpty();
		boolean hasCharacterContent = elementDecl.hasCharacterContent();

		String name = elementDecl.getName();
		schema.startElement(prefix, ELEMENT_ELT, false);
		schema.addSingleAttribute(ELEMENT_NAME_ATTR, name, true);
		ElementDeclaration parentDecl = elementDecl.getParent();
		Cardinality cardinality = parentDecl != null ? parentDecl.getChildrenProperties().getCardinalities().get(name)
				: null;
		if (cardinality != null) {
			if (cardinality.getMin() == 0) {
				// <xs:element minOccurs="0"
				schema.addSingleAttribute(MIN_OCCURS_ATTR, "0", true);
			} else if (cardinality.getMax() > 1) {
				// <xs:element maxOccurences="unbounded"
				schema.addSingleAttribute(MAX_OCCURS_ATTR, MAX_OCCURS_UNBOUNDED_VALUE, true);
			}
		}
		if (!hasChildren && !hasAttributes) {
			if (hasCharacterContent) {
				schema.addSingleAttribute(ELEMENT_TYPE_ATTR, prefix + ":string", true);
			}
			schema.selfCloseElement();
		} else {
			schema.closeStartElement();
			schema.startElement(prefix, COMPLEX_TYPE_ELT, false);
			if (hasChildren && hasCharacterContent) {
				// Mixed content
				schema.addSingleAttribute(COMPLEX_TYPE_MIXED_ATTR, "true", true);
			}
			schema.closeStartElement();
			// xs:sequence
			if (hasChildren) {
				schema.startElement(prefix, SEQUENCE_ELT, false);
				if (allChildrenAreOptional(elementDecl.getChildrenProperties().getCardinalities().values())) {
					// all children element are optional
					// --> <xs:sequence minOccurs="0"
					schema.addSingleAttribute(MIN_OCCURS_ATTR, "0", true);
				}
				schema.closeStartElement();
				for (ElementDeclaration child : children) {
					generateXSElement(schema, prefix, child);
				}
				schema.endElement(prefix, SEQUENCE_ELT);
			}

			if (hasAttributes) {
				if (!hasChildren) {
					// <xs:simpleContent>
					// <xs:extension base="xs:string">
					// <xs:attribute name="attr1" type="xs:string" use="required" />
					// </xs:extension>
					// </xs:simpleContent>
					schema.startElement(prefix, SIMPLE_CONTENT_ELT, true);
					schema.startElement(prefix, EXTENSION_ELT, false);
					schema.addSingleAttribute(EXTENSION_BASE_ATTR, prefix + ":string", true);
					schema.closeStartElement();
				}
				// xs:attribute
				for (AttributeDeclaration attribute : attributes) {
					schema.startElement(prefix, ATTRIBUTE_ELT, false);
					schema.addSingleAttribute(ATTRIBUTE_NAME_ATTR, attribute.getName(), true);
					schema.selfCloseElement();
				}
				if (!hasChildren) {
					schema.endElement(prefix, EXTENSION_ELT);
					schema.endElement(prefix, SIMPLE_CONTENT_ELT);
				}
			}
			schema.endElement(prefix, COMPLEX_TYPE_ELT);
			schema.endElement(prefix, ELEMENT_ELT);
		}
	}

	private static boolean allChildrenAreOptional(Collection<Cardinality> values) {
		return values.stream().allMatch(cardinality -> cardinality.getMin() == 0);
	}

	@Override
	protected String getFileExtension() {
		return "xsd";
	}
}
