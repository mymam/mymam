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
import net.mymam.entity.GenerateProxyVideosTask;
import net.mymam.entity.GenerateThumbnailImagesTask;
import net.mymam.entity.MediaFile;
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
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * The {@link GenerateProxyVideosTask} is only scheduled when a new video is
 * created; there is no public API in {@link MediaFileEJB} for scheduling a
 * new {@link GenerateProxyVideosTask}.
 *
 * <p/>
 * This test verifies if the task can be executed successfully, and if
 * incorrect test results are handled correctly.
 *
 * @author fstab
 */
@RunWith(Arquillian.class)
public class GenerateProxyVideosTaskTest {

    /**
     * Relative path to the src/main/webapp directory.
     * Will be used to find WEB-INF/jboss-ejb3.xml
     */
    private static final String WEBAPP_SRC = ArquillianTestHelper.makeWebappSrc();

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
                .addClass(ArquillianTestHelper.class)
                        // add all JPA entities
                .addPackage(MediaFile.class.getPackage())
                        // add some EJBs
                .addClass(ConfigEJB.class)
                .addClass(MediaFileEJB.class)
                .addClass(MediaFileEJB_GrabTaskWithDelay.class)
                .addClass(PermissionEJB.class)
                .addClass(UserEJB.class)
                .addClass(UserMgmtEJB.class)
                .addClass(ValidationHelper.class)
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
     * Create test users and test video.
     */
    @Before
    public void setUp() throws LoginException, NamingException, PrivilegedActionException {
        mediaFileEJB_authWrapper = new MediaFileEJB_AuthWrapper(mediaFileEJB);
        ArquillianTestHelper.createUsers(userMgmtEJB);
        MediaFile file = mediaFileEJB_authWrapper.as("testuser", "testuser").createNewMediaFile(ArquillianTestHelper.ROOT_DIR, ArquillianTestHelper.ORIG_FILE);
        testFileId = file.getId();
    }

    /**
     * Delete the data created in {@link #setUp()}.
     *
     * The tearDown() method should leave an empty database.
     */
    @After
    public void tearDown() throws InvalidImportStateException, PermissionDeniedException, NotFoundException, NamingException, LoginException, PrivilegedActionException, NoSuchTaskException {
        ArquillianTestHelper.deleteMediaFile(testFileId, mediaFileEJB_authWrapper);
        ArquillianTestHelper.deleteUsers(userMgmtEJB);
    }

    @Test
    public void testCorrectExecution() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException, NoSuchTaskException {
        // grab task and verify parameters
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateProxyVideosTask.class);
        assertEquals((long) file.getId(), testFileId);
        GenerateProxyVideosTask task = (GenerateProxyVideosTask) file.getPendingTasksQueue().get(0);
        assertEquals(FileProcessorTaskStatus.IN_PROGRESS, task.getStatus());
        // post result
        Map<String, String> data = new HashMap<>();
        data.put(FileProcessorTaskDataKeys.LOW_RES_MP4, "my-proxy-low-res.mp4");
        data.put(FileProcessorTaskDataKeys.LOW_RES_WEMB, "my-proxy-low-res.webm");
        mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(testFileId, FileProcessorTaskType.GENERATE_PROXY_VIDEOS, data);
        // load file and verify thumbnail data
        file = mediaFileEJB.findById(testFileId);
        assertEquals("my-proxy-low-res.mp4", file.getProxyVideoData().getLowResMp4());
        assertEquals("my-proxy-low-res.webm", file.getProxyVideoData().getLowResWebm());
    }

    @Test
    public void testIncorrectResults() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException, NoSuchTaskException {
        // grab task and verify parameters
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateProxyVideosTask.class);
        assertEquals((long) file.getId(), testFileId);
        GenerateProxyVideosTask task = (GenerateProxyVideosTask) file.getPendingTasksQueue().get(0);
        assertEquals(FileProcessorTaskStatus.IN_PROGRESS, task.getStatus());
        // post result with webm missing
        Map<String, String> data = new HashMap<>();
        data.put(FileProcessorTaskDataKeys.LOW_RES_MP4, "my-proxy-low-res.mp4");
        try {
            mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(testFileId, FileProcessorTaskType.GENERATE_PROXY_VIDEOS, data);
        }
        catch ( Throwable t ) {
            // We expect a ConstraintViolationException
            assertTrue(t.getCause() instanceof ConstraintViolationException);
            return;
        }
        fail();
    }
}
