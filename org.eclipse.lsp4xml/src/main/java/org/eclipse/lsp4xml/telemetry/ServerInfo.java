/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.telemetry;

import org.eclipse.lsp4xml.Platform;
import org.eclipse.lsp4xml.Platform.JVM;
import org.eclipse.lsp4xml.Platform.OS;
import org.eclipse.lsp4xml.utils.VersionHelper;

/**
 * Telemetry data to collect.
 * 
 * <ul>
 * <li>Server Version</li>
 * <li>JVM information</li>
 * <li>Memory information</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class ServerInfo {
	
	private final String version;

	private OS os;

	private JVM jvm;

	public ServerInfo() {
		this.version = VersionHelper.getVersion();
		this.os = Platform.getOS();
		this.jvm = new JVM();
	}

	public String getVersion() {
		return version;
	}

	public OS getOs() {
		return os;
	}
	
	public JVM getJvm() {
		return jvm;
	}
}
