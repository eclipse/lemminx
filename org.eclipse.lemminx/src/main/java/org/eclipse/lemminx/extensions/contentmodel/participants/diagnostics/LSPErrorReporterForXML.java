/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.model.ReferencedGrammarInfo;
import org.eclipse.lemminx.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.eclipse.lemminx.extensions.xerces.AbstractLSPErrorReporter;
import org.eclipse.lemminx.extensions.xsd.participants.XSDErrorCode;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * LSP error reporter for XML syntax and error grammar (XML Schema/DTD).
 *
 */
public class LSPErrorReporterForXML extends AbstractLSPErrorReporter {

	private static final String XML_DIAGNOSTIC_SOURCE = "xml";

	private final ContentModelManager contentModelManager;

	private Set<ReferencedGrammarInfo> referencedGrammars;

	private final Map<String, ReferencedGrammarDiagnosticsInfo> referencedGrammarDiagnosticsInfoCache;

	private final boolean hasRelatedInformation;

	public LSPErrorReporterForXML(DOMDocument xmlDocument, List<Diagnostic> diagnostics,
			ContentModelManager contentModelManager, boolean hasRelatedInformation,
			Map<String, ReferencedGrammarDiagnosticsInfo> referencedGrammarDiagnosticsInfoCache) {
		super(XML_DIAGNOSTIC_SOURCE, xmlDocument, diagnostics, hasRelatedInformation);
		this.contentModelManager = contentModelManager;
		this.hasRelatedInformation = hasRelatedInformation;
		this.referencedGrammarDiagnosticsInfoCache = referencedGrammarDiagnosticsInfoCache == null ? new HashMap<>()
				: referencedGrammarDiagnosticsInfoCache;
	}

	/**
	 * Create the LSP range from the SAX error.
	 *
	 * @param location
	 * @param key
	 * @param arguments
	 * @param document
	 * @return the LSP range from the SAX error.
	 */
	@Override
	protected Range toLSPRange(XMLLocator location, String key, Object[] arguments, String message,
			DiagnosticSeverity diagnosticSeverity, boolean fatalError, DOMDocument document) {
		String documentOrGrammarURI = location.getExpandedSystemId();
		boolean errorForDocument = documentOrGrammarURI != null ? documentOrGrammarURI.endsWith(document.getDocumentURI()) : true;
		// try adjust positions for XML syntax error
		XMLSyntaxErrorCode syntaxCode = XMLSyntaxErrorCode.get(key);
		if (syntaxCode != null) {
			if (errorForDocument) {
				Range range = XMLSyntaxErrorCode.toLSPRange(location, syntaxCode, arguments, document);
				if (range != null) {
					return range;
				}
			} else {
				fillReferencedGrammarDiagnostic(location, key, arguments, message, diagnosticSeverity, fatalError,
						document.getResolverExtensionManager(), syntaxCode, null, null, documentOrGrammarURI);
				return NO_RANGE;
			}
		} else {
			// try adjust positions for XML schema error
			XMLSchemaErrorCode schemaCode = XMLSchemaErrorCode.get(key);
			if (schemaCode != null) {
				Range range = XMLSchemaErrorCode.toLSPRange(location, schemaCode, arguments, document);
				if (range != null) {
					return range;
				}
			} else {
				// try adjust positions for DTD error
				DTDErrorCode dtdCode = DTDErrorCode.get(key);
				if (dtdCode != null) {
					if (errorForDocument) {
						Range range = DTDErrorCode.toLSPRange(location, dtdCode, arguments, document);
						if (range != null) {
							return range;
						}
					} else {
						fillReferencedGrammarDiagnostic(location, key, arguments, message, diagnosticSeverity,
								fatalError, document.getResolverExtensionManager(), null, dtdCode, null, documentOrGrammarURI);
						return NO_RANGE;
					}
				} else {
					XSDErrorCode xsdCode = XSDErrorCode.get(key);
					if (xsdCode != null && !errorForDocument) {
						// The error comes from the referenced XSD (with xsi:schemaLocation, xml-model,
						// etc)

						// Try to get the declared xsi:schemaLocation, xsi:noNamespaceLocation range
						// which declares the XSD.
						fillReferencedGrammarDiagnostic(location, key, arguments, message, diagnosticSeverity,
								fatalError, document.getResolverExtensionManager(), null, null, xsdCode, documentOrGrammarURI);
						return NO_RANGE;
					}
				}
			}
		}
		if (!errorForDocument) {
			// The error is not for the document, we ignore the diagnostic
			return NO_RANGE;
		}
		return null;
	}

	/**
	 * Create a diagnostic root where XSD, DTD which have the error if needed and
	 * attach the error as related information if LSP client support it.
	 *
	 * @param location                 the Xerces location.
	 * @param key                      the Xerces error key
	 * @param arguments                the Xerces error arguments
	 * @param message                  the Xerces error message
	 * @param diagnosticSeverity       the the Xerces severity
	 * @param fatalError
	 * @param resolverExtensionManager the resolver
	 * @param syntaxCode               the Syntax error code and null otherwise.
	 * @param dtdCode                  the DTD error code and null otherwise.
	 * @param xsdCode                  the XSD error code and null otherwise.
	 * @param grammarURI               the referenced grammar URI.
	 */
	private void fillReferencedGrammarDiagnostic(XMLLocator location, String key, Object[] arguments, String message,
			DiagnosticSeverity diagnosticSeverity, boolean fatalError,
			URIResolverExtensionManager resolverExtensionManager, XMLSyntaxErrorCode syntaxCode, DTDErrorCode dtdCode,
			XSDErrorCode xsdCode, String grammarURI) {
		// Create diagnostic where DDT, XSD which have errors is declared if needed
		ReferencedGrammarDiagnosticsInfo info = getReferencedGrammarDiagnosticsInfo(grammarURI,
				resolverExtensionManager);
		if (info.isFatalError()) {
			// The last error was fatal, we ignore the other error to be consistent with
			// XSD, DTD validator (when XSD or DTD is edited and validated) which stops the
			// validation on the first fatal error.
			return;
		}
		info.addError(fatalError);
		if (hasRelatedInformation && info.getGrammarDocument() != null) {
			DOMDocument grammarDocument = info.getGrammarDocument();
			Range range = null;
			if (dtdCode != null) {
				range = DTDErrorCode.toLSPRange(location, dtdCode, arguments, grammarDocument);
			} else if (xsdCode != null) {
				range = XSDErrorCode.toLSPRange(location, xsdCode, arguments, grammarDocument);
			} else {
				range = XMLSyntaxErrorCode.toLSPRange(location, syntaxCode, arguments, grammarDocument);
			}
			if (range == null) {
				range = createDefaultRange(location, grammarDocument);
			}
			if (range == null) {
				try {
					range = new Range(new Position(0, 0), grammarDocument.positionAt(grammarDocument.getEnd()));
				} catch (BadLocationException e) {
				}
			}
			DiagnosticRelatedInformation r = new DiagnosticRelatedInformation(
					range != null ? new Location(grammarURI, range) : null, message);
			info.addDiagnosticRelatedInformation(r);
		}
	}

	/**
	 * Returns the referenced grammar diagnostics info from the given grammar URI.
	 *
	 * @param grammarURI               the referenced grammar URI.
	 * @param resolverExtensionManager the resolver used to load the DOM document of
	 *                                 the referenced grammar.
	 * @return
	 */
	private ReferencedGrammarDiagnosticsInfo getReferencedGrammarDiagnosticsInfo(String grammarURI,
			URIResolverExtensionManager resolverExtensionManager) {
		ReferencedGrammarDiagnosticsInfo info = referencedGrammarDiagnosticsInfoCache.get(grammarURI);
		if (info == null) {
			// Create diagnostic where DDT, XSD which have errors is declared
			Range range = getReferencedGrammarRange(grammarURI);
			String message = "";
			Diagnostic diagnostic = super.addDiagnostic(range, message, DiagnosticSeverity.Error, null, null);
			// Register the diagnostic as root diagnostic for the XSD, DTD grammar uri
			info = new ReferencedGrammarDiagnosticsInfo(grammarURI, resolverExtensionManager, diagnostic);
			referencedGrammarDiagnosticsInfoCache.put(grammarURI, info);
		}
		return info;
	}

	private Range getReferencedGrammarRange(String grammarURI) {
		Set<ReferencedGrammarInfo> referencedGrammars = getReferencedGrammars();
		for (ReferencedGrammarInfo referencedGrammarInfo : referencedGrammars) {
			if (grammarURI.equals(referencedGrammarInfo.getResolvedURIInfo().getResolvedURI())) {
				DOMRange range = referencedGrammarInfo.getIdentifier() != null
						? referencedGrammarInfo.getIdentifier().getRange()
						: null;
				if (range != null) {
					return XMLPositionUtility.createRange(range);
				}
			}
		}
		// Set the error range in the root start tag
		return XMLPositionUtility.selectRootStartTag(getDOMDocument());
	}

	private Set<ReferencedGrammarInfo> getReferencedGrammars() {
		if (referencedGrammars != null) {
			return referencedGrammars;
		}
		return referencedGrammars = contentModelManager.getReferencedGrammarInfos(super.getDOMDocument());
	}

	@Override
	protected boolean isIgnoreFatalError(String key) {
		// Don't stop the validation when there are
		// * EntityNotDeclared error
		return DTDErrorCode.EntityNotDeclared.name().equals(key);
	}

	public void endReport() {
		if (referencedGrammarDiagnosticsInfoCache.isEmpty()) {
			return;
		}
		// When a XML is validated by a DTD or XSD which have syntax error, the XSD, DTD
		// grammar is cached in the pool.
		// This behavior is annoying because when XML is validate in the second time,
		// Xerces uses the cached XSD, DTD grammar (which have syntax error)
		// and the referenced grammar error disappear.

		// To fix this problem, the grammar pool is updated by removing the referenced
		// grammars which have problems.
		LSPXMLGrammarPool grammarPool = (LSPXMLGrammarPool) contentModelManager.getGrammarPool();
		if (grammarPool == null) {
			return;
		}
		// Remove referenced grammar which have problem from the Xerces pool cache.
		Set<String> grammarURIs = referencedGrammarDiagnosticsInfoCache.keySet();
		for (String grammarURI : grammarURIs) {
			grammarPool.removeGrammar(grammarURI);
		}
	}
}
