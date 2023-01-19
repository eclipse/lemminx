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
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.missingelement;

/**
 * Data entry field used in the codeAction/resolve for inserting missing
 * elements.
 *
 */
public class MissingElementDataConstants {

	/**
	 * The element name used to identify which choice element is to be generated.
	 */
	public static final String DATA_ELEMENT_FIELD = "element";

	/**
	 * Indicates whether only required elements is to be generated or all.
	 */
	public static final String DATA_REQUIRED_FIELD = "onlyGenerateRequired";

}
