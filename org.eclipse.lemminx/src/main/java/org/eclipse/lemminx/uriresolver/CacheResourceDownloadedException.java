/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.uriresolver;

import java.nio.file.Path;
import java.text.MessageFormat;

/**
 * Exception thrown when a resource (XML Schema, DTD) has error while
 * downloading.
 *
 */
public class CacheResourceDownloadedException extends CacheResourceException {

	private static final long serialVersionUID = 1L;

	public enum CacheResourceDownloadedError {

		ERROR_WHILE_DOWNLOADING("Error while downloading ''{0}'' to ''{1}''.");

		private final String rawMessage;

		private CacheResourceDownloadedError(String rawMessage) {
			this.rawMessage = rawMessage;
		}

		public String getMessage(Object... arguments) {
			return MessageFormat.format(rawMessage, arguments);
		}

	}

	private CacheResourceDownloadedError errorCode;

	public CacheResourceDownloadedException(String resourceURI, Path resourceCachePath, Throwable e) {
		this(resourceURI, resourceCachePath, CacheResourceDownloadedError.ERROR_WHILE_DOWNLOADING, e);
	}

	public CacheResourceDownloadedException(String resourceURI, Path resourceCachePath,
			CacheResourceDownloadedError errorCode, Throwable e) {
		super(resourceURI, errorCode.getMessage(resourceURI, resourceCachePath), e);
		this.errorCode = errorCode;
	}

	public CacheResourceDownloadedError getErrorCode() {
		return errorCode;
	};
}
