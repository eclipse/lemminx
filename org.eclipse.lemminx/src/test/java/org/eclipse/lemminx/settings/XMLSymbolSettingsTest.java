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
package org.eclipse.lemminx.settings;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * XMLSymbolsSettingsTest
 */
public class XMLSymbolSettingsTest {

	@Test
	public void isExcludedTest() {
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		symbolSettings.setExcluded(new String[] { "**/*.xsd", "**/*.xml" });
		assertTrue(symbolSettings.isExcluded("file:///nikolas/komonen/test.xml"));
		assertTrue(symbolSettings.isExcluded("file:///C:/Users/Nikolas/test.xsd"));
		assertFalse(symbolSettings.isExcluded("file:///nikolas/komonen/test.java"));
	}

	@Test
	public void defaultNumberOfSymbols() {
		assertEquals(5000, new XMLSymbolSettings().getMaxItemsComputed());
	}
}