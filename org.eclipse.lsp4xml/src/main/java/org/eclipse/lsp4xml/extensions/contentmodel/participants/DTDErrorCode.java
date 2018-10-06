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
package org.eclipse.lsp4xml.extensions.contentmodel.participants;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics.IXMLErrorCode;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * DTD error code.
 * 
 * @see https://wiki.xmldation.com/Support/Validator
 *
 */
public enum DTDErrorCode implements IXMLErrorCode {

	MSG_ELEMENT_NOT_DECLARED, MSG_CONTENT_INCOMPLETE, MSG_CONTENT_INVALID, MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED,
	MSG_ATTRIBUTE_NOT_DECLARED, MSG_ATTRIBUTE_VALUE_NOT_IN_LIST;

	private final String code;

	private DTDErrorCode() {
		this(null);
	}

	private DTDErrorCode(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	private final static Map<String, DTDErrorCode> codes;

	static {
		codes = new HashMap<>();
		for (DTDErrorCode errorCode : values()) {
			codes.put(errorCode.getCode(), errorCode);
		}
	}

	public static DTDErrorCode get(String name) {
		return codes.get(name);
	}

	/**
	 * Create the LSP range from the SAX error.
	 * 
	 * @param location
	 * @param key
	 * @param arguments
	 * @param document
	 * @return the LSP range from the SAX error.
	 */
	public static Range toLSPRange(XMLLocator location, DTDErrorCode code, Object[] arguments, XMLDocument document) {
		int offset = location.getCharacterOffset() - 1;
		// adjust positions
		switch (code) {
		case MSG_CONTENT_INCOMPLETE:
		case MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED:
		case MSG_ELEMENT_NOT_DECLARED:
			return XMLPositionUtility.selectStartTag(offset, document);
		case MSG_ATTRIBUTE_NOT_DECLARED:
			return XMLPositionUtility.selectAttributeNameAt(offset, document);
		case MSG_ATTRIBUTE_VALUE_NOT_IN_LIST: {
			String attrName = "";
			return XMLPositionUtility.selectAttributeValueAt(attrName, offset, document);
		}
		}
		return null;
	}
}
