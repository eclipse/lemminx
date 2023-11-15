/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.uriresolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.lemminx.utils.ProjectUtils;

/**
 * Test server
 */
public class FileServer {

	private static Logger LOG = Logger.getLogger(FileServer.class.getName());

	private Server server;

	/**
	 * Creates an http server on a random port, serving the
	 * <code>src/test/resources</code> directory.
	 *
	 * @throws IOException
	 */
	public FileServer() throws IOException {
		this(ProjectUtils.getProjectDirectory().resolve("src/test/resources"));
	}

	/**
	 * Creates an http server on a random port, serving the <code>dir</code>
	 * directory (relative to the current project).
	 *
	 * @param baseDir the base directory.
	 * @throws IOException
	 */
	public FileServer(Path baseDir) throws IOException {
		Files.createDirectories(baseDir);
		ResourceHandler resourceHandler = new ModifiedResourceHandler();
		resourceHandler.setBaseResource(new PathResource(baseDir.toRealPath()));
		resourceHandler.setDirectoriesListed(true);
		serve(resourceHandler, new DefaultHandler());
	}
	
	/**
	 * Creates an http server on a random port, delegating to the given handlers
	 * @param handlers the Handlers to delegate serving to.
	 * @throws IOException
	 */
	public FileServer(Handler...handlers) throws IOException {
		serve(handlers);
	}

	private void serve(Handler...handlers) {
		server = new Server(0);
		server.setHandler(new HandlerList(handlers));
	}
	
	/**
	 * @return the port the server was started on.
	 * @throws Exception
	 */
	public int start() throws Exception {
		if (!server.isStarting() && !server.isStarted() && !server.isRunning()) {
			server.start();
		}
		int port = getPort();
		LOG.info("http server started on port " + port);
		return port;
	}

	public int getPort() {
		return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
	}

	public void stop() throws Exception {
		server.stop();
	}

	public String getUri(String resourcePath) {
		StringBuilder sb = new StringBuilder("http://localhost:").append(getPort());
		if (!resourcePath.startsWith("/")) {
			sb.append("/");
		}
		sb.append(resourcePath);
		LOG.info("remote uri : " + sb.toString());
		return sb.toString();
	}
}