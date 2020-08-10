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
package com.thaiopensource.relaxng.pattern;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.lemminx.extensions.contentmodel.model.CMElementDeclaration;

import com.thaiopensource.util.VoidValue;

/**
 * RelaxNG class used to collect content model elements children for a given
 * {@link ElementPatter}.
 * 
 * <p>
 * NOTE : this class is hosted in 'com.thaiopensource.relaxng.pattern' because
 * {@link Pattern} implementation like {@link ElementPattern} are not public.
 * Once https://github.com/relaxng/jing-trang/issues/271 will be fixed we could
 * move this class in 'org.eclipse.lemminx.extensions.relaxng.contentmodel'
 * package.
 * </p>
 * 
 * @author Angelo ZERR
 *
 */
public class CMRelaxNGElementDeclarationCollector extends AbstractCMRelaxNGCollector {

	private final CMRelaxNGDocument document;

	private final Collection<CMElementDeclaration> elements;

	public CMRelaxNGElementDeclarationCollector(CMRelaxNGDocument document, Pattern pattern) {
		this.document = document;
		this.elements = new ArrayList<>();
		pattern.apply(this);
	}

	@Override
	public VoidValue caseElement(ElementPattern p) {
		NameClass nameClass = p.getNameClass();
		if (nameClass instanceof SimpleNameClass) {
			CMRelaxNGElementDeclaration elementDeclaration = document.getPatternElement(p);
			elements.add(elementDeclaration);
		}
		return VoidValue.VOID;
	}

	public Collection<CMElementDeclaration> getElements() {
		return elements;
	}
}
