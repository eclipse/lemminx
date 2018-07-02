package org.eclipse.xml.languageserver.xsd;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.xml.languageserver.model.Node;
import org.eclipse.xml.languageserver.model.SchemaLocation;
import org.eclipse.xml.languageserver.model.XMLDocument;

public class XMLSchemaManager {

	private static final XMLSchemaManager INSTANCE = new XMLSchemaManager();

	public static XMLSchemaManager getInstance() {
		return INSTANCE;
	}

	private final XMLSchemaLoader loader;
	private Map<URI, XSModel> modelsCache;

	public XMLSchemaManager() {
		loader = new XMLSchemaLoader();
		modelsCache = new HashMap<>();
	}

	public XSModel getXSModel(URI uri) {
		XSModel model = modelsCache.get(uri);
		if (model == null) {
			model = loader.loadURI(uri.toString());
			modelsCache.put(uri, model);
		}
		return model;
	}

	public XSElementDeclaration findElementDeclaration(Node element) throws Exception {
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

		URI uri = new URI(schemaURI);
		XSModel model = XMLSchemaManager.getInstance().getXSModel(uri);
		return findElementDeclaration(element, model);
	}

	private static XSElementDeclaration findElementDeclaration(Node node, XSModel model) {
		String tagName = node.tag;
		String namespace = node.getOwnerDocument().getNamespaceURI();
		List<Node> paths = new ArrayList<>();
		Node element = node;
		while (element != null && !(element instanceof XMLDocument)) {
			paths.add(0, element);
			element = element.parent;
		}
		XSElementDeclaration declaration = null;
		for (int i = 0; i < paths.size(); i++) {
			Node elt = paths.get(i);
			if (i == 0) {
				declaration = model.getElementDeclaration(elt.tag, namespace);
			} else {
				declaration = findElementDeclaration(elt.tag, namespace, declaration);
			}
			if (declaration == null) {
				break;
			}
		}
		return declaration;
	}

	private static XSElementDeclaration findElementDeclaration(String tag, String namespace,
			XSElementDeclaration elementDecl) {
		XSTypeDefinition typeDefinition = elementDecl.getTypeDefinition();
		switch (typeDefinition.getTypeCategory()) {
		case XSTypeDefinition.SIMPLE_TYPE:
			return null;
		case XSTypeDefinition.COMPLEX_TYPE:
			return findElementDeclaration(tag, namespace, ((XSComplexTypeDefinition) typeDefinition));
		}
		return null;
	}

	private static XSElementDeclaration findElementDeclaration(String tag, String namespace,
			XSComplexTypeDefinition typeDefinition) {
		XSParticle particle = typeDefinition.getParticle();
		if (particle != null) {
			return findElementDeclaration(tag, namespace, particle.getTerm());
		}
		return null;
	}

	private static XSElementDeclaration findElementDeclaration(String tag, String namespace, XSTerm term) {
		switch (term.getType()) {
		case XSConstants.MODEL_GROUP:
			XSObjectList particles = ((XSModelGroup) term).getParticles();
			for (Object pp : particles) {
				XSElementDeclaration e = findElementDeclaration(tag, namespace, ((XSParticle) pp).getTerm());
				if (e != null && tag.equals(e.getName())) {
					return e;
				}
			}
			return null;
		case XSConstants.ELEMENT_DECLARATION:
			return (XSElementDeclaration) term;
		}
		return null;
	}

	public String generate(XSElementDeclaration elementDeclaration, Node currentNode) {
		StringBuilder xml = new StringBuilder();
		generate(elementDeclaration, xml);
		return xml.toString();
	}

	private void generate(XSElementDeclaration elementDeclaration, StringBuilder xml) {
		xml.append("<");
		xml.append(elementDeclaration.getName());
		xml.append(">");
		XSTypeDefinition typeDefinition = elementDeclaration.getTypeDefinition();
		switch (typeDefinition.getTypeCategory()) {
		case XSTypeDefinition.SIMPLE_TYPE:
			generate(((XSSimpleTypeDefinition) typeDefinition), xml);
			break;
		case XSTypeDefinition.COMPLEX_TYPE:
			generate(((XSComplexTypeDefinition) typeDefinition), xml);
			break;
		}
		xml.append("</");
		xml.append(elementDeclaration.getName());
		xml.append(">");
	}

	private void generate(XSSimpleTypeDefinition typeDefinition, StringBuilder xml) {

	}

	private void generate(XSComplexTypeDefinition complexType, StringBuilder xml) {
		XSObjectList attributeUses = complexType.getAttributeUses();
		if (!(attributeUses.isEmpty())) {
			for (int i = 0; i < attributeUses.getLength(); i++) {
				XSAttributeUse attrUse = (XSAttributeUse) attributeUses.get(i);
				if (attrUse.getRequired()) {
					xml.append(" ");
					xml.append(attrUse.getName());
					xml.append("=\"");
					xml.append("\"");
				}
			}
		}
		XSParticle particle = complexType.getParticle();
		if (particle != null) {
			generate(particle.getTerm(), xml);
		}
	}

	private void generate(XSTerm term, StringBuilder xml) {
		switch (term.getType()) {
		case XSConstants.MODEL_GROUP:
			XSObjectList particles = ((XSModelGroup) term).getParticles();
			for (Object particle : particles) {
				generate(((XSParticle) particle).getTerm(), xml);
			}
			break;
		case XSConstants.ELEMENT_DECLARATION:
			generate((XSElementDeclaration) term, xml);
		}
	}
}