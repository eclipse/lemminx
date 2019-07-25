package org.eclipse.lsp4xml.extensions.contentmodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

public class BaseFileTempTest {

	protected static final String tempDirPath = "target/temp/";
	protected static final URI tempDirUri = Paths.get(tempDirPath).toAbsolutePath().toUri();

	@BeforeClass
	public static void setup() throws FileNotFoundException, IOException {
		deleteTempDirIfExists();
		createTempDir();
	}

	@AfterClass
	public static void tearDown() throws IOException {
		deleteTempDirIfExists();
	}

	private static void deleteTempDirIfExists() throws IOException {
		File tempDir = new File(tempDirUri);
		if (tempDir.exists()) {
			MoreFiles.deleteRecursively(tempDir.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
		}
	}

	private static void createTempDir() {
		File tempDir = new File(tempDirUri);
		tempDir.mkdir();
	}

	protected static void createFile(String path, String contents) throws IOException {
		Files.asCharSink(new File(path), Charsets.UTF_8).write(contents);
	}
}
