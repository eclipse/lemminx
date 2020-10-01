/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.model;

import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelProvider.Identifier;
import org.eclipse.lemminx.uriresolver.ResolvedURIInfo;

/**
 * Class which holds a referenced grammar information.
 * 
 * <p>
 * In other words, it gives information about
 * 
 * <ul>
 * <li>which XSD, DTD file is bound to the DOM document.</li>
 * <li>which binding strategies is used (catalog, file association,
 * xsi:schemaLocation, xsi:noNamespaceSchemaLocation, DOCTYPE).</li>
 * <li>the cache file path (for remote grammar with http://)</li>
 * 
 * </p>
 */
public class ReferencedGrammarInfo {

	private final ResolvedURIInfo resolvedURIInfo;
	private final GrammarCacheInfo grammarCacheInfo;

	private final Identifier identifier;

	public ReferencedGrammarInfo(ResolvedURIInfo resolvedURIInfo, GrammarCacheInfo grammarCacheInfo,
			Identifier identifier) {
		this.resolvedURIInfo = resolvedURIInfo;
		this.grammarCacheInfo = grammarCacheInfo;
		this.identifier = identifier;
	}

	/**
	 * Returns the resolved URI information (the result of the resolve and the
	 * resolution strategy (ex : catalog, file association, etc)).
	 * 
	 * @return the resolved URI information (the result of the resolve and the
	 *         resolution strategy
	 */
	public ResolvedURIInfo getResolvedURIInfo() {
		return resolvedURIInfo;
	}

	/**
	 * Returns the grammar cache information and null otherwise. (grammar is not a
	 * remote grammar with http://)
	 * 
	 * @return the grammar cache information and null otherwise.
	 */
	public GrammarCacheInfo getGrammarCacheInfo() {
		return grammarCacheInfo;
	}

	/**
	 * Returns the identifier (xml-model, xsi:schemaLocation,
	 * xsi:noNamespaceSchemaLocation, DOCTYPE) and null otherwise.
	 * 
	 * @return the identifier (xml-model, xsi:schemaLocation,
	 *         xsi:noNamespaceSchemaLocation, DOCTYPE) and null otherwise.
	 */
	public Identifier getIdentifier() {
		return identifier;
	}

	/**
	 * Returns true if the grammar is in the .lemminx cache and false otherwise.
	 * 
	 * @return true if the grammar is in the .lemminx cache and false otherwise.
	 */
	public boolean isInCache() {
		return grammarCacheInfo != null;
	}

}
