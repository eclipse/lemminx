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
package org.eclipse.lsp4xml.uriresolver;

/**
 * Exception thrown when a resource (XML Schema, DTD) has error while
 * downloading.
 *
 */
public class CacheResourceDownloadedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CacheResourceDownloadedException(String message, Throwable cause) {
		super(message, cause);
	}
}
