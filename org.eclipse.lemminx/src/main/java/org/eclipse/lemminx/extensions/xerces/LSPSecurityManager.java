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
package org.eclipse.lemminx.extensions.xerces;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.util.SecurityManager;

/**
 * LSP security manager.
 * 
 * @author Angelo ZERR
 *
 */
public class LSPSecurityManager {

	private static final Logger LOGGER = Logger.getLogger(LSPSecurityManager.class.getName());

	private static final String ENTITY_EXPANSION_LIMIT_PROPERTY_NAME = "jdk.xml.entityExpansionLimit";
	private static final String MAX_OCCUR_LIMIT_PROPERTY_NAME = "jdk.xml.maxOccur";
	private static final int ENTITY_EXPANSION_LIMIT_DEFAULT_VALUE = 64000;
	private static final int MAX_OCCUR_LIMIT_DEFAULT_VALUE = 5000;

	/**
	 * Returns the Xerces security manager created from JVM arguments.
	 * @return
	 */
	public static SecurityManager getSecurityManager() {
		// FIXME: those parameters should come from settings. 
		SecurityManager securityManager = new SecurityManager();
		securityManager.setEntityExpansionLimit(
				getPropertyValue(ENTITY_EXPANSION_LIMIT_PROPERTY_NAME, ENTITY_EXPANSION_LIMIT_DEFAULT_VALUE));
		securityManager
				.setMaxOccurNodeLimit(getPropertyValue(MAX_OCCUR_LIMIT_PROPERTY_NAME, MAX_OCCUR_LIMIT_DEFAULT_VALUE));
		return securityManager;
	}

	private static int getPropertyValue(String propertyName, int defaultValue) {
		String value = System.getProperty(propertyName, "");
		if (!value.isEmpty()) {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error while getting system property '" + propertyName + "'.", e);
			}
		}
		return defaultValue;
	}
}
