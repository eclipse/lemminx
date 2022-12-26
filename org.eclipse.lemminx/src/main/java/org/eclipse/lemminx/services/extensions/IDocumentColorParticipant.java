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
package org.eclipse.lemminx.services.extensions;

import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.ColorPresentation;
import org.eclipse.lsp4j.ColorPresentationParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Document color participant API.
 * 
 * @author Angelo ZERR
 *
 */
public interface IDocumentColorParticipant {

	/**
	 * Fill the given the <code>colors</code> list of {@link ColorInformation} for
	 * the given DOM document <code>xmlDocument</code>.
	 * 
	 * @param xmlDocument   the DOM document.
	 * @param colors        the colors list to update.
	 * @param cancelChecker the cancel checker.
	 */
	void doDocumentColor(DOMDocument xmlDocument, List<ColorInformation> colors, CancelChecker cancelChecker);

	/**
	 * Fill the given the <code>presentations</code> list of
	 * {@link ColorPresentation} for the given DOM document <code>xmlDocument</code>
	 * and the given <code>params</code> colors presentation parameter.
	 * 
	 * @param xmlDocument   the DOM document.
	 * @param params        the color presentation parameter.
	 * @param presentations the presentations list to update.
	 * @param cancelChecker the cancel checker.
	 */
	void doColorPresentations(DOMDocument xmlDocument, ColorPresentationParams params,
			List<ColorPresentation> presentations,
			CancelChecker cancelChecker);

}
