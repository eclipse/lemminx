/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.relaxng.utils;

import org.eclipse.lemminx.extensions.contentmodel.model.FilesChangedTracker;
import org.eclipse.lemminx.extensions.relaxng.jing.RelaxNGGrammar;
import org.eclipse.lemminx.utils.URIUtils;

/**
 * RelaxNG utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class RelaxNGUtils {

	public static FilesChangedTracker createFilesChangedTracker(RelaxNGGrammar grammar) {
		FilesChangedTracker tracker = new FilesChangedTracker();
		// Track the grammar
		String relaxNGURI = getRelaxNGURI(grammar);
		if (relaxNGURI != null && URIUtils.isFileResource(relaxNGURI)) {
			// The RelaxNG is a file, track when file changed
			tracker.addFileURI(relaxNGURI);
		}
		return tracker;
	}

	private static String getRelaxNGURI(RelaxNGGrammar grammar) {
		return grammar.getGrammarDescription().getExpandedSystemId();
	}

}
