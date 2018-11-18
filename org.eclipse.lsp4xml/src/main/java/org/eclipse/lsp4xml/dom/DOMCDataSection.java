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
 * A CData section node.
 *
 */
public class DOMCDataSection extends DOMText implements org.w3c.dom.CDATASection {

	int startContent;
	int endContent;

	public DOMCDataSection(int start, int end, DOMDocument ownerDocument) {
		super(start, end, ownerDocument);
	}

	@Override
	public int getStartContent() {
		return startContent;
	}

	@Override
	public int getEndContent() {
		return endContent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.w3c.dom.Text#getNodeType()
	 */
	@Override
	public short getNodeType() {
		return DOMNode.CDATA_SECTION_NODE;
	}
}
