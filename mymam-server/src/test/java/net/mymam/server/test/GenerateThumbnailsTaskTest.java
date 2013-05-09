/* MyMAM - Open Source Digital Media Asset Management.
 * http://www.mymam.net
 *
 * Copyright 2013, MyMAM contributors as indicated by the @author tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mymam.server.test;

import net.mymam.data.json.FileProcessorTaskDataKeys;
import net.mymam.data.json.FileProcessorTaskStatus;
import net.mymam.data.json.FileProcessorTaskType;
import net.mymam.data.json.MediaFileImportStatus;
import net.mymam.ejb.*;
import net.mymam.entity.*;
import net.mymam.exceptions.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.validation.ConstraintViolationException;
import java.io.File;
import java.nio.file.Paths;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * TODO: Clean up copy-and-paste code and make common helper class for this and GrabTaskTest.
 *
 * @author fstab
 */
@RunWith(Arquillian.class)
public class GenerateThumbnailsTaskTest {

    /**
     * Relative path to the src/main/webapp directory.
     * Will be used to find WEB-INF/jboss-ejb3.xml
     */
    private static final String WEBAPP_SRC = makeWebappSrc();

    /**
     * Get the relative path to the src/main/webapp directory.
     * <p/>
     * The current working directory might be "mymam" or "mymam/mymam-server",
     * depending on if the test is run from Maven or from the IDE.
     *
     * @return Relative path to the src/main/webapp directory.
     */
    private static String makeWebappSrc() {
        if (Paths.get("mymam-server/src/main/webapp").toFile().isDirectory()) {
            return "mymam-server/src/main/webapp";
        }
        return "src/main/webapp";
    }

    /**
     * Create the WAR deployment for the Arquillian test.
     *
     * @return The {@link org.jboss.shrinkwrap.api.spec.WebArchive} deployment used in this test.
     */
    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                // test implementation
                .addClass(JBossLoginContextFactory.class)
                .addClass(MediaFileEJB_AuthWrapper.class)
                        // add all JPA entities
                .addPackage(MediaFile.class.getPackage())
                        // add some EJBs
                .addClass(ConfigEJB.class)
                .addClass(MediaFileEJB.class)
                .addClass(MediaFileEJB_GrabTaskWithDelay.class)
                .addClass(PermissionEJB.class)
                .addClass(UserEJB.class)
                .addClass(UserMgmtEJB.class)
                        // add all Exceptions
                .addPackage(InvalidImportStateException.class.getPackage())
                        // add classes from mymam-common
                .addPackage(MediaFileImportStatus.class.getPackage())
                        // add resources
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(new File(WEBAPP_SRC, "WEB-INF/jboss-ejb3.xml"))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        // For debugging: Print the files in WAR. This helps to find missing files.
        System.out.println(war.toString(true));
        return war;
    }

    @EJB
    UserMgmtEJB userMgmtEJB;

    @EJB
    MediaFileEJB mediaFileEJB;

    // initialized in setUp() method
    MediaFileEJB_AuthWrapper mediaFileEJB_authWrapper;
    long testFileId;

    /**
     * Populate the database.
     *
     * The setUp() method creates
     * <ul>
     *     <li>A <i>system</i> user in role <i>system</i>.</li>
     *     <li>A <i>testuser</i> in role <i>user</i>.</li>
     *     <li>A {@link net.mymam.entity.MediaFile} with status NEW.</li>
     * </ul>
     */
    @Before
    public void setUp() throws LoginException, NamingException, PrivilegedActionException {
        User systemUser = userMgmtEJB.createUser("system", "system");
        Role systemRole = userMgmtEJB.createRole("system");
        userMgmtEJB.addRole(systemUser, systemRole);

        User testUser = userMgmtEJB.createUser("testuser", "testuser");
        Role userRole = userMgmtEJB.createRole("user");
        userMgmtEJB.addRole(testUser, userRole);

        mediaFileEJB_authWrapper = new MediaFileEJB_AuthWrapper(mediaFileEJB);

        testFileId = mediaFileEJB_authWrapper.as("testuser", "testuser").createNewMediaFile("test/video/root", "test-video-orig.mp4").getId();

        // Newly created files have a "Generate Proxy Task" and a "Generate Thumbnails Task". Get rid of these tasks.
        dummyExecuteGenerateProxyTask(testFileId);
        dummyExecuteGenerateThumbnailsTask(testFileId);
    }

    /**
     * Delete the data created in {@link #setUp()}.
     *
     * The tearDown() method should leave an empty database.
     */
    @After
    public void tearDown() throws InvalidImportStateException, PermissionDeniedException, NotFoundException, NamingException, LoginException, PrivilegedActionException, NoSuchTaskException {
        deleteFile(testFileId);
        userMgmtEJB.removeUser(userMgmtEJB.findUserByName("system"));
        userMgmtEJB.removeRole(userMgmtEJB.findRoleByName("system"));
        userMgmtEJB.removeUser(userMgmtEJB.findUserByName("testuser"));
        userMgmtEJB.removeRole(userMgmtEJB.findRoleByName("user"));
    }

    // Helper method to delete file with id=fileId
    private void deleteFile(long fileId) throws LoginException, PrivilegedActionException, NotFoundException, NoSuchTaskException {
        MediaFile testFile = mediaFileEJB.findById(fileId);

        // If the current task associated with the file is IN_PROGRESS,
        // it must be finished before the delete task can be executed.
        if ( testFile.getPendingTasksQueue() != null && testFile.getPendingTasksQueue().size() > 0 ) {
            FileProcessorTask curTask = testFile.getPendingTasksQueue().get(0);
            if ( curTask.getStatus() == FileProcessorTaskStatus.IN_PROGRESS ) {
                if ( curTask instanceof GenerateProxyVideosTask) {
                    Map<String, String> data = new HashMap<>();
                    data.put(FileProcessorTaskDataKeys.LOW_RES_MP4, "test.mp4");
                    data.put(FileProcessorTaskDataKeys.LOW_RES_WEMB, "test.webm");
                    mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(fileId, FileProcessorTaskType.GENERATE_PROXY_VIDEOS, data);
                }
                else if ( curTask instanceof GenerateThumbnailImagesTask ) {
                    Map<String, String> data = new HashMap<>();
                    data.put(FileProcessorTaskDataKeys.SMALL_IMG, "small.jpg");
                    data.put(FileProcessorTaskDataKeys.MEDIUM_IMG, "medium.jpg");
                    data.put(FileProcessorTaskDataKeys.LARGE_IMG, "large.jpg");
                    data.put(FileProcessorTaskDataKeys.THUMBNAIL_OFFSET_MS, "" + 0L);
                    mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(fileId, FileProcessorTaskType.GENERATE_THUMBNAILS, data);
                }
            }
        }

        // Schedule and execute delete task
        mediaFileEJB_authWrapper.as("testuser", "testuser").scheduleDeleteTask(fileId);
        Collection<Class> types = new ArrayList<>();
        types.add(DeleteTask.class);
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(types);
        mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(file.getId(), FileProcessorTaskType.DELETE, new HashMap<String, String>());
    }

    private void dummyExecuteGenerateProxyTask(long fileId) throws LoginException, PrivilegedActionException {
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateProxyVideosTask.class);
        assertEquals((long) file.getId(), fileId);
        Map<String, String> data = new HashMap<>();
        data.put(FileProcessorTaskDataKeys.LOW_RES_MP4, "test.mp4");
        data.put(FileProcessorTaskDataKeys.LOW_RES_WEMB, "test.webm");
        mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(file.getId(), FileProcessorTaskType.GENERATE_PROXY_VIDEOS, data);
    }

    private void dummyExecuteGenerateThumbnailsTask(long fileId) throws LoginException, PrivilegedActionException {
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertEquals((long) file.getId(), fileId);
        Map<String, String> data = new HashMap<>();
        data.put(FileProcessorTaskDataKeys.SMALL_IMG, "small.jpg");
        data.put(FileProcessorTaskDataKeys.MEDIUM_IMG, "medium.jpg");
        data.put(FileProcessorTaskDataKeys.LARGE_IMG, "large.jpg");
        data.put(FileProcessorTaskDataKeys.THUMBNAIL_OFFSET_MS, "" + 0L);
        mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(fileId, FileProcessorTaskType.GENERATE_THUMBNAILS, data);
    }

    @Test(expected = EJBAccessException.class)
    public void testUnauthenticated() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException, NoSuchTaskException {
        mediaFileEJB.scheduleGenerateThumbnailsTask(testFileId, 3L);
    }

    @Test(expected = EJBAccessException.class)
    public void testWrongRole() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException, NoSuchTaskException {
        mediaFileEJB_authWrapper.as("system", "system").scheduleGenerateThumbnailsTask(testFileId, 3L);
    }

    @Test
    public void testEmptyTaskList() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException, NoSuchTaskException {
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertNull(file);
    }

    @Test
    public void testCorrectExecution() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException, NoSuchTaskException {
        // generate task
        mediaFileEJB_authWrapper.as("testuser", "testuser").scheduleGenerateThumbnailsTask(testFileId, 42L);
        // grab task and verify parameters
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertEquals((long) file.getId(), testFileId);
        GenerateThumbnailImagesTask task = (GenerateThumbnailImagesTask) file.getPendingTasksQueue().get(0);
        assertEquals(FileProcessorTaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(42L, (long) task.getThumbnailOffsetMs());
        // post result
        Map<String, String> data = new HashMap<>();
        data.put(FileProcessorTaskDataKeys.SMALL_IMG, "test42.small.jpg");
        data.put(FileProcessorTaskDataKeys.MEDIUM_IMG, "test42.medium.jpg");
        data.put(FileProcessorTaskDataKeys.LARGE_IMG, "test42.large.jpg");
        data.put(FileProcessorTaskDataKeys.THUMBNAIL_OFFSET_MS, "" + 43L);
        mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(testFileId, FileProcessorTaskType.GENERATE_THUMBNAILS, data);
        // load file and verify thumbnail data
        file = mediaFileEJB.findById(testFileId);
        assertEquals(43L, (long) file.getThumbnailData().getThumbnailOffsetMs());
        assertEquals("test42.small.jpg", file.getThumbnailData().getSmallImg());
        assertEquals("test42.medium.jpg", file.getThumbnailData().getMediumImg());
        assertEquals("test42.large.jpg", file.getThumbnailData().getLargeImg());
    }

    @Test
    public void testMultipleTasks() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException, NoSuchTaskException {
        // Schedule "generate thumbnail task" and grab it.
        mediaFileEJB_authWrapper.as("testuser", "testuser").scheduleGenerateThumbnailsTask(testFileId, 3L);
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertEquals((long) file.getId(), testFileId);
        // Try to grab the task again -> should return null because it is already taken.
        file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertNull(file);
        // Schedule another task and try to grab it -> should return null because other task is still in progress.
        mediaFileEJB_authWrapper.as("testuser", "testuser").scheduleGenerateThumbnailsTask(testFileId, 3L);
        file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertNull(file);
        // Resolve the task
        Map<String, String> data = new HashMap<>();
        data.put(FileProcessorTaskDataKeys.SMALL_IMG, "small.jpg");
        data.put(FileProcessorTaskDataKeys.MEDIUM_IMG, "medium.jpg");
        data.put(FileProcessorTaskDataKeys.LARGE_IMG, "large.jpg");
        data.put(FileProcessorTaskDataKeys.THUMBNAIL_OFFSET_MS, "" + 0L);
        mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(testFileId, FileProcessorTaskType.GENERATE_THUMBNAILS, data);
        // Now the other task should be available
        file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertEquals((long) file.getId(), testFileId);
        // Resolve the other task
        mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(testFileId, FileProcessorTaskType.GENERATE_THUMBNAILS, data);
        file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertNull(file);
    }

    @Test
    public void testIncorrectParameter() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException, NoSuchTaskException {
        try {
            mediaFileEJB_authWrapper.as("testuser", "testuser").scheduleGenerateThumbnailsTask(testFileId, -1L);
        }
        catch ( Throwable t ) {
            // We expect a ConstraintViolationException
            assertTrue(t.getCause() instanceof ConstraintViolationException);
            return;
        }
        fail();
    }

    @Test
    public void testIncorrectResults1() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException, NoSuchTaskException {
        mediaFileEJB_authWrapper.as("testuser", "testuser").scheduleGenerateThumbnailsTask(testFileId, 0);
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertEquals((long) file.getId(), testFileId);
        // post result with small img missing
        Map<String, String> data = new HashMap<>();
        data.put(FileProcessorTaskDataKeys.MEDIUM_IMG, "medium.jpg");
        data.put(FileProcessorTaskDataKeys.LARGE_IMG, "large.jpg");
        data.put(FileProcessorTaskDataKeys.THUMBNAIL_OFFSET_MS, "" + 43L);
        try {
            mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(testFileId, FileProcessorTaskType.GENERATE_THUMBNAILS, data);
        }
        catch ( Throwable t ) {
            // We expect a ConstraintViolationException
            assertTrue(t.getCause() instanceof ConstraintViolationException);
            return;
        }
        fail();
    }

    @Test
    public void testIncorrectResults2() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException, NoSuchTaskException {
        mediaFileEJB_authWrapper.as("testuser", "testuser").scheduleGenerateThumbnailsTask(testFileId, 0);
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertEquals((long) file.getId(), testFileId);
        // post result with negative offset
        Map<String, String> data = new HashMap<>();
        data.put(FileProcessorTaskDataKeys.SMALL_IMG, "small.jpg");
        data.put(FileProcessorTaskDataKeys.MEDIUM_IMG, "medium.jpg");
        data.put(FileProcessorTaskDataKeys.LARGE_IMG, "large.jpg");
        data.put(FileProcessorTaskDataKeys.THUMBNAIL_OFFSET_MS, "-1");
        try {
            mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(testFileId, FileProcessorTaskType.GENERATE_THUMBNAILS, data);
        }
        catch ( Throwable t ) {
            // We expect a ConstraintViolationException
            assertTrue(t.getCause() instanceof ConstraintViolationException);
            return;
        }
        fail();
    }

    @Test
    public void testIncorrectResults3() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException, NoSuchTaskException {
        mediaFileEJB_authWrapper.as("testuser", "testuser").scheduleGenerateThumbnailsTask(testFileId, 0);
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertEquals((long) file.getId(), testFileId);
        // post result with illegal number format
        Map<String, String> data = new HashMap<>();
        data.put(FileProcessorTaskDataKeys.SMALL_IMG, "small.jpg");
        data.put(FileProcessorTaskDataKeys.MEDIUM_IMG, "medium.jpg");
        data.put(FileProcessorTaskDataKeys.LARGE_IMG, "large.jpg");
        data.put(FileProcessorTaskDataKeys.THUMBNAIL_OFFSET_MS, "illegal");
        try {
            mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(testFileId, FileProcessorTaskType.GENERATE_THUMBNAILS, data);
        }
        catch ( Throwable t ) {
            // We expect a ConstraintViolationException
            assertTrue(t.getCause() instanceof NumberFormatException);
            return;
        }
        fail();
    }
}
