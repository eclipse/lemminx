package org.eclipse.lsp4xml.extensions.dtd.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMDocumentType;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DTDAttlistDecl;
import org.eclipse.lsp4xml.dom.DTDDeclNode;
import org.eclipse.lsp4xml.dom.DTDDeclParameter;
import org.eclipse.lsp4xml.dom.DTDElementDecl;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DTDUtils {

	public static void searchDTDTargetElementDecl(DTDDeclParameter elementName, boolean matchName,
			Consumer<DTDDeclParameter> collector) {
		DOMDocumentType docType = elementName.getOwnerDoctype();
		if (docType.hasChildNodes()) {
			// Loop for element complexType.
			NodeList children = docType.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node node = children.item(i);
				if (node.getNodeType() == DOMNode.DTD_ELEMENT_DECL_NODE) {
					DTDElementDecl targetElement = (DTDElementDecl) node;
					// node is a xs:complexType, xs:simpleType element, xsl:element, xs:group which
					// matches the binding type of the originAttr
					DTDDeclParameter targetElementName = targetElement.getNameNode();
					if (targetElementName != null && (!matchName
							|| (matchName && elementName.getParameter().equals(targetElementName.getParameter())))) {
						collector.accept(targetElementName);
					}
				}
			}
		}
	}

	/**
	 * Search origin attributes from the given target node.
	 * 
	 * @param targetNode the referenced node
	 * @param collector  the collector to collect reference between an origin and
	 *                   target attribute.
	 */
	public static void searchDTDOriginElementDecls(DTDDeclNode targetNode,
			BiConsumer<DTDDeclParameter, DTDDeclParameter> collector, CancelChecker cancelChecker) {
		List<DTDDeclNode> targetNodes = getTargetNodes(targetNode);
		if (targetNodes.isEmpty()) {
			// None referenced nodes, stop the search of references
			return;
		}

		DOMDocumentType docType = targetNode.getOwnerDoctype();
		if (docType.hasChildNodes()) {
			// Loop for element .
			NodeList children = docType.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (cancelChecker != null) {
					cancelChecker.checkCanceled();
				}
				Node origin = children.item(i);

				for (DTDDeclNode target : targetNodes) {

					if (target.isDTDElementDecl()) {
						DTDElementDecl targetElement = (DTDElementDecl) target;
						String name = targetElement.getName();
						if (origin.getNodeType() == DOMNode.DTD_ELEMENT_DECL_NODE) {
							DTDElementDecl originElement = (DTDElementDecl) origin;
							originElement.collectParameters(targetElement.getNameNode(), collector);
						} else if (origin.getNodeType() == DOMNode.DTD_ATT_LIST_NODE) {
							DTDAttlistDecl originAttribute = (DTDAttlistDecl) origin;
							if (name.equals(originAttribute.getElementName())) {
								collector.accept(originAttribute.getElementNameNode(), targetElement.getNameNode());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the referenced attributes list from the given referenced node.
	 * 
	 * @param referencedNode the referenced node.
	 * @return the referenced attributes list from the given referenced node.
	 */
	private static List<DTDDeclNode> getTargetNodes(DTDDeclNode referencedNode) {
		List<DTDDeclNode> referencedNodes = new ArrayList<>();
		switch (referencedNode.getNodeType()) {
		case DOMNode.DTD_ELEMENT_DECL_NODE:
			addTargetNode(referencedNode, referencedNodes);
			break;
		case Node.DOCUMENT_TYPE_NODE:
			DOMDocumentType docType = (DOMDocumentType) referencedNode;
			if (docType.hasChildNodes()) {
				// Loop for element <!ELEMENT.
				NodeList children = docType.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node node = children.item(i);
					if (node.getNodeType() == DOMNode.DTD_ELEMENT_DECL_NODE) {
						addTargetNode((DTDElementDecl) node, referencedNodes);
					}
				}
			}
			break;
		}
		return referencedNodes;
	}

	private static void addTargetNode(DTDDeclNode referencedNode, List<DTDDeclNode> referencedNodes) {
		if (referencedNode.isDTDElementDecl()) {
			// Add only <!ELEMENT which defines a name.
			DTDElementDecl elementDecl = (DTDElementDecl) referencedNode;
			if (elementDecl.getNameNode() != null) {
				referencedNodes.add(elementDecl);
			}
		}
	}
}
