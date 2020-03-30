/*******************************************************************************
* Copyright (c) 2018 Red Hat Inc. and others.
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

import static org.eclipse.lemminx.utils.ExceptionUtils.getRootCause;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class ExceptionUtilsTest {

	@Test
	public void testGetRootCause() {
		assertNull(getRootCause(null));
		
		Throwable root = new Throwable();
		assertSame(root, getRootCause(root));
		
		Exception mid = new Exception(root);
		assertSame(root, getRootCause(mid));

		RuntimeException top = new RuntimeException(mid);
		assertSame(root, getRootCause(top));
	}	
}