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

import net.mymam.data.json.MediaFileImportStatus;
import net.mymam.ejb.*;
import net.mymam.entity.GenerateProxyVideosTask;
import net.mymam.entity.GenerateThumbnailImagesTask;
import net.mymam.entity.MediaFile;
import net.mymam.exceptions.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
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
import java.io.File;
import java.security.PrivilegedActionException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * @author fstab
 */
@RunWith(Arquillian.class)
public class GrabTaskTest {

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

    @EJB
    MediaFileEJB_GrabTaskWithDelay mediaFileEJB_importStatusWithDelay1;

    @EJB
    MediaFileEJB_GrabTaskWithDelay mediaFileEJB_importStatusWithDelay2;

    Long testFileId;
    MediaFileEJB_AuthWrapper mediaFileEJB_authWrapper;

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
        mediaFileEJB_authWrapper = new MediaFileEJB_AuthWrapper(mediaFileEJB);
        ArquillianTestHelper.createUsers(userMgmtEJB);
        testFileId = ArquillianTestHelper.createMediaFile(mediaFileEJB_authWrapper);
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

    /**
     * unauthenticated
     */
    @Test(expected = EJBAccessException.class)
    @InSequence(1)
    public void testRoleDeniedUnauthenticated() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException {
        mediaFileEJB.grabNextFileProcessorTask(GenerateProxyVideosTask.class, GenerateThumbnailImagesTask.class);
    }

    /**
     * wrong role
     */
    @Test(expected = EJBAccessException.class)
    @InSequence(2)
    public void testRoleDeniedWrongRole() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException {
        mediaFileEJB_authWrapper.as("testuser", "testuser").grabNextFileProcessorTask(GenerateProxyVideosTask.class, GenerateThumbnailImagesTask.class);
    }

    /**
     * correct role
     */
    @Test
    @InSequence(3)
    public void testRoleAllowed() throws InvalidInputStatusChangeException, NotFoundException, LoginException, NamingException, PrivilegedActionException {
        mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateProxyVideosTask.class, GenerateThumbnailImagesTask.class);
    }

    /**
     * Test correct handling of OptimisticLockException when two clients
     * try to grab a task at the same time: Only the first attempt
     * must be successful, the second attempt must return null.
     */
    @Test
    @InSequence(4)
    public void testConcurrentProcessing() throws Throwable {
        mediaFileEJB_authWrapper.as("testuser", "testuser").scheduleGenerateThumbnailsTask(testFileId, 3L);
        // Make sure that two different EJBs have been injected.
        assertFalse(mediaFileEJB_importStatusWithDelay1 == mediaFileEJB_importStatusWithDelay2);
        ExecutorService exec = Executors.newFixedThreadPool(2);
        Future<MediaFile> future1 = exec.submit(new Callable<MediaFile>() {
            @Override
            public MediaFile call() throws InterruptedException {
                return mediaFileEJB_importStatusWithDelay1.grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
            }
        });
        Future<MediaFile> future2 = exec.submit(new Callable<MediaFile>() {
            @Override
            public MediaFile call() throws InterruptedException {
                Thread.sleep(100); // make sure that this one is second
                return mediaFileEJB_importStatusWithDelay2.grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
            }
        });
        MediaFile first = future1.get();
        MediaFile second = future2.get();
        assertNotNull(first);
        assertNull(second);
    }
}
