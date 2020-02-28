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
package org.eclipse.lemminx.utils;

import static org.eclipse.lemminx.utils.OSUtils.SLASH;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Files utilities.
 *
 */
public class FilesUtils {

	private static final Logger LOGGER = Logger.getLogger(FilesUtils.class.getName());

	public static final String FILE_SCHEME = "file://";
	public static final String LEMMINX_WORKDIR_KEY = "lemminx.workdir";
	private static String cachePathSetting = null;

	private static Pattern uriSchemePattern = Pattern.compile("^([a-zA-Z\\-]+:\\/\\/).*");
	private static Pattern endFilePattern = Pattern.compile(".*[\\\\\\/]\\.[\\S]+");

	public static String getCachePathSetting() {
		return cachePathSetting;
	}

	public static void setCachePathSetting(String cachePathSetting) {
		if (StringUtils.isEmpty(cachePathSetting)) {
			FilesUtils.cachePathSetting = null;
		} else {
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
	 * Given a file path as a string, will normalize it and return the normalized
	 * string if valid, or null if not.
	 * 
	 * The '~' home symbol will be converted into the actual home path. Slashes will
	 * be corrected depending on the OS.
	 */
	public static String normalizePath(String pathString) {
		if (pathString != null && !pathString.isEmpty()) {
			if (pathString.indexOf("~") == 0) {
				pathString = System.getProperty("user.home") + (pathString.length() > 1 ? pathString.substring(1) : "");
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
		String dir = System.getProperty(LEMMINX_WORKDIR_KEY);
		if (dir != null) {
			return Paths.get(dir);
		}
		if (cachePathSetting != null && !cachePathSetting.isEmpty()) {
			return Paths.get(cachePathSetting);
		}
		dir = System.getProperty("user.home");
		if (dir == null) {
			dir = System.getProperty("user.dir");
		}
		if (dir == null) {
			dir = "";
		}
		return Paths.get(dir, ".lemminx");
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
		saveToFile(IOUtils.convertStreamToString(in), outFile);
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
		// Make sure it's not executable
		try {
			Files.setPosixFilePermissions(outFile, PosixFilePermissions.fromString("rw-r-----"));
		} catch (UnsupportedOperationException e) {
			// The associated file system does not support the PosixFileAttributeView,
			// ignore the error
			LOGGER.log(Level.SEVERE,
					"The associated file system '" + outFile + "' + does not support the PosixFileAttributeView", e);
		}
	}

	public static int getOffsetAfterScheme(String uri) {
		Matcher m = uriSchemePattern.matcher(uri);

		if (m.matches()) {
			return m.group(1).length();
		}

		return -1;
	}

	/**
	 * Will return a Path object representing an existing path.
	 * 
	 * If this method is able to find an existing file/folder then it will return
	 * the path to that, else it will try to find the parent directory of the
	 * givenPath.
	 * 
	 * **IMPORTANT** The slashes of the given paths have to match the supported OS
	 * file path slash
	 * 
	 * @param parentDirectory The directory that the given path is relative to
	 * @param givenPath       Path that could be absolute or relative
	 * @return
	 */
	public static Path getNormalizedPath(String parentDirectory, String givenPath) {

		if (givenPath == null) {
			return null;
		}

		int lastIndexOfSlash = givenPath.lastIndexOf(SLASH);

		// in case the given path is incomplete, trim the end
		String givenPathCleaned;
		if (lastIndexOfSlash == 0) { // Looks like `/someFileOrFolder`
			return Paths.get(SLASH);
		} else {
			givenPathCleaned = lastIndexOfSlash > -1 ? givenPath.substring(0, lastIndexOfSlash) : null;
		}

		Path p;

		// The following 2 are for when the given path is already valid
		p = getPathIfExists(givenPath);
		if (p != null) {
			// givenPath is absolute
			return p;
		}

		p = getPathIfExists(givenPathCleaned);
		if (p != null) {
			// givenPath is absolute
			return p;
		}

		if (parentDirectory == null) {
			return null;
		}

		if (parentDirectory.endsWith(SLASH)) {
			parentDirectory = parentDirectory.substring(0, parentDirectory.length() - 1);
		}

		String combinedPath = parentDirectory + SLASH + givenPath;
		p = getPathIfExists(combinedPath);
		if (p != null) {
			return p;
		}

		combinedPath = parentDirectory + SLASH + givenPathCleaned;
		p = getPathIfExists(combinedPath);
		if (p != null) {
			return p;
		}

		return null;
	}

	private static Path getPathIfExists(String path) {
		try {
			Path p = Paths.get(path).normalize();
			return p.toFile().exists() ? p : null;
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Returns the slash ("/" or "\") that is used by the given string. If no slash
	 * is given "/" is returned by default.
	 * 
	 * @param text
	 * @return
	 */
	public static String getFilePathSlash(String text) {
		if (text.contains("\\")) {
			return "\\";
		}
		return "/";
	}

	/**
	 * Ensures there is no slash before a drive letter, and forces use of '\'
	 * 
	 * @param pathString
	 * @return
	 */
	public static String convertToWindowsPath(String pathString) {
		String pathSlash = getFilePathSlash(pathString);
		if (pathString.startsWith(pathSlash)) {
			if (pathString.length() > 3) {
				char letter = pathString.charAt(1);
				char colon = pathString.charAt(2);
				if (Character.isLetter(letter) && ':' == colon) {
					pathString = pathString.substring(1);
				}
			}
		}
		return pathString.replace("/", "\\");
	}

	public static boolean pathEndsWithFile(String pathString) {
		Matcher m = endFilePattern.matcher(pathString);
		return m.matches();
	}

	public static boolean isIncludedInDeployedPath(Path resourceCachePath) {
		return resourceCachePath.normalize().startsWith(DEPLOYED_BASE_PATH.get());
	}
}
