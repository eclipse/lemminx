/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.settings;

/**
 * XML experimental capabilities.
 *
 */
public class XMLExperimentalSettings {

	private XMLIncrementalSupportSettings incrementalSupport;

	public void setIncrementalSupport(XMLIncrementalSupportSettings incrementalSupport) {
		this.incrementalSupport = incrementalSupport;
	}

	public XMLIncrementalSupportSettings getIncrementalSupport() {
		if(incrementalSupport == null) {
			incrementalSupport = new XMLIncrementalSupportSettings();
		}
		return incrementalSupport;
	}
}
