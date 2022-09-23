package org.eclipse.lemminx.extensions.relaxng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lemminx.services.extensions.ISharedSettingsRequest;

import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.util.ExpressionWalker;

public class CMRelaxNGElementDeclaration implements CMElementDeclaration {

	private static final Logger LOGGER = Logger.getLogger(CMRelaxNGElementDeclaration.class.getName());

	private final ElementExp exp;
	private List<CMElementDeclaration> childElementDeclarations;
	private transient List<CMAttributeDeclaration> attributeDeclarations;

	public CMRelaxNGElementDeclaration(ElementExp exp) {
		this.exp = exp;
	}

	@Override
	public String getName() {
		if (!(exp.getNameClass() instanceof SimpleNameClass)) {
			throw new RuntimeException("Cannot get the name of the element for the given RelaxNG element declaration");
		}
		SimpleNameClass simpleNameClass = (SimpleNameClass) exp.getNameClass();
		return simpleNameClass.localName;
	}

	@Override
	public String getNamespace() {
		if (!(exp.getNameClass() instanceof SimpleNameClass)) {
			throw new RuntimeException("Cannot get the namespace of the element for the given RelaxNG element declaration");
		}
		SimpleNameClass simpleNameClass = (SimpleNameClass) exp.getNameClass();
		return simpleNameClass.namespaceURI;
	}

	@Override
	public Collection<CMElementDeclaration> getElements() {
		collectChildElementsIfNeeded();
		return childElementDeclarations;
	}

	@Override
	public Collection<CMElementDeclaration> getPossibleElements(DOMElement parentElement, int offset) {
		collectChildElementsIfNeeded();
		return childElementDeclarations;
	}

	@Override
	public CMElementDeclaration findCMElement(String tag, String namespace) {
		collectChildElementsIfNeeded();
		for (CMElementDeclaration elementDeclaration: childElementDeclarations) {
			if (Objects.equals(elementDeclaration.getName(), tag)
			// TODO: uncomment once namespaces are supported properly
			// && Objects.equals(elementDeclaration.getNamespace(), namespace)
			) {
				return elementDeclaration;
			}
		}
		return null;
	}

	private void collectChildElementsIfNeeded() {
		if (childElementDeclarations != null) {
			return;
		}
		childElementDeclarations = new ArrayList<>();
		exp.visit(new ExpressionWalker() {

			boolean hasVisitedParent = false;
			@Override
			public void onElement(ElementExp exp) {
				if (!hasVisitedParent) {
					// this is the current element
					hasVisitedParent = true;
					super.onElement(exp);
				} else {
					// do not recurse into grandchild elements
					childElementDeclarations.add(new CMRelaxNGElementDeclaration(exp));
				}
			}
		});
	}

	@Override
	public Collection<CMAttributeDeclaration> getAttributes() {
		collectAttributesIfNeeded();
		return attributeDeclarations;
	}

	@Override
	public CMAttributeDeclaration findCMAttribute(String attributeName) {
		collectAttributesIfNeeded();
		for (CMAttributeDeclaration attributeDeclaration : attributeDeclarations) {
			if (Objects.equals(attributeDeclaration.getName(), attributeName)) {
				return attributeDeclaration;
			}
		}
		return null;
	}

	private void collectAttributesIfNeeded() {
		if (attributeDeclarations != null) {
			return;
		}
		attributeDeclarations = new ArrayList<>();
		exp.visit(new ExpressionWalker() {

			boolean visitedCurrentElement = false;

			@Override
			public void onElement(ElementExp exp) {
				if (!visitedCurrentElement) {
					visitedCurrentElement = false;
					super.onElement(exp);
				}
			}

			@Override
			public void onAttribute(AttributeExp exp) {
				attributeDeclarations.add(new CMRelaxNGAttributeDeclaration(exp));
			}
		});
	}

	@Override
	public String getDocumentation(ISharedSettingsRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<String> getEnumerationValues() {
		// TODO Implement properly, an NPE is caused if this returns null
		return Collections.emptyList();
	}

	@Override
	public String getTextDocumentation(String value, ISharedSettingsRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStringType() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMixedContent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOptional(String childElementName) {
		// TODO Auto-generated method stub
		return false;
	}

}
