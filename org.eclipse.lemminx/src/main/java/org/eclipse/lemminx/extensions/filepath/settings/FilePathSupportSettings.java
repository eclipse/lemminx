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
package org.eclipse.lemminx.extensions.filepath.settings;

import java.util.List;

import org.eclipse.lemminx.utils.JSONUtility;

/**
 * File paths settings:
 * 
 * <code>
 "xml.filePathSupport.mappings": [
   // File paths applied for text node items.xml files
   {
      "pattern": "*.xml",
      "expressions": [
         {
            "xpath": "path/text()"
         },
         {
            "xpath": "item/@path"
         },
         {
            "xpath": "item/@paths",
            "separator": " "
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
public class FilePathSupportSettings {

	private FilePathSupport filePathSupport;

	public FilePathSupport getFilePathSupport() {
		return filePathSupport;
	}

	public void setFilePathSupport(FilePathSupport filePathSupport) {
		this.filePathSupport = filePathSupport;
	}

	public static FilePathSupportSettings getFilePathsSettings(Object initializationOptionsSettings) {
		return JSONUtility.toModel(initializationOptionsSettings, FilePathSupportSettings.class);
	}

	public List<FilePathMapping> getFilePathMappings() {
		return filePathSupport != null ? filePathSupport.getMappings() : null;
	}

}
