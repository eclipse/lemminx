package org.eclipse.lemminx.extensions.relaxng.jing;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.thaiopensource.xml.sax.XMLReaderCreator;

public class RelaxNGXMLReaderCreator implements XMLReaderCreator {

	private final SAXParserFactory factory;

	/**
	 * Default constructor.
	 */
	public RelaxNGXMLReaderCreator(boolean xIncludeEnabled) {
		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		factory.setXIncludeAware(xIncludeEnabled);
	}

	public XMLReader createXMLReader() throws SAXException {
		try {
			return factory.newSAXParser().getXMLReader();
		} catch (ParserConfigurationException e) {
			throw new SAXException(e);
		}
	}
}
