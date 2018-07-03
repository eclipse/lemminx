package org.eclipse.xml.languageserver.xsd;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xs.XSModel;
import org.eclipse.xml.languageserver.contentmodel.CMDocument;
import org.eclipse.xml.languageserver.contentmodel.CMElement;
import org.eclipse.xml.languageserver.model.Node;
import org.eclipse.xml.languageserver.model.SchemaLocation;
import org.eclipse.xml.languageserver.model.XMLDocument;

public class XMLSchemaManager {

	private static final XMLSchemaManager INSTANCE = new XMLSchemaManager();

	public static XMLSchemaManager getInstance() {
		return INSTANCE;
	}

	private final XMLSchemaLoader loader;
	private Map<URI, CMDocument> modelsCache;

	public XMLSchemaManager() {
		loader = new XMLSchemaLoader();
		modelsCache = new HashMap<>();
	}

	public CMDocument getCMDocument(URI uri) {
		CMDocument cmDocument = modelsCache.get(uri);
		if (cmDocument == null) {
			XSModel model = loader.loadURI(uri.toString());
			cmDocument = new XSDDocument(model);
			modelsCache.put(uri, cmDocument);
		}
		return cmDocument;
	}

	public CMElement findCMElement(Node element) throws Exception {
		XMLDocument xmlDocument = element.getOwnerDocument();
		SchemaLocation schemaLocation = xmlDocument.getSchemaLocation();
		if (schemaLocation == null) {
			return null;
		}
		String namespaceURI = xmlDocument.getNamespaceURI();
		String schemaURI = schemaLocation.getLocationHint(namespaceURI);
		if (schemaURI == null) {
			return null;
		}

		URI uri = /* new File("maven-4.0.0.xsd").toURI(); // */ new URI(schemaURI);
		CMDocument cmDocument = getCMDocument(uri);
		return cmDocument.findCMElement(element);
	}

}