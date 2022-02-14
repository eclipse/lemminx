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
package org.eclipse.lemminx.uriresolver;

import java.text.MessageFormat;

import org.eclipse.lemminx.uriresolver.CacheResourceDownloadingException.CacheResourceDownloadingError;

/**
 * Invalid URI exception.
 *
 * @author Fred Bricon
 *
 */
public class InvalidURIException extends CacheResourceException {

	private static final long serialVersionUID = 1L;
	
	public enum InvalidURIError {

		ILLEGAL_SYNTAX("The ''{0}'' URI cannot be parsed: {1}"),
		
		INVALID_PATH("''{0}'' does not resolve to a valid URI.");

		private final String rawMessage;

		private InvalidURIError(String rawMessage) {
			this.rawMessage = rawMessage;
		}

		public String getMessage(Object... arguments) {
			return MessageFormat.format(rawMessage, arguments);
		}

	}
	
	private final InvalidURIError errorCode;
	
	public InvalidURIException(String resourceURI, InvalidURIError errorCode, Throwable cause) {
		super(resourceURI,  errorCode.getMessage(resourceURI, cause.getMessage()));
		this.errorCode = errorCode;
	}

	public InvalidURIException(String resourceURI, InvalidURIError errorCode) {
		super(resourceURI, errorCode.getMessage(resourceURI));
		this.errorCode = errorCode;
	}

	public InvalidURIError getErrorCode() {
		return errorCode;
	}

}
