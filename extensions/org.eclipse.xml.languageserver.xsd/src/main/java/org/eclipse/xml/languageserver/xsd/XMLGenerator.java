package org.eclipse.xml.languageserver.xsd;

import java.util.Collection;

import org.eclipse.xml.languageserver.contentmodel.CMElement;

public class XMLGenerator {

	private final String startWhitespaces;
	private final boolean useTabs;
	private final int tabWidth;
	private final String lineDelimiter;

	public XMLGenerator(String startWhitespaces, boolean useTabs, int tabWidth, String lineDelimiter) {
		this.startWhitespaces = startWhitespaces;
		this.useTabs = useTabs;
		this.tabWidth = tabWidth;
		this.lineDelimiter = lineDelimiter;
	}

	public String generate(CMElement elementDeclaration) {
		StringBuilder xml = new StringBuilder();
		generate(elementDeclaration, 0, xml);
		return xml.toString();
	}

	private void generate(CMElement elementDeclaration, int indent, StringBuilder xml) {
		if (indent > 0) {
			indent(indent, xml);
		}
		xml.append("<");
		xml.append(elementDeclaration.getName());

		Collection<CMElement> children = elementDeclaration.getElements();
		if (children.size() > 0) {
			xml.append(">");
			indent++;
			for (CMElement child : children) {
				generate(child, indent, xml);
			}
			indent--;
			indent(indent, xml);
			xml.append("</");
			xml.append(elementDeclaration.getName());
			xml.append(">");
		} else {
			xml.append(" />");
		}
	}

	private void indent(int indent, StringBuilder xml) {
		xml.append(lineDelimiter);
		xml.append(startWhitespaces);
		for (int i = 0; i < indent; i++) {
			for (int j = 0; j < tabWidth; j++) {
				xml.append(useTabs ? "\t" : " ");
			}
		}
	}

}
