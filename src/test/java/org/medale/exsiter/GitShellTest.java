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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Setup: mkdir jex-repo<br>
 * cd jex-repo<br>
 * git init<br>
 * 
 * http://stackoverflow.com/questions/6861881/jgit-cannot-find-a-tutorial-or-
 * simple-example
 */
public class GitShellTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Date testDate;

    @Before
    public void setUp() {
        final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("EST"));
        cal.set(2013, Calendar.FEBRUARY, 27);
        this.testDate = cal.getTime();
    }

    @Test
    public void testGetDateTag() {
        final String dateTag = GitShell.getDateTag(this.testDate);
        assertEquals("2013Feb27", dateTag);
    }

    @Test
    public void testInitGitRepo() throws IOException {
        final File testHome = this.tempFolder.getRoot();
        final File expectedGitDir = new File(testHome, GitShell.GIT_DIR);
        assertFalse(expectedGitDir.exists());
        final Repository repo = GitShell.getGitRepository(testHome);
        GitShell.initGitRepository(repo);
        assertTrue(expectedGitDir.exists());
    }

    @Test
    public void testAddAllChanges() throws IOException {
        final File testHome = this.tempFolder.getRoot();
        final Repository repo = GitShell.getGitRepository(testHome);
        GitShell.initGitRepository(repo);
        final String[] fileNames = { "a.txt" };
        final String[] fileContent = fileNames;
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

    @Test
    public void testAddAllChangesWithModsAndDeletes() throws IOException {
        final File testHome = this.tempFolder.getRoot();
        final Repository repo = GitShell.getGitRepository(testHome);
        GitShell.initGitRepository(repo);
        final String[] fileNames = { "a.txt", "b.txt", "c.txt" };
        final String[] fileContent = fileNames;
        writeFiles(testHome, fileNames, fileContent);
        GitShell.addAllChanges(repo);
        GitShell.commitAllChanges(repo, "original");
        Status status = GitShell.getStatus(repo);
        assertTrue(status.isClean());

        // mod a.txt
        FileUtils.writeStringToFile(new File(testHome, fileNames[0]), "mod");
        // delete b.txt
        final File bDotTxt = new File(testHome, fileNames[1]);
        final boolean delete = bDotTxt.delete();
        assertTrue(delete);
        // add d.txt
        FileUtils.writeStringToFile(new File(testHome, "d.txt"), "d.txt");
        status = GitShell.getStatus(repo);
        assertFalse(status.isClean());

        GitShell.addAllChanges(repo);
        GitShell.commitAllChanges(repo, "changed");

        status = GitShell.getStatus(repo);
        assertTrue(status.isClean());
    }

    private void assertContainsAll(final List<String> expectedFileNames,
            final Set<String> actualFileNames) {
        assertEquals(expectedFileNames.size(), actualFileNames.size());
        assertTrue(actualFileNames.containsAll(expectedFileNames));
    }

    @Test
    public void testCommitAllChanges() throws IOException {
        final File testHome = this.tempFolder.getRoot();
        final Repository repo = GitShell.getGitRepository(testHome);
        GitShell.initGitRepository(repo);
        final String[] fileNames = { "a.txt" };
        final String[] fileContent = fileNames;
        writeFiles(testHome, fileNames, fileContent);
        GitShell.addAllChanges(repo);
        final String commitMessage = "Test commit";
        GitShell.commitAllChanges(repo, commitMessage);
        final Status status = GitShell.getStatus(repo);
        assertTrue(status.isClean());
    }

    @Test
    public void testCreateNewTag() throws IOException {
        final File testHome = this.tempFolder.getRoot();
        final Repository repo = GitShell.getGitRepository(testHome);
        GitShell.initGitRepository(repo);
        final String[] fileNames = { "a.txt" };
        final String[] fileContent = fileNames;
        writeFiles(testHome, fileNames, fileContent);
        GitShell.addAllChanges(repo);
        final String commitMessage = "Test commit";
        GitShell.commitAllChanges(repo, commitMessage);

        final String tagName = GitShell.TAG_PREFIX + "2013Feb27";
        GitShell.createNewTag(repo, tagName);

        final Git git = new Git(repo);
        final ListTagCommand tagList = git.tagList();
        try {
            final List<Ref> tags = tagList.call();
            assertEquals(1, tags.size());
            final Ref ref = tags.get(0);
            assertEquals("refs/tags/" + tagName, ref.getName());
        } catch (final GitAPIException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void testCloneRepoDir() throws IOException {
        final File testHome = this.tempFolder.getRoot();
        final Repository repo = GitShell.getGitRepository(testHome);
        GitShell.initGitRepository(repo);
        final String[] fileNames = { "a.txt" };
        final String[] fileContent = fileNames;
        writeFiles(testHome, fileNames, fileContent);
        GitShell.addAllChanges(repo);
        final String commitMessage = "Test commit";
        GitShell.commitAllChanges(repo, commitMessage);

        final String cloneHomeLocation = "clone";
        final File expectedCloneDir = new File(testHome, cloneHomeLocation);
        assertFalse(expectedCloneDir.exists());

        GitShell.cloneRepoDir(testHome, testHome, cloneHomeLocation);

        assertTrue(expectedCloneDir.exists());
        final Set<String> expectedFileSet = new HashSet<String>(
                Arrays.asList(new String[] { "a.txt", ".git" }));

        final String[] actualFiles = expectedCloneDir.list();
        final Set<String> actualFileSet = new HashSet<String>(
                Arrays.asList(actualFiles));
        assertTrue(expectedFileSet.equals(actualFileSet));
    }

    @Test
    public void testingJGitApi() throws IOException, NoFilepatternException,
            GitAPIException {

        final File testHome = this.tempFolder.getRoot();
        final File cloneHome = this.tempFolder.newFolder();

        System.out.println("Created temp test dir in "
                + testHome.getAbsolutePath());
        final File testGitHome = new File(testHome, ".git");
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository repo = builder.setGitDir(testGitHome).build();
        repo.create();
        final Git git = new Git(repo);
        final String[] fileNames = { "a.txt", "b.txt", "c.txt" };
        final String[] fileContent = fileNames;
        writeFiles(testHome, fileNames, fileContent);
        AddCommand addCommand = git.add();
        addCommand.addFilepattern(".").call();
        CommitCommand commitCommand = git.commit();
        commitCommand.setMessage("Test commit").call();

        final String[] newContent = { "updateA", "updateB", "updateC" };
        writeFiles(testHome, fileNames, newContent);
        // must create new addCommand and commitCommand!
        addCommand = git.add();
        addCommand.addFilepattern(".").call();
        commitCommand = git.commit();
        commitCommand.setMessage("Commit after update.").call();

        final TagCommand tagCommand = git.tag();
        final String tagName = GitShell.getDateTag(this.testDate);
        tagCommand.setName(tagName).call();

        final CloneCommand cloneCommand = Git.cloneRepository();
        final String testGitHomeUri = "file:///"
                + testGitHome.getAbsolutePath();
        cloneCommand.setURI(testGitHomeUri);
        cloneCommand.setDirectory(cloneHome).call();

        final File[] cloneDirFiles = cloneHome.listFiles();
        final List<File> expectedFiles = Arrays.asList(new File(cloneHome,
                "a.txt"), new File(cloneHome, "b.txt"), new File(cloneHome,
                "c.txt"), new File(cloneHome, ".git"));

        Arrays.asList(cloneDirFiles);
        assertEquals(expectedFiles.size(), cloneDirFiles.length);
        for (final File file : cloneDirFiles) {
            assertTrue(expectedFiles.contains(file));
        }
    }

    private void writeFiles(final File testHome, final String[] fileNames,
            final String[] fileContent) throws IOException {
        for (int i = 0; i < fileContent.length; i++) {
            final String fileName = fileNames[i];
            final String content = fileContent[i] + "\n";
            final File file = new File(testHome, fileName);
            FileUtils.writeStringToFile(file, content);
        }
    }

}
