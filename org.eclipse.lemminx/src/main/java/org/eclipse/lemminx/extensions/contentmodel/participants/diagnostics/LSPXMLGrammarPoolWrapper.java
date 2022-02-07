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
package org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics;

import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;

/**
 * Xerces grammar pool which tracks all DTD grammars which are cached for an XML
 * validation.
 * 
 * @author Angelo ZERR
 *
 */
public class LSPXMLGrammarPoolWrapper extends LSPXMLGrammarPool {

	private final LSPXMLGrammarPool delegate;

	private final List<Grammar> cachedGrammars;

	public LSPXMLGrammarPoolWrapper(LSPXMLGrammarPool delegate) {
		this.delegate = delegate;
		this.cachedGrammars = new ArrayList<>();
	}

	public Grammar[] retrieveInitialGrammarSet(String grammarType) {
		return delegate.retrieveInitialGrammarSet(grammarType);
	}

	public void cacheGrammars(String grammarType, Grammar[] grammars) {
		for (Grammar grammar : grammars) {
			cachedGrammars.add(grammar);
		}
		delegate.cacheGrammars(grammarType, grammars);
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public Grammar retrieveGrammar(XMLGrammarDescription desc) {
		return delegate.retrieveGrammar(desc);
	}

	public Grammar removeGrammar(XMLGrammarDescription desc) {
		return delegate.removeGrammar(desc);
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	public void removeGrammar(String grammarURI) {
		delegate.removeGrammar(grammarURI);
	}

	public void lockPool() {
		delegate.lockPool();
	}

	public void unlockPool() {
		delegate.unlockPool();
	}

	/**
	 * Clear the cached grammar.
	 */
	public void clear() {
		for (Grammar grammar : cachedGrammars) {
			delegate.removeGrammar(grammar.getGrammarDescription());
		}
	}

	public boolean equals(XMLGrammarDescription desc1, XMLGrammarDescription desc2) {
		return delegate.equals(desc1, desc2);
	}

	public int hashCode(XMLGrammarDescription desc) {
		return delegate.hashCode(desc);
	}

	public String toString() {
		return delegate.toString();
	}

	@Override
	public boolean setInternalSubset(XMLDTDDescription grammarDesc, String internalSubset) {
		return delegate.setInternalSubset(grammarDesc, internalSubset);
	}
}