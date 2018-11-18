/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services.extensions.save;

import java.util.function.Predicate;

import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.services.IXMLDocumentProvider;

/**
 * Save context API.
 *
 */
public interface ISaveContext extends IXMLDocumentProvider {

	/**
	 * The save context type:
	 * 
	 * <ul>
	 * <li>DOCUMENT: a document is saved.</li>
	 * <li>SETTINGS: a settings is saved.</li>
	 * </ul>
	 */
	public enum SaveContextType {
		DOCUMENT, SETTINGS;
	}

	/**
	 * This method is called to collect document to validate after the save.
	 * 
	 * @param validateDocumentPredicate
	 */
	void collectDocumentToValidate(Predicate<DOMDocument> validateDocumentPredicate);

	/**
	 * Returns the save context type.
	 * 
	 * @return the save context type.
	 */
	SaveContextType getType();

	/**
	 * Returns the document URI which was saved and null if it's a settings which
	 * was saved.
	 * 
	 * @return the document URI which was saved and null if it's a settings which
	 *         was saved.
	 */
	String getUri();

	/**
	 * Returns the settings which has changed and null otherwise.
	 * 
	 * @return the settings which has changed and null otherwise.
	 */
	Object getSettings();

}
