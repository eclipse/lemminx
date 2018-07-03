package org.eclipse.xml.languageserver.xsd;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xml.languageserver.contentmodel.CMElement;
import org.eclipse.xml.languageserver.extensions.ICompletionParticipant;
import org.eclipse.xml.languageserver.extensions.ICompletionRequest;
import org.eclipse.xml.languageserver.extensions.ICompletionResponse;
import org.eclipse.xml.languageserver.model.Node;

public class XSDCompletionParticipant implements ICompletionParticipant {

	@Override
	public void onXMLContent(ICompletionRequest request, ICompletionResponse response) {
		try {
			Node parentNode = request.getParentNode();
			CMElement cmlElement = XMLSchemaManager.getInstance().findCMElement(parentNode);
			if (cmlElement != null) {

				int lineNumber = request.getPosition().getLine();
				String lineText = parentNode.getOwnerDocument().lineText(lineNumber);
				String startWhitespaces = getStartWhitespaces(lineText);
				boolean useTabs = true;
				int tabWidth = 1;
				String lineDelimiter = "\n";

				XMLGenerator generator = new XMLGenerator(startWhitespaces, useTabs, tabWidth, lineDelimiter);
				for (CMElement child : cmlElement.getElements()) {
					CompletionItem item = new CompletionItem(child.getName());
					item.setKind(CompletionItemKind.Property);
					String documentation = child.getDocumentation();
					if (documentation != null) {
						item.setDetail(documentation);
					}
					String xml = generator.generate(child);
					item.setTextEdit(new TextEdit(new Range(request.getPosition(), request.getPosition()), xml));
					item.setInsertTextFormat(InsertTextFormat.Snippet);
					response.addCompletionItem(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getStartWhitespaces(String lineText) {
		StringBuilder whitespaces = new StringBuilder();
		char[] chars = lineText.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (Character.isWhitespace(c)) {
				whitespaces.append(c);
			} else {
				break;
			}
		}
		return whitespaces.toString();
	}

	@Override
	public void onAttributeValue(String valuePrefix, Range fullRange, ICompletionRequest request,
			ICompletionResponse response) {
	}

}
