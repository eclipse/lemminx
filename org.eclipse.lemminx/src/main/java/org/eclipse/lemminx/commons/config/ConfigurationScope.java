/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.commons.config;

/**
 * Configuration scope for settings fields, mirroring VS Code's setting scopes.
 *
 * @see Scope
 */
public enum ConfigurationScope {

	/**
	 * The setting is global and not scoped per document or folder.
	 */
	GLOBAL,

	/**
	 * The setting can be overridden per folder or per language.
	 */
	RESOURCE
}
