/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.lsp4xml.commons;

/**
 * Model (like DOMDocument for XML) which can be disposed.
 * 
 * @author Angelo ZERR
 *
 */
public interface Disposable {

	/**
	 * Dispose the model.
	 */
	void dispose();
}
