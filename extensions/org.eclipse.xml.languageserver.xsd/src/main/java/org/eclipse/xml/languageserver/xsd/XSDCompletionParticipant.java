package org.eclipse.xml.languageserver.xsd;

import java.io.File;

import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Range;
import org.eclipse.xml.languageserver.extensions.ICompletionParticipant;
import org.eclipse.xml.languageserver.extensions.ICompletionRequest;
import org.eclipse.xml.languageserver.extensions.ICompletionResponse;

public class XSDCompletionParticipant implements ICompletionParticipant {

	@Override
	public void onXMLContent(ICompletionRequest request, ICompletionResponse response) {
		try {
			XMLSchemaLoader loader = new XMLSchemaLoader();
			XSModel model = loader.loadURI(new File("maven-4.0.0.xsd").toURI().toString());
			XSElementDeclaration elementDecl = model.getElementDeclaration("project", "http://maven.apache.org/POM/4.0.0");
			if (elementDecl != null) {
				System.err.println(elementDecl);
			}
			//model.
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onAttributeValue(String valuePrefix, Range fullRange, ICompletionRequest request,
			ICompletionResponse response) {
		response.addCompletionItem(new CompletionItem("AbcA"));
		response.addCompletionItem(new CompletionItem("BcBB"));
	}
	
	

}
