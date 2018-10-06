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
package org.eclipse.lsp4xml.extensions.contentmodel.xsd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;

/**
 * XSD element declaration implementation.
 *
 */
public class XSDElementDeclaration implements CMElementDeclaration {

	private final XSDDocument document;

	private final XSElementDeclaration elementDeclaration;

	private Collection<CMAttributeDeclaration> attributes;

	private Collection<CMElementDeclaration> elements;

	private XSDAnnotationModel annotationModel;

	public XSDElementDeclaration(XSDDocument document, XSElementDeclaration elementDeclaration) {
		this.document = document;
		this.elementDeclaration = elementDeclaration;
	}

	@Override
	public String getName() {
		return elementDeclaration.getName();
	}

	@Override
	public String getName(String prefix) {
		String name = getName();
		if (prefix == null || prefix.isEmpty()) {
			return name;
		}
		return prefix + ":" + name;
	}

	@Override
	public Collection<CMAttributeDeclaration> getAttributes() {
		if (attributes == null) {
			attributes = new ArrayList<>();
			collectAttributesDeclaration(elementDeclaration, attributes);
		}
		return attributes;
	}

	private void collectAttributesDeclaration(XSElementDeclaration elementDecl,
			Collection<CMAttributeDeclaration> attributes) {
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
			Collection<CMAttributeDeclaration> attributes) {
		XSObjectList list = typeDefinition.getAttributeUses();
		if (list != null) {
			for (int i = 0; i < list.getLength(); i++) {
				XSObject object = list.item(i);
				if (object.getType() == XSConstants.ATTRIBUTE_USE) {
					XSAttributeUse attributeUse = (XSAttributeUse) object;
					attributes.add(new XSDAttributeDeclaration(attributeUse));
				}

			}
		}
//		XSParticle particle = typeDefinition.getParticle();
//		if (particle != null) {
//			collectAttributesDeclaration(particle.getTerm(), attributes);
//		}
	}

	/*
	 * @SuppressWarnings("unchecked") private void
	 * collectAttributesDeclaration(XSTerm term, Collection<CMAttribute> attributes)
	 * { if (term == null) { return; } switch (term.getType()) { case
	 * XSConstants.MODEL_GROUP: XSObjectList particles = ((XSModelGroup)
	 * term).getParticles(); particles.forEach(p ->
	 * collectAttributesDeclaration(((XSParticle) p).getTerm(), attributes)); break;
	 * case XSConstants.ATTRIBUTE_DECLARATION: XSAttributeDeclaration
	 * attributeDeclaration = (XSAttributeDeclaration) term; attributes.add(new
	 * XSDAttribute(attributeDeclaration)); break; } }
	 */

	@Override
	public Collection<CMElementDeclaration> getElements() {
		if (elements == null) {
			elements = new ArrayList<>();
			collectElementsDeclaration(elementDeclaration, elements);
		}
		return elements;
	}

	private void collectElementsDeclaration(XSElementDeclaration elementDecl,
			Collection<CMElementDeclaration> elements) {
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

	private void collectElementsDeclaration(XSComplexTypeDefinition typeDefinition,
			Collection<CMElementDeclaration> elements) {
		XSParticle particle = typeDefinition.getParticle();
		if (particle != null) {
			collectElementsDeclaration(particle.getTerm(), elements);
		}
	}

	@SuppressWarnings("unchecked")
	private void collectElementsDeclaration(XSTerm term, Collection<CMElementDeclaration> elements) {
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
			CMElementDeclaration cmElement = document.getXSDElement(elementDeclaration);
			// check element declaration is not already added (ex: xs:annotation)
			if (!elements.contains(cmElement)) {
				elements.add(cmElement);
			}
			break;
		}
	}

	@Override
	public String getDocumentation() {
		if (annotationModel == null && elementDeclaration.getAnnotation() != null) {
			annotationModel = XSDAnnotationModel.load(elementDeclaration.getAnnotation());
		}
		return annotationModel != null ? annotationModel.getDocumentation() : null;
	}

	@Override
	public CMElementDeclaration findCMElement(String tag, String namespace) {
		for (CMElementDeclaration cmElement : getElements()) {
			if (cmElement.getName().equals(tag)) {
				return cmElement;
			}
		}
		return null;
	}

	@Override
	public CMAttributeDeclaration findCMAttribute(String attributeName) {
		for (CMAttributeDeclaration cmAttribute : getAttributes()) {
			if (cmAttribute.getName().equals(attributeName)) {
				return cmAttribute;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean isEmpty() {
		XSTypeDefinition typeDefinition = elementDeclaration.getTypeDefinition();
		if (typeDefinition != null && typeDefinition.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
			XSComplexTypeDefinition complexTypeDefinition = (XSComplexTypeDefinition) typeDefinition;
			return complexTypeDefinition.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_EMPTY;
		}
		return false;
	}

	@Override
	public Collection<String> getEnumerationValues() {
		XSTypeDefinition typeDefinition = elementDeclaration.getTypeDefinition();
		if (typeDefinition != null && typeDefinition.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
			return XSDDocument.getEnumerationValues((XSSimpleTypeDefinition) typeDefinition);
		}
		return Collections.emptyList();
	}
}
