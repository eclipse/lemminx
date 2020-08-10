/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.xerces.xmlmodel;

/**
 * XML model constants.
 *
 * @see <a href=
 *      "https://www.w3.org/TR/xml-model/">https://www.w3.org/TR/xml-model/</a>
 */
public class XMLModelConstants {

	private XMLModelConstants() {
	}

	public static final String XML_MODEL_PI = "xml-model";

	// xml-model atributes
	public static final String HREF_ATTR = "href";
	public static final String TYPE_ATTR = "type";
	public static final String SCHEMATYPENS_ATTR = "schematypens";

	// type values
	// see https://www.w3.org/TR/xml-model/#d0e689
	public static final String DTD_TYPE = "application/xml-dtd";
	public static final String APPLICATION_XML_TYPE = "application/xml";
	public static final String APPLICATION_RELAXNG_COMPACT_SYNTAX_TYPE = "application/relax-ng-compact-syntax";

	// schematypens values
	// see https://www.w3.org/TR/xml-model/#d0e689
	public static final String RELAXNG_SCHEMATYPENS = "http://relaxng.org/ns/structure/1.0";
	public static final String W3C_XML_SCHEMA_SCHEMATYPENS = "http://www.w3.org/2001/XMLSchema";
	public static final String SCHEMATRON_SCHEMATYPENS = "http://purl.oclc.org/dsdl/schematron";
	public static final String NVDL_SCHEMATYPENS = "http://purl.oclc.org/dsdl/nvdl/ns/structure/1.0";
}
