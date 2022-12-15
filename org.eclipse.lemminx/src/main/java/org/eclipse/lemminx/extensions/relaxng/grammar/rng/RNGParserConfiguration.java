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
package org.eclipse.lemminx.extensions.relaxng.grammar.rng;

import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics.LSPErrorReporterForXML;

/**
 * RNG parser configuration used to set the LSP error reporter.
 * 
 * @author Angelo ZERR
 *
 */
public class RNGParserConfiguration extends XIncludeAwareParserConfiguration {

	
	public RNGParserConfiguration (LSPErrorReporterForXML reporter) {
		fErrorReporter = reporter;
	}
}
