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
package org.eclipse.lemminx.extensions.relaxng.xml.validator;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.eclipse.lemminx.extensions.relaxng.RelaxNGConstants;
import org.eclipse.lemminx.extensions.relaxng.jing.SchemaProvider;
import org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelAwareParserConfiguration;
import org.xml.sax.XMLReader;

import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;

/**
 * RelaxNG external validator.
 * 
 * @author Angelo ZERR
 *
 */
public class ExternalRelaxNGValidator implements XMLComponent, XMLDocumentFilter {

	public final static String RELAXNG = "http://apache.org/xml/properties/relaxng/external-relaxng"; //$NON-NLS-1$ ;

	private XMLErrorReporter errorReporterForXML;

	private XMLErrorReporter errorReporterForGrammar;

	private XMLLocator locator;

	private String externalRelaxNG;

	private boolean processed;

	private XMLEntityResolver entityResolver;

	private XMLReader xmlReader;

	private XMLGrammarPool grammarPool;

	private XMLDocumentHandler documentHandler;

	private XMLDocumentSource documentSource;

	public static final String ERROR_REPORTER = Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

	protected static final String ENTITY_RESOLVER = Constants.XERCES_PROPERTY_PREFIX
			+ Constants.ENTITY_RESOLVER_PROPERTY;

	/** Property identifier: grammar pool. */
	protected static final String XMLGRAMMAR_POOL = Constants.XERCES_PROPERTY_PREFIX
			+ Constants.XMLGRAMMAR_POOL_PROPERTY;

	public void startDocument(XMLLocator locator, String encoding, NamespaceContext namespaceContext,
			Augmentations augs) throws XNIException {
		setLocator(locator);
		if (documentHandler != null) {
			documentHandler.startDocument(locator, encoding, namespaceContext, augs);
		}
	}

	@Override
	public void xmlDecl(String version, String encoding, String standalone, Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.xmlDecl(version, encoding, standalone, augs);
		}
	}

	@Override
	public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs)
			throws XNIException {
		if (documentHandler != null) {
			documentHandler.doctypeDecl(rootElement, publicId, systemId, augs);
		}
	}

	@Override
	public void comment(XMLString text, Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.comment(text, augs);
		}
	}

	@Override
	public void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.processingInstruction(target, data, augs);
		}
	}

	@Override
	public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
		if (!processed) {
			try {
				if (externalRelaxNG != null && xmlReader != null) {
					String location = null;
					try {
						location = XMLEntityManager.expandSystemId(externalRelaxNG, locator.getBaseSystemId(), false);
						Schema schema = SchemaProvider.getSchema(externalRelaxNG, locator.getBaseSystemId(),
								entityResolver, errorReporterForGrammar, grammarPool);
						SchemaProvider.validate(schema, xmlReader, errorReporterForXML);
					} catch (IncorrectSchemaException e) {
						// ignore the error.
					} catch (Exception e) {
						errorReporterForXML.reportError(NULL_LOCATOR, RelaxNGConstants.RELAX_NG_DOMAIN,
								RelaxNGErrorCode.RelaxNGNotFound.getCode(), new Object[] { null, location },
								XMLErrorReporter.SEVERITY_ERROR, e);
					}
				}
			} finally {
				processed = true;
			}
		}
		if (documentHandler != null) {
			documentHandler.startElement(element, attributes, augs);
		}
	}

	public void setExternalRelaxNG(String externalRelaxNG) {
		this.externalRelaxNG = externalRelaxNG;
	}

	public void setLocator(XMLLocator locator) {
		this.locator = locator;
	}

	public void setXMLReader(XMLReader xmlReader) {
		this.xmlReader = xmlReader;
	}

	@Override
	public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
		// get external RelaxNG
		try {
			setExternalRelaxNG((String) componentManager.getProperty(RELAXNG));
		} catch (XMLConfigurationException e) {
			setExternalRelaxNG(null);
		}
		// Get error reporter for XML.
		try {
			errorReporterForXML = (XMLErrorReporter) componentManager.getProperty(ERROR_REPORTER);
		} catch (XMLConfigurationException e) {
			errorReporterForXML = null;
		}
		// Get error reporter for Grammar.
		try {
			errorReporterForGrammar = (XMLErrorReporter) componentManager
					.getProperty(XMLModelAwareParserConfiguration.ERROR_REPORTER_FOR_GRAMMAR);
		} catch (XMLConfigurationException e) {
			errorReporterForGrammar = null;
		}
		// Get error reporter.
		try {
			entityResolver = (XMLEntityResolver) componentManager.getProperty(ENTITY_RESOLVER);
		} catch (XMLConfigurationException e) {
			entityResolver = null;
		}
		// Get grammar pool.
		try {
			grammarPool = (XMLGrammarPool) componentManager.getProperty(XMLGRAMMAR_POOL);
		} catch (XMLConfigurationException e) {
			grammarPool = null;
		}

	}

	@Override
	public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.emptyElement(element, attributes, augs);
		}
	}

	@Override
	public void startGeneralEntity(String name, XMLResourceIdentifier identifier, String encoding, Augmentations augs)
			throws XNIException {
		if (documentHandler != null) {
			documentHandler.startGeneralEntity(name, identifier, encoding, augs);
		}
	}

	@Override
	public void textDecl(String version, String encoding, Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.textDecl(version, encoding, augs);
		}
	}

	@Override
	public void endGeneralEntity(String name, Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.endGeneralEntity(name, augs);
		}
	}

	@Override
	public void characters(XMLString text, Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.characters(text, augs);
		}
	}

	@Override
	public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.ignorableWhitespace(text, augs);
		}
	}

	@Override
	public void endElement(QName element, Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.endElement(element, augs);
		}
	}

	@Override
	public void startCDATA(Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.startCDATA(augs);
		}
	}

	@Override
	public void endCDATA(Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.endCDATA(augs);
		}
	}

	@Override
	public void endDocument(Augmentations augs) throws XNIException {
		if (documentHandler != null) {
			documentHandler.endDocument(augs);
		}
	}

	@Override
	public XMLDocumentSource getDocumentSource() {
		return documentSource;
	}

	@Override
	public void setDocumentSource(XMLDocumentSource source) {
		documentSource = source;
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

	private static final XMLLocator NULL_LOCATOR = new XMLLocator() {

		@Override
		public String getXMLVersion() {
			return null;
		}

		@Override
		public String getPublicId() {
			return null;
		}

		@Override
		public String getLiteralSystemId() {
			return null;
		}

		@Override
		public int getLineNumber() {
			return 0;
		}

		@Override
		public String getExpandedSystemId() {
			return null;
		}

		@Override
		public String getEncoding() {
			return null;
		}

		@Override
		public int getColumnNumber() {
			return 0;
		}

		@Override
		public int getCharacterOffset() {
			return 0;
		}

		@Override
		public String getBaseSystemId() {
			return null;
		}
	};
}
