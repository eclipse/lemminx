/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.services.extensions;

import static org.junit.Assert.assertTrue;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;
import org.junit.Test;

/**
 * Ensures XML LS extensions are correctly unregistered when
 * {@code XMLExtensionRegistry.dispose()} is called. During extension
 * unregistration, the {@code IXMLExtension.stop()} function is called.
 * 
 * @author aobuchow
 *
 */
public class ExtensionRegistryDisposeTest {

	@Test
	public void testExtensionRegistryDipose() {
		XMLExtensionsRegistry registry = new XMLExtensionsRegistry();
		registry.initializeIfNeeded();
		MockXMLExtension extension = new MockXMLExtension();
		registry.registerExtension(extension);
		assertTrue(!extension.isStopped());
		registry.dispose();
		assertTrue(extension.isStopped());
		assertTrue(registry.getExtensions().isEmpty());
	}
}

class MockXMLExtension implements IXMLExtension {
	boolean stopped;

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		stopped = false;
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		stopped = true;
	}

	@Override
	public void doSave(ISaveContext context) {
	}

	public boolean isStopped() {
		return stopped;
	}

}
