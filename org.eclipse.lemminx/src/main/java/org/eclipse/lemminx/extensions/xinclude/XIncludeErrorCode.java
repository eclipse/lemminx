/**
 *  Copyright (c) 2022 Red Hat Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.xinclude;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.lemminx.extensions.xinclude.XIncludeUtils.HREF_ATTR;
import static org.eclipse.lemminx.extensions.xinclude.XIncludeUtils.PARSE_ATTR;
import static org.eclipse.lemminx.extensions.xinclude.XIncludeUtils.ACCEPT_ATTR;
import static org.eclipse.lemminx.extensions.xinclude.XIncludeUtils.ACCEPT_LANGUAGE_ATTR;
import static org.eclipse.lemminx.extensions.xinclude.XIncludeUtils.XPOINTER_ATTR;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.diagnostics.IXMLErrorCode;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

/**
 * XInclude error code.
 *
 * @see https://wiki.xmldation.com/Support/Validator
 *
 *      All error code types and messages can be found in the Xerces library
 *      https://github.com/apache/xerces2-j/blob/trunk/src/org/apache/xerces/impl/msg/XIncludeMessages.properties
 *
 */
public enum XIncludeErrorCode implements IXMLErrorCode {

	NoFallback, //
	MultipleFallbacks, //
	FallbackParent, //
	IncludeChild, //
	FallbackChild, //
	HrefMissing, //
	RecursiveInclude, //
	InvalidParseValue, //
	XMLParseError, //
	XMLResourceError, //
	TextResourceError, //
	NonDuplicateNotation, //
	NonDuplicateUnparsedEntity, //
	XpointerMissing, //
	AcceptMalformed, //
	AcceptLanguageMalformed, //
	RootElementRequired, //
	MultipleRootElements, //
	ContentIllegalAtTopLevel, //
	UnexpandedEntityReferenceIllegal, //
	HrefFragmentIdentifierIllegal, //
	HrefSyntacticallyInvalid, //
	XPointerStreamability, //
	XPointerResolutionUnsuccessful;

	private final String code;

	private XIncludeErrorCode() {
		this(null);
	}

	private XIncludeErrorCode(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	private final static Map<String, XIncludeErrorCode> codes;

	static {
		codes = new HashMap<>();
		for (XIncludeErrorCode errorCode : values()) {
			codes.put(errorCode.getCode(), errorCode);
		}
	}

	public static XIncludeErrorCode get(String name) {
		return codes.get(name);
	}

	/**
	 * Create the LSP range from the SAX error.
	 *
	 * @param characterOffset
	 * @param key
	 * @param arguments
	 * @param document
	 * @return the LSP range from the SAX error.
	 */
	public static Range toLSPRange(XMLLocator location, XIncludeErrorCode code, Object[] arguments,
			DOMDocument document) {
		int offset = location.getCharacterOffset() - 1;
		// adjust positions
		switch (code) {
			case AcceptMalformed:
				return XMLPositionUtility.selectAttributeValueAt(ACCEPT_ATTR, offset, document);
			case AcceptLanguageMalformed:
				return XMLPositionUtility.selectAttributeValueAt(ACCEPT_LANGUAGE_ATTR, offset, document);
			case InvalidParseValue:
				return XMLPositionUtility.selectAttributeValueAt(PARSE_ATTR, offset, document);
			case NoFallback:
			case XMLResourceError:
			case HrefFragmentIdentifierIllegal:
			case HrefSyntacticallyInvalid:
			case TextResourceError:
			case XMLParseError:
				return XMLPositionUtility.selectAttributeValueAt(HREF_ATTR, offset, document);
			case XPointerStreamability:
			case XPointerResolutionUnsuccessful:
				return XMLPositionUtility.selectAttributeValueAt(XPOINTER_ATTR, offset, document);
			case MultipleFallbacks:
			case FallbackParent:
			case IncludeChild:
			case FallbackChild:
			case HrefMissing:
			case RecursiveInclude:
			case NonDuplicateNotation:
			case NonDuplicateUnparsedEntity:
			case XpointerMissing:
			case RootElementRequired:
			case MultipleRootElements:
			case ContentIllegalAtTopLevel:
			case UnexpandedEntityReferenceIllegal:
				return XMLPositionUtility.selectStartTagName(offset, document);
		}
		return null;
	}

}
