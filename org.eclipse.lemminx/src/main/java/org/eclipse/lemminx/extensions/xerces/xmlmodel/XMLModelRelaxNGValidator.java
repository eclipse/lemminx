package org.eclipse.lemminx.extensions.xerces.xmlmodel;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics.RNGErrorCode;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.sun.msv.verifier.ValidityViolation;

public class XMLModelRelaxNGValidator implements XMLModelValidator {

	private static final Logger LOGGER = Logger.getLogger(XMLModelRelaxNGValidator.class.getName());
	private static final VerifierFactory VERIFIER_FACTORY = new com.sun.msv.verifier.jarv.TheFactoryImpl();
	public static final String ERROR_REPORTER = Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

	private String href;
	private Interceptor interceptor;
	private XMLLocator locator;
	private XMLErrorReporter errorReporter;

	private void createVerifier() throws SAXException {
		try {
			String expandedLoc = XMLEntityManager.expandSystemId(href, locator.getBaseSystemId(), false);
			Verifier verifier = VERIFIER_FACTORY.newVerifier(expandedLoc);
			verifier.setErrorHandler(com.sun.msv.verifier.util.ErrorHandlerImpl.theInstance);
			VerifierHandler verifierHandler = verifier.getVerifierHandler();
			interceptor = new Interceptor();
			interceptor.setErrorHandler(errorReporter.getSAXErrorHandler());
			interceptor.setContentHandler(verifierHandler);
			interceptor.startDocument();
		} catch (VerifierConfigurationException vce) {
			errorReporter.reportError("https://relaxng.org", RNGErrorCode.InvalidRelaxNG.getCode(), new Object[] {},
					XMLErrorReporter.SEVERITY_FATAL_ERROR);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to create RelaxNG validator: ", e);
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
	}

	@Override
	public Boolean getFeatureDefault(String featureId) {
		return null;
	}

	@Override
	public Object getPropertyDefault(String propertyId) {
		return null;
	}

	@Override
	public void startDocument(XMLLocator locator, String encoding, NamespaceContext namespaceContext,
			Augmentations augs) throws XNIException {
		try {
			if (interceptor == null) {
				createVerifier();
			}
			interceptor.startDocument();
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
			interceptor.processingInstruction(target, data.toString());
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	@Override
	public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
		try {
			if (interceptor == null) {
				createVerifier();
			}
			interceptor.startElement(element.uri, element.localpart, element.rawname,
					new AttributesWrapper(attributes));
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	@Override
	public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
		try {
			interceptor.startElement(element.uri, element.localpart, element.rawname, new AttributesWrapper(attributes));
			interceptor.endElement(element.uri, element.localpart, element.rawname);
		} catch (SAXException e) {
			throw new XNIException(e);
		}
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
			interceptor.characters(text.ch, text.offset, text.length);
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	@Override
	public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
		try {
			interceptor.ignorableWhitespace(text.ch, text.offset, text.length);
		} catch (SAXException e) {
			throw new XNIException(e);
		}
	}

	@Override
	public void endElement(QName element, Augmentations augs) throws XNIException {
		try {
			interceptor.endElement(element.uri, element.localpart, element.rawname);
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
			interceptor.endDocument();
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

	private final class LocatorWrapper implements Locator {

		private final XMLLocator xmlLocator;

		public LocatorWrapper(XMLLocator xmlLocator) {
			this.xmlLocator = xmlLocator;
		}

		@Override
		public String getPublicId() {
			return xmlLocator.getPublicId();
		}

		@Override
		public String getSystemId() {
			return xmlLocator.getLiteralSystemId();
		}

		@Override
		public int getLineNumber() {
			return xmlLocator.getLineNumber();
		}

		@Override
		public int getColumnNumber() {
			return xmlLocator.getColumnNumber();
		}
	}

	/**
	 * Please refer to the MSV demo:
	 *
	 * https://github.com/xmlark/msv/blob/main/msv/examples/errorinfo/ErrorReporter.java
	 */
	private static class Interceptor extends XMLFilterImpl {

		private static final ErrorHandler IGNORE_ERROR_HANDLER = new ErrorHandler() {

			@Override
			public void warning(SAXParseException exception) throws SAXException {
			}

			@Override
			public void error(SAXParseException exception) throws SAXException {
			}

			@Override
			public void fatalError(SAXParseException exception) throws SAXException {
			}

		};

		public Interceptor() {
			super();
			setErrorHandler(IGNORE_ERROR_HANDLER);
		}

		@Override
		public void startElement(
				String ns, String local, String qname, Attributes atts)
				throws SAXException {
			try {
				super.startElement(ns, local, qname, atts);
			} catch (ValidityViolation vv) {
				this.getErrorHandler().error(vv);
			}
		}

		@Override
		public void endElement(String ns, String local, String qname) throws SAXException {
			try {
				super.endElement(ns, local, qname);
			} catch (ValidityViolation vv) {
				this.getErrorHandler().error(vv);
			}
		}
	}

}
