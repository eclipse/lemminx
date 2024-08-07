/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.missinggrammar;

import org.eclipse.lemminx.extensions.generators.FileContentGeneratorSettings;
import org.eclipse.lemminx.extensions.generators.xml2xsd.XMLSchemaGeneratorSettings;

/**
 * Code action resolver participant to generate the missing XSD which is
 * declared in the XML as association via xsi:noNamespaceSchemaLocation or
 * xml-model.
 * 
 * @author Angelo ZERR
 *
 */
public class GenerateXSDCodeActionResolver extends AbstractGenerateGrammarCodeActionResolver {

	public static final String PARTICIPANT_ID = GenerateXSDCodeActionResolver.class.getName();

	@Override
	protected FileContentGeneratorSettings getFileContentGeneratorSettings() {
		return new XMLSchemaGeneratorSettings();
	}

}
