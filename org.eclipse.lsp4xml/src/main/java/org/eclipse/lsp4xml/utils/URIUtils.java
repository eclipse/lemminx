/*******************************************************************************
* Copyright (c) 2018 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml.utils;

/**
 * URIUtils
 */
public class URIUtils {

	private URIUtils() {
	}

	/**
	 * Returns <code>true</code> if the given URL is a remote resource,
	 * <code>false</code> otherwise.
	 * 
	 * @param resourceURI
	 * @return <code>true</code> if the given URL is a remote resource, false
	 *         otherwise.
	 */
	public static boolean isRemoteResource(String resourceURI) {
		return resourceURI != null && (resourceURI.startsWith("http:") || resourceURI.startsWith("https:")
				|| resourceURI.startsWith("ftp:"));
	}

	/**
	 * Returns <code>true</code> if the given URL is a file resource,
	 * <code>false</code> otherwise.
	 * 
	 * @param resourceURI
	 * @return <code>true</code> if the given URL is a file resource, false
	 *         otherwise.
	 */
	public static boolean isFileResource(String resourceURI) {
		return resourceURI != null && (resourceURI.startsWith("file:"));
	}

	/**
	 * Returns a well folder URI which ends with '/' according the URI specification
	 * https://tools.ietf.org/html/rfc3986#section-6 which is used with Xerces
	 * XMLEntityManager#expandSystemId
	 * 
	 * @param uri
	 * @return a well folder URI which ends with '/'
	 */
	public static String sanitizingUri(String uri) {
		if (StringUtils.isEmpty(uri)) {
			return uri;
		}
		if (uri.charAt(uri.length() - 1) != '/') {
			return uri + "/";
		}
		return uri;
	}
}