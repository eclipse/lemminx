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
package org.eclipse.lsp4xml.dom.parser;

/**
 * XML Token type.
 *
 */
public enum TokenType {

	StartCommentTag,
	Comment,
	EndCommentTag,
	CDATATagOpen,
	CDATAContent,
	CDATATagClose,
	StartTagOpen,
	StartTagClose,
	StartTagSelfClose,
	StartTag,
	EndTagOpen,
	EndTagClose,
	EndTag,
	DelimiterAssign,
	AttributeName,
	AttributeValue,
	StartPrologOrPI,
	PrologName,
	PIName,
	PIContent,
	PIEnd,
	PrologEnd,
	Content,
	Whitespace,
	Unknown,
	EOS, 
	StartDoctypeTag, 
	DoctypeName, 
	DocTypeKindPUBLIC, 
	DocTypeKindSYSTEM, 
	DoctypePublicId, 
	DoctypeSystemId, 
	InternalDTDStart, 
	EndDoctypeTag, 
	EndInternalDTD, 
	InternalDTDContent, 
	StartElementDTD, 
	StartAttlistDTD, 
	StartEntityDTD, 
	ElementDTDName, 
	StartElementDTDContent, 
	ElementDTDCategory, 
	ElementDTDContent, 
	ElementDTDContentComma, 
	EndElementDTDContent, 
	AttlistDeclName, 
	AttlistDTDAttributeValue, 
	AttlistDTDType, 
	AttlistDTDElementName, 
	AttlistDTDAttributeName, 
	DTDEntityName, 
	DTDEntityValue, 
	DTDEntityKind, 
	DTDEntityURL,
	DTDUndefineTag, 
	EndDTDTag, DTDTagExcessContent
}
