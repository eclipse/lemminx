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

/**
 * DTDDeclParameter
 */
public class DTDDeclParameter {

	String parameter;

	int start, end;

	DOMDocumentType parentDoctype;

	public DTDDeclParameter(DOMDocumentType doctype, int start, int end) {
		this.parentDoctype = doctype;
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getParameter() {
		if (parameter == null) {
			parameter = parentDoctype.getSubstring(start, end);
		}
		return parameter;
	}

	/**
	 * Will get the parameter with the first and last character removed
	 * 
	 * Can be used to remove the quotations from a URL value...
	 */
	public String getParameterWithoutFirstAndLastChar() {
		if (parameter == null) {
			parameter = parentDoctype.getSubstring(start + 1, end - 1);
		}
		return parameter;
	}


	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DTDDeclParameter)) {
			return false;
		}
		DTDDeclParameter temp = (DTDDeclParameter) obj;
		return start == temp.start && end == temp.end;
	}

	

}