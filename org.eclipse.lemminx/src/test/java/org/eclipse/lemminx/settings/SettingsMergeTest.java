/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lsp4j.PublishDiagnosticsCapabilities;
import org.junit.jupiter.api.Test;

public class SettingsMergeTest {

	@Test
	public void testValidationSettingsMerge() throws IllegalArgumentException, IllegalAccessException {
		XMLValidationSettings settings = new XMLValidationSettings();
		settings.setEnabled(false);
		settings.setDisallowDocTypeDecl(true);
		settings.setNoGrammar("ignore");
		settings.setResolveExternalEntities(true);
		settings.setCapabilities(new PublishDiagnosticsCapabilities());
		settings.setNamespaces(null);
		settings.setSchema(null);
		XMLValidationSettings mergeTargetSettings = new XMLValidationSettings();
		mergeTargetSettings.merge(settings);
		assertEquals(settings, mergeTargetSettings);
	}

}
