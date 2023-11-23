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

import java.util.List;

/**
 * File path support settings.
 */
public class FilePathSupport {

	private List<FilePathMapping> mappings;

	public List<FilePathMapping> getMappings() {
		return mappings;
	}

	public void setMappings(List<FilePathMapping> mappings) {
		this.mappings = mappings;
	}
}
