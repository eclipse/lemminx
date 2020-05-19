/**
 *  Copyright (c) 2020 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.entities.participants;

import java.util.Collection;
import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.entities.EntitiesDocumentationUtils;
import org.eclipse.lemminx.extensions.entities.EntitiesDocumentationUtils.PredefinedEntity;
import org.eclipse.lemminx.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.ICompletionResponse;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;

/**
 * Entities completion used in a text node (ex : &amp;).
 *
 */
public class EntitiesCompletionParticipant extends CompletionParticipantAdapter {

	@Override
	public void onXMLContent(ICompletionRequest request, ICompletionResponse response) throws Exception {
		Range entityRange = XMLPositionUtility.selectEntity(request.getOffset(), request.getXMLDocument());
		if (entityRange == null) {
			return;
		}
		// There is the '&' character before the offset where completion was triggered
		boolean markdown = request.canSupportMarkupKind(MarkupKind.MARKDOWN);
		DOMDocument document = request.getXMLDocument();
		collectInternalEntityProposals(document, entityRange, markdown, response);
		collectExternalEntityProposals(document, entityRange, markdown, request, response);
		collectPredefinedEntityProposals(entityRange, markdown, request, response);
	}

	/**
	 * Collect internal entities.
	 * 
	 * @param document    the DOM document.
	 * @param entityRange the entity range.
	 * @param markdown    true if the documentation can be formatted as markdown and
	 *                    false otherwise.
	 * @param request     the completion request.
	 * @param response    the completion response.
	 */
	private static void collectInternalEntityProposals(DOMDocument document, Range entityRange, boolean markdown,
			ICompletionResponse response) {
		DOMDocumentType docType = document.getDoctype();
		if (docType == null) {
			return;
		}
		NamedNodeMap entities = docType.getEntities();
		for (int i = 0; i < entities.getLength(); i++) {
			Entity entity = (Entity) entities.item(i);
			if (entity.getNodeName() != null) {
				// provide completion for the locally declared entity
				fillCompletion(entity.getNodeName(), entity.getNotationName(), false, false, entityRange, markdown,
						response);
			}
		}
	}

	/**
	 * Collect external entities.
	 * 
	 * @param document    the DOM document.
	 * @param entityRange the entity range.
	 * @param markdown    true if the documentation can be formatted as markdown and
	 *                    false otherwise.
	 * @param request     the completion request.
	 * @param response    the completion response.
	 */
	private static void collectExternalEntityProposals(DOMDocument document, Range entityRange, boolean markdown,
			ICompletionRequest request, ICompletionResponse response) {
		ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
		Collection<CMDocument> cmDocuments = contentModelManager.findCMDocument(document, null, false);
		for (CMDocument cmDocument : cmDocuments) {			
			List<Entity> entities = cmDocument.getEntities();
			for (Entity entity : entities) {
				if (entity.getNodeName() != null) {
					// provide completion for the external declared entity
					fillCompletion(entity.getNodeName(), entity.getNotationName(), true, false, entityRange, markdown,
							response);
				}
			}
		}
	}

	/**
	 * Collect predefined entities.
	 * 
	 * @param entityRange the entity range.
	 * @param markdown    true if the documentation can be formatted as markdown and
	 *                    false otherwise.
	 * @param request     the completion request.
	 * @param response    the completion response.
	 * 
	 * @see https://www.w3.org/TR/xml/#sec-predefined-ent
	 */
	private void collectPredefinedEntityProposals(Range entityRange, boolean markdown, ICompletionRequest request,
			ICompletionResponse response) {
		PredefinedEntity[] entities = PredefinedEntity.values();
		for (PredefinedEntity entity : entities) {
			fillCompletion(entity.name(), entity.getValue(), false, true, entityRange, markdown, response);
		}
	}

	private static void fillCompletion(String name, String entityValue, boolean external, boolean predefined,
			Range entityRange, boolean markdown, ICompletionResponse response) {
		String entityName = "&" + name + ";";
		CompletionItem item = new CompletionItem();
		item.setLabel(entityName);
		item.setKind(CompletionItemKind.Keyword);
		item.setInsertTextFormat(InsertTextFormat.PlainText);
		String insertText = entityName;
		item.setFilterText(insertText);
		item.setTextEdit(new TextEdit(entityRange, insertText));
		item.setDocumentation(
				EntitiesDocumentationUtils.getDocumentation(name, entityValue, external, predefined, markdown));
		response.addCompletionItem(item);
	}

}
