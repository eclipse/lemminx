/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.references.participants;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.lemminx.services.extensions.diagnostics.IXMLErrorCode;

/**
 * XML references error code.
 *
 */
public enum XMLReferencesErrorCode implements IXMLErrorCode {

	UndefinedReference, InvalidPrefix;

	private final String code;

	private XMLReferencesErrorCode() {
		this(null);
	}

	private XMLReferencesErrorCode(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	private final static Map<String, XMLReferencesErrorCode> codes;

	static {
		codes = new HashMap<>();
		for (XMLReferencesErrorCode errorCode : values()) {
			codes.put(errorCode.getCode(), errorCode);
		}
	}

	public static XMLReferencesErrorCode get(String name) {
		return codes.get(name);
	}

}
