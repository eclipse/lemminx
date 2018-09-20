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
 * A processing instruction node.
 *
 */
public class ProcessingInstruction extends CharacterData {

	boolean startTagClose;
	String target;
	boolean prolog = false;
	boolean processingInstruction = false;
	int startContent;
	int endContent;
	Integer endTagOpenOffset;

	public ProcessingInstruction(int start, int end, XMLDocument ownerDocument) {
		super(start, end, ownerDocument);
	}

	public boolean isProlog() {
		return prolog;
	}

	public boolean isProcessingInstruction() {
		return processingInstruction;
	}

	@Override
	public short getNodeType() {
		return Node.PROCESSING_INSTRUCTION_NODE;
	}

	@Override
	public String getData() {
		return super.getData().trim();
	}

	@Override
	public int getStartContent() {
		return startContent;
	}

	@Override
	public int getEndContent() {
		return endContent;
	}
	
	public String getTarget() {
		return target;
	}
	
	@Override
	public String getNodeName() {
		return getTarget();
	}
	
	public Integer getEndTagStart() {
		return endTagOpenOffset;
	}
}
