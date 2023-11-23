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
package org.eclipse.lemminx.extensions.xsd.participants;

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
 * XML Schema file path support for *.xsd to provide completion for attributes :
 * 
 * <ul>
 * <li>include/@schemaLocation</li>
 * <li>import/@schemaLocation</li>
 * </ul>
 */
public class XSDFilePathSupportParticipant implements IFilePathSupportParticipant {

	private static class XSDFilePathExpression extends FilePathExpression {

		public XSDFilePathExpression(String xpath) {
			super(xpath);
		}

		@Override
		protected boolean acceptFile(Path path) {
			return DOMUtils.isXSD(getFileName(path));
		}
	}

	private static final List<IFilePathExpression> XSD_FILE_PATH_EXPRESSIONS;

	static {
		XSD_FILE_PATH_EXPRESSIONS = Arrays.asList(new XSDFilePathExpression("include/@schemaLocation"),
				new XSDFilePathExpression("import/@schemaLocation"));
	}

	@Override
	public List<IFilePathExpression> collectFilePathExpressions(DOMDocument document) {
		if (!DOMUtils.isXSD(document)) {
			return Collections.emptyList();
		}
		return XSD_FILE_PATH_EXPRESSIONS;
	}

}
