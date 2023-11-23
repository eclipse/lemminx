/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.catalog.participants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.filepath.IFilePathExpression;
import org.eclipse.lemminx.extensions.filepath.IFilePathSupportParticipant;
import org.eclipse.lemminx.extensions.filepath.settings.FilePathExpression;
import org.eclipse.lemminx.utils.DOMUtils;

/**
 * Catalog file path support for catalog.xml to provide completion for @uri
 * attribute.
 */
public class CatalogFilePathSupportParticipant implements IFilePathSupportParticipant {

	private static final List<IFilePathExpression> CATALOG_FILE_PATH_EXPRESSIONS;

	static {
		CATALOG_FILE_PATH_EXPRESSIONS = Arrays.asList(new FilePathExpression("@uri"));
	}

	@Override
	public List<IFilePathExpression> collectFilePathExpressions(DOMDocument document) {
		if (!DOMUtils.isCatalog(document)) {
			return Collections.emptyList();
		}
		// The DOM document is an XML catalog, returns their expressions.
		return CATALOG_FILE_PATH_EXPRESSIONS;
	}

}
