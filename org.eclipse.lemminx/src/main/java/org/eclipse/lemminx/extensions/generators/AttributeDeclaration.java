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
package org.eclipse.lemminx.extensions.generators;

/**
 * Attribute declaration.
 *
 */
public class AttributeDeclaration {

	private final String name;

	public AttributeDeclaration(String name) {
		this.name = name;
	}

	/**
	 * Returns the attribute name.
	 * 
	 * @return the attribute name.
	 */
	public String getName() {
		return name;
	}
}
