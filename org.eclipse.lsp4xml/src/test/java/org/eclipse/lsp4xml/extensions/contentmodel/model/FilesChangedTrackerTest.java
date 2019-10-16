/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.contentmodel.model;

import java.io.IOException;

import org.eclipse.lsp4xml.extensions.contentmodel.BaseFileTempTest;
import org.junit.Assert;
import org.junit.Test;

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

		Assert.assertFalse("No dirty after file creation", tracker.isDirty());

		updateFile(fileURI, "<root />");
		Assert.assertTrue("Dirty after file modification on isDirty first call", tracker.isDirty());
		Assert.assertFalse("NO Dirty after file modification on isDirty second call", tracker.isDirty());

	}
}
