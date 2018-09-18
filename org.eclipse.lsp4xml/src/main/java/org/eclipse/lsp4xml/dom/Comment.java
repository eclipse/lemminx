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
 * A Comment node.
 *
 */
public class Comment extends CharacterData {

	boolean commentSameLineEndTag;

	int startContent;

	int endContent;

	public Comment(int start, int end, XMLDocument ownerDocument) {
		super(start, end, ownerDocument);
	}

	public boolean isCommentSameLineEndTag() {
		return commentSameLineEndTag;
	}

	@Override
	public short getNodeType() {
		return Node.COMMENT_NODE;
	}

	@Override
	public String getNodeName() {
		return "#comment";
	}

	@Override
	public int getStartContent() {
		return startContent;
	}

	@Override
	public int getEndContent() {
		return endContent;
	}

}
