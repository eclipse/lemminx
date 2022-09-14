package org.eclipse.lemminx.extensions.xerces.xmlmodel;

import java.io.IOException;

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
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class NewXMLModelRelaxNGValidator implements XMLModelValidator {

	private static final VerifierFactory VERIFIER_FACTORY = new com.sun.msv.verifier.jarv.TheFactoryImpl();
	public static final String ERROR_REPORTER = Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

	private String href;
	private VerifierHandler verifierHandler;
	private XMLLocator locator;
	private XMLErrorReporter errorReporter;

	private void createVerifier() throws SAXException {
		try {
			String expandedLoc = XMLEntityManager.expandSystemId(href, locator.getBaseSystemId(), false);
			Verifier verifier = VERIFIER_FACTORY.newVerifier(expandedLoc);
			verifier.setErrorHandler(errorReporter.getSAXErrorHandler());
			verifierHandler = verifier.getVerifierHandler();
			verifierHandler.startDocument();
		} catch (VerifierConfigurationException | IOException e) {
			// TODO: log severe
		}
	}

	@Override
	public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
		try {
			errorReporter = (XMLErrorReporter) componentManager.getProperty(ERROR_REPORTER);
		} catch (XMLConfigurationException e) {
			errorReporter = null;
		}
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
		// TODO Auto-generated method stub

	}

	@Override
	public Boolean getFeatureDefault(String featureId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPropertyDefault(String propertyId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startDocument(XMLLocator locator, String encoding, NamespaceContext namespaceContext,
			Augmentations augs) throws XNIException {
		try {
			if (verifierHandler == null) {
				createVerifier();
			}
			verifierHandler.startDocument();
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	@Override
	public void xmlDecl(String version, String encoding, String standalone, Augmentations augs) throws XNIException {
	}

	@Override
	public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs)
			throws XNIException {
	}

	@Override
	public void comment(XMLString text, Augmentations augs) throws XNIException {
	}

	@Override
	public void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException {
		try {
			verifierHandler.processingInstruction(target, data.toString());
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	@Override
	public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
		try {
			if (verifierHandler == null) {
				createVerifier();
			}
			verifierHandler.startElement(element.uri, element.localpart, element.rawname,
					new AttributesWrapper(attributes));
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	@Override
	public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
	}

	@Override
	public void startGeneralEntity(String name, XMLResourceIdentifier identifier, String encoding, Augmentations augs)
			throws XNIException {
	}

	@Override
	public void textDecl(String version, String encoding, Augmentations augs) throws XNIException {
	}

	@Override
	public void endGeneralEntity(String name, Augmentations augs) throws XNIException {
	}

	@Override
	public void characters(XMLString text, Augmentations augs) throws XNIException {
		try {
			verifierHandler.characters(text.ch, text.offset, text.length);
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	@Override
	public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
		try {
			verifierHandler.ignorableWhitespace(text.ch, text.offset, text.length);
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	@Override
	public void endElement(QName element, Augmentations augs) throws XNIException {
		try {
			verifierHandler.endElement(element.uri, element.localpart, element.rawname);
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	@Override
	public void startCDATA(Augmentations augs) throws XNIException {
	}

	@Override
	public void endCDATA(Augmentations augs) throws XNIException {
	}

	@Override
	public void endDocument(Augmentations augs) throws XNIException {
		try {
			verifierHandler.endDocument();
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	@Override
	public void setDocumentSource(XMLDocumentSource source) {
		// do nothing
	}

	@Override
	public XMLDocumentSource getDocumentSource() {
		return null;
	}

	@Override
	public void setDocumentHandler(XMLDocumentHandler handler) {
		// do nothing
	}

	@Override
	public XMLDocumentHandler getDocumentHandler() {
		return this; // TODO: ??
	}

	@Override
	public void setLocator(XMLLocator locator) {
		this.locator = locator;
	}

	@Override
	public void setHref(String href) {
		this.href = href;
	}

	private final class AttributesWrapper implements Attributes {
		private final XMLAttributes attributes;

		private AttributesWrapper(XMLAttributes attributes) {
			this.attributes = attributes;
		}

		@Override
		public int getLength() {
			return attributes.getLength();
		}

		@Override
		public String getURI(int index) {
			return attributes.getURI(index);
		}

		@Override
		public String getLocalName(int index) {
			return attributes.getLocalName(index);
		}

		@Override
		public String getQName(int index) {
			return attributes.getQName(index);
		}

		@Override
		public String getType(int index) {
			return attributes.getType(index);
		}

		@Override
		public String getValue(int index) {
			return attributes.getValue(index);
		}

		@Override
		public int getIndex(String uri, String localName) {
			return attributes.getIndex(uri, localName);
		}

		@Override
		public int getIndex(String qName) {
			return attributes.getIndex(qName);
		}

		@Override
		public String getType(String uri, String localName) {
			return attributes.getType(uri, localName);
		}

		@Override
		public String getType(String qName) {
			return attributes.getType(qName);
		}

		@Override
		public String getValue(String uri, String localName) {
			return attributes.getValue(uri, localName);
		}

		@Override
		public String getValue(String qName) {
			return attributes.getValue(qName);
		}
	}

}
