package org.eclipse.lemminx.extensions.relaxng;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.lemminx.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lemminx.services.extensions.ISharedSettingsRequest;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.SimpleNameClass;

public class CMRelaxNGAttributeDeclaration implements CMAttributeDeclaration {

	private final AttributeExp exp;

	public CMRelaxNGAttributeDeclaration(AttributeExp exp) {
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
	public String getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getEnumerationValues() {
		// TODO fix
		return Collections.emptyList();
	}

	@Override
	public String getAttributeNameDocumentation(ISharedSettingsRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttributeValueDocumentation(String value, ISharedSettingsRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRequired() {
		// TODO Auto-generated method stub
		return false;
	}

}
