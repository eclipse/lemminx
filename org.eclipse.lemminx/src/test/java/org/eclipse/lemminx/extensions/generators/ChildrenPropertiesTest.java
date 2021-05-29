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
package org.eclipse.lemminx.extensions.generators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

/**
 * Tests for children properties.
 *
 */
public class ChildrenPropertiesTest {

	@Test
	public void sequence() {
		ChildrenProperties properties = new ChildrenProperties();
		properties.addChildHierarchy(Arrays.asList("a", "b"));
		properties.addChildHierarchy(Arrays.asList("a", "b"));
		assertTrue(properties.isSequenced(), "Child properties should be sequenced.");
		assertEquals("a [1-1]" + //
				"b [1-1]", toString(properties));
	}

	@Test
	public void choice() {
		ChildrenProperties properties = new ChildrenProperties();
		properties.addChildHierarchy(Arrays.asList("a", "b"));
		properties.addChildHierarchy(Arrays.asList("b", "a"));
		assertFalse(properties.isSequenced(), "Child properties should be choiced.");
		assertEquals("a [1-1]" + //
				"b [1-1]", toString(properties));
	}

	@Test
	public void optionalOnFirst() {
		ChildrenProperties properties = new ChildrenProperties();
		properties.addChildHierarchy(Arrays.asList("a", "b", "c"));
		properties.addChildHierarchy(Arrays.asList("a", "b"));
		assertTrue(properties.isSequenced(), "Child properties should be sequenced.");
		assertEquals("a [1-1]" + //
				"b [1-1]" + //
				"c [0-1]", toString(properties));
	}

	@Test
	public void optionalOnLast() {
		ChildrenProperties properties = new ChildrenProperties();
		properties.addChildHierarchy(Arrays.asList("a", "b"));
		properties.addChildHierarchy(Arrays.asList("a", "b", "c"));
		assertTrue(properties.isSequenced(), "Child properties should be sequenced.");
		assertEquals("a [1-1]" + //
				"b [1-1]" + //
				"c [0-1]", toString(properties));
	}

	@Test
	public void complex() {
		ChildrenProperties properties = new ChildrenProperties();
		properties.addChildHierarchy(Arrays.asList("a", "c"));
		properties.addChildHierarchy(Arrays.asList("a", "c", "c"));
		properties.addChildHierarchy(Arrays.asList("a", "b", "c"));
		assertTrue(properties.isSequenced(), "Child properties should be sequenced.");
		assertEquals("a [1-1]" + //
				"c [1-2]" + //
				"b [0-1]", toString(properties));
	}

	private static String toString(ChildrenProperties properties) {
		StringBuilder result = new StringBuilder();
		for (Entry<String, Cardinality> entry : properties.getCardinalities().entrySet()) {
			result.append(entry.getKey());
			result.append(" ");
			result.append(entry.getValue());
		}
		return result.toString();
	}
}
