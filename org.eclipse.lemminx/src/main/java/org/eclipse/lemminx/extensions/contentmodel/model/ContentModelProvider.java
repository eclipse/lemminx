/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel.model;

import java.util.Collection;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMRange;

/**
 * Content model provider API.
 *
 */
public interface ContentModelProvider {

	public class Identifier {

		private final String publicId;

		private final String systemId;

		private final DOMRange range;

		private final String kind;

		public Identifier(String publicId, String systemId, DOMRange range, String kind) {
			this.publicId = publicId;
			this.systemId = systemId;
			this.range = range;
			this.kind = kind;
		}

		public String getPublicId() {
			return publicId;
		}

		public String getSystemId() {
			return systemId;
		}

		public DOMRange getRange() {
			return range;
		}

		public String getKind() {
			return kind;
		}

	}

	/**
	 * Returns true if the given document can be adapted for this content model
	 * and false otherwise.
	 *
	 * @param document the DOM document.
	 * @param internal true if it is an internal content model (ex : DOCCTYPE
	 *                 subset) and false otherwise.
	 */
	boolean adaptFor(DOMDocument document, boolean internal);

	/**
	 * Returns true if the given resource uri can be adapted for this content model
	 * and false otherwise.
	 *
	 * o@param uri the resource Uri.
	 *
	 * @return true if the given resource uri can be adapted for this content model
	 *         and false otherwise.
	 */
	boolean adaptFor(String uri);

	/**
	 * Returns the identifiers list from the given document and namespace.
	 *
	 * @param xmlDocument  the DOM document.
	 * @param namespaceURI the namespace.
	 *
	 * @return the identifiers list from the given document and namespace.
	 */
	Collection<Identifier> getIdentifiers(DOMDocument xmlDocument, String namespaceURI);

	/**
	 * Create content model document (XSD, DTD, etc) from the given resource key and
	 * null otherwise.
	 *
	 * @param key                     the resource key.
	 * @param resolveExternalEntities true if external entities can be resolved and
	 *                                false otherwise.
	 * @return the content model document (XSD, DTD, etc) from the given resource
	 *         key and null otherwise.
	 */
	CMDocument createCMDocument(String key, boolean resolveExternalEntities);

	/**
	 * Create the internal content model (for DOCTYPE subset) from the given DOM
	 * document.
	 *
	 * @param xmlDocument             the DOM document.
	 * @param resolveExternalEntities true if external entities can be resolved and
	 *                                false otherwise.
	 * @return the internal content model (for DOCTYPE subset) from the given DOM
	 *         document.
	 */
	CMDocument createInternalCMDocument(DOMDocument xmlDocument, boolean resolveExternalEntities);
}
