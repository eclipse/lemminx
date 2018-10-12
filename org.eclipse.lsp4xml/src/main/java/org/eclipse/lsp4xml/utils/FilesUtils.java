/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Files utilities.
 *
 */
public class FilesUtils {

	private static final Path DEPLOYED_BASE_PATH = getDeployedBasePath();

	private static Path getDeployedBasePath() {
		String dir = System.getProperty("user.home");
		if (dir == null) {
			dir = System.getProperty("user.dir");
		}
		if (dir == null) {
			dir = "";
		}
		return Paths.get(dir, ".lsp4xml");
	}

	/**
	 * Returns the deployed path from the given <code>path</code>.
	 * 
	 * @param path the path
	 * @return the deployed path from the given <code>path</code>.
	 * @throws IOException
	 */
	public static Path getDeployedPath(Path path) throws IOException {
		return DEPLOYED_BASE_PATH.resolve(path);
	}

	/**
	 * Save the given input stream <code>in</code> in the give out file
	 * <code>outFile</code>
	 * 
	 * @param in      the input stream
	 * @param outFile the output file
	 * @throws IOException
	 */
	public static void saveToFile(InputStream in, Path outFile) throws IOException {
		saveToFile(toString(in), outFile);
	}

	/**
	 * Save the given String <code>content</code> in the give out file
	 * <code>outFile</code>
	 * 
	 * @param content the string content
	 * @param outFile the output file
	 * @throws IOException
	 */
	public static void saveToFile(String content, Path outFile) throws IOException {
		if (!Files.exists(outFile.getParent())) {
			Files.createDirectories(outFile.getParent());
		}
		try (Writer writer = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
			writer.write(content);
		}
	}

	static String toString(InputStream is) {
		try (Scanner s = new Scanner(is)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}
}
