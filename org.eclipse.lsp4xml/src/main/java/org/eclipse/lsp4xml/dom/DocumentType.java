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
package org.eclipse.lsp4xml.dom;

/**
 * A doctype node.
 *
 */
public class DocumentType extends Node {

	/** Document type name. */
	String name;

	private String content;
	int startContent;
	int endContent;

	public DocumentType(int start, int end, XMLDocument ownerDocument) {
		super(start, end, ownerDocument);
	}

	@Override
	public short getNodeType() {
		return Node.DOCUMENT_TYPE_NODE;
	}

	public String getContent() {
		if (content == null) {
			content = getOwnerDocument().getText().substring(getStartContent(), getEndContent());
		}
		return content;
	}

	public int getStartContent() {
		return startContent;
	}

	public int getEndContent() {
		return endContent;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getNodeName() {
		return getName();
	}
}
