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
package org.eclipse.lsp4xml.extensions.contentmodel.participants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.xerces.impl.xs.XSWildcardDecl;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.utils.XMLGenerator;
import org.eclipse.lsp4xml.extensions.dtd.contentmodel.CMDTDElementDeclaration;
import org.eclipse.lsp4xml.extensions.xsd.contentmodel.CMXSDDocument;
import org.eclipse.lsp4xml.extensions.xsd.contentmodel.CMXSDElementDeclaration;
import org.eclipse.lsp4xml.services.AttributeCompletionItem;
import org.eclipse.lsp4xml.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.CompletionSortTextHelper;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.settings.SharedSettings;
import org.eclipse.lsp4xml.uriresolver.CacheResourceDownloadingException;

/**
 * Extension to support XML completion based on content model (XML Schema
 * completion, etc)
 */
public class ContentModelCompletionParticipant extends CompletionParticipantAdapter {

	@Override
	public void onTagOpen(ICompletionRequest request, ICompletionResponse response) throws Exception {
		try {
			DOMDocument document = request.getXMLDocument();
			ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
			DOMElement parentElement = request.getParentElement();
			if (parentElement == null) {
				// XML is empty, in case of XML file associations, a XMl Schema/DTD can be bound
				// check if it's root element (in the case of XML file associations, the link to
				// XML Schema is done with pattern and not with XML root element)
				CMDocument cmDocument = contentModelManager.findCMDocument(document, null);
				if (cmDocument != null) {
					fillWithChildrenElementDeclaration(null, cmDocument, null, false, request, response);
				}
				return;
			}
			// Try to retrieve XML Schema/DTD element declaration for the parent element
			// where completion was triggered.
			CMElementDeclaration cmElement = contentModelManager.findCMElement(parentElement);
			String defaultPrefix = null;
			if (cmElement != null) {
				response.addCompletionItem(null, true); //Set a fake item to prevent existing elements as completion items
				defaultPrefix = parentElement.getPrefix();
				fillWithChildrenElementDeclaration(parentElement, cmElement, defaultPrefix, false,
						request, response);
				return;
			}
			if (parentElement.isDocumentElement()) {
				// root document element
				Collection<String> prefixes = parentElement.getAllPrefixes();
				for (String prefix : prefixes) {
					if (defaultPrefix != null && prefix.equals(defaultPrefix)) {
						continue;
					}
					String namespaceURI = parentElement.getNamespaceURI(prefix);
					CMDocument cmDocument = contentModelManager.findCMDocument(parentElement, namespaceURI);
					if (cmDocument != null) {
						fillWithChildrenElementDeclaration(parentElement, cmDocument, prefix, true,
								request, response);
					}
				}
			}
			// Completion on tag based on internal content model (ex : internal DTD declared
			// in XML)
			CMElementDeclaration cmInternalElement = contentModelManager.findInternalCMElement(parentElement);
			if (cmInternalElement != null) {
				defaultPrefix = parentElement.getPrefix();
				fillWithChildrenElementDeclaration(parentElement, cmInternalElement, defaultPrefix, false,
						request, response);
			}
		} catch (CacheResourceDownloadingException e) {
			// XML Schema, DTD is loading, ignore this error
		}
	}

	private void fillWithChildrenElementDeclaration(DOMElement element, CMElementDeclaration parentCMElement,
			String p, boolean forceUseOfPrefix, ICompletionRequest request, ICompletionResponse response)
			throws BadLocationException {
		if(parentCMElement instanceof CMXSDElementDeclaration) {
			CMXSDElementDeclaration parentCMXSDElement = (CMXSDElementDeclaration) parentCMElement;
			
			List<CMElementDeclaration> finalItems = getNextValidDeclsFromModelGroup(element, parentCMXSDElement, parentCMXSDElement.getParticle(), p, forceUseOfPrefix, request, response);
			
			CompletionSortTextHelper sort = new CompletionSortTextHelper(CompletionItemKind.Property);
			createElementCompletionItems(element, finalItems, p, request, response, sort, forceUseOfPrefix);
		}
		else if(parentCMElement instanceof CMDTDElementDeclaration) {
			CompletionSortTextHelper sort = new CompletionSortTextHelper(CompletionItemKind.Property);

			createElementCompletionItems(element, parentCMElement.getElements(), p, request, response, sort, forceUseOfPrefix);
		}
	}

	private void fillWithChildrenElementDeclaration(DOMElement element, CMDocument cmDocument,
			String p, boolean forceUseOfPrefix, ICompletionRequest request, ICompletionResponse response)
			throws BadLocationException {
		CompletionSortTextHelper sort = new CompletionSortTextHelper(CompletionItemKind.Property);
		createElementCompletionItems(element, cmDocument.getElements(), p, request, response, sort, forceUseOfPrefix);

	}

	private List<CMElementDeclaration> getNextValidDeclsFromModelGroup(DOMElement element, CMXSDElementDeclaration parentCMElement, XSParticle modelGroupParticle,
	String prefix, boolean forceUseOfPrefix, ICompletionRequest request, ICompletionResponse response)
			throws BadLocationException {

		if(parentCMElement == null) {
			return null;
		}

		if(modelGroupParticle.getTerm().getType() != XSConstants.MODEL_GROUP) {
			return null;
		}

		XSModelGroup modelGroup = (XSModelGroup) modelGroupParticle.getTerm();

		List<DOMElement> domElements = null;
		if(element != null) {
			domElements = element.getChildElements();
		} 
		
		int offset = request.getOffset();

		short modelGroupType = modelGroup.getCompositor();
		switch(modelGroupType) {
			case -1:
				return null;
			case XSModelGroup.COMPOSITOR_CHOICE:
			case XSModelGroup.COMPOSITOR_ALL: {
				if(modelGroupType == XSModelGroup.COMPOSITOR_CHOICE) {
					
					
					if(!modelGroupParticle.getMaxOccursUnbounded()) {
						int elementCounter = 0;
						List<CMElementDeclaration> zz = new ArrayList<CMElementDeclaration>();
						DOMElement rootElement = element.getOwnerDocument().getDocumentElement();
						collectElementsDeclaration(modelGroup, zz,parentCMElement.getDocument(), rootElement, prefix, request);
						for (DOMElement domElement : domElements) {
							for (CMElementDeclaration cmElement : zz) {
								if(domElement.getLocalName().equals(cmElement.getName())) {
									elementCounter++;
									if(elementCounter >= modelGroupParticle.getMaxOccurs()) {
										return null;
									}
								}
							}
						}
					}
				}

				List<CMElementDeclaration> validElements = new ArrayList<CMElementDeclaration>();
				
				
				XSObjectList particles = modelGroup.getParticles();
				int particleSize = particles.getLength();
				int particleIndex = 0;
				while(particleIndex < particleSize) { // All possible element declarations
					List<CMElementDeclaration> currentDeclElements = new ArrayList<CMElementDeclaration>();
					XSParticle particle = (XSParticle) particles.get(particleIndex);
					particleIndex++;
					XSTerm term = particle.getTerm();
					if(term.getType() == XSConstants.ELEMENT_DECLARATION) {
						//populates list with all possible options for this declaration
						//considers if it is a reference/substitution groups.
						parentCMElement.getDocument().collectElement((XSElementDeclaration) term, currentDeclElements);
					}
					else if(term.getType() == XSConstants.MODEL_GROUP){
						currentDeclElements.addAll(getNextValidDeclsFromModelGroup(element, parentCMElement, particle, prefix, forceUseOfPrefix, request, response));
					}
					else if(term.getType() == XSConstants.WILDCARD) {
							
						DOMDocument domDocument = element.getOwnerDocument();
						DOMElement rootElement = domDocument.getDocumentElement();
						collectElementsFromWildcard(term, currentDeclElements, rootElement, prefix, request);
					}

					boolean addDecl = true;
					for (DOMElement childDom: domElements) {
						int matchingDeclIndex = getElementOffsetInList(currentDeclElements, childDom);
						if(matchingDeclIndex >= 0) {
							if(modelGroupType == XSModelGroup.COMPOSITOR_CHOICE) { // choice only allows for one of it's options
								boolean isUnderMaxOccurs = particle.getMaxOccurs() > element.getNumberOfChildElementsWithTagName(childDom.getTagName());
								if(modelGroupParticle.getMaxOccursUnbounded() || isUnderMaxOccurs) {
									// This choice is allowed again since max occurs has not yet been reached
									CMElementDeclaration singleCMElement = currentDeclElements.get(getElementOffsetInList(currentDeclElements, childDom));
									currentDeclElements.clear();
									currentDeclElements.add(singleCMElement);
									break;
								}
							}
							addDecl = false; // already exists, don't give as option.
							break;
						}
					}
					if(addDecl) {
						validElements.addAll(currentDeclElements);
					}
				}
				return validElements;
			}
			
			case XSModelGroup.COMPOSITOR_SEQUENCE: {
				List<CMElementDeclaration> validElements = getNextValidElementInSequence(element, parentCMElement, modelGroup, prefix, forceUseOfPrefix, request, response, offset);
				if(validElements == null) {
					break;
				}
				return validElements;
			}
		}
		return null;
	}


	public static void collectElementsFromWildcard(XSTerm term , Collection<CMElementDeclaration> elements, DOMElement rootElement, String prefix, ICompletionRequest request) {
		XSWildcardDecl wc = (XSWildcardDecl) term;
		Collection<String> prefixes = rootElement.getAllPrefixes();
		boolean useOnlyOtherNamespaces = false; // ##other
		for (String wildCardNamespace : wc.fNamespaceList) {
			if(wildCardNamespace == null ) {
				continue;
			}
			if(wildCardNamespace.equals("##other")) {
				useOnlyOtherNamespaces = true;
				continue;
			}
			for (String p : prefixes) {
				if (prefix != null && p.equals(prefix) && useOnlyOtherNamespaces) {
					continue;
				}
				else {
					String namespaceURI = rootElement.getNamespaceURI(p);
					ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
					CMDocument cmDocument = contentModelManager.findCMDocument(rootElement, namespaceURI);
					if(cmDocument != null) {
						elements.addAll(cmDocument.getElements());
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void collectElementsDeclaration(XSTerm term, Collection<CMElementDeclaration> elements, CMXSDDocument document, DOMElement rootElement, String prefix, ICompletionRequest request) {
		if (term == null) {
			return;
		}

		switch (term.getType()) {

		case XSConstants.WILDCARD:
			// XSWildcard wildcard = (XSWildcard) term;
			// ex : xsd:any
			collectElementsFromWildcard(term, elements, rootElement, prefix, request);
			break;
		case XSConstants.MODEL_GROUP:
			XSObjectList particles = ((XSModelGroup) term).getParticles();
			particles.forEach(p -> collectElementsDeclaration(((XSParticle) p).getTerm(), elements, document, rootElement, prefix, request));
			break;
		case XSConstants.ELEMENT_DECLARATION:
			XSElementDeclaration elementDeclaration = (XSElementDeclaration) term;
			document.collectElement(elementDeclaration, elements);
			break;
		}
	}
	
	private List<CMElementDeclaration> getNextValidElementInSequence(DOMElement parentDOMElement, CMXSDElementDeclaration parentCMElement, XSTerm termModelGroup,
	String prefix, boolean forceUseOfPrefix, ICompletionRequest request, ICompletionResponse response, int offset) throws BadLocationException {
		CMXSDDocument cmXSDDocument = parentCMElement.getDocument();
		
		if(termModelGroup.getType() != XSConstants.MODEL_GROUP) {
			return null;
		}
		XSModelGroup modelGroup = (XSModelGroup) termModelGroup;
		// In the case of multiple unique optional decls and the (eg 3rd) one exists
		// and completion is attempted before it: we queue all previous optional declarations
		// in case we find one that exists later, if true we discard/clear all previous to it
		// since the most recent existing optional declaration is the lower bound. Else we add
		// the queued up optional decls
		List<CMElementDeclaration> optionalElementDeclarations = new ArrayList<>();
		List<CMElementDeclaration> allValidDeclarations = new ArrayList<>();
		List<DOMElement> domElements = parentDOMElement != null ? parentDOMElement.getChildElements() : null;
		boolean domElementsHasValues = domElements != null && !domElements.isEmpty();
		int handledExistingElements = 0; // used as an index to point to the next unhandled dom element
		int iDecl;
		XSObjectList particles = modelGroup.getParticles();
		for(iDecl = 0; iDecl < particles.size(); iDecl++) {
			XSParticle particle = (XSParticle) particles.get(iDecl);
			boolean isOptional = particle.getMinOccurs() == 0; //TODO: check if elementDecl is nillable
			XSTerm term = particle.getTerm();
			List<CMElementDeclaration> currentElementDeclarations = new ArrayList<>();
			if(term.getType() == XSConstants.ELEMENT_DECLARATION) {
				//populates list with all possible options for this declaration
				//considers if it is a reference/substitution groups.
				cmXSDDocument.collectElement((XSElementDeclaration) term, currentElementDeclarations);
			}
			else if(term.getType() == XSConstants.MODEL_GROUP){
				List<CMElementDeclaration> l = getNextValidDeclsFromModelGroup(parentDOMElement, parentCMElement, particle, prefix, forceUseOfPrefix, request, response);
				if(l == null) {
					continue;
				}
				currentElementDeclarations.addAll(l);
				

				// DOMElement rootElement = parentDOMElement.getOwnerDocument().getDocumentElement();
				// collectElementsDeclaration(term, currentElementDeclarations, cmXSDDocument, rootElement, prefix, request);
			}
			else if(term.getType() == XSConstants.WILDCARD) {
				DOMDocument domDocument = parentDOMElement.getOwnerDocument();
				DOMElement rootElement = domDocument.getDocumentElement();
				collectElementsFromWildcard(term, currentElementDeclarations, rootElement, prefix, request);
			}
			
			if(domElements == null || handledExistingElements > domElements.size() - 1) { // Seen all xml document elements, no more child elements after this.
				if(!isOptional) { 
					allValidDeclarations.addAll(optionalElementDeclarations);
					allValidDeclarations.addAll(currentElementDeclarations);
					return allValidDeclarations;
				}
				// If this point is reached, then this element declaration was optional
				// so continue to the next declaration and add those too if valid.
				addOptionalElementDecls(optionalElementDeclarations, currentElementDeclarations);
				continue;
			}
			
			if(domElementsHasValues){
				DOMElement domElement = domElements.get(handledExistingElements);
				boolean canHaveMultipleOccurrences = particle.getMaxOccursUnbounded() || particle.getMaxOccurs() > parentDOMElement.getNumberOfSameSurroundingElements(domElement);
				boolean declsContainElement = doElementDeclsContainElement(currentElementDeclarations, domElement);
				if(offset < domElement.getStart()) { // This element is after the completion offset position
					if(declsContainElement) { // The element after offset is already the next valid decl
						allValidDeclarations.addAll(optionalElementDeclarations);
						if(canHaveMultipleOccurrences) {
							allValidDeclarations.addAll(currentElementDeclarations);
						}
						return allValidDeclarations;
					}
					
					if(!isOptional) { // element decl is not optional, search is done.
						allValidDeclarations.addAll(optionalElementDeclarations);
						allValidDeclarations.addAll(currentElementDeclarations); // The element after the offset is not valid, so give the correct element
						return allValidDeclarations;
					}
					addOptionalElementDecls(optionalElementDeclarations, currentElementDeclarations);
					continue; // go to next element decl, this one was optional 
				}
				else if(!declsContainElement || canHaveMultipleOccurrences) { // at this point the offset is after the dom element
					if(!isOptional) { //element decl is not optional
						allValidDeclarations.addAll(optionalElementDeclarations);
						allValidDeclarations.addAll(currentElementDeclarations);
						return allValidDeclarations;
					}
					
					if(!declsContainElement) {
						//This optional element decl is missing and the current element is not it
						addOptionalElementDecls(optionalElementDeclarations, currentElementDeclarations);
					}
					else {
						//all previous optional elements are now invalid since this one exists.
						optionalElementDeclarations.clear();
						addOptionalElementDecls(optionalElementDeclarations, currentElementDeclarations);
						//Go to next different xml element (skips all same named optional elements)
						int elementAfterOffsetIndex = skipOverSameElements(domElements, handledExistingElements, offset);
						if(elementAfterOffsetIndex > domElements.size() - 1) { // all elements after were the same 
							continue;
						}
						DOMElement elementAfterOffset = domElements.get(elementAfterOffsetIndex);

						//edge case of completion between the same optional element
						if(domElement.getTagName().equals(elementAfterOffset.getTagName())) {
							return optionalElementDeclarations;
							
						}
						// the next different element was reached
						handledExistingElements = elementAfterOffsetIndex;
					}
					
				}
				else { // Existing element decl already exists in the document.
					handledExistingElements++;
					// Since we found a non-optional element decl, we know previous optional decls are invalid.
					optionalElementDeclarations.clear();
					
				}
			}
		}
		allValidDeclarations.addAll(optionalElementDeclarations);
		return allValidDeclarations;
	}


	public boolean doElementDeclsContainElement(List<CMElementDeclaration> elements, DOMElement element) {
		return getElementOffsetInList(elements, element) != -1;
	}

	public int getElementOffsetInList(List<CMElementDeclaration> elements, DOMElement element) {
		int i;
		for(i = 0; i < elements.size(); i++) {
			CMElementDeclaration cmElement = elements.get(i);
			if(cmElement.getName().equals(element.getTagName())) {
				return i;
			}
		}
		return -1;
	}

	public void addOptionalElementDecls(List<CMElementDeclaration> addTo, List<CMElementDeclaration> addFrom) {
		
		for (CMElementDeclaration from : addFrom) {
			if(from instanceof CMXSDElementDeclaration) {
				CMXSDElementDeclaration xsdFrom = (CMXSDElementDeclaration) from;
				xsdFrom.setOptional(true);
				addTo.add(xsdFrom);
			}
		}
	}

	/**
	 * Will go to the first element that has a different name from the initialElement.
	 * 
	 * If the stop offset is reached the element after it is returned. There is
	 * a chance this is an element with the same name as the inital element.
	 */
	public int skipOverSameElements(List<DOMElement> domElements, int initialElementIndex, int stopOffset) {
		DOMElement initalElement = domElements.get(initialElementIndex);
		String initialElementName = initalElement.getTagName();
		int size = domElements.size();
		int index = initialElementIndex + 1;
		while(index < size) {
			DOMElement currentElement = domElements.get(index);
			if(stopOffset < currentElement.getStart()) {
				return index;
			}
			if(!initialElementName.equals(currentElement.getTagName())) {
				return index;
			}
			index++;
		}
		return index;
	}

	public void createElementCompletionItems(DOMElement element, List<CMElementDeclaration> children, String prefix,
	ICompletionRequest request, ICompletionResponse response, CompletionSortTextHelper sort, boolean forceUseOfPrefix) 
			throws BadLocationException {
		
		for (CMElementDeclaration child : children) {
			createElementCompletionItem(element, child, prefix, request, response, sort, forceUseOfPrefix);
		}
	}
	
	public void createElementCompletionItem(DOMElement element, CMElementDeclaration child, String prefix,
			ICompletionRequest request, ICompletionResponse response, CompletionSortTextHelper sort, boolean forceUseOfPrefix) 
			throws BadLocationException {
		
		prefix = forceUseOfPrefix ? prefix : (element != null ? element.getPrefix(child.getNamespace()) : null);
		String label = child.getName(prefix);
		if(response.hasSeen(label)) {
			return;
		}
		XMLGenerator generator = request.getXMLGenerator();
		CompletionItem item = new CompletionItem(label);
		item.setFilterText(request.getFilterForStartTagName(label));
		item.setKind(CompletionItemKind.Property);
		item.setSortText(sort.next());
		if(child instanceof CMXSDElementDeclaration) {
			CMXSDElementDeclaration xsdDecl = (CMXSDElementDeclaration) child;
			if(xsdDecl.isOptional) {
				String completionPrefix = CompletionSortTextHelper.getSortText(CompletionItemKind.Property);
				item.setDetail("optional");
				item.setSortText(completionPrefix + "b");
			}
		}
		
		String documentation = child.getDocumentation();
		if (documentation != null) {
			item.setDocumentation(documentation);
		}
		String xml = generator.generate(child, prefix);
		item.setTextEdit(new TextEdit(request.getReplaceRange(), xml));
		InsertTextFormat textFormat = request.getCompletionSettings().isCompletionSnippetsSupported() ? InsertTextFormat.Snippet : InsertTextFormat.PlainText;
		item.setInsertTextFormat(textFormat);
		response.addCompletionItemAsSeen(item);
	}

	@Override
	public void onAttributeName(boolean generateValue, Range fullRange, ICompletionRequest request,
			ICompletionResponse response, SharedSettings settings) throws Exception {
		// otherwise, manage completion based on XML Schema, DTD.
		DOMElement parentElement = request.getNode().isElement() ? (DOMElement) request.getNode() : null;
		if (parentElement == null) {
			return;
		}
		try {
			boolean canSupportSnippet = request.getCompletionSettings().isCompletionSnippetsSupported();
			ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
			// Completion on attribute based on external grammar
			CMElementDeclaration cmElement = contentModelManager.findCMElement(parentElement);
			fillAttributesWithCMAttributeDeclarations(parentElement, fullRange, cmElement, canSupportSnippet,
					generateValue, response, settings);
			// Completion on attribute based on internal grammar
			cmElement = contentModelManager.findInternalCMElement(parentElement);
			fillAttributesWithCMAttributeDeclarations(parentElement, fullRange, cmElement, canSupportSnippet,
					generateValue, response, settings);
		} catch (CacheResourceDownloadingException e) {
			// XML Schema, DTD is loading, ignore this error
		}
	}

	private void fillAttributesWithCMAttributeDeclarations(DOMElement parentElement, Range fullRange,
			CMElementDeclaration cmElement, boolean canSupportSnippet, boolean generateValue,
			ICompletionResponse response, SharedSettings settings) {
		if (cmElement == null) {
			return;
		}
		Collection<CMAttributeDeclaration> attributes = cmElement.getAttributes();
		if (attributes == null) {
			return;
		}
		for (CMAttributeDeclaration cmAttribute : attributes) {
			String attrName = cmAttribute.getName();
			if (!parentElement.hasAttribute(attrName)) {
				CompletionItem item = new AttributeCompletionItem(attrName, canSupportSnippet, fullRange, generateValue,
						cmAttribute.getDefaultValue(), cmAttribute.getEnumerationValues(), settings);
				String documentation = cmAttribute.getDocumentation();
				if (documentation != null) {
					item.setDetail(documentation);
				}
				response.addCompletionItemAsSeen(item);
			}
		}
	}

	@Override
	public void onAttributeValue(String valuePrefix, Range fullRange, boolean addQuotes, ICompletionRequest request,
			ICompletionResponse response, SharedSettings settings) throws Exception {
		DOMElement parentElement = request.getNode().isElement() ? (DOMElement) request.getNode() : null;
		if (parentElement == null) {
			return;
		}
		try {
			ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
			// Completion on attribute values based on external grammar
			CMElementDeclaration cmElement = contentModelManager.findCMElement(parentElement);
			fillAttributeValuesWithCMAttributeDeclarations(cmElement, request, response);
			// Completion on attribute values based on internal grammar
			cmElement = contentModelManager.findInternalCMElement(parentElement);
			fillAttributeValuesWithCMAttributeDeclarations(cmElement, request, response);
		} catch (CacheResourceDownloadingException e) {
			// XML Schema, DTD is loading, ignore this error
		}
	}

	private void fillAttributeValuesWithCMAttributeDeclarations(CMElementDeclaration cmElement,
			ICompletionRequest request, ICompletionResponse response) {
		if (cmElement != null) {
			String attributeName = request.getCurrentAttributeName();
			CMAttributeDeclaration cmAttribute = cmElement.findCMAttribute(attributeName);
			if (cmAttribute != null) {
				cmAttribute.getEnumerationValues().forEach(value -> {
					CompletionItem item = new CompletionItem();
					item.setLabel(value);
					item.setKind(CompletionItemKind.Value);
					response.addCompletionItemAsSeen(item);
				});
			}
		}
	}

}
