/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.contentmodel.xsd;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.lsp4xml.contentmodel.CMAttribute;
import org.eclipse.lsp4xml.contentmodel.CMElement;

/**
 * XSD element declaration implementation.
 *
 */
public class XSDElement implements CMElement {

	private final XSDDocument document;

	private final XSElementDeclaration elementDeclaration;

	private Collection<CMAttribute> attributes;

	private Collection<CMElement> elements;

	public XSDElement(XSDDocument document, XSElementDeclaration elementDeclaration) {
		this.document = document;
		this.elementDeclaration = elementDeclaration;
	}

	@Override
	public String getName() {
		return elementDeclaration.getName();
	}

	@Override
	public Collection<CMAttribute> getAttributes() {
		if (attributes == null) {
			attributes = new ArrayList<>();
			collectAttributesDeclaration(elementDeclaration, attributes);
		}
		return attributes;
	}

	private void collectAttributesDeclaration(XSElementDeclaration elementDecl, Collection<CMAttribute> attributes) {
		XSTypeDefinition typeDefinition = elementDecl.getTypeDefinition();
		switch (typeDefinition.getTypeCategory()) {
		case XSTypeDefinition.SIMPLE_TYPE:
			// TODO...
			break;
		case XSTypeDefinition.COMPLEX_TYPE:
			collectAttributesDeclaration((XSComplexTypeDefinition) typeDefinition, attributes);
			break;
		}
	}

	private void collectAttributesDeclaration(XSComplexTypeDefinition typeDefinition,
			Collection<CMAttribute> attributes) {
		XSParticle particle = typeDefinition.getParticle();
		if (particle != null) {
			collectAttributesDeclaration(particle.getTerm(), attributes);
		}
	}

	@SuppressWarnings("unchecked")
	private void collectAttributesDeclaration(XSTerm term, Collection<CMAttribute> attributes) {
		if (term == null) {
			return;
		}
		switch (term.getType()) {
		case XSConstants.MODEL_GROUP:
			XSObjectList particles = ((XSModelGroup) term).getParticles();
			particles.forEach(p -> collectAttributesDeclaration(((XSParticle) p).getTerm(), attributes));
			break;
		case XSConstants.ATTRIBUTE_DECLARATION:
			XSAttributeDeclaration attributeDeclaration = (XSAttributeDeclaration) term;
			attributes.add(new XSDAttribute(attributeDeclaration));
			break;
		}
	}

	@Override
	public Collection<CMElement> getElements() {
		if (elements == null) {
			elements = new ArrayList<>();
			collectElementsDeclaration(elementDeclaration, elements);
		}
		return elements;
	}

	private void collectElementsDeclaration(XSElementDeclaration elementDecl, Collection<CMElement> elements) {
		XSTypeDefinition typeDefinition = elementDecl.getTypeDefinition();
		switch (typeDefinition.getTypeCategory()) {
		case XSTypeDefinition.SIMPLE_TYPE:
			// TODO...
			break;
		case XSTypeDefinition.COMPLEX_TYPE:
			collectElementsDeclaration((XSComplexTypeDefinition) typeDefinition, elements);
			break;
		}
	}

	private void collectElementsDeclaration(XSComplexTypeDefinition typeDefinition, Collection<CMElement> elements) {
		XSParticle particle = typeDefinition.getParticle();
		if (particle != null) {
			collectElementsDeclaration(particle.getTerm(), elements);
		}
	}

	@SuppressWarnings("unchecked")
	private void collectElementsDeclaration(XSTerm term, Collection<CMElement> elements) {
		if (term == null) {
			return;
		}
		switch (term.getType()) {
		case XSConstants.MODEL_GROUP:
			XSObjectList particles = ((XSModelGroup) term).getParticles();
			particles.forEach(p -> collectElementsDeclaration(((XSParticle) p).getTerm(), elements));
			break;
		case XSConstants.ELEMENT_DECLARATION:
			XSElementDeclaration elementDeclaration = (XSElementDeclaration) term;
			elements.add(document.getXSDElement(elementDeclaration));
			break;
		}
	}

	@Override
	public String getDocumentation() {
		XSAnnotation annotation = elementDeclaration.getAnnotation();
		return annotation != null ? annotation.getAnnotationString() : null;
	}

	@Override
	public CMElement findCMElement(String tag, String namespace) {
		for (CMElement cmElement : getElements()) {
			if (cmElement.getName().equals(tag)) {
				return cmElement;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}
}
