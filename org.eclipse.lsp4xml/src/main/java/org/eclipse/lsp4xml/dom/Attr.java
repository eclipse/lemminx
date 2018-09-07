package org.eclipse.lsp4xml.dom;

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
