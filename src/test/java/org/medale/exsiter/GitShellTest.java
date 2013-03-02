package org.medale.exsiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * Setup: mkdir jex-repo<br>
 * cd jex-repo<br>
 * git init<br>
 * 
 * http://stackoverflow.com/questions/6861881/jgit-cannot-find-a-tutorial-or-
 * simple-example
 */
public class GitShellTest {

	private Date testDate;

	@Before
	public void setUp() {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("EST"));
		cal.set(2013, Calendar.FEBRUARY, 27);
		testDate = cal.getTime();
	}

	@Test
	public void testGetDateTag() {
		String dateTag = GitShell.getDateTag(testDate);
		assertEquals("2013Feb27", dateTag);
	}

	@Test
	public void testingJGitApi() throws IOException, NoFilepatternException,
			GitAPIException {
		String tmpDir = System.getProperty("java.io.tmpdir");
		File testHome = new File(tmpDir, "test");
		File cloneHome = new File(tmpDir, "clone");
		if (testHome.exists()) {
			FileUtils.deleteDirectory(testHome);
		}
		if (cloneHome.exists()) {
			FileUtils.deleteDirectory(cloneHome);
		}
		testHome.mkdir();
		cloneHome.mkdir();

		System.out.println("Created temp test dir in "
				+ testHome.getAbsolutePath());
		File testGitHome = new File(testHome, ".git");
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repo = builder.setGitDir(testGitHome).build();
		repo.create();
		Git git = new Git(repo);
		String[] fileNames = { "a.txt", "b.txt", "c.txt" };
		String[] fileContent = fileNames;
		writeFiles(testHome, fileNames, fileContent);
		AddCommand addCommand = git.add();
		addCommand.addFilepattern(".").call();
		CommitCommand commitCommand = git.commit();
		commitCommand.setMessage("Test commit").call();

		String[] newContent = { "updateA", "updateB", "updateC" };
		writeFiles(testHome, fileNames, newContent);
		// must create new addCommand and commitCommand!
		addCommand = git.add();
		addCommand.addFilepattern(".").call();
		commitCommand = git.commit();
		commitCommand.setMessage("Commit after update.").call();

		TagCommand tagCommand = git.tag();
		String tagName = GitShell.getDateTag(testDate);
		tagCommand.setName(tagName).call();

		CloneCommand cloneCommand = Git.cloneRepository();
		String testGitHomeUri = "file:///" + testGitHome.getAbsolutePath();
		cloneCommand.setURI(testGitHomeUri);
		cloneCommand.setDirectory(cloneHome).call();

		File[] cloneDirFiles = cloneHome.listFiles();
		List<File> expectedFiles = Arrays.asList(new File(cloneHome, "a.txt"),
				new File(cloneHome, "b.txt"), new File(cloneHome, "c.txt"),
				new File(cloneHome, ".git"));

		Arrays.asList(cloneDirFiles);
		assertEquals(expectedFiles.size(), cloneDirFiles.length);
		for (File file : cloneDirFiles) {
			assertTrue(expectedFiles.contains(file));
		}
	}

	private void writeFiles(File testHome, String[] fileNames,
			String[] fileContent) throws IOException {
		for (int i = 0; i < fileContent.length; i++) {
			String fileName = fileNames[i];
			String content = fileContent[i] + "\n";
			File file = new File(testHome, fileName);
			FileUtils.writeStringToFile(file, content);
		}
	}

}
