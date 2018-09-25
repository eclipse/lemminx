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
package org.eclipse.lsp4xml.services.extensions;

import org.eclipse.lsp4j.InitializeParams;

/**
 * XML extension.
 *
 */
public interface IXMLExtension {

	/**
	 * Start method to register participants like {@link ICompletionParticipant},
	 * {@link IHoverParticipant}, {@link IDiagnosticsParticipant} in the given
	 * registry.
	 * @param params 
	 * 
	 * @param registry
	 * @param settings
	 */
	void start(InitializeParams params, XMLExtensionsRegistry registry);

	/**
	 * Stop method to un-register participants like {@link ICompletionParticipant},
	 * {@link IHoverParticipant}, {@link IDiagnosticsParticipant} in the given
	 * registry.
	 * 
	 * @param registry
	 */
	void stop(XMLExtensionsRegistry registry);

	/**
	 * Update settings.
	 * 
	 * @param settings
	 */
	void updateSettings(Object settings);
}
