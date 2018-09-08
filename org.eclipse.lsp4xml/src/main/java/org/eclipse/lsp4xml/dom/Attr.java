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
 * An attribute node.
 *
 */
public class Attr {

	private final String name;

	private final Node nodeName;

	private Node nodeValue;

	public Attr(String name, Node nodeName) {
		this.name = name;
		this.nodeName = nodeName;
	}

	public String getName() {
		return name;
	}

	public Node getNodeName() {
		return nodeName;
	}

	public Node getNodeValue() {
		return nodeValue;
	}

	public void setNodeValue(Node nodeValue) {
		this.nodeValue = nodeValue;
	}

}
