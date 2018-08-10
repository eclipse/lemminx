/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services.extensions;

import org.eclipse.lsp4j.Hover;

/**
 * Hover participant API.
 *
 */
public interface IHoverParticipant {

	Hover onTag(IHoverRequest request) throws Exception;

	Hover onAttributeName(IHoverRequest request) throws Exception;

	Hover onAttributeValue(IHoverRequest request) throws Exception;

}
