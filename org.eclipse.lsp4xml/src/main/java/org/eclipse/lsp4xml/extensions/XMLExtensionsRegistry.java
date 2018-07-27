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
package org.eclipse.lsp4xml.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * XML extensions registry.
 *
 */
public class XMLExtensionsRegistry {

	private final Collection<IXMLExtension> extensions;

	private boolean initialized;

	public XMLExtensionsRegistry() {
		extensions = new ArrayList<>();
	}

	public Collection<IXMLExtension> getExtensions() {
		initializeIfNeeded();
		return extensions;
	}

	public Collection<ICompletionParticipant> getCompletionParticipants() {
		initializeIfNeeded();
		return extensions.stream().filter(extension -> extension.getCompletionParticipant() != null)
				.map(IXMLExtension::getCompletionParticipant).collect(Collectors.toList());
	}

	public Collection<IHoverParticipant> getHoverParticipants() {
		initializeIfNeeded();
		return extensions.stream().filter(extension -> extension.getHoverParticipant() != null)
				.map(IXMLExtension::getHoverParticipant).collect(Collectors.toList());
	}

	public Collection<IDiagnosticsParticipant> getDiagnosticsParticipants() {
		initializeIfNeeded();
		return extensions.stream().filter(extension -> extension.getDiagnosticsParticipant() != null)
				.map(IXMLExtension::getDiagnosticsParticipant).collect(Collectors.toList());
	}

	private void initializeIfNeeded() {
		if (initialized) {
			return;
		}
		initialize();
	}

	private synchronized void initialize() {
		if (initialized) {
			return;
		}
		ServiceLoader<IXMLExtension> extensions = ServiceLoader.load(IXMLExtension.class);
		extensions.forEach(extension -> {
			registerExtension(extension);
		});
		initialized = true;
	}

	public void registerExtension(IXMLExtension extension) {
		extensions.add(extension);
	}

	public void unregisterExtension(IXMLExtension extension) {
		extensions.remove(extension);
	}
}
