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
package org.eclipse.lemminx.extensions.xsl.participants;

import static org.eclipse.lemminx.utils.FilesUtils.getFileName;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.filepath.IFilePathExpression;
import org.eclipse.lemminx.extensions.filepath.IFilePathSupportParticipant;
import org.eclipse.lemminx.extensions.filepath.settings.FilePathExpression;
import org.eclipse.lemminx.utils.DOMUtils;

/**
 * XML Stylesheet file path support for *.xsl to provide completion for
 * attributes :
 * 
 * <ul>
 * <li>include/@href</li>
 * <li>import/@href</li>
 * </ul>
 */
public class XSLFilePathSupportParticipant implements IFilePathSupportParticipant {

	private static class XSLFilePathExpression extends FilePathExpression {

		public XSLFilePathExpression(String xpath) {
			super(xpath);
		}

		@Override
		protected boolean acceptFile(Path path) {
			return DOMUtils.isXSL(getFileName(path));
		}
	}

	private static final List<IFilePathExpression> XSL_FILE_PATH_EXPRESSIONS;

	static {
		XSL_FILE_PATH_EXPRESSIONS = Arrays.asList(new XSLFilePathExpression("include/@href"),
				new XSLFilePathExpression("import/@href"));
	}

	@Override
	public List<IFilePathExpression> collectFilePathExpressions(DOMDocument document) {
		if (!DOMUtils.isXSL(document)) {
			return Collections.emptyList();
		}
		return XSL_FILE_PATH_EXPRESSIONS;
	}

}
