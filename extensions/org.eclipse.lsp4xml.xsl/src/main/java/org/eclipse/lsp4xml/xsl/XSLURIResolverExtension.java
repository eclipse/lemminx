package org.eclipse.lsp4xml.xsl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;

public class XSLURIResolverExtension implements URIResolverExtension {

	/**
	 * The XSL namespace URI (= http://www.w3.org/1999/XSL/Transform)
	 */
	private static final String XSL_NAMESPACE_URI = "http://www.w3.org/1999/XSL/Transform"; //$NON-NLS-1$

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if (!XSL_NAMESPACE_URI.equals(publicId)) {
			return null;
		}
		else {
			
		}
		// TODO: extract version from XML Document
		String version = "1.0";
		String schemaFileName = "xslt-" + version + ".xsd";
		String schemaPath = "/xslt-schemas/" + schemaFileName;

		try {
			Path baseDir = Paths.get("C://lsp4xml");
			Files.createDirectories(baseDir);
			Path outFile = baseDir.resolve(schemaFileName);
			if (!outFile.toFile().exists()) {
				InputStream in = XSLURIResolverExtension.class.getResourceAsStream(schemaPath);
				String xml = convertStreamToString(in);
				saveToFile(xml, outFile);
			}
			return outFile.toFile().toURI().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void saveToFile(String xml, Path outFile) throws IOException {
		try (Writer writer = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
			writer.write(xml);
		}
	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

}
