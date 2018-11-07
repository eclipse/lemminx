/*******************************************************************************
* Copyright (c) 2018 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.utils;

import static org.junit.Assert.*;
import static org.eclipse.lsp4xml.utils.ExceptionUtils.getRootCause;

import org.junit.Test;

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