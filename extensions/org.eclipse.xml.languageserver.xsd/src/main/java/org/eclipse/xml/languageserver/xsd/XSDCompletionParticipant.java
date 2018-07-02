package org.eclipse.xml.languageserver.xsd;

import java.util.function.Consumer;

import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Range;
import org.eclipse.xml.languageserver.extensions.ICompletionParticipant;
import org.eclipse.xml.languageserver.extensions.ICompletionRequest;
import org.eclipse.xml.languageserver.extensions.ICompletionResponse;
import org.eclipse.xml.languageserver.model.Node;

public class XSDCompletionParticipant implements ICompletionParticipant {

	@Override
	public void onXMLContent(ICompletionRequest request, ICompletionResponse response) {
		try {
			Node currentNode = request.getNode();
			Node parentNode = currentNode;
			int startTagEndOffset = currentNode.start + currentNode.tag.length() + ">".length();
			if (!(request.getOffset() > startTagEndOffset && request.getOffset() < currentNode.end)) {
				parentNode = request.getNode().parent;
			}
			XSElementDeclaration elementDecl = XMLSchemaManager.getInstance().findElementDeclaration(parentNode);
			if (elementDecl != null) {
				visitChildrenElementDeclaration(elementDecl, e -> {
					CompletionItem completionItem = new CompletionItem();
					completionItem.setLabel(e.getName());
					XSAnnotation annotation = e.getAnnotation();
					if (annotation != null) {
						completionItem.setDetail(annotation.getAnnotationString());
					}
					response.addCompletionItem(completionItem);
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void visitChildrenElementDeclaration(XSElementDeclaration elementDecl,
			Consumer<XSElementDeclaration> visitor) {
		XSTypeDefinition typeDefinition = elementDecl.getTypeDefinition();
		switch (typeDefinition.getTypeCategory()) {
		case XSTypeDefinition.SIMPLE_TYPE:
			// TODO...
			break;
		case XSTypeDefinition.COMPLEX_TYPE:
			visitChildrenElementDeclaration((XSComplexTypeDefinition) typeDefinition, visitor);
			break;
		}
	}

	private void visitChildrenElementDeclaration(XSComplexTypeDefinition typeDefinition,
			Consumer<XSElementDeclaration> visitor) {
		XSParticle particle = typeDefinition.getParticle();
		if (particle != null) {
			visitChildrenElementDeclaration(particle.getTerm(), visitor);
		}
	}

	@SuppressWarnings("unchecked")
	private void visitChildrenElementDeclaration(XSTerm term, Consumer<XSElementDeclaration> visitor) {
		if (term == null) {
			return;
		}
		switch (term.getType()) {
		case XSConstants.MODEL_GROUP:
			XSObjectList particles = ((XSModelGroup) term).getParticles();
			particles.forEach(p -> visitChildrenElementDeclaration(((XSParticle) p).getTerm(), visitor));
			break;
		case XSConstants.ELEMENT_DECLARATION:
			visitor.accept((XSElementDeclaration) term);
			break;
		}
	}

	@Override
	public void onAttributeValue(String valuePrefix, Range fullRange, ICompletionRequest request,
			ICompletionResponse response) {
		response.addCompletionItem(new CompletionItem("AbcA"));
		response.addCompletionItem(new CompletionItem("BcBB"));
	}

}
