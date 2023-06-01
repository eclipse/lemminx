/*******************************************************************************
* Copyright (c) 2019, 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions.rename;

import org.eclipse.lemminx.services.extensions.IPositionRequest;

/**
 * Rename request API.
 *
 */
public interface IRenameRequest extends IPositionRequest {
	String getNewText();
}