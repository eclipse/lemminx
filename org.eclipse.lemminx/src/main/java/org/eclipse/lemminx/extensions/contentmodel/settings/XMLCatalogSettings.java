/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.settings;

/**
 * XML catalog settings.
 *
 */
public class XMLCatalogSettings {

	public static String DEFAULT_PREFER_PUBLIC = "public";

	public static boolean DEFAULT_USE_PREFER_PUBLIC = true;

	public static boolean DEFAULT_USE_LITERAL_SYSTEM_ID = true;

	public static final XMLCatalogSettings DEFAULT_CATALOG = new XMLCatalogSettings();
	
	private String prefer;

	private String[] files = null;

	private boolean useLiteralSystemId;

	public XMLCatalogSettings() {
		setPrefer(DEFAULT_PREFER_PUBLIC);
		setUseLiteralSystemId(DEFAULT_USE_LITERAL_SYSTEM_ID);
	}

	public String getPrefer() {
		return prefer;
	}

	public void setPrefer(String prefer) {
		this.prefer = prefer;
	}

	public boolean isPreferPublic() {
		return DEFAULT_PREFER_PUBLIC.equals(getPrefer());
	}

	public String[] getFiles() {
		return files;
	}

	public void setFiles(String[] files) {
		this.files = files;
	}

	public boolean isUseLiteralSystemId() {
		return useLiteralSystemId;
	}

	public void setUseLiteralSystemId(boolean useLiteralSystemId) {
		this.useLiteralSystemId = useLiteralSystemId;
	}

}
