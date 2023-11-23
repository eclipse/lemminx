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
package org.eclipse.lemminx.extensions.relaxng.grammar.rng;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.filepath.IFilePathExpression;
import org.eclipse.lemminx.extensions.filepath.IFilePathSupportParticipant;
import org.eclipse.lemminx.extensions.filepath.settings.FilePathExpression;
import org.eclipse.lemminx.utils.DOMUtils;

/**
 * RelaxNG file path support for *.rng to provide completion for attributes :
 * 
 * <ul>
 * <li>include/@href</li>
 * <li>externalRef/@href</li>
 * </ul>
 */
public class RNGFilePathSupportParticipant implements IFilePathSupportParticipant {

	private static final List<IFilePathExpression> RNG_FILE_PATH_EXPRESSIONS;

	static {
		RNG_FILE_PATH_EXPRESSIONS = Arrays.asList(new FilePathExpression("include/@href"),
				new FilePathExpression("externalRef/@href"));
	}

	@Override
	public List<IFilePathExpression> collectFilePathExpressions(DOMDocument document) {
		if (!DOMUtils.isRelaxNG(document)) {
			return Collections.emptyList();
		}
		return RNG_FILE_PATH_EXPRESSIONS;
	}

}
