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
package org.eclipse.lemminx.extensions.filepath;

import java.nio.file.Path;

import org.w3c.dom.Node;

/**
 * File path support expression API.
 */
public interface IFilePathExpression {

	/**
	 * Returns true if the given DOM node matches the file path expression and false
	 * otherwise.
	 * 
	 * @param node the DOM node.
	 * 
	 * @return true if the given DOM node matches the file path expression and false
	 *         otherwise.
	 */
	boolean match(Node node);

	/**
	 * Returns the separator character (ex: ';') used to separate multiple files
	 * declaration (ex:
	 * file1.xml;file2.xml) and null otherwise.
	 * 
	 * @return the separator character (ex: ';') used to separate multiple files
	 *         declaration (ex:
	 *         file1.xml;file2.xml) and null otherwise.
	 */
	Character getSeparator();

	/**
	 * Returns true if given file path is allowed for the file path completion and
	 * false otherwise.
	 * 
	 * @param path the file path.
	 * 
	 * @return true if given file path is allowed for the file path completion and
	 *         false otherwise.
	 */
	boolean acceptPath(Path path);

}
