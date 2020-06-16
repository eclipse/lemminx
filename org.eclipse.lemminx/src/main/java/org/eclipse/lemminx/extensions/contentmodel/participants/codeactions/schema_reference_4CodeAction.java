/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.IComponentProvider;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

/**
 * Code Action that creates a schema file referenced by
 * xsi:noNamespaceSchemaLocation if it is missing
 */
public class schema_reference_4CodeAction implements ICodeActionParticipant {

	private static final Pattern PATH_FINDING_REGEX = Pattern.compile("[^']+'file:///(.+)',.*");

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			SharedSettings sharedSettings, IComponentProvider componentProvider) {

		String missingFilePath = getPathFromDiagnostic(diagnostic);
		if (StringUtils.isEmpty(missingFilePath)) {
			return;
		}
		Path p = Paths.get(missingFilePath);
		if (p.toFile().exists()) {
			return;
		}

		// TODO: use the generator
		String schemaTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n\n</xs:schema>";

		CodeAction makeSchemaFile = CodeActionFactory.createFile("Generate missing file '" + p.toFile().getName() + "'",
				"file:///" + missingFilePath, schemaTemplate, diagnostic);

		codeActions.add(makeSchemaFile);
	}

	/**
	 * Extract the file to create from the diagnostic. Example diagnostic:
	 * schema_reference.4: Failed to read schema document
	 * 'file:///home/dthompson/Documents/TestFiles/potationCoordination.xsd',
	 * because 1) could not find the document; 2) the document could not be read; 3)
	 * the root element of the document is not <xsd:schema>.
	 */
	private String getPathFromDiagnostic(Diagnostic diagnostic) {
		Matcher m = PATH_FINDING_REGEX.matcher(diagnostic.getMessage());
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

}