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

/**
 * Exception Utils
 */
public class ExceptionUtils {

	private ExceptionUtils(){}

	/**
	 * Returns the root cause of a {@link Throwable}
	 * @param t the top level {@link Throwable}
	 * @return the root cause of <code>t</code>, or <code>t</code> itself if it has no cause.
	 */
	public static Throwable getRootCause(Throwable t) {
		if (t == null) {
			return t;
		}
		while (t.getCause() != null) {
			t = t.getCause();
		}
		return t;
	}
}