/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.contentmodel.participants.diagnostics;

/**
 * XML error code.
 * 
 * @see https://wiki.xmldation.com/Support/Validator
 *
 */
public enum XMLErrorCode {

	AttributeNotUnique, // https://wiki.xmldation.com/Support/Validator/AttributeNotUnique
	AttributeNSNotUnique, // https://wiki.xmldation.com/Support/Validator/AttributeNSNotUnique
	ContentIllegalInProlog, // https://wiki.xmldation.com/Support/Validator/ContentIllegalInProlog
	EmptyPrefixedAttName, // https://wiki.xmldation.com/Support/Validator/EmptyPrefixedAttName
	ElementUnterminated, // https://wiki.xmldation.com/Support/Validator/ElementUnterminated
	ETagRequired; // https://wiki.xmldation.com/Support/Validator/ETagRequired

	public static XMLErrorCode get(String name) {
		try {
			return XMLErrorCode.valueOf(name);
		} catch (Exception e) {
			return null;
		}
	}
}
