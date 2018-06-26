package org.eclipse.xml.languageserver.xsd;

import java.io.File;

import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.util.XMLCatalogResolver;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;

public class XSDocument {

	public static void main(String[] args) {

		XMLSchemaLoader loader = new XMLSchemaLoader();
		
		String [] catalogs = { "catalog.xml" };
		XMLCatalogResolver resolver = new XMLCatalogResolver(catalogs);
		loader.setEntityResolver(resolver);
		
		XSModel model = loader.loadURI(new File("myschema.xsd").toURI().toString());
		XSElementDeclaration elementDecl = model.getElementDeclaration("property", null);
		XSComplexTypeDefinition def = (XSComplexTypeDefinition) elementDecl.getTypeDefinition();
		XSAttributeUse attrDecl = (XSAttributeUse) def.getAttributeUses().get(1);		
		System.err.println(attrDecl.getAttrDeclaration().getName());
	}
}
