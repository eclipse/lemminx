/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Files utilities.
 *
 */
public class FilesUtils {

	public static final String LSP4XML_WORKDIR_KEY = "lsp4xml.workdir";
	private static String cachePathSetting = null;

	public static String getCachePathSetting() {
		return cachePathSetting;
	}

	public static void setCachePathSetting(String cachePathSetting) {
		if(StringUtils.isEmpty(cachePathSetting)) {
			FilesUtils.cachePathSetting = null;
		}
		else {
			FilesUtils.cachePathSetting = cachePathSetting;
		}
		resetDeployPath();
	}

	private FilesUtils() {
	}

	public static Supplier<Path> DEPLOYED_BASE_PATH;

	static {
		resetDeployPath();
	}

	/** Public for test purposes */
	public static void resetDeployPath() {
		DEPLOYED_BASE_PATH = Suppliers.memoize(() -> getDeployedBasePath());
	}

	/**
	 * Given a file path as a string, will normalize it
	 * and return the normalized string if valid, or null if not.
	 * 
	 * The '~' home symbol will be converted into the actual home path.
	 * Slashes will be corrected depending on the OS.
	 */
	public static String normalizePath(String pathString) {
		if (pathString != null && !pathString.isEmpty()) {
			if (pathString.indexOf("~") == 0) {
				pathString = System.getProperty("user.home") + (pathString.length() > 1? pathString.substring(1):"");
			}
			pathString = pathString.replace("/", File.separator);
			pathString = pathString.replace("\\", File.separator);
			Path p = Paths.get(pathString);
			pathString = p.normalize().toString();
			return pathString;
		}
		return null;
	}

	private static Path getDeployedBasePath() {
		String dir = System.getProperty(LSP4XML_WORKDIR_KEY);
		if (dir != null) {
			return Paths.get(dir);
		}
		if(cachePathSetting != null && !cachePathSetting.isEmpty()) {
			return Paths.get(cachePathSetting);
		}
		dir = System.getProperty("user.home");
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
		return DEPLOYED_BASE_PATH.get().resolve(path);
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
