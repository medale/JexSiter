package org.medale.exsiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Ref;
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
    public void testInitGitRepo() throws IOException {
        File testHome = getTestHome();
        File expectedGitDir = new File(testHome, GitShell.GIT_DIR);
        assertFalse(expectedGitDir.exists());
        Repository repo = GitShell.getGitRepository(testHome);
        GitShell.initGitRepository(repo);
        assertTrue(expectedGitDir.exists());
    }

    @Test
    public void testAddAllChanges() throws IOException {
        File testHome = getTestHome();
        Repository repo = GitShell.getGitRepository(testHome);
        GitShell.initGitRepository(repo);
        String[] fileNames = { "a.txt" };
        String[] fileContent = fileNames;
        writeFiles(testHome, fileNames, fileContent);
        Status status = GitShell.getStatus(repo);
        Set<String> untracked = status.getUntracked();
        assertContainsAll(Arrays.asList(fileNames), untracked);
        Set<String> added = status.getAdded();
        assertTrue(added.isEmpty());

        GitShell.addAllChanges(repo);
        status = GitShell.getStatus(repo);
        untracked = status.getUntracked();
        assertTrue(untracked.isEmpty());
        added = status.getAdded();
        assertContainsAll(Arrays.asList(fileNames), added);
    }

    private void assertContainsAll(List<String> expectedFileNames,
            Set<String> actualFileNames) {
        assertEquals(expectedFileNames.size(), actualFileNames.size());
        assertTrue(actualFileNames.containsAll(expectedFileNames));
    }

    @Test
    public void testCommitAllChanges() throws IOException {
        File testHome = getTestHome();
        Repository repo = GitShell.getGitRepository(testHome);
        GitShell.initGitRepository(repo);
        String[] fileNames = { "a.txt" };
        String[] fileContent = fileNames;
        writeFiles(testHome, fileNames, fileContent);
        GitShell.addAllChanges(repo);
        String commitMessage = "Test commit";
        GitShell.commitAllChanges(repo, commitMessage);
        Status status = GitShell.getStatus(repo);
        assertTrue(status.isClean());
    }

    @Test
    public void testCreateNewTag() throws IOException {
        File testHome = getTestHome();
        Repository repo = GitShell.getGitRepository(testHome);
        GitShell.initGitRepository(repo);
        String[] fileNames = { "a.txt" };
        String[] fileContent = fileNames;
        writeFiles(testHome, fileNames, fileContent);
        GitShell.addAllChanges(repo);
        String commitMessage = "Test commit";
        GitShell.commitAllChanges(repo, commitMessage);

        String tagName = GitShell.TAG_PREFIX + "2013Feb27";
        GitShell.createNewTag(repo, tagName);

        Git git = new Git(repo);
        ListTagCommand tagList = git.tagList();
        try {
            List<Ref> tags = tagList.call();
            assertEquals(1, tags.size());
            Ref ref = tags.get(0);
            assertEquals("refs/tags/" + tagName, ref.getName());
        } catch (GitAPIException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void testCloneRepoDir() throws IOException {
        File testHome = getTestHome();
        Repository repo = GitShell.getGitRepository(testHome);
        GitShell.initGitRepository(repo);
        String[] fileNames = { "a.txt" };
        String[] fileContent = fileNames;
        writeFiles(testHome, fileNames, fileContent);
        GitShell.addAllChanges(repo);
        String commitMessage = "Test commit";
        GitShell.commitAllChanges(repo, commitMessage);

        String cloneHomeLocation = "clone";
        File expectedCloneDir = new File(testHome, cloneHomeLocation);
        assertFalse(expectedCloneDir.exists());

        GitShell.cloneRepoDir(testHome, testHome, cloneHomeLocation);

        assertTrue(expectedCloneDir.exists());
        Set<String> expectedFileSet = new HashSet<String>(
                Arrays.asList(new String[] { "a.txt", ".git" }));

        String[] actualFiles = expectedCloneDir.list();
        Set<String> actualFileSet = new HashSet<String>(
                Arrays.asList(actualFiles));
        assertTrue(expectedFileSet.equals(actualFileSet));
    }

    @Test
    public void testingJGitApi() throws IOException, NoFilepatternException,
            GitAPIException {

        File testHome = getTestHome();
        File cloneHome = new File(testHome, "clone");
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

    private File getTestHome() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File testHome = new File(tmpDir, "test-git-func");
        if (testHome.exists()) {
            FileUtils.deleteDirectory(testHome);
        }
        return testHome;
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
