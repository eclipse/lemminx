/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.generators.xml2relaxng;

import java.util.Collection;
import java.util.Set;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.extensions.generators.AbstractXML2GrammarGenerator;
import org.eclipse.lemminx.extensions.generators.AttributeDeclaration;
import org.eclipse.lemminx.extensions.generators.AttributeDeclaration.DataType;
import org.eclipse.lemminx.extensions.generators.Cardinality;
import org.eclipse.lemminx.extensions.generators.ElementDeclaration;
import org.eclipse.lemminx.extensions.generators.Grammar;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lemminx.utils.XMLBuilder;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * File Generator implementation to generate RelaxNG from a given XML source.
 *
 */
public class XML2RelaxNGGenerator extends AbstractXML2GrammarGenerator<RelaxNGGeneratorSettings> {

	private static final String ELEMENT_ELT = "element";

	private static final String GRAMMAR_ELT = "grammar";

	private static final String START_ELT = "start";

	private static final String REF_ELT = "ref";

	private static final String DEFINE_ELT = "define";

	private static final String NAME_ATTR = "name";

	private static final String OPTIONAL_ELT = "optional";

	private static final String ONE_OR_MORE_ELT = "oneOrMore";

	private static final String TEXT_ELT = "text";

	private static final String CHOICE_ELT = "choice";

	private static final String ATTRIBUTE_ELT = "attribute";

	private static final String VALUE_ELT = "value";

	private static final String DATA_ELT = "data";

	private static final String TYPE_ATTR = "type";

	private static final String EMPTY_ELT = "empty";

	private static final String MIXED_ELT = "mixed";

	private static final String NAMESPACE_ATTR = "ns";

	private static final String DATATYPE_LIBRARY_ATTR = "datatypeLibrary";

	private static final String XML_SCHEMA_DATATYPES = "http://www.w3.org/2001/XMLSchema-datatypes";

	private static final String INTERLEAVE_ELT = "interleave";

	private static final String RELAXNG_NAMESPACE_URI = "http://relaxng.org/ns/structure/1.0";

	@Override
	protected void generate(Grammar grammar, RelaxNGGeneratorSettings settings, XMLBuilder schema,
			CancelChecker cancelChecker) {
		// <grammar xmlns="http://relaxng.org/ns/structure/1.0"
		// datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
		schema.startElement(null, GRAMMAR_ELT, false);
		schema.addSingleAttribute(DOMAttr.XMLNS_ATTR, RELAXNG_NAMESPACE_URI, true);
		String targetNamespace = grammar.getDefaultNamespace();
		if (!StringUtils.isEmpty(targetNamespace)) {
			schema.addSingleAttribute(NAMESPACE_ATTR, targetNamespace, true);
		}
		// add link to XML schema data library
		schema.addSingleAttribute(DATATYPE_LIBRARY_ATTR, XML_SCHEMA_DATATYPES, true);

		schema.closeStartElement();
		ElementDeclaration rootElement = grammar.getElements().stream().findFirst().orElse(null);
		String rootElementName = rootElement == null ? "" : rootElement.getName();
		schema.startElement(null, START_ELT, true);

		schema.startElement(null, REF_ELT, false);
		schema.addSingleAttribute(NAME_ATTR, rootElementName + "Content", true);
		schema.selfCloseElement();

		schema.endElement(null, START_ELT);

		// List of rng:element
		for (ElementDeclaration element : grammar.getElements()) {
			cancelChecker.checkCanceled();
			generateRngElement(schema, null, element, settings, rootElementName.equals(element.getName()),
					rootElementName + "Content", cancelChecker);
		}
		schema.endElement(null, GRAMMAR_ELT);
	}

	private void generateRngElement(XMLBuilder schema, String prefix, ElementDeclaration elementDecl,
			RelaxNGGeneratorSettings settings, boolean isRoot, String parentPatternName, CancelChecker cancelChecker) {
		Collection<ElementDeclaration> children = elementDecl.getElements();
		Collection<AttributeDeclaration> attributes = elementDecl.getAttributes();
		boolean hasChildren = !children.isEmpty();
		boolean hasAttributes = !attributes.isEmpty();
		boolean hasCharacterContent = elementDecl.hasCharacterContent();
		boolean mixedContent = hasChildren && hasCharacterContent;
		boolean isOneOrMore = false;

		String name = elementDecl.getName();
		boolean isOptionalElement = false;

		// <define name="parentPatternName">
		schema.startElement(null, DEFINE_ELT, false);
		schema.addSingleAttribute(NAME_ATTR, parentPatternName, true);
		schema.closeStartElement();

		// <element name="elementName">
		schema.startElement(prefix, ELEMENT_ELT, false);
		schema.addSingleAttribute(NAME_ATTR, name, true);
		schema.closeStartElement();

		if (!hasChildren && !hasAttributes) {
			if (hasCharacterContent) {
				schema.startElement(prefix, TEXT_ELT, false);
				schema.selfCloseElement();
			} else {
				schema.startElement(prefix, EMPTY_ELT, false);
				schema.selfCloseElement();
			}
			schema.endElement(prefix, ELEMENT_ELT);
			schema.endElement(prefix, DEFINE_ELT);
		} else {
			if (mixedContent) {
				schema.startElement(prefix, MIXED_ELT, true);
			} else if (hasCharacterContent) {
				schema.startElement(prefix, TEXT_ELT, false);
				schema.selfCloseElement();
			}
			if (hasChildren) {
				boolean sequenced = elementDecl.getChildrenProperties().isSequenced();
				if (!sequenced) {
					schema.startElement(prefix, INTERLEAVE_ELT, true);
				}
				for (ElementDeclaration child : children) {
					String childName = child.getName();
					Cardinality childCardinality = elementDecl != null
							? elementDecl.getChildrenProperties().getCardinalities().get(childName)
							: null;
					if (childCardinality != null) {
						if (childCardinality.getMin() == 0) {
							isOptionalElement = true;
						} else if (childCardinality.getMax() > 1) {
							isOneOrMore = true;
						}
					}

					if (isOptionalElement) {
						schema.startElement(prefix, OPTIONAL_ELT, true);
					} else if (isOneOrMore) {
						schema.startElement(prefix, ONE_OR_MORE_ELT, true);
					}

					schema.startElement(null, REF_ELT, false);
					schema.addSingleAttribute(NAME_ATTR, childName + "Content", true);
					schema.selfCloseElement();

					if (isOptionalElement) {
						schema.endElement(prefix, OPTIONAL_ELT);
					} else if (isOneOrMore) {
						schema.endElement(prefix, ONE_OR_MORE_ELT, true);
					}
				}
				if (!sequenced) {
					schema.endElement(prefix, INTERLEAVE_ELT);
				}
				if (mixedContent) {
					schema.endElement(prefix, MIXED_ELT);
				}
				if (hasAttributes) {
					generateAttribute(schema, attributes, prefix, settings, cancelChecker);
				}
				schema.endElement(prefix, ELEMENT_ELT);
				schema.endElement(prefix, DEFINE_ELT);

				for (ElementDeclaration child : children) {
					generateRngElement(schema, prefix, child, settings, false, child.getName() + "Content",
							cancelChecker);
				}
			} else if (hasAttributes) {
				generateAttribute(schema, attributes, prefix, settings, cancelChecker);
				schema.endElement(prefix, ELEMENT_ELT);
				schema.endElement(prefix, DEFINE_ELT);
			}
		}
	}

	private void generateAttribute(XMLBuilder schema, Collection<AttributeDeclaration> attributes, String prefix,
			RelaxNGGeneratorSettings settings, CancelChecker cancelChecker) {
		// Generate list of rng:attribute
		for (AttributeDeclaration attribute : attributes) {
			String attrType = getRngType(attribute.getDataType());
			cancelChecker.checkCanceled();

			boolean required = attribute.isRequired();
			boolean fixed = attribute.isFixedValue(settings);
			boolean enums = attribute.isEnums(settings);
			if (!required) {
				// <optional>
				schema.startElement(prefix, OPTIONAL_ELT, true);
			}
			// rng:attribute
			schema.startElement(prefix, ATTRIBUTE_ELT, false);
			schema.addSingleAttribute(NAME_ATTR, attribute.getName(), true);

			if (enums && !fixed) {
				schema.closeStartElement();
				// <rng:choice>
				// <rng:value>A</rng:value>
				// <rng:value>A</rng:value>
				// </rng:choice>
				schema.startElement(prefix, CHOICE_ELT, true);

				Set<String> values = attribute.getValues();
				for (String value : values) {
					cancelChecker.checkCanceled();
					schema.startElement(prefix, VALUE_ELT, true);
					schema.addContent(value);
					schema.endElement(prefix, VALUE_ELT);
				}

				schema.endElement(prefix, CHOICE_ELT);
				schema.endElement(prefix, ATTRIBUTE_ELT);
			} else if (fixed) {
				schema.closeStartElement();
				String value = attribute.getValues().stream().findFirst().orElse(null);
				cancelChecker.checkCanceled();
				schema.startElement(prefix, VALUE_ELT, true);
				schema.addContent(value);
				schema.endElement(prefix, VALUE_ELT);
				schema.endElement(prefix, ATTRIBUTE_ELT);
			} else if (attrType != null) {
				schema.closeStartElement();
				// <rng:data type="decimal"
				schema.startElement(prefix, DATA_ELT, false);
				schema.addSingleAttribute(TYPE_ATTR, attrType, true);
				schema.selfCloseElement();
				schema.endElement(prefix, ATTRIBUTE_ELT);
			} else {
				schema.selfCloseElement();
			}
			if (!required) {
				schema.endElement(prefix, OPTIONAL_ELT);
			}
		}
	}

	private static String getRngType(DataType dataType) {
		switch (dataType) {
			case DATE:
				return "date";
			case DATE_TIME:
				return "dateTime";
			case INTEGER:
				return "integer";
			case DECIMAL:
				return "decimal";
			case BOOLEAN:
				return "boolean";
			default:
				return null;
		}
	}

	@Override
	protected String getFileExtension() {
		return "rng";
	}
}
