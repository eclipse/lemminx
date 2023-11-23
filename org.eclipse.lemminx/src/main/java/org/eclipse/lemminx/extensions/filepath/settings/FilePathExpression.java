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
package org.eclipse.lemminx.extensions.filepath.settings;

import static org.eclipse.lemminx.utils.FilesUtils.getFileName;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.lemminx.extensions.filepath.SimpleFilePathExpression;
import org.eclipse.lemminx.xpath.matcher.XPathMatcher;
import org.w3c.dom.Node;

/**
 * File path expression
 * 
 * <code>
 * {
     "xpath": "@paths",
     "separator": " "
   }
 * </code>
 * 
 * @author Angelo ZERR
 *
 */
public class FilePathExpression extends SimpleFilePathExpression {

	private transient XPathMatcher pathMatcher;

	private String xpath;

	private Character separator;

	private List<String> filter;

	public FilePathExpression() {
		this(null);
	}

	public FilePathExpression(String xpath) {
		setXPath(xpath);
	}

	public String getXPath() {
		return xpath;
	}

	@Override
	public Character getSeparator() {
		return separator;
	}

	public List<String> getFilter() {
		return filter;
	}

	public FilePathExpression setXPath(String xpath) {
		this.xpath = xpath;
		return this;
	}

	public FilePathExpression setSeparator(Character separator) {
		this.separator = separator;
		return this;
	}

	public FilePathExpression setFilter(List<String> filter) {
		this.filter = filter;
		return this;
	}

	@Override
	public boolean match(final Node node) {
		if (xpath == null) {
		return false;
		}
		if (pathMatcher == null) {
			pathMatcher = new XPathMatcher(xpath);
		}
		return pathMatcher.match(node);
	}

	@Override
	protected boolean acceptFile(Path path) {
		if (filter == null || filter.isEmpty()) {
			return true;
		}
		String fileName = getFileName(path);
		for (String fileExtension : filter) {
			if (fileName.endsWith(fileExtension)) {
				return true;
			}
		}
		return false;
	}
}
