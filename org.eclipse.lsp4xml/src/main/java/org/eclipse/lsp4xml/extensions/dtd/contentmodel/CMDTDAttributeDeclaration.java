/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.dtd.contentmodel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.xerces.impl.dtd.XMLAttributeDecl;
import org.apache.xerces.impl.dtd.XMLSimpleType;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;

/**
 * DTD attribute declaration.
 *
 */
public class CMDTDAttributeDeclaration extends XMLAttributeDecl implements CMAttributeDeclaration {

	@Override
	public String getName() {
		return super.name.localpart;
	}

	@Override
	public String getDefaultValue() {
		return super.simpleType.defaultValue;
	}

	@Override
	public Collection<String> getEnumerationValues() {
		String[] values = super.simpleType.enumeration;
		if (values == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(values);
	}

	@Override
	public String getDocumentation() {
		return null;
	}

	@Override
	public boolean isRequired() {
		return super.simpleType.defaultType == XMLSimpleType.DEFAULT_TYPE_REQUIRED;
	}

}
