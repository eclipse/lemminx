package org.eclipse.lemminx.extensions.xsl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.uriresolver.AbstractClasspathResourceResolver;
import org.eclipse.lemminx.uriresolver.CacheResourcesManager.ResourceToDeploy;

public class XSLURIResolverExtension extends AbstractClasspathResourceResolver {
	/**
	 * The XSL namespace URI (= http://www.w3.org/1999/XSL/Transform)
	 */
	private static final String XSL_NAMESPACE_URI = "http://www.w3.org/1999/XSL/Transform"; //$NON-NLS-1$

	private static final ResourceToDeploy XML_SCHEMA_10 = new ResourceToDeploy("https://www.w3.org/1999/11/xslt10.xsd",
			"/schemas/xslt/xslt-1.0.xsd");

	private static final Map<String, ResourceToDeploy> XSL_RESOURCES;

	static {
		XSL_RESOURCES = new HashMap<>();
		XSL_RESOURCES.put("1.0", XML_SCHEMA_10);
		XSL_RESOURCES.put("2.0",
				new ResourceToDeploy("https://www.w3.org/2007/schema-for-xslt20.xsd", "/schemas/xslt/xslt-2.0.xsd"));
		XSL_RESOURCES.put("3.0", new ResourceToDeploy("https://www.w3.org/TR/xslt-30/schema-for-xslt30.xsd",
				"/schemas/xslt/xslt-3.0.xsd"));
	}

	private final IXMLDocumentProvider documentProvider;

	public XSLURIResolverExtension(IXMLDocumentProvider documentProvider) {
		this.documentProvider = documentProvider;
	}

	@Override
	public ResourceToDeploy resourceToResolve(String baseLocation, String publicId, String systemId) {
		if (!XSL_NAMESPACE_URI.equals(publicId)) {
			return null;
		}

		String version = getVersion(baseLocation);
		return XSL_RESOURCES.getOrDefault(version, XML_SCHEMA_10);
	}

	/**
	 * Returns the version coming from xsl:stylesheet/@version of the XML document
	 * retrieved by the given uri
	 * 
	 * @param uri
	 * @return the version coming from xsl:stylesheet/@version of the XML document
	 *         retrieved by the given uri
	 */
	private String getVersion(String uri) {
		if (documentProvider == null) {
			return null;
		}
		DOMDocument document = documentProvider.getDocument(uri);
		if (document != null) {
			DOMElement element = document.getDocumentElement();
			if (element != null) {
				String version = element.getAttribute("version");
				if (version != null) {
					return version;
				}
			}
		}
		return "1.0";
	}
}
