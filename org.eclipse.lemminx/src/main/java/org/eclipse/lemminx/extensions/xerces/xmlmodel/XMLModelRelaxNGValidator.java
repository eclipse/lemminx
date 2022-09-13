package org.eclipse.lemminx.extensions.xerces.xmlmodel;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

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
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XMLModelRelaxNGValidator implements XMLModelValidator {

	private static final Logger LOGGER = Logger.getLogger(XMLModelRelaxNGValidator.class.getName());

	private XMLErrorReporter errorReporter;
	private XMLEntityResolver entityResolver;
	private String href;
	private XMLLocator locator;
	private boolean processed = false;
	private VerifierFactory verifierFactory;

	public XMLModelRelaxNGValidator() {
		verifierFactory = new com.sun.msv.verifier.jarv.TheFactoryImpl();
	}

	public static final String ERROR_REPORTER = Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

	protected static final String ENTITY_RESOLVER = Constants.XERCES_PROPERTY_PREFIX
			+ Constants.ENTITY_RESOLVER_PROPERTY;

	@Override
	public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
		// Get error reporter.
		try {
			errorReporter = (XMLErrorReporter) componentManager.getProperty(ERROR_REPORTER);
		} catch (XMLConfigurationException e) {
			errorReporter = null;
		}
		// Get error reporter.
		try {
			entityResolver = (XMLEntityResolver) componentManager.getProperty(ENTITY_RESOLVER);
		} catch (XMLConfigurationException e) {
			entityResolver = null;
		}
	}

	@Override
	public String[] getRecognizedFeatures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFeature(String featureId, boolean state) throws XMLConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getRecognizedProperties() {
		// TODO Auto-generated method stub
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

	}

	@Override
	public void xmlDecl(String version, String encoding, String standalone, Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs)
			throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void comment(XMLString text, Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
		if (!processed) {
			try {
				String expandedLoc = XMLEntityManager.expandSystemId(href, locator.getExpandedSystemId(), false);
				Verifier verifier = verifierFactory.newVerifier(new InputSource(expandedLoc));
				verifier.setErrorHandler(errorReporter.getSAXErrorHandler());
				SAXParserFactory parserFactory = SAXParserFactory.newInstance();
				parserFactory.setNamespaceAware(true);
				XMLReader xmlReader = parserFactory.newSAXParser().getXMLReader();
				xmlReader.setContentHandler(verifier.getVerifierHandler());
				xmlReader.parse(locator.getLiteralSystemId());
				processed = true;
			} catch (VerifierConfigurationException | SAXException | IOException | ParserConfigurationException e) {
				LOGGER.log(Level.SEVERE, "Something went wrong when validating useing relaxng:", e);
			}
		}
	}

	@Override
	public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startGeneralEntity(String name, XMLResourceIdentifier identifier, String encoding, Augmentations augs)
			throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void textDecl(String version, String encoding, Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endGeneralEntity(String name, Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void characters(XMLString text, Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endElement(QName element, Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startCDATA(Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endCDATA(Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endDocument(Augmentations augs) throws XNIException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDocumentSource(XMLDocumentSource source) {
		// TODO Auto-generated method stub

	}

	@Override
	public XMLDocumentSource getDocumentSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDocumentHandler(XMLDocumentHandler handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public XMLDocumentHandler getDocumentHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLocator(XMLLocator locator) {
		this.locator = locator;
	}

	@Override
	public void setHref(String href) {
		this.href = href;
	}

}
