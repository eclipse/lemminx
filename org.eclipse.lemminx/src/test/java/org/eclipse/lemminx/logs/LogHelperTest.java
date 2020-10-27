/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.logs;

import org.eclipse.lemminx.MockXMLLanguageClient;
import org.eclipse.lemminx.settings.LogsSettings;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link LogHelper}.
 * 
 * @author Angelo ZERR
 *
 */
public class LogHelperTest {

	@Test
	public void emptyPath() {
		// Empty file path should not throw an error.

		MockXMLLanguageClient languageClient = new MockXMLLanguageClient();
		LogsSettings settings = new LogsSettings();
		// Enable client
		settings.setClient(true);

		// Enable log file
		settings.setFile(null);
		LogHelper.initializeRootLogger(languageClient, settings);

		// Enable log file
		settings.setFile("");
		LogHelper.initializeRootLogger(languageClient, settings);

		// Enable log file
		settings.setFile("    ");
		LogHelper.initializeRootLogger(languageClient, settings);
	}
}
