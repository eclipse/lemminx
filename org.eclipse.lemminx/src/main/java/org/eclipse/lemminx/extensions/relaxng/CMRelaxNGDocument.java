/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.relaxng;

import static org.eclipse.lemminx.extensions.relaxng.RelaxNGConstants.RELAXNG_NAMESPACE;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lemminx.uriresolver.CacheResourcesManager;
import org.eclipse.lemminx.utils.URIUtils;
import org.eclipse.lsp4j.LocationLink;
import org.xml.sax.SAXException;

import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.util.ExpressionWalker;
import com.sun.msv.reader.util.GrammarLoader;

public class CMRelaxNGDocument implements CMDocument {

	private final Grammar grammar;

	private transient List<CMElementDeclaration> elementDeclarations;

	public CMRelaxNGDocument(String key) throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
		if (URIUtils.isFileResource(key) && Files.exists(Paths.get(new URI(key)))) {
			grammar = GrammarLoader.loadSchema(key);
		} else {
			Path path = CacheResourcesManager.getResourceCachePath(key);
			grammar = GrammarLoader.loadSchema(path.toUri().toString());
		}
	}

	@Override
	public boolean hasNamespace(String namespaceURI) {
		return RELAXNG_NAMESPACE.equals(namespaceURI);
	}

	@Override
	public Collection<CMElementDeclaration> getElements() {
		collectElementsIfNeeded();
		return elementDeclarations;
	}

	@Override
	public CMElementDeclaration findCMElement(DOMElement element, String namespace) {
		collectElementsIfNeeded();

		// copied from XSD support
		List<DOMElement> paths = new ArrayList<>();
		while (element != null && (namespace == null || namespace.equals(element.getNamespaceURI()))) {
			paths.add(0, element);
			element = element.getParentNode() instanceof DOMElement ? (DOMElement) element.getParentNode() : null;
		}
		CMElementDeclaration declaration = null;
		for (int i = 0; i < paths.size(); i++) {
			DOMElement elt = paths.get(i);
			if (i == 0) {
				declaration = findElementDeclaration(elt.getLocalName(), namespace);
			} else {
				declaration = declaration.findCMElement(elt.getLocalName(), namespace);
			}
			if (declaration == null) {
				break;
			}
		}
		return declaration;
	}

	private CMElementDeclaration findElementDeclaration(String localName, String namespace) {
		for (CMElementDeclaration elementDeclaration : elementDeclarations) {
			if (elementDeclaration.getName().equals(localName)
			// FIXME: get namespaces working, then uncomment this line
			// && Objects.equals(element.getNamespaceURI(), elementDeclaration.getNamespace())
			) {
				return elementDeclaration;
			}
		}
		return null;
	}

	@Override
	public LocationLink findTypeLocation(DOMNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDirty() {
		// TODO: manage correctly
		return true;
	}

	private void collectElementsIfNeeded() {
		if (elementDeclarations != null) {
			return;
		}
		elementDeclarations = new ArrayList<>();
		grammar.getTopLevel().visit(new ExpressionWalker() {
			@Override
			public void onElement(ElementExp exp) {
				elementDeclarations.add(new CMRelaxNGElementDeclaration(exp));
				// do not recurse
			}
		});
	}

}
