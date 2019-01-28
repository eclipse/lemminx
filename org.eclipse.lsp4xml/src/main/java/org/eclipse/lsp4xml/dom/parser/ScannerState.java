/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Nikolas Komonen <nkomonen@redhat.com> - additions for CDATA and Processing Instruction
 */
package org.eclipse.lsp4xml.dom.parser;

/**
 * XML Scanner state.
 *
 */
public enum ScannerState {
	WithinContent, AfterOpeningStartTag, AfterOpeningEndTag, WithinProlog, WithinTag, WithinEndTag,
	WithinComment, AfterAttributeName, BeforeAttributeValue, WithinCDATA, AfterClosingCDATATag, StartCDATATag,
	AfterPrologOpen, PrologOrPI, WithinPI,

	// DTD
	DTDWithinDoctype, DTDAfterDoctypeName, DTDAfterDoctypePUBLIC, DTDAfterDoctypeSYSTEM,
	DTDAfterDoctypePublicId,DTDWithinContent, DTDWithinElement, DTDWithinAttlist, DTDWithinEntity, 
	DTDElementAfterName, DTDElementWithinContent, DTDAfterAttlistName, DTDAfterAttlistElementName, 
	DTDAfterAttlistAttributeName, DTDAfterAttlistAttributeType, DTDAfterEntityName, DTDUnrecognizedParameters, 
	DTDWithinNotation, DTDAfterNotationName, DTDAfterNotationPUBLIC, DTDAfterNotationSYSTEM, 
	DTDAfterNotationPublicId, DTDAfterEntityPUBLIC, DTDAfterEntitySYSTEM, DoctypeUnrecognizedParameters;

}