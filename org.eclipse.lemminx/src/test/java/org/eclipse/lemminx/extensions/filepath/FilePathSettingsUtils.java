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
package org.eclipse.lemminx.extensions.filepath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.lemminx.extensions.filepath.settings.FilePathExpression;
import org.eclipse.lemminx.extensions.filepath.settings.FilePathMapping;
import org.eclipse.lemminx.extensions.filepath.settings.FilePathSupport;
import org.eclipse.lemminx.extensions.filepath.settings.FilePathSupportSettings;

/**
 * {@link FilePathSupportSettings} for tests.
 * 
 */
public class FilePathSettingsUtils {

	public static FilePathSupportSettings createFilePathsSettings() {
		FilePathSupportSettings filePathsSettings = new FilePathSupportSettings();
		FilePathSupport support = new FilePathSupport();
		filePathsSettings.setFilePathSupport(support);
		support.setMappings(createFilePathMappings());
		return filePathsSettings;
	}

	private static List<FilePathMapping> createFilePathMappings() {
		List<FilePathMapping> filePaths = new ArrayList<>();

		FilePathMapping path = new FilePathMapping();
		path.setPattern("**/*.xml");
		filePaths.add(path);
		/*
		 * {
		 * "xpath": "@path"
		 * },
		 * {
		 * "xpath": "path/text()"
		 * },
		 * {
		 * "xpath": "@paths",
		 * "separator": " "
		 * }
		 */
		FilePathExpression attrPath = new FilePathExpression("@path");
		FilePathExpression textPath = new FilePathExpression("path/text()");
		FilePathExpression multiAttrPath = new FilePathExpression("@paths").setSeparator(' ');

		path.setExpressions(Arrays.asList(attrPath, textPath, multiAttrPath));

		return filePaths;
	}

}
