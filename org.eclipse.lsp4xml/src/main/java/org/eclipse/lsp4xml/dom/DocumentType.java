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

import java.util.ArrayList;

/**
 * A doctype node.
 *
 */
public class DocumentType extends Node {

	public DocumentType(int start, int end, Node parent, XMLDocument ownerDocument) {
		super(start, end, new ArrayList<>(), parent, ownerDocument);
	}

	@Override
	public short getNodeType() {
		return Node.DOCUMENT_TYPE_NODE;
	}

}
