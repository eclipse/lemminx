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
package org.eclipse.lsp4xml.uriresolver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4xml.utils.FilesUtils;

/**
 * Cache resources manager.
 *
 */
public class CacheResourcesManager {

	private static final CacheResourcesManager INSTANCE = new CacheResourcesManager();

	public static CacheResourcesManager getInstance() {
		return INSTANCE;
	}

	private final Map<String, CompletableFuture<Path>> resourcesLoading;
	private boolean useCache;

	class ResourceInfo {

		String resourceURI;

		CompletableFuture<Path> future;

	}

	private CacheResourcesManager() {
		resourcesLoading = new HashMap<>();
	}

	public Path getResource(final String resourceURI) throws IOException {
		Path resourceCachePath = getResourceCachePath(resourceURI);
		if (Files.exists(resourceCachePath)) {
			return resourceCachePath;
		}
		CompletableFuture<Path> f = null;
		synchronized (resourcesLoading) {
			if (resourcesLoading.containsKey(resourceURI)) {
				CompletableFuture<Path> future = resourcesLoading.get(resourceURI);
				throw new CacheResourceDownloadingException(resourceURI, future);
			}
			f = downloadResource(resourceURI, resourceCachePath);
			resourcesLoading.put(resourceURI, f);
		}

		if (f.getNow(null) == null) {
			throw new CacheResourceDownloadingException(resourceURI, f);
		}

		return resourceCachePath;
	}

	private CompletableFuture<Path> downloadResource(final String resourceURI, Path resourceCachePath) {
		return CompletableFuture.supplyAsync(() -> {
			URLConnection conn = null;
			try {
				String actualURI = resourceURI;
				URL url = new URL(actualURI);
				conn = url.openConnection();
				/* XXX: This should really be implemented using HttpClient or similar */
				int allowedRedirects = 5;
				while (conn.getHeaderField("Location") != null && allowedRedirects > 0) //$NON-NLS-1$
				{
					allowedRedirects--;
					url = new URL(actualURI = conn.getHeaderField("Location")); //$NON-NLS-1$
					conn = url.openConnection();
				}

				// Download resource in a temporary file
				Path path = Files.createTempFile(resourceCachePath.getFileName().toString(), ".lsp4xml");
				try (ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
						FileOutputStream fos = new FileOutputStream(path.toFile())) {
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				}

				// Move the temporary file in the lsp4xml cache folder.
				Path dir = resourceCachePath.getParent();
				if (!Files.exists(dir)) {
					Files.createDirectories(dir);
				}
				Files.move(path, resourceCachePath);
			} catch (Exception e) {
				// Do nothing
				return null;
			} finally {
				synchronized (resourcesLoading) {
					resourcesLoading.remove(resourceURI);
				}
				if (conn != null && conn instanceof HttpURLConnection) {
					((HttpURLConnection) conn).disconnect();
				}
			}
			return resourceCachePath;
		});
	}

	private static Path getResourceCachePath(String resourceURI) throws IOException {
		URI uri = URI.create(resourceURI);
		Path resourceCachePath = Paths.get("cache", uri.getScheme(), uri.getHost(), uri.getPath());
		return FilesUtils.getDeployedPath(resourceCachePath);
	}

	/**
	 * Returns true if cache is enabled and url comes from "http" or "ftp" and false
	 * otherwise.
	 * 
	 * @param url
	 * @return true if cache is enabled and url comes from "http" or "ftp" and false
	 *         otherwise.
	 */
	public boolean canUseCache(String url) {
		return isUseCache() && url != null && (url.startsWith("http:") || url.startsWith("ftp:"));
	}

	/**
	 * Set true if cache must be used and false otherwise.
	 * 
	 * @param useCache true if cache must be used and false otherwise.
	 */
	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	/**
	 * Returns true if cache must be used and false otherwise.
	 * 
	 * @return true if cache must be used and false otherwise.
	 */
	public boolean isUseCache() {
		return useCache;
	}
}
