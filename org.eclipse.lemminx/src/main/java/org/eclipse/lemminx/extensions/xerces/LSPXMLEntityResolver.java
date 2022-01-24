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
package org.eclipse.lemminx.extensions.xerces;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lemminx.services.extensions.diagnostics.DiagnosticsResult;
import org.eclipse.lemminx.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lemminx.uriresolver.CacheResourceException;

/**
 * {@link XMLEntityResolver} used when XML, XSD, DTD is validated and declared
 * some external resources (with DOCTYPE, xsi:schemaLocation, etc).
 * 
 * This resolver catches the {@link CacheResourceException} to collect all
 * futures which loads the external resources.
 * 
 * @author Angelo ZERR
 *
 */
public class LSPXMLEntityResolver implements XMLEntityResolver {

	private final XMLEntityResolver entityResolver;
	private final DiagnosticsResult diagnostics;

	public LSPXMLEntityResolver(XMLEntityResolver entityResolver, DiagnosticsResult diagnostics) {
		this.entityResolver = entityResolver;
		this.diagnostics = diagnostics;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		try {
			return entityResolver.resolveEntity(resourceIdentifier);
		} catch (CacheResourceException e) {
			if (e instanceof CacheResourceDownloadingException) {
				CompletableFuture<Path> future = ((CacheResourceDownloadingException) e).getFuture();
				if (future != null) {
					diagnostics.addFuture(future);
				}
			}
			throw new IOException(e);
		}
	}

}
