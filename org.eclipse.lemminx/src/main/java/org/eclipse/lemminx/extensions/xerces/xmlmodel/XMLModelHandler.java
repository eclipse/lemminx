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
package org.eclipse.lemminx.extensions.xerces.xmlmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.eclipse.lemminx.utils.StringUtils;
import org.xml.sax.XMLReader;

/**
 * Xerces component which associates a XML with several grammar (XML Schema,
 * DTD, ..) by using &lt;?xml-model ?&gt; processing instruction.
 * 
 * <pre>
 * 	&lt;?xml-model href="http://www.docbook.org/xml/5.0/xsd/docbook.xsd"?&gt;
 *	&lt;book xmlns="http://docbook.org/ns/docbook"&gt;
 *		&lt;title /&gt;
 *	&lt;/book&gt;
 * </pre>
 * 
 * @see https://www.w3.org/TR/xml-model/
 *
 */
public class XMLModelHandler implements XMLComponent, XMLDocumentFilter {

	private static final Logger LOGGER = Logger.getLogger(XMLModelHandler.class.getName());

	private static final String VALIDATION = Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;

	private static final String PARSER_SETTINGS = Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;

	private static final List<XMLModelValidatorFactory> MODEL_VALIDATOR_FACTORIES;

	static {
		MODEL_VALIDATOR_FACTORIES = new ArrayList<>();
		Iterator<XMLModelValidatorFactory> factories = ServiceLoader.load(XMLModelValidatorFactory.class).iterator();
		while (factories.hasNext()) {
			try {
				MODEL_VALIDATOR_FACTORIES.add(factories.next());
			} catch (ServiceConfigurationError e) {
				LOGGER.log(Level.SEVERE, "Error while instantiating xml model validator factory", e);
			}
		}
	}

	private List<XMLModelValidator> xmlModelValidators;

	private XMLComponentManager configuration;

	private XMLDocumentHandler documentHandler;

	private XMLDocumentSource documentSource;

	private XMLLocator locator;

	private XMLReader xmlReader;

	public XMLModelHandler() {
	}

	@Override
	public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
		// XML model validators uses Xerces XMLDTDValidator (for DTD) and
		// XMLSchemaValidator (for XML Schema).
		// Those validators are created when a xml-model processing instruction is
		// parsed and not before the XML parse (which is the case for standard DTD
		// DOCTYPE or xsi:schemaLocation)

		// That's why we need to force some features,by wrapping the existing Xerces
		// configuration to force to true the features:
		// - http://apache.org/xml/features/internal/parser-settings
		// - http://xml.org/sax/features/validation
		configuration = new XMLComponentManager() {

			@Override
			public Object getProperty(String propertyId) throws XMLConfigurationException {
				return componentManager.getProperty(propertyId);
			}

			@Override
			public boolean getFeature(String featureId) throws XMLConfigurationException {
				if (PARSER_SETTINGS.equals(featureId)) {
					return true;
				}
				if (VALIDATION.equals(featureId)) {
					return true;
				}
				return componentManager.getFeature(featureId);
			}
		};
	}

	@Override
	public void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException {
		if (XMLModelConstants.XML_MODEL_PI.equals(target)) {
			XMLModelDeclaration model = XMLModelDeclaration.parse(data);
			XMLModelValidator validator = createValidator(model);
			if (validator != null) {
				validator.reset(configuration);
				if (xmlReader != null) {
					validator.setXMLReader(xmlReader);
				}
				if (xmlModelValidators == null) {
					xmlModelValidators = new ArrayList<>();
				}
				xmlModelValidators.add(validator);
			}
		}

		if (documentHandler != null) {
			documentHandler.processingInstruction(target, data, augs);
		}
	}

	private XMLModelValidator createValidator(XMLModelDeclaration modelDeclaration) {
		String href = modelDeclaration.getHref();
		if (StringUtils.isEmpty(href)) {
			return null;
		}
		for (XMLModelValidatorFactory factory : MODEL_VALIDATOR_FACTORIES) {
			XMLModelValidator validator = factory.createValidator(modelDeclaration);
			if (validator != null) {
				return validator;
			}
		}
		return null;
	}

	@Override
	public void startDocument(XMLLocator locator, String encoding, NamespaceContext namespaceContext,
			Augmentations augs) throws XNIException {
		this.locator = locator;
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.startDocument(locator, encoding, namespaceContext, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.startDocument(locator, encoding, namespaceContext, augs);
		}
	}

	@Override
	public void xmlDecl(String version, String encoding, String standalone, Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.xmlDecl(version, encoding, standalone, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.xmlDecl(version, encoding, standalone, augs);
		}
	}

	@Override
	public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs)
			throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.doctypeDecl(rootElement, publicId, systemId, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.doctypeDecl(rootElement, publicId, systemId, augs);
		}
	}

	@Override
	public void comment(XMLString text, Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.comment(text, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.comment(text, augs);
		}
	}

	@Override
	public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.setLocator(locator);
				if (documentHandler instanceof XMLReader) {
					validator.setXMLReader((XMLReader) documentHandler);
				}
				validator.startElement(element, attributes, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.startElement(element, attributes, augs);
		}
	}

	@Override
	public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.emptyElement(element, attributes, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.emptyElement(element, attributes, augs);
		}
	}

	@Override
	public void startGeneralEntity(String name, XMLResourceIdentifier identifier, String encoding, Augmentations augs)
			throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.startGeneralEntity(name, identifier, encoding, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.startGeneralEntity(name, identifier, encoding, augs);
		}
	}

	@Override
	public void textDecl(String version, String encoding, Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.textDecl(version, encoding, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.textDecl(version, encoding, augs);
		}
	}

	@Override
	public void endGeneralEntity(String name, Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.endGeneralEntity(name, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.endGeneralEntity(name, augs);
		}
	}

	@Override
	public void characters(XMLString text, Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.characters(text, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.characters(text, augs);
		}
	}

	@Override
	public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.ignorableWhitespace(text, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.ignorableWhitespace(text, augs);
		}
	}

	@Override
	public void endElement(QName element, Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.endElement(element, augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.endElement(element, augs);
		}
	}

	@Override
	public void startCDATA(Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.startCDATA(augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.startCDATA(augs);
		}
	}

	@Override
	public void endCDATA(Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.endCDATA(augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.endCDATA(augs);
		}
	}

	@Override
	public void endDocument(Augmentations augs) throws XNIException {
		if (xmlModelValidators != null) {
			for (XMLModelValidator validator : xmlModelValidators) {
				validator.endDocument(augs);
			}
		}

		if (documentHandler != null) {
			documentHandler.endDocument(augs);
		}
	}

	@Override
	public void setDocumentSource(XMLDocumentSource source) {
		documentSource = source;
	}

	@Override
	public XMLDocumentSource getDocumentSource() {
		return documentSource;
	}

	@Override
	public void setDocumentHandler(XMLDocumentHandler handler) {
		documentHandler = handler;
		if (documentHandler instanceof XMLReader) {
			this.xmlReader = (XMLReader) documentHandler;
		}
	}

	@Override
	public XMLDocumentHandler getDocumentHandler() {
		return documentHandler;
	}

	@Override
	public String[] getRecognizedFeatures() {
		return null;
	}

	@Override
	public void setFeature(String featureId, boolean state) throws XMLConfigurationException {

	}

	@Override
	public String[] getRecognizedProperties() {
		return null;
	}

	@Override
	public void setProperty(String propertyId, Object value) throws XMLConfigurationException {

	}

	@Override
	public Boolean getFeatureDefault(String featureId) {
		return null;
	}

	@Override
	public Object getPropertyDefault(String propertyId) {
		return null;
	}

}
