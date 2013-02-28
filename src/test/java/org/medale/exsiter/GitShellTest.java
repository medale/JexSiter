package org.medale.exsiter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.AddCommand;
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
	public void test() {
		String dateTag = GitShell.getDateTag(testDate);
		assertEquals("2013Feb27", dateTag);
	}

	@Test
	public void testingJGitApi() throws IOException, NoFilepatternException,
			GitAPIException {
		String tmpDir = System.getProperty("java.io.tmpdir");
		File testHome = new File(tmpDir, "test");
		if (testHome.exists()) {
			FileUtils.deleteDirectory(testHome);
		}
		testHome.mkdir();
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
