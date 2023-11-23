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

import java.nio.file.Files;
import java.nio.file.Path;

import org.w3c.dom.Node;

/**
 * Basic implementation of the {@link IFilePathExpression}.
 */
public class SimpleFilePathExpression implements IFilePathExpression {

	@Override
	public boolean match(Node node) {
		return true;
	}

	@Override
	public Character getSeparator() {
		return null;
	}

	@Override
	public boolean acceptPath(Path path) {
		if (!Files.isDirectory(path)) {
			return acceptFile(path);
		}
		return true;
	}

	protected boolean acceptFile(Path path) {
		return false;
	}

}
