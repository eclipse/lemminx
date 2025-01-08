/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.uriresolver;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class ModifiedResourceHandler extends ResourceHandler {

	public ModifiedResourceHandler() {
		super();
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		// 403 if user agent starts with Java/1. with https://lime.software/.
		// See https://github.com/redhat-developer/vscode-xml/issues/429#issuecomment-784875083
		String userAgent = request.getHeader("User-Agent");
		if (userAgent != null && userAgent.startsWith("Java/1.")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, userAgent + " is not allowed");
			return;
		}
		super.handle(target, baseRequest, request, response);
	}

}
