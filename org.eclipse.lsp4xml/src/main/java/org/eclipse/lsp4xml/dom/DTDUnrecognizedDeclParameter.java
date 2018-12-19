package org.eclipse.lsp4xml.dom;

/**
 	DTDUnrecognizedDeclParamater
 */
public class DTDUnrecognizedDeclParameter extends DTDDeclParameter {
	public DTDUnrecognizedDeclParameter(DOMDocumentType doctype, int start, int end) {
		super(doctype, start, end);
	}

	@Override
	public String getParameter() {
		return super.getParameter().trim();
	}
}