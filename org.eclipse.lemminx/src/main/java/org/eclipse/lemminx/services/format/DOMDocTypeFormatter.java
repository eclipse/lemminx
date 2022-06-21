/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.format;

import java.util.List;

import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DTDAttlistDecl;
import org.eclipse.lemminx.dom.DTDDeclNode;
import org.eclipse.lemminx.dom.DTDDeclParameter;
import org.eclipse.lsp4j.TextEdit;
import org.w3c.dom.Node;

/**
 * DOM docType formatter.
 * 
 * @author Angelo ZERR
 *
 */
public class DOMDocTypeFormatter {

	private final XMLFormatterDocumentNew formatterDocument;

	public DOMDocTypeFormatter(XMLFormatterDocumentNew formatterDocument) {
		this.formatterDocument = formatterDocument;
	}

	public void formatDocType(DOMDocumentType docType, XMLFormattingConstraints parentConstraints, int start, int end,
			List<TextEdit> edits) {
		boolean isDTD = docType.getOwnerDocument().isDTD();
		if (isDTD) {
			formatDTD(docType, parentConstraints, start, end, edits);
		} else {
			List<DTDDeclParameter> parameters = docType.getParameters();
			if (!parameters.isEmpty()) {
				for (DTDDeclParameter parameter : parameters) {
					replaceLeftSpacesWithOneSpace(parameter.getStart(), edits);
					if (docType.isInternalSubset(parameter)) {
						// level + 1 since the 'level' value is the doctype tag's level
						XMLFormattingConstraints constraints = new XMLFormattingConstraints();
						constraints.copyConstraints(parentConstraints);
						constraints.setIndentLevel(constraints.getIndentLevel() + 1);
						formatDTD(docType, constraints, start, end, edits);
					}

				}
			}
			DTDDeclParameter internalSubset = docType.getInternalSubsetNode();
			if (internalSubset == null) {
				if (docType.isClosed()) {
					int endDocType = docType.getEnd() - 1;
					removeLeftSpaces(endDocType, edits);
				}
			} else {
				int endDocType = internalSubset.getEnd() - 1;
				String lineDelimiter = formatterDocument.getLineDelimiter();
				replaceLeftSpacesWith(endDocType, lineDelimiter, edits);
			}
		}
	}

	private void formatDTD(DOMDocumentType docType, XMLFormattingConstraints parentConstraints, int start, int end,
			List<TextEdit> edits) {
		boolean addLineSeparator = !docType.getOwnerDocument().isDTD();
		for (DOMNode child : docType.getChildren()) {
			switch (child.getNodeType()) {

			case DOMNode.DTD_ELEMENT_DECL_NODE:
			case DOMNode.DTD_ATT_LIST_NODE:
			case Node.ENTITY_NODE:
			case DOMNode.DTD_NOTATION_DECL:
				DTDDeclNode nodeDecl = (DTDDeclNode) child;
				formatDTDNodeDecl(nodeDecl, parentConstraints, addLineSeparator, edits);
				addLineSeparator = true;
				break;

			default:
				// unknown, so just leave alone for now but make sure to update
				// available line width
				int width = updateLineWidthWithLastLine(child, parentConstraints.getAvailableLineWidth());
				parentConstraints.setAvailableLineWidth(width);
			}
		}
	}

	private int updateLineWidthWithLastLine(DOMNode child, int availableLineWidth) {
		return formatterDocument.updateLineWidthWithLastLine(child, availableLineWidth);
	}

	private void formatDTDNodeDecl(DTDDeclNode nodeDecl, XMLFormattingConstraints parentConstraints,
			boolean addLineSeparator, List<TextEdit> edits) {
		// 1) indent the DTD element, entity, notation declaration
		// before formatting : [space][space]<!ELEMENT>
		// after formatting : <!ELEMENT>
		replaceLeftSpacesWithIndentation(parentConstraints.getIndentLevel(), nodeDecl.getStart(), addLineSeparator,
				edits);

		// 2 separate each parameters with one space
		// before formatting : <!ELEMENT[space][space]note>
		// after formatting : <!ELEMENT[space]note>
		DTDAttlistDecl attlist = nodeDecl.isDTDAttListDecl() ? (DTDAttlistDecl) nodeDecl : null;
		if (attlist != null) {
			int indentLevel = nodeDecl.getOwnerDocument().isDTD() ? 1 : 2;
			List<DTDAttlistDecl> internalDecls = attlist.getInternalChildren();
			if (internalDecls == null) {
				for (DTDDeclParameter parameter : attlist.getParameters()) {
					replaceLeftSpacesWithOneSpace(parameter.getStart(), edits);
				}
			} else {
				boolean multipleInternalAttlistDecls = false;
				List<DTDDeclParameter> params = attlist.getParameters();
				DTDDeclParameter parameter;
				for (int i = 0; i < params.size(); i++) {
					parameter = params.get(i);
					if (attlist.getNameParameter().equals(parameter)) {
						replaceLeftSpacesWithOneSpace(parameter.getStart(), edits);
						if (attlist.getParameters().size() > 1) { // has parameters after elementName
							multipleInternalAttlistDecls = true;
						}
					} else {
						if (multipleInternalAttlistDecls && i == 1) {
							replaceLeftSpacesWithIndentation(indentLevel, parameter.getStart(), true, edits);
						} else {
							replaceLeftSpacesWithOneSpace(parameter.getStart(), edits);
						}
					}
				}

				for (DTDAttlistDecl attlistDecl : internalDecls) {
					params = attlistDecl.getParameters();
					for (int i = 0; i < params.size(); i++) {
						parameter = params.get(i);
						if (i == 0) {
							replaceLeftSpacesWithIndentation(indentLevel, parameter.getStart(), true, edits);
						} else {
							replaceLeftSpacesWithOneSpace(parameter.getStart(), edits);
						}
					}
				}
			}
		} else {
			List<DTDDeclParameter> parameters = nodeDecl.getParameters();
			if (!parameters.isEmpty()) {
				for (DTDDeclParameter parameter : parameters) {
					replaceLeftSpacesWithOneSpace(parameter.getStart(), edits);
				}
			}
		}
	}

	private void replaceLeftSpacesWith(int to, String replacement, List<TextEdit> edits) {
		formatterDocument.replaceLeftSpacesWith(to, replacement, edits);
	}

	private void replaceLeftSpacesWithOneSpace(int offset, List<TextEdit> edits) {
		formatterDocument.replaceLeftSpacesWithOneSpace(offset, edits);
	}

	private int replaceLeftSpacesWithIndentation(int indentLevel, int offset, boolean addLineSeparator,
			List<TextEdit> edits) {
		return formatterDocument.replaceLeftSpacesWithIndentation(indentLevel, offset, addLineSeparator, edits);
	}

	private void removeLeftSpaces(int to, List<TextEdit> edits) {
		formatterDocument.removeLeftSpaces(to, edits);
	}

}
