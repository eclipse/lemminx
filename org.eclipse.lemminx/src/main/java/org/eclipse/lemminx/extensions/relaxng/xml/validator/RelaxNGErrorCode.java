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
package org.eclipse.lemminx.extensions.relaxng.xml.validator;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLModelUtils;
import org.eclipse.lemminx.services.extensions.diagnostics.IXMLErrorCode;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

/**
 * RelaxNG error code when XML is validated with *.rng / *.rnc schemas.
 * 
 * @author Angelo ZERR
 * 
 * @see <a href, //
 *      "https://github.com/relaxng/jing-trang/blob/master/mod/pattern/src/main/com/thaiopensource/relaxng/pattern/resources/Messages.properties">https://github.com/relaxng/jing-trang/blob/master/mod/pattern/src/main/com/thaiopensource/relaxng/pattern/resources/Messages.properties</a>
 *
 */
public enum RelaxNGErrorCode implements IXMLErrorCode {

	unknown_element, // element {0} not allowed anywhere{1}
	unexpected_element_required_element_missing, // , element {0} not allowed yet; missing required element {1}
	unexpected_element_required_elements_missing, // element {0} not allowed yet; missing required elements {1}
	element_not_allowed_yet, // element {0} not allowed yet{1}
	out_of_context_element, // element {0} not allowed here{1}
	no_attributes_allowed, // found attribute {0}, but no attributes allowed here
	invalid_attribute_name, // attribute {0} not allowed here{1}
	invalid_attribute_value, // value of attribute {0} is invalid{1}
	required_attributes_missing_expected, // element {0} missing one or more required attributes{1}
	required_attribute_missing, // element {0} missing required attribute {1}
	required_attributes_missing, // element {0} missing required attributes {1}
	incomplete_element_required_elements_missing_expected, // element {0} incomplete{1}
	incomplete_element_required_element_missing, // element {0} incomplete; missing required element {1}
	incomplete_element_required_elements_missing, // element {0} incomplete; missing required elements {1}
	text_not_allowed, // text not allowed here{0}
	document_incomplete, // document incompletely matched
	invalid_element_value, // character content of element {0} invalid{1}
	blank_not_allowed, // empty content for element {0} not allowed{1}
	schema_allows_nothing, // schema does not allow anything: it is equivalent to <notAllowed/>

	RelaxNGNotFound, //
	to_implement;

	private final String code;

	private RelaxNGErrorCode() {
		this(null);
	}

	private RelaxNGErrorCode(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	private final static Map<String, RelaxNGErrorCode> codes;

	static {
		codes = new HashMap<>();
		for (RelaxNGErrorCode errorCode : values()) {
			codes.put(errorCode.getCode(), errorCode);
		}
	}

	public static RelaxNGErrorCode get(String name) {
		return codes.get(name);
	}

	public static Range toLSPRange(XMLLocator location, RelaxNGErrorCode rngCode, Object[] arguments,
			DOMDocument document) {
		int offset = location.getCharacterOffset() - 1;
		// adjust positions
		switch (rngCode) {
			case unknown_element:
			case out_of_context_element:
			case incomplete_element_required_elements_missing_expected:
			case unexpected_element_required_element_missing:
			case incomplete_element_required_element_missing:
			case required_attributes_missing_expected:
			case required_attributes_missing:
			case required_attribute_missing: {
				return XMLPositionUtility.selectStartTagName(offset, document);
			}
			case no_attributes_allowed:
			case invalid_attribute_name: {
				String attrName = (String) arguments[0];
				return XMLPositionUtility.selectAttributeNameFromGivenNameAt(attrName, offset, document);
			}
			case invalid_attribute_value: {
				String attrName = (String) arguments[0];
				return XMLPositionUtility.selectAttributeValueAt(attrName, offset, document);
			}
			case RelaxNGNotFound: {
				// Check if RelaxNG location is declared with xml-model/@href
				// ex : <xml-model href=""addressBook.rnc" [
				String hrefLocation = (String) arguments[1];
				DOMRange locationRange = XMLModelUtils.getHrefNode(document, hrefLocation);
				if (locationRange != null) {
					return XMLPositionUtility.createRange(locationRange);
				}
			}
		}
		return null;
	}

}
