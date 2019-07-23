/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.TextEdit;

/**
 * Rename participant API.
 *
 */
public interface IRenameParticipant {

	void doRename(IRenameRequest request, List<TextEdit> locations);
}