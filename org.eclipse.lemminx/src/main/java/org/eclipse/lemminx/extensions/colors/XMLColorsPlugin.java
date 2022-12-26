/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.colors;

import org.eclipse.lemminx.extensions.colors.participants.XMLDocumentColorParticipant;
import org.eclipse.lemminx.extensions.colors.settings.XMLColorsSettings;
import org.eclipse.lemminx.services.extensions.IDocumentColorParticipant;
import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.save.ISaveContext;
import org.eclipse.lsp4j.InitializeParams;

/**
 * XML colors plugin.
 * 
 * This plugin provides the capability to support color with 'xml.colors'
 * settings.
 * 
 * <code>
 "xml.colors": [
   // XML colors applied for text node for android colors.xml files
   {
      "pattern": "/res/values/colors.xml",
      "expressions": [
         {
            "path": "resources/color/text()"
         }
      ]
   },
   // XML colors applied for @color attribute for another files 
   {
      "pattern": "/my-colors.xml",
      "expressions": [
         {
            "path": "@color"
         }
      ]
   }
]
 * 
 * </code>
 * 
 * 
 * @author Angelo ZERR
 *
 */
public class XMLColorsPlugin implements IXMLExtension {

	private final IDocumentColorParticipant documentColorParticipant;

	private XMLColorsSettings colorsSettings;

	public XMLColorsPlugin() {
		documentColorParticipant = new XMLDocumentColorParticipant(this);
	}

	@Override
	public void doSave(ISaveContext context) {
		if (context.getType() != ISaveContext.SaveContextType.DOCUMENT) {
			// Settings
			updateSettings(context);
		}
	}

	private void updateSettings(ISaveContext saveContext) {
		Object initializationOptionsSettings = saveContext.getSettings();
		XMLColorsSettings referencesSettings = XMLColorsSettings
				.getXMLColorsSettings(initializationOptionsSettings);
		updateSettings(referencesSettings, saveContext);
	}

	private void updateSettings(XMLColorsSettings settings, ISaveContext context) {
		this.colorsSettings = settings;
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		registry.registerDocumentColorParticipant(documentColorParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterDocumentColorParticipant(documentColorParticipant);
	}

	public XMLColorsSettings getColorsSettings() {
		return colorsSettings;
	}
}
