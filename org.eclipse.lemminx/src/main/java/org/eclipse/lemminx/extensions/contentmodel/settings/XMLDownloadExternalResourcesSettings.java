/**
 *  Copyright (c) 2022 Red Hat Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel.settings;

/**
 * XML download external resources settings.
 *
 */
public class XMLDownloadExternalResourcesSettings {

	public XMLDownloadExternalResourcesSettings() {
		setEnabled(true);
	}

	private boolean enabled;

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns true if the external resources can be downloaded and false otherwise.
	 * 
	 * An external resource can be declared with:
	 * 
	 * <ul>
	 * <li>DOCTYPE SYSTEM</li>
	 * <li><!ENTITY SYSTEM</li>
	 * <li>xml-model/@href</li>
	 * <li>xsi:noNamespaceSchemaLocation</li>
	 * <li>xsi:chemaLocation</li>
	 * <li>xs:include|xs:import/@schemaLocation</li>
	 * </ul>
	 * 
	 * @return true if the external resources can be downloaded and false otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}
}
