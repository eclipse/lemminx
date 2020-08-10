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
package org.eclipse.lemminx.extensions.relaxng.jing;

import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;

import com.thaiopensource.validate.Schema;

/**
 * RelaxNG grammar.
 * 
 * @author Angelo ZERR
 *
 */
public class RelaxNGGrammar implements Grammar {

	private final XMLGrammarDescription description;
	private final Schema schema;

	public RelaxNGGrammar(Schema schema, XMLGrammarDescription description) {
		this.schema = schema;
		this.description = description;
	}

	@Override
	public XMLGrammarDescription getGrammarDescription() {
		return description;
	}

	public Schema getSchema() {
		return schema;
	}

}
