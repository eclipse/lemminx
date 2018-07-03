package org.eclipse.xml.languageserver.xsd;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.xml.languageserver.contentmodel.CMElement;

public class XSDElement implements CMElement {

	private final XSElementDeclaration elementDeclaration;

	private Collection<CMElement> elements;

	public XSDElement(XSElementDeclaration elementDeclaration) {
		this.elementDeclaration = elementDeclaration;
	}

	@Override
	public String getName() {
		return elementDeclaration.getName();
	}

	@Override
	public Collection<CMElement> getElements() {
		if (elements == null) {
			elements = new ArrayList<>();
			collectElementsDeclaration(elementDeclaration, elements);
		}
		return elements;
	}

	protected static void collectElementsDeclaration(XSElementDeclaration elementDecl, Collection<CMElement> elements) {
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

	private static void collectElementsDeclaration(XSComplexTypeDefinition typeDefinition,
			Collection<CMElement> elements) {
		XSParticle particle = typeDefinition.getParticle();
		if (particle != null) {
			collectElementsDeclaration(particle.getTerm(), elements);
		}
	}

	@SuppressWarnings("unchecked")
	private static void collectElementsDeclaration(XSTerm term, Collection<CMElement> elements) {
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
			elements.add(new XSDElement(elementDeclaration));
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
}
