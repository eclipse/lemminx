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
package org.eclipse.lemminx.extensions.references.settings;

import java.util.List;

import org.eclipse.lemminx.utils.JSONUtility;

/**
 * XML references settings:
 * 
 * <code>
 * "xml.references": [
 * // references for tei.xml files
 * {
 *   "pattern": "*.xml",
 *   "expressions": [
 *     {
 *       "prefix": "#",
 *       "from": "@resp",
 *      "to": "persName/@xml:id"
 *      },
 *     {
 *       "prefix": "#",
 *       "from": "@corresp",
 *       "to": "@xml:id"
 *     }
 *   ]
 * },
 * // references for docbook.xml files
 * {
 *   "pattern": "*.xml",
 *   "expressions": [
 *     {
 *       "from": "xref/@linkend",
 *       "to": "@id"
 *     }
 *   ]
 * }
 *]
 * 
 * </code>
 * 
 * @author Angelo ZERR
 *
 */
public class XMLReferencesSettings {

	private List<XMLReferences> references;

	public List<XMLReferences> getReferences() {
		return references;
	}

	public void setReferences(List<XMLReferences> references) {
		this.references = references;
	}

	public static XMLReferencesSettings getXMLReferencesSettings(Object initializationOptionsSettings) {
		return JSONUtility.toModel(initializationOptionsSettings, XMLReferencesSettings.class);
	}

}
