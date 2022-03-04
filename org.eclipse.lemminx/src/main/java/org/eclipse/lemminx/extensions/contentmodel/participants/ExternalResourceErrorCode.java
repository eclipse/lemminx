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
package org.eclipse.lemminx.extensions.contentmodel.participants;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.DownloadDisabledResourceCodeAction;
import org.eclipse.lemminx.services.extensions.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.diagnostics.IXMLErrorCode;

/**
 * External resource error code when referenced XSD, DTD (with DOCTYPE,
 * xsi:schemaLocation) is downloaded.
 * 
 */
public enum ExternalResourceErrorCode implements IXMLErrorCode {

	DownloadResourceDisabled, //
	DownloadingResource, //
	ResourceNotInDeployedPath, //
	DownloadProblem;

	private final String code;

	private ExternalResourceErrorCode() {
		this(null);
	}

	private ExternalResourceErrorCode(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	private final static Map<String, ExternalResourceErrorCode> codes;

	static {
		codes = new HashMap<>();
		for (ExternalResourceErrorCode errorCode : values()) {
			codes.put(errorCode.getCode(), errorCode);
		}
	}

	public static ExternalResourceErrorCode get(String name) {
		return codes.get(name);
	}

	public static void registerCodeActionParticipants(Map<String, ICodeActionParticipant> codeActionParticipants) {
		codeActionParticipants.put(DownloadResourceDisabled.getCode(), new DownloadDisabledResourceCodeAction());		
	}
}
