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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lemminx.commons.config.ConfigurationField;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationRootSettings;
import org.eclipse.lsp4j.PublishDiagnosticsCapabilities;
import org.eclipse.lsp4j.ResourceOperationKind;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.junit.jupiter.api.Test;

public class SettingsMergeTest {

	@Test
	public void testValidationSettingsMerge() throws IllegalArgumentException, IllegalAccessException {
		XMLValidationRootSettings settings = new XMLValidationRootSettings();
		settings.setEnabled(false);
		settings.setDisallowDocTypeDecl(true);
		settings.setNoGrammar("ignore");
		settings.setResolveExternalEntities(true);
		settings.setCapabilities(new PublishDiagnosticsCapabilities());
		settings.setNamespaces(null);
		settings.setSchema(null);
		settings.setXInclude(null);
		XMLValidationRootSettings mergeTargetSettings = new XMLValidationRootSettings();
		mergeTargetSettings.merge(settings);
		assertEquals(settings, mergeTargetSettings);
	}

	@Test
	public void testCopyConstructorCopiesWorkspaceSettings() {
		SharedSettings original = new SharedSettings();
		WorkspaceClientCapabilities workspace = new WorkspaceClientCapabilities();
		WorkspaceEditCapabilities workspaceEdit = new WorkspaceEditCapabilities();
		workspaceEdit.setResourceOperations(Arrays.asList(ResourceOperationKind.Create));
		workspace.setWorkspaceEdit(workspaceEdit);
		original.getWorkspaceSettings().setCapabilities(workspace);

		SharedSettings copy = new SharedSettings(original);
		assertTrue(copy.getWorkspaceSettings().isResourceOperationSupported(ResourceOperationKind.Create));
	}

	@Test
	public void testCopyConstructorWithoutWorkspaceSettings() {
		SharedSettings original = new SharedSettings();

		SharedSettings copy = new SharedSettings(original);
		assertFalse(copy.getWorkspaceSettings().isResourceOperationSupported(ResourceOperationKind.Create));
	}

	@Test
	public void testHaveSameValuesSplitAttributes() {
		List<ConfigurationField> fields = ConfigurationField
				.computeConfigurationFields(XMLGeneralClientSettings.class, "xml.");

		SharedSettings a = new SharedSettings();
		a.getFormattingSettings().setSplitAttributes(
				XMLFormattingOptions.SplitAttributes.preserveAligned);

		SharedSettings b = new SharedSettings(a);

		assertTrue(ConfigurationField.haveSameValues(a, b, fields));
	}

}
