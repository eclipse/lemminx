package org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.lemminx.extensions.contentmodel.participants.DTDErrorCode;

// TODO: move to correct package?
public enum RNGErrorCode {

	// TODO: fill out the codes
	IncompleteContentModel;

	private static Map<String, RNGErrorCode> codes;

	static {
		codes = new HashMap<>();
		for (RNGErrorCode errorCode : values()) {
			codes.put(errorCode.getCode(), errorCode);
		}
	}

	public static RNGErrorCode get(String name) {
		return codes.get(name);
	}

	private String getCode() {
		return null; // TODO: implement
	}

}
