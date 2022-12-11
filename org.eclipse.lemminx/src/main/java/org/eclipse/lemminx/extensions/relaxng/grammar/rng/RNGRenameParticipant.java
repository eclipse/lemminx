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
package org.eclipse.lemminx.extensions.relaxng.grammar.rng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.extensions.relaxng.utils.RelaxNGUtils;
import org.eclipse.lemminx.services.extensions.IRenameParticipant;
import org.eclipse.lemminx.services.extensions.IRenameRequest;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

/**
 * RNG rename
 * 
 */
public class RNGRenameParticipant implements IRenameParticipant {

	@Override
	public void doRename(IRenameRequest request, List<TextEdit> locations) {
		DOMDocument xmlDocument = request.getXMLDocument();

		if (!DOMUtils.isRelaxNGXMLSyntax(xmlDocument)) {
			return;
		}
		locations.addAll(getRenameTextEdits(request));

	}

	private List<TextEdit> getRenameTextEdits(IRenameRequest request) {
		DOMDocument document = request.getXMLDocument();
		DOMNode node = request.getNode();

		if (!node.isAttribute()) {
			return Collections.emptyList();
		}

		DOMAttr attr = (DOMAttr) node;
		DOMElement ownerElement = attr.getOwnerElement();

		if (ownerElement == null) {
			return Collections.emptyList();
		}

		String newText = request.getNewText();

		if (RelaxNGUtils.isDefine(ownerElement)) {
			// <define
			if (attr.getName().equals("name")) {
				// <define name=""
				List<Location> locations = getReferenceLocations(ownerElement);
				return renameAttributeValueTextEdits(document, attr, newText, locations);
			}
		}

		return Collections.emptyList();
	}

	private List<Location> getReferenceLocations(DOMNode node) {

		List<Location> locations = new ArrayList<>();

		RelaxNGUtils.searchRNGOriginAttributes(node,
				(origin, target) -> locations.add(XMLPositionUtility.createLocation(origin.getNodeAttrValue())),
				null);

		return locations;
	}

	private List<TextEdit> renameAttributeValueTextEdits(DOMDocument document, DOMAttr attribute, String newText,
			List<Location> locations) {
		DOMRange attrValue = attribute.getNodeAttrValue();
		List<TextEdit> textEdits = new ArrayList<>();

		int valueStart = attrValue.getStart();
		int valueEnd = attrValue.getEnd();
		Range range = XMLPositionUtility.createRange(valueStart, valueEnd, document);

		// make range not cover " on both ends
		reduceRangeFromBothEnds(range, 1);

		textEdits.add(new TextEdit(range, newText));

		for (Location location : locations) {
			Range textEditRange = location.getRange();
			reduceRangeFromBothEnds(textEditRange, 1);

			TextEdit textEdit = new TextEdit(textEditRange, newText);
			textEdits.add(textEdit);
		}

		return textEdits;
	}

	private void reduceRangeFromBothEnds(Range range, int reduce) {
		increaseStartRange(range, reduce);
		decreaseEndRange(range, reduce);
	}

	private void increaseStartRange(Range range, int increase) {
		int startCharacter = range.getStart().getCharacter();
		range.getStart().setCharacter(startCharacter + increase);
	}

	private void decreaseEndRange(Range range, int decrease) {
		int endCharacter = range.getEnd().getCharacter();
		range.getEnd().setCharacter(endCharacter - decrease);
	}

}