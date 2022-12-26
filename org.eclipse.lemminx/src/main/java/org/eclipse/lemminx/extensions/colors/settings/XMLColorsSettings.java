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
package org.eclipse.lemminx.extensions.colors.settings;

import java.util.List;

import org.eclipse.lemminx.utils.JSONUtility;

/**
 * XML colors settings:
 * 
 * <code>
 "xml.colors": [
   // XML colors applied for text node for android colors.xml files
   {
      "pattern": "/res/values/colors.xml",
      "expressions": [
         {
            "xpath": "resources/color/text()"
         }
      ]
   },
   // XML colors applied for @color attribute for another files 
   {
      "pattern": "/my-colors.xml",
      "expressions": [
         {
            "xpath": "@color"
         }
      ]
   }
]
 * 
 * </code>
 * 
 * @author Angelo ZERR
 *
 */
public class XMLColorsSettings {

	private List<XMLColors> colors;

	public List<XMLColors> getColors() {
		return colors;
	}

	public void setColors(List<XMLColors> colors) {
		this.colors = colors;
	}

	public static XMLColorsSettings getXMLColorsSettings(Object initializationOptionsSettings) {
		return JSONUtility.toModel(initializationOptionsSettings, XMLColorsSettings.class);
	}

}
