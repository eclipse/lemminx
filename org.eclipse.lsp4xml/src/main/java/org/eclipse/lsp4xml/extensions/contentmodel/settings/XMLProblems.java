/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.settings;

import org.eclipse.lsp4j.DiagnosticSeverity;

/**
 * XML problems settings.
 *
 */
public class XMLProblems {

	/**
	 * This severity preference to mark the root element of XML document which is
	 * not bound to a XML Schema/DTD.
	 * 
	 * Values are {ignore, hint, info, warning, error}
	 */
	private String noGrammar;

	public void setNoGrammar(String noGrammar) {
		this.noGrammar = noGrammar;
	}

	public String getNoGrammar() {
		return noGrammar;
	}

	/**
	 * Returns the <code>noGrammar</code> severity according the given settings and
	 * {@link DiagnosticSeverity#Hint} otherwise.
	 * 
	 * @param settings the settings
	 * @return the <code>noGrammar</code> severity according the given settings and
	 *         {@link DiagnosticSeverity#Hint} otherwise.
	 */
	public static DiagnosticSeverity getNoGrammarSeverity(ContentModelSettings settings) {
		DiagnosticSeverity defaultSeverity = DiagnosticSeverity.Hint;
		if (settings == null || settings.getProblems() == null) {
			return defaultSeverity;
		}
		XMLProblems problems = settings.getProblems();
		String noGrammar = problems.getNoGrammar();
		if ("ignore".equalsIgnoreCase(noGrammar)) {
			// Ignore "noGrammar", return null.
			return null;
		} else if ("info".equalsIgnoreCase(noGrammar)) {
			return DiagnosticSeverity.Information;
		} else if ("warning".equalsIgnoreCase(noGrammar)) {
			return DiagnosticSeverity.Warning;
		} else if ("error".equalsIgnoreCase(noGrammar)) {
			return DiagnosticSeverity.Error;
		}
		return defaultSeverity;
	}
}
