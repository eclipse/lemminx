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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4xml.utils.FilesUtils;

public class CacheResourcesManager {

	private static final CacheResourcesManager INSTANCE = new CacheResourcesManager();

	public static CacheResourcesManager getInstance() {
		return INSTANCE;
	}

	private final List<String> resourcesLoading;
	private boolean useCache;

	private CacheResourcesManager() {
		resourcesLoading = new ArrayList<>();
	}

	public Path getResources(final String resourceURI) throws IOException {
		Path resourceCachePath = getResourceCachePath(resourceURI);
		if (Files.exists(resourceCachePath)) {
			return resourceCachePath;
		}
		synchronized (resourcesLoading) {
			if (resourcesLoading.contains(resourceURI)) {
				throw new CacheResourceLoadingException(resourceURI);
			}
			resourcesLoading.add(resourceURI);
		}

		CompletableFuture f = CompletableFuture.supplyAsync(() -> {
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

				Path dir = resourceCachePath.getParent();
				if (!Files.exists(dir)) {
					Files.createDirectories(dir);
				}

				ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
				FileOutputStream fos = new FileOutputStream(resourceCachePath.toFile());
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
				rbc.close();
				synchronized (resourcesLoading) {
					resourcesLoading.remove(resourceURI);
				}

			} catch (Exception e) {
				synchronized (resourcesLoading) {
					resourcesLoading.remove(resourceURI);
				}
			} finally {
				if (conn != null && conn instanceof HttpURLConnection) {
					((HttpURLConnection) conn).disconnect();
				}
			}
			return "";
		});

		if (f.getNow(null) == null) {
			throw new CacheResourceLoadingException(resourceURI);
		}

		return resourceCachePath;
	}

	private static Path getResourceCachePath(String resourceURI) throws IOException {
		URI uri = URI.create(resourceURI);
		Path resourceCachePath = Paths.get("cache", uri.getScheme(), uri.getPath());
		return FilesUtils.getDeployedPath(resourceCachePath);
	}

	public boolean canUseCache(String url) {
		return isUseCache() && url != null && (url.startsWith("http:") || url.startsWith("ftp:"));
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	public boolean isUseCache() {
		return useCache;
	}
}
