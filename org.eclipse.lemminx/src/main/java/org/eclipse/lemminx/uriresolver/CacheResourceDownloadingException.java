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
import java.util.concurrent.CompletableFuture;

/**
 * Exception thrown when a resource (XML Schema, DTD) is downloading.
 *
 */
public class CacheResourceDownloadingException extends CacheResourceException {

	private static final long serialVersionUID = 1L;

	public enum CacheResourceDownloadingError {

		DOWNLOAD_DISABLED("Downloading external resources is disabled."), //
		
		RESOURCE_LOADING("The resource ''{0}'' is downloading in the cache path ''{1}''."), //

		RESOURCE_NOT_IN_DEPLOYED_PATH("The resource ''{0}'' cannot be downloaded in the cache path ''{1}''.");

		private final String rawMessage;

		private CacheResourceDownloadingError(String rawMessage) {
			this.rawMessage = rawMessage;
		}

		public String getMessage(Object... arguments) {
			return MessageFormat.format(rawMessage, arguments);
		}

	}

	private final CompletableFuture<Path> future;

	private final CacheResourceDownloadingError errorCode;

	public CacheResourceDownloadingException(String resourceURI, Path resourceCachePath,
			CacheResourceDownloadingError errorCode, CompletableFuture<Path> future, Throwable e) {
		super(resourceURI, errorCode.getMessage(resourceURI, resourceCachePath), e);
		this.errorCode = errorCode;
		this.future = future;
	}

	public CacheResourceDownloadingError getErrorCode() {
		return errorCode;
	}

	public CompletableFuture<Path> getFuture() {
		return future;
	}
}
