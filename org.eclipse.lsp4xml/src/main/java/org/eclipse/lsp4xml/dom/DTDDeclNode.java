/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.lsp4xml.dom;

import java.util.ArrayList;
import java.util.List;


/**
 * DTDNode
 */
public class DTDDeclNode extends DOMNode{

	/**
	 * This class is the base for all declaration nodes for DTD's.
	 * 
	 * It can also be used to represent an undefined tag, meaning
	 * it is not any of: ELEMENT, ATTLIST, ENTITY, or NOTATION
	 */
	
	protected final DOMDocumentType parentDocumentType;
	protected final DOMDocument parentDocument;

	public DTDDeclParameter unrecognized; // holds all content after parsing goes wrong in a DTD declaration (ENTITY, ATTLIST, ...).
	public DTDDeclParameter declType; // represents the actual name of the decl eg: ENTITY, ATTLIST, ...

	private List<DTDDeclParameter> parameters;

	public DTDDeclNode(int start, int end, DOMDocumentType parentDocumentType) {
		super(start, end);
		this.parentDocumentType = parentDocumentType;
		this.parentDocument = null;
	}

	public DTDDeclNode(int start, int end, DOMDocument parentDocumentType) {
		super(start, end);
		this.parentDocument = parentDocumentType;
		this.parentDocumentType = null;
	}

	@Override
	public String getNodeName() {
		return null;
	}

	@Override
	public short getNodeType() {
		return DOMNode.DTD_DECL_NODE;
	}

	public String getUnrecognized() {
		return unrecognized.getParameter();
	}

	public void setUnrecognized(int start, int end) {
		unrecognized = addNewParameter(start, end);
	}	

	public DTDDeclParameter addNewParameter(int start, int end) {
		if(parameters == null) {
			parameters = new ArrayList<DTDDeclParameter>();
		}
		DTDDeclParameter parameter =
				new DTDDeclParameter(this, start, end);
		parameters.add(parameter);
		this.end = end; // updates end position of the node.
		return parameter;
	}

	public void updateLastParameterEnd(int end) {
		if(parameters != null && parameters.size() > 0) {
			DTDDeclParameter last = parameters.get(parameters.size() - 1);
			last.end = end;
			this.end = end;
		}
	}

	public List<DTDDeclParameter> getParameters() {
		if(parameters == null) {
			parameters = new ArrayList<DTDDeclParameter>();
		}
		return parameters;
	}

	public void setDeclType(int start, int end) {
		declType = new DTDDeclParameter(this, start, end);
	}

	public String getDeclType() {
		if(declType != null) {
			return declType.getParameter();
		}
		return null;
	}
	
	public DOMDocumentType getOwnerDoctype() {
		return parentDocumentType;
	}

}