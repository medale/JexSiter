package org.medale.exsiter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVWriter;

public class MapStoreTest {

	@Test
	public void test() throws IOException {
		String tmpDir = System.getProperty("java.io.tmpdir");
		File testHome = new File(tmpDir, "test");
		if (testHome.exists()) {
			FileUtils.deleteDirectory(testHome);
		}
		testHome.mkdirs();
		System.out.println("Created temp test dir in "
				+ testHome.getAbsolutePath());

		CSVWriter writer = null;
		try {
			File csvFile = new File(testHome, "test.csv");
			// generate default , separator with " quote char
			writer = new CSVWriter(new FileWriter(csvFile));

		} finally {
			IOUtils.closeQuietly(writer);
		}
	}
}
