package org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.diagnostics.IXMLErrorCode;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

public enum RNGErrorCode implements IXMLErrorCode {

	// TODO: fill out the codes
	IncompleteContentModel,
	BadTagName,
	BadAttribute,
	MissingAttribute,
	InvalidRelaxNG;

	private final String code;
	private final static Map<String, RNGErrorCode> codes;

	static {
		codes = new HashMap<>();
		for (RNGErrorCode errorCode : values()) {
			codes.put(errorCode.getCode(), errorCode);
		}
	}

	private RNGErrorCode() {
		this(null);
	}

	private RNGErrorCode(String code) {
		this.code = code;
	}

	public static RNGErrorCode get(String name) {
		return codes.get(name);
	}

	@Override
	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	// -------------------------------------------------------------------------

	private static final Pattern BAD_ATTRIBUTE_ATTRIBUTE_EXTRACTOR = Pattern.compile("[^\"]+\"([^\"]*)\"");

	public static Range toLSPRange(XMLLocator location, RNGErrorCode code, String message, Object[] arguments,
			DOMDocument document) {
		int offset = location.getCharacterOffset() - 1;
		switch (code) {
			case BadTagName:
			case MissingAttribute:
				return XMLPositionUtility.selectStartTagName(offset, document);
			case BadAttribute: {
				Matcher m = BAD_ATTRIBUTE_ATTRIBUTE_EXTRACTOR.matcher(message);
				m.find();
				String attrName = m.group(1);
				return XMLPositionUtility.selectAttributeNameFromGivenNameAt(attrName, offset, document);
			}
			case InvalidRelaxNG: {
				return XMLPositionUtility.selectRootStartTag(document);
			}
			default:
				return null;
		}
	}

}
