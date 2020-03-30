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
package org.eclipse.lemminx.extensions.contentmodel.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.eclipse.lemminx.extensions.contentmodel.BaseFileTempTest;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link FilesChangedTracker}
 * 
 * @author Angelo ZERR
 *
 */
public class FilesChangedTrackerTest extends BaseFileTempTest {

	@Test
	public void trackFile() throws IOException {
		FilesChangedTracker tracker = new FilesChangedTracker();
		String fileURI = tempDirUri.getPath() + "/track.xml";
		createFile(fileURI, "<root />");
		tracker.addFileURI("file://" + fileURI);

		assertFalse(tracker.isDirty(),"No dirty after file creation");

		updateFile(fileURI, "<root />");
		assertTrue(tracker.isDirty(), "Dirty after file modification on isDirty first call");
		assertFalse(tracker.isDirty(), "NO Dirty after file modification on isDirty second call");

	}
}
