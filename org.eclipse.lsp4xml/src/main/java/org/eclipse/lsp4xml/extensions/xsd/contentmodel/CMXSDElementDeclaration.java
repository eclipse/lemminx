/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.xsd.contentmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSElementDecl;
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
 * VARIABLE_NAME(CLASS)[RETRIEVAL_METHOD\]
 * 
 * elementDeclaration(XSElementDeclaration, XSConstants.ELEMENT_DECLARATION)
 * fName(String)[getName()]
 * fSubGroup(XSElementDeclaration)[getSubstitutionGroupAffiliation()]
 * fType(XSTypeDefinition->Simple/Complex)[getTypeDefinition()]
 * fParticle(XSParticle)[getParticle()] fValue(XSTerm -> XSConstants.MODEL_GROUP
 * || XSConstants.ELEMENT_DECLARATION)[getTerm()] fCompositor(short ->
 * XSModelGroup.X)[getCompositor()] fParticles(XSParticle[])[getParticles]
 * 
 * TYPE(XSConstants)[getType()]
 *
 */
public class CMXSDElementDeclaration implements CMElementDeclaration {

	private final CMXSDDocument document;

	private final XSElementDeclaration elementDeclaration;

	private Collection<CMAttributeDeclaration> attributes;

	private List<CMElementDeclaration> elements;

	private String documentation;

	public boolean isOptional;

	public CMXSDElementDeclaration(CMXSDDocument document, XSElementDeclaration elementDeclaration) {
		this.document = document;
		this.elementDeclaration = elementDeclaration;
	}

	@Override
	public String getName() {
		return elementDeclaration.getName();
	}

	@Override
	public String getNamespace() {
		return elementDeclaration.getNamespace();
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
					attributes.add(new CMXSDAttributeDeclaration(attributeUse));
				}

			}
		}
	}

	@Override
	/**
	 * Gets all possible children elements of this element.
	 * 
	 * The modelGroup(all, choice, equence) and existing elements in the XML are not
	 * considered and must be handled.
	 */
	public List<CMElementDeclaration> getElements() {
		if (elements == null) {
			elements = new ArrayList<>();
			collectElementsDeclaration(elementDeclaration, elements);
		}
		return elements;
	}

	public XSParticle getParticle() {
		if (elementDeclaration instanceof XSElementDecl) {
			XSElementDecl decl = (XSElementDecl) elementDeclaration;
			short type = elementDeclaration.getTypeDefinition().getTypeCategory();
			if (type != XSTypeDefinition.COMPLEX_TYPE) {
				return null;
			}
			XSComplexTypeDefinition complexType = (XSComplexTypeDefinition) decl.fType;
			return complexType.getParticle();
		}
		return null;
	}

	/**
	 * The term is the object that holds the content inside a complex type tag.
	 * 
	 * The fValue of an XSParticleDecl
	 * 
	 * XSModelGroup | XSWildCard | XSElementDecl
	 */
	public XSTerm getTerm() {
		XSParticle particle = getParticle();
		return particle != null ? particle.getTerm() : null;
	}

	/**
	 * Returns the model group short value or -1 if the term is not a model group
	 * 
	 * @return
	 */
	public short getModelGroup() {
		XSTerm term = getTerm();
		if (term == null || XSConstants.MODEL_GROUP != term.getType()) {
			return -1;
		}
		XSModelGroup modelGroup = (XSModelGroup) term;
		return modelGroup.getCompositor();
	}

	/**
	 * True if xsd element contains: <xs:complexType> <xs:sequence> ...
	 * </xs:sequence> <xs:complexType>
	 * 
	 * @return
	 */
	public boolean isModelGroupSequence() {
		return XSModelGroup.COMPOSITOR_SEQUENCE == getModelGroup();
	}

	private void collectElementsDeclaration(XSElementDeclaration elementDecl, Collection<CMElementDeclaration> elements) {
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
	public void collectElementsDeclaration(XSTerm term, Collection<CMElementDeclaration> elements) {
		if (term == null) {
			return;
		}

		switch (term.getType()) {

		case XSConstants.WILDCARD:
			// XSWildcard wildcard = (XSWildcard) term;
			// ex : xsd:any
			// document.getElements().forEach(e -> {
			// 	if (!elements.contains(e)) {
			// 		elements.add(e);
			// 	}
			// });
			
			break;
		case XSConstants.MODEL_GROUP:
			XSObjectList particles = ((XSModelGroup) term).getParticles();
			particles.forEach(p -> collectElementsDeclaration(((XSParticle) p).getTerm(), elements));
			break;
		case XSConstants.ELEMENT_DECLARATION:
			XSElementDeclaration elementDeclaration = (XSElementDeclaration) term;
			document.collectElement(elementDeclaration, elements);
			break;
		}
	}

	@Override
	public String getDocumentation() {
		if (documentation != null) {
			return documentation;
		}
		// Try get xs:annotation from the element declaration or type
		XSObjectList annotations = getAnnotations();
		documentation = XSDAnnotationModel.getDocumentation(annotations);
		return documentation;
	}

	/**
	 * Returns list of xs:annotation from the element declaration or type
	 * declaration.
	 * 
	 * @return list of xs:annotation from the element declaration or type
	 *         declaration.
	 */
	private XSObjectList getAnnotations() {
		// Try get xs:annotation from the element declaration
		XSObjectList annotation = elementDeclaration.getAnnotations();
		if (annotation != null && annotation.getLength() > 0) {
			return annotation;
		}
		// Try get xs:annotation from the type of element declaration
		XSTypeDefinition typeDefinition = elementDeclaration.getTypeDefinition();
		if (typeDefinition == null) {
			return null;
		}
		if (typeDefinition.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
			return ((XSComplexTypeDecl) typeDefinition).getAnnotations();
		} else if (typeDefinition.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
			return ((XSSimpleTypeDecl) typeDefinition).getAnnotations();
		}
		return null;
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
			return CMXSDDocument.getEnumerationValues((XSSimpleTypeDefinition) typeDefinition);
		}
		return Collections.emptyList();
	}

	/**
	 * @return the document
	 */
	public CMXSDDocument getDocument() {
		return document;
	}

	public void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}

	public boolean isOptional() {
		return isOptional;
	}
}
