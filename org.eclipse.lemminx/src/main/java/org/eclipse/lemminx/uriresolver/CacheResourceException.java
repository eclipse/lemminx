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

import org.eclipse.lemminx.utils.DOMUtils;

/**
 * Base class for cache resource exception.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class CacheResourceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String resourceURI;

	public CacheResourceException(String resourceURI, String message) {
		this(resourceURI, message, null);
	}

	public CacheResourceException(String resourceURI, String message, Throwable cause) {
		super(message, cause);
		this.resourceURI = resourceURI;
	}

	/**
	 * Returns the resource URI which is downloading.
	 * 
	 * @return the resource URI which is downloading.
	 */
	public String getResourceURI() {
		return resourceURI;
	}

	/**
	 * Returns true if it's a DTD which causes the exception and false otherwise.
	 * 
	 * @return true if it's a DTD which causes the exception and false otherwise.
	 */
	public boolean isDTD() {
		return DOMUtils.isDTD(resourceURI);
	}
}
