/**
 *  Copyright (c) 2018 Angelo ZERR, Daniel Dekany.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.xml.languageserver.internal.parser;

/**
 * Scanner state.
 *
 */
public enum ScannerState {
	WithinContent, AfterOpeningStartTag, AfterOpeningEndTag, WithinDoctype, WithinTag, WithinEndTag, WithinComment,
        WithinScriptContent, WithinStyleContent, AfterAttributeName, BeforeAttributeValue, WithinCDATA, AfterClosingCDATATag,
        StartCDATATag

}