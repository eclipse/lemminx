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

import static org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelConstants.DTD_TYPE;
import static org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelConstants.RELAXNG_SCHEMATYPENS;
import static org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelConstants.W3C_XML_SCHEMA_SCHEMATYPENS;

import org.apache.xerces.xni.XMLString;
import org.eclipse.lemminx.utils.DOMUtils;

/**
 * XML model declaration.
 * 
 * <pre>
 * 	&lt;?xml-model href="http://www.docbook.org/xml/5.0/xsd/docbook.xsd"?&gt;
 * </pre>
 * 
 * 
 * @see https://www.w3.org/TR/xml-model/
 *
 */
public class XMLModelDeclaration {

	private int hrefOffset;
	private String href;

	private int typeOffset;
	private String type;

	private int schematypensOffset;

	private String schematypens;

	/**
	 * Returns the location of the referenced schema
	 * 
	 * @return the location of the referenced schema
	 */
	public String getHref() {
		return href;
	}

	/**
	 * Set the location of the referenced schema
	 * 
	 * @param href the location of the referenced schema
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * Returns the start offset where href is declared.
	 * 
	 * @return the start offset where href is declared.
	 */
	public int getHrefOffset() {
		return hrefOffset;
	}

	/**
	 * Set the start offset where href is declared.
	 * 
	 * @param hrefOffset the start offset where href is declared
	 */
	public void setHrefOffset(int hrefOffset) {
		this.hrefOffset = hrefOffset;
	}

	/**
	 * Returns the location of the type
	 * 
	 * @return the location of the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set the location of the type
	 * 
	 * @param type the location of type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Returns the start offset where type is declared.
	 * 
	 * @return the start offset where type is declared.
	 */
	public int getTypeOffset() {
		return typeOffset;
	}

	/**
	 * Set the start offset where type is declared.
	 * 
	 * @param typeOffset the start offset where type is declared
	 */
	public void setTypeOffset(int typeOffset) {
		this.typeOffset = typeOffset;
	}

	/**
	 * Returns the location of the schematypens
	 * 
	 * @return the location of the schematypens
	 */
	public String getSchematypens() {
		return schematypens;
	}

	/**
	 * Set the location of the schematypens
	 * 
	 * @param schematypens the location of schematypens
	 */
	public void setSchematypens(String schematypens) {
		this.schematypens = schematypens;
	}

	/**
	 * Returns the start offset where schematypens is declared.
	 * 
	 * @return the start offset where schematypens is declared.
	 */
	public int getSchematypensOffset() {
		return schematypensOffset;
	}

	/**
	 * Set the start offset where schematypens is declared.
	 * 
	 * @param schematypensOffset the start offset where schematypens is declared
	 */
	public void setSchematypensOffset(int schematypensOffset) {
		this.schematypensOffset = schematypensOffset;
	}

	private enum State {
		Content, AttName, AfterAttName, AfterEquals, AttValue;
	}

	public static XMLModelDeclaration parse(XMLString data) {
		return parse(data.ch, data.offset, data.length);
	}

	/**
	 * Returns the result of parse the data of xml-model processing insruction.
	 * 
	 * @param data   the xml-model processing instruction content data.
	 * @param offset the offset
	 * @param length the length
	 * @return the result of parse the data of xml-model processing insruction.
	 */
	public static XMLModelDeclaration parse(char[] data, int offset, int length) {
		XMLModelDeclaration model = new XMLModelDeclaration();
		StringBuilder name = new StringBuilder();
		StringBuilder value = new StringBuilder();
		State state = State.Content;
		char equals = '"';
		for (int i = offset; i < length; i++) {
			char ch = data[i];
			switch (state) {
				case Content:
					if (!Character.isWhitespace(ch)) {
						name.append(ch);
						state = State.AttName;
					}
					break;
				case AttName:
					if (Character.isWhitespace(ch)) {
						state = State.AfterAttName;
					} else if (ch == '=') {
						state = State.AfterEquals;
					} else {
						name.append(ch);
					}
					break;
				case AfterAttName:
					if (ch == '=') {
						state = State.AfterEquals;
					}
					break;
				case AfterEquals:
					if (ch == '"' || ch == '\'') {
						equals = ch;
						state = State.AttValue;
					}
					break;
				case AttValue:
					if (ch == equals) {
						state = State.Content;
						switch (name.toString()) {
							case XMLModelConstants.HREF_ATTR:
								String href = value.toString();
								model.setHrefOffset(i - href.length());
								model.setHref(href);
								break;
							case XMLModelConstants.TYPE_ATTR:
								String type = value.toString();
								model.setTypeOffset(i - type.length());
								model.setType(type);
								break;
							case XMLModelConstants.SCHEMATYPENS_ATTR:
								String schematypens = value.toString();
								model.setSchematypensOffset(i - schematypens.length());
								model.setSchematypens(schematypens);
								break;
						}
						name.setLength(0);
						value.setLength(0);
					} else {
						value.append(ch);
					}
					break;
			}
		}
		return model;
	}

	public static boolean isApplicableForDTD(XMLModelDeclaration modelDeclaration) {
		String href = modelDeclaration.getHref();
		String type = modelDeclaration.getType();
		if (DOMUtils.isDTD(href)) {
			return type == null || DTD_TYPE.equals(type);
		}
		return DTD_TYPE.equals(type);
	}

	public static boolean isApplicableForXSD(XMLModelDeclaration modelDeclaration) {
		String href = modelDeclaration.getHref();
		String schematypens = modelDeclaration.getSchematypens();
		if (DOMUtils.isXSD(href)) {
			return schematypens == null || W3C_XML_SCHEMA_SCHEMATYPENS.equals(schematypens);
		}
		return W3C_XML_SCHEMA_SCHEMATYPENS.equals(schematypens);
	}

	public static boolean isApplicableForRelaxNG(XMLModelDeclaration modelDeclaration) {
		String href = modelDeclaration.getHref();
		String schematypens = modelDeclaration.getSchematypens();
		if (DOMUtils.isRelaxNGUri(href)) {
			return schematypens == null || RELAXNG_SCHEMATYPENS.equals(schematypens);
		}
		return RELAXNG_SCHEMATYPENS.equals(schematypens);
	}

}
