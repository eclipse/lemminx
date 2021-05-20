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

/**
 * XML formatting constraints.
 * 
 * @author Angelo ZERR
 *
 */
public class XMLFormattingConstraints {

	private FormatElementCategory formatElementCategory;

	private int availableLineWidth = 0;
	private int indentLevel = 0;

	/**
	 * Initializes the values in this formatting constraint with values from
	 * constraints
	 * 
	 * @param constraints cannot be null
	 */
	public void copyConstraints(XMLFormattingConstraints constraints) {
		setFormatElementCategory(constraints.getFormatElementCategory());
		setAvailableLineWidth(constraints.getAvailableLineWidth());
		setIndentLevel(constraints.getIndentLevel());
	}

	public FormatElementCategory getFormatElementCategory() {
		return formatElementCategory;
	}

	public void setFormatElementCategory(FormatElementCategory formatElementCategory) {
		this.formatElementCategory = formatElementCategory;
	}

	public int getAvailableLineWidth() {
		return availableLineWidth;
	}

	public void setAvailableLineWidth(int availableLineWidth) {
		this.availableLineWidth = availableLineWidth;
	}

	public int getIndentLevel() {
		return indentLevel;
	}

	public void setIndentLevel(int indentLevel) {
		this.indentLevel = indentLevel;
	}

}
