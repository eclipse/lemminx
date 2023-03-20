/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.telemetry;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.model.ReferencedGrammarInfo;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.URIUtils;

public class DocumentTelemetryInfo {

	private static final String DOC_PROP_EXT = "file.extension";
	private static final String DOC_PROP_RESOLVER = "file.resolver";
	private static final String DOC_PROP_IDENTIFIER = "file.identifier";
	private static final String DOC_PROP_GRAMMAR_NONE = "file.grammar.none";
	private static final String DOC_PROP_GRAMMAR_DOCTYPE = "file.grammar.doctype";
	private static final String DOC_PROP_GRAMMAR_XMLMODEL = "file.grammar.xmlmodel";
	private static final String DOC_PROP_GRAMMAR_RELAXNG = "file.grammar.relaxng";
	private static final String DOC_PROP_GRAMMAR_SCHEMALOC = "file.grammar.schemalocation";
	private static final String DOC_PROP_GRAMMAR_NONSSCHEMALOC = "file.grammar.nonsschemalocation";

	public static void collectDocumentTelemetryInfo (DOMDocument doc, ContentModelManager manager, TelemetryCache cache) {
		String uri = doc.getDocumentURI();
		int index = uri.lastIndexOf('.');
		String fileExtension = uri.substring(index + 1, uri.length()).toLowerCase();
		boolean isXML = !DOMUtils.isXSD(doc) && !DOMUtils.isDTD(uri);
		Set<ReferencedGrammarInfo> referencedGrammarInfos = manager.getReferencedGrammarInfos(doc);
		cache.put(DOC_PROP_EXT, fileExtension);

		if (referencedGrammarInfos.isEmpty()) {
			if (isXML) {
				cache.put(DOC_PROP_GRAMMAR_NONE);
			}
		} else {
			for (ReferencedGrammarInfo info : referencedGrammarInfos) {
				String kind = info.getBindingKind() == null ? "none" : info.getBindingKind();
				String resolver = info.getResolvedBy();
				String identifier = info.getIdentifierURI();
				if (isXML) {
					switch (kind) {
					case "none":
						cache.put(DOC_PROP_GRAMMAR_NONE);
						break;
					case "doctype":
						cache.put(DOC_PROP_GRAMMAR_DOCTYPE);
						break;
					case "xml-model":
						if (DOMUtils.isRelaxNGUri(info.getIdentifierURI())) {
							cache.put(DOC_PROP_GRAMMAR_RELAXNG);
						} else {
							cache.put(DOC_PROP_GRAMMAR_XMLMODEL);
						}
						break;
					case "xsi:schemaLocation":
						cache.put(DOC_PROP_GRAMMAR_SCHEMALOC);
						break;
					case "xsi:noNamespaceSchemaLocation":
						cache.put(DOC_PROP_GRAMMAR_NONSSCHEMALOC);
						break;
					}
				}
				if (identifier != null) {
					try {
						// non-local identifiers only
						if (new URI(identifier).getScheme() != null && !URIUtils.isFileResource(identifier)) {
							int limit = Math.min(identifier.length(), 200); // 200 char limit
							String shortId = identifier.substring(0, limit);
							cache.put(DOC_PROP_IDENTIFIER, shortId);
						}
					} catch (URISyntaxException e) {
						// continue
					}
				}
				cache.put(String.join(".", DOC_PROP_RESOLVER, resolver == null ? "default" : resolver));
			}
		}
	}
}
