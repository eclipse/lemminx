/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.utils;

/**
 * OSUtils
 */
public class OSUtils {
	
	public static final boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
	public static String SLASH = isWindows ? "\\" : "/";

	
}