/*******************************************************************************
* Copyright (c) 2022, 2023 Red Hat Inc. and others.
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

import static org.eclipse.lemminx.utils.TextEditUtils.creatTextDocumentEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.extensions.relaxng.utils.RelaxNGUtils;
import org.eclipse.lemminx.services.extensions.IPositionRequest;
import org.eclipse.lemminx.services.extensions.rename.IPrepareRenameRequest;
import org.eclipse.lemminx.services.extensions.rename.IRenameParticipant;
import org.eclipse.lemminx.services.extensions.rename.IRenameRequest;
import org.eclipse.lemminx.services.extensions.rename.IRenameResponse;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * RNG rename
 * 
 */
public class RNGRenameParticipant implements IRenameParticipant {

	// --------------- Prepare rename

	@Override
	public Either<Range, PrepareRenameResult> prepareRename(IPrepareRenameRequest request,
			CancelChecker cancelChecker) {
		// RNG rename can be applied for:
		// - define/@name
		DOMAttr attr = findAttrToRename(request);
		if (attr != null) {
			Range range = XMLPositionUtility.selectAttributeValue(attr, true);
			String placeholder = attr.getValue();
			return Either.forRight(new PrepareRenameResult(range, placeholder));
		}
		return null;
	}

	// --------------- Rename

	@Override
	public void doRename(IRenameRequest request, IRenameResponse renameResponse, CancelChecker cancelChecker) {
		renameResponse.addTextDocumentEdit(getRenameTextDocumentEdit(request, cancelChecker));
	}

	private TextDocumentEdit getRenameTextDocumentEdit(IRenameRequest request, CancelChecker cancelChecker) {
		// RNG rename can be applied for:
		// - define/@name
		DOMAttr attr = findAttrToRename(request);
		if (attr == null) {
			return null;
		}
		DOMElement ownerElement = attr.getOwnerElement();
		DOMDocument document = request.getXMLDocument();
		String newText = request.getNewText();
		List<Location> locations = getReferenceLocations(ownerElement, cancelChecker);
		return creatTextDocumentEdit(document, renameAttributeValueTextEdits(document, attr, newText, locations));
	}

	private List<Location> getReferenceLocations(DOMNode node, CancelChecker cancelChecker) {

		List<Location> locations = new ArrayList<>();

		RelaxNGUtils.searchRNGOriginAttributes(node,
				(origin, target) -> locations.add(XMLPositionUtility.createLocation(origin.getNodeAttrValue())),
				cancelChecker);

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
			textEdits.add(new TextEdit(textEditRange, newText));
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

	/**
	 * Returns the xsd:complexType/@name or xs:simpleType/@name to rename or null
	 * otherwise.
	 * 
	 * @param request the position request.
	 * 
	 * @return the xsd:complexType/@name or xs:simpleType/@name to rename or null
	 *         otherwise.
	 */
	private static DOMAttr findAttrToRename(IPositionRequest request) {
		DOMDocument xmlDocument = request.getXMLDocument();
		if (!DOMUtils.isRelaxNGXMLSyntax(xmlDocument)) {
			return null;
		}
		DOMNode node = request.getNode();
		if (node == null || !node.isAttribute()) {
			return null;
		}

		DOMAttr attr = (DOMAttr) node;
		DOMElement ownerElement = attr.getOwnerElement();
		if (ownerElement == null) {
			return null;
		}
		if (RelaxNGUtils.isDefine(ownerElement)) {
			if (attr.getName().equals("name")) {
				return attr;
			}
		}
		return null;
	}
}