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
import net.mymam.ejb.MediaFileEJB;
import net.mymam.ejb.UserMgmtEJB;
import net.mymam.entity.MediaFile;
import net.mymam.entity.Role;
import net.mymam.entity.User;
import net.mymam.exceptions.InvalidImportStateException;
import net.mymam.exceptions.InvalidInputStatusChangeException;
import net.mymam.exceptions.NotFoundException;
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
import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.*;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;

/**
 * Unit test for {@link MediaFileEJB#setImportStatusInProgress(long)}.
 *
 * @author fstab
 */
@RunWith(Arquillian.class)
public class MediaFileStatusTest {

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
     * @return The {@link WebArchive} deployment used in this test.
     */
    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                // add all JPA entities
                .addPackage(MediaFile.class.getPackage())
                // add some EJBs
                .addClass(MediaFileEJB.class)
                .addClass(UserMgmtEJB.class)
                .addClass(MediaFileEJB_AuthWrapper.class)
                .addClass(MediaFileEJB_ImportStatusWithDelay.class)
                // add all Exceptions
                .addPackage(InvalidImportStateException.class.getPackage())
                // add classes from mymam-common
                .addClass(MediaFileImportStatus.class)
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
    MediaFileEJB_AuthWrapper mediaFileEJB_AuthWrapper;

    @EJB
    MediaFileEJB_ImportStatusWithDelay mediaFileEJB_importStatusWithDelay1;

    @EJB
    MediaFileEJB_ImportStatusWithDelay mediaFileEJB_importStatusWithDelay2;

    Long testFileId;

    /**
     * Populate the database.
     *
     * The setUp() method creates
     * <ul>
     *     <li>A <i>system</i> user in role <i>system</i>.</li>
     *     <li>A <i>testuser</i> in role <i>user</i>.</li>
     *     <li>A {@link MediaFile} with status NEW.</li>
     * </ul>
     */
    @Before
    public void setUp() {
        User systemUser = userMgmtEJB.createUser("system", "system");
        Role systemRole = userMgmtEJB.createRole("system");
        userMgmtEJB.addRole(systemUser, systemRole);

        User testUser = userMgmtEJB.createUser("testuser", "testuser");
        Role userRole = userMgmtEJB.createRole("user");
        userMgmtEJB.addRole(testUser, userRole);

        testFileId = mediaFileEJB_AuthWrapper.createNewMediaFile("test/video/root", "test-video-orig.mp4", testUser).getId();
    }

    /**
     * Delete the data created in {@link #setUp()}.
     *
     * The tearDown() method should leave an empty database.
     */
    @After
    public void tearDown() {
        mediaFileEJB_AuthWrapper.removeMediaFile(testFileId);
        userMgmtEJB.removeUser(userMgmtEJB.findUserByName("system"));
        userMgmtEJB.removeRole(userMgmtEJB.findRoleByName("system"));
        userMgmtEJB.removeUser(userMgmtEJB.findUserByName("testuser"));
        userMgmtEJB.removeRole(userMgmtEJB.findRoleByName("user"));
    }

    /**
     * Test if calling {@link MediaFileEJB#setImportStatusInProgress(long)} is denied when
     * the caller is not in role <i>system</i>.
     *
     * @throws InvalidInputStatusChangeException
     * @throws NotFoundException
     */
    @Test(expected = EJBAccessException.class)
    @InSequence(1)
    public void testRoleDenied() throws InvalidInputStatusChangeException, NotFoundException {
        mediaFileEJB.setImportStatusInProgress(testFileId);
    }

    /**
     * Test if calling {@link MediaFileEJB#setImportStatusInProgress(long)} is allowed when
     * the caller is in role <i>system</i>.
     *
     * @throws InvalidInputStatusChangeException
     * @throws NotFoundException
     */
    @Test
    @InSequence(2)
    public void testRoleAllowed() throws InvalidInputStatusChangeException, NotFoundException {
        mediaFileEJB_AuthWrapper.setImportStatusInProgress(testFileId);
    }

    /**
     * The {@link MediaFileEJB#setImportStatusInProgress(long)} method should make sure that
     * only one file processor can sucessfully set the status IN_PROGRESS.
     * <p/>
     *
     * When two concurrent file processors try to set the status from NEW to IN_PROGRESS,
     * at the same time, only the first call should succeed and subsequent calls should
     * result in an {@link InvalidInputStatusChangeException}.
     * <p/>
     *
     * In most cases, this is handled explicitly in {@link MediaFileEJB}. However, there
     * is a <i>check-then-act</i> race condition in {@link MediaFileEJB#setImportStatusInProgress(long)},
     * which might result in an {@link javax.persistence.OptimisticLockException}.
     * The expected behaviour is that {@link MediaFileEJB#setImportStatusInProgress(long)} catches
     * the {@link javax.persistence.OptimisticLockException}, and throws an
     * {@link InvalidInputStatusChangeException}.
     * <p/>
     *
     * This test provokes the {@link javax.persistence.OptimisticLockException} and tests if
     * it is handled correctly.
     *
     * @throws Throwable
     */
    @Test(expected = InvalidInputStatusChangeException.class)
    @InSequence(3)
    public void testConcurrentProcessing() throws Throwable {
        // Make sure that two different EJBs have been injected.
        assertFalse(mediaFileEJB_importStatusWithDelay1 == mediaFileEJB_importStatusWithDelay2);
        ExecutorService exec = Executors.newFixedThreadPool(2);
        Future future1 = exec.submit(new Callable() {
            @Override
            public Object call() throws InvalidInputStatusChangeException, InterruptedException {
                mediaFileEJB_importStatusWithDelay1.setImportStatusInProgressWithDelay(testFileId);
                return null;
            }
        });
        Future future2 = exec.submit(new Callable() {
            @Override
            public Object call() throws InvalidInputStatusChangeException, InterruptedException {
                Thread.sleep(100); // make sure that this one is second
                mediaFileEJB_importStatusWithDelay2.setImportStatusInProgressWithDelay(testFileId);
                return null;
            }
        });
        try {
            future1.get(); // should not throw exception
        } catch (ExecutionException e) {
            fail("Unexpected Exception: " + e);
        }
        try {
            future2.get(); // should throw ExecutionException e with
            // e.getCause() instanceof InvalidInputStatusChangeException
        } catch (ExecutionException e) {
            throw e.getCause(); // Should be InvalidInputStatusChangeException
        }
    }
}
