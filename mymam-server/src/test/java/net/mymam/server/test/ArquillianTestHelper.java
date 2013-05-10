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
import net.mymam.ejb.UserMgmtEJB;
import net.mymam.entity.*;
import net.mymam.exceptions.NoSuchTaskException;
import net.mymam.exceptions.NotFoundException;

import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import java.nio.file.Paths;
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author fstab
 */
public class ArquillianTestHelper {

    public static final String ROOT_DIR = "test/video/root";
    public static final String ORIG_FILE = "orig.mov";
    public static final String PROXY_VIDEO_WEBM = "proxy.webm";
    public static final String PROXY_VIDEO_MP4 = "proxy.mp4";
    public static final String THUMBNAIL_SMALL = "thumb-small.jpg";
    public static final String THUMBNAIL_MEDIUM = "thumb-medium.jpg";
    public static final String THUMBNAIL_LARGE = "thumb-large.jpg";
    public static final long THUMBNAIL_OFFSET = 3;

    /**
     * Get the relative path to the src/main/webapp directory.
     * <p/>
     * The current working directory might be "mymam" or "mymam/mymam-server",
     * depending on if the test is run from Maven or from the IDE.
     *
     * @return Relative path to the src/main/webapp directory.
     */
    public static String makeWebappSrc() {
        if (Paths.get("mymam-server/src/main/webapp").toFile().isDirectory()) {
            return "mymam-server/src/main/webapp";
        }
        return "src/main/webapp";
    }

    public static void createUsers(UserMgmtEJB userMgmtEJB) {
        User systemUser = userMgmtEJB.createUser("system", "system");
        Role systemRole = userMgmtEJB.createRole("system");
        userMgmtEJB.addRole(systemUser, systemRole);

        User testUser = userMgmtEJB.createUser("testuser", "testuser");
        Role userRole = userMgmtEJB.createRole("user");
        userMgmtEJB.addRole(testUser, userRole);
    }

    /**
     * Remove the users and roles created with {@link #createUsers(net.mymam.ejb.UserMgmtEJB)}
     */
    public static void deleteUsers(UserMgmtEJB userMgmtEJB) {
        userMgmtEJB.removeUser(userMgmtEJB.findUserByName("system"));
        userMgmtEJB.removeRole(userMgmtEJB.findRoleByName("system"));
        userMgmtEJB.removeUser(userMgmtEJB.findUserByName("testuser"));
        userMgmtEJB.removeRole(userMgmtEJB.findRoleByName("user"));
    }

    /**
     * Simulate the entire file import process:
     * <ul>
     *     <li>create file</li>
     *     <li>generate proxy videos</li>
     *     <li>generate thumbnail images</li>
     * </ul>
     */
    public static long createMediaFile(MediaFileEJB_AuthWrapper mediaFileEJB_authWrapper) throws LoginException, NamingException, PrivilegedActionException {
        MediaFile file = mediaFileEJB_authWrapper.as("testuser", "testuser").createNewMediaFile(ROOT_DIR, ORIG_FILE);
        MediaFile taskFile = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateProxyVideosTask.class);
        assertEquals(file.getId(), taskFile.getId());
        executeGenerateProxyTask(taskFile, mediaFileEJB_authWrapper);
        taskFile = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(GenerateThumbnailImagesTask.class);
        assertEquals(file.getId(), taskFile.getId());
        executeGenerateThumbnailsTask(taskFile, mediaFileEJB_authWrapper);
        // re-load file and verify that all tasks have been executed successfully
        file = mediaFileEJB_authWrapper.as("system", "system").findById(file.getId());
        assertTrue(file.getPendingTasksQueue().isEmpty());
        return file.getId();
    }

    public static void executeGenerateProxyTask(MediaFile file, MediaFileEJB_AuthWrapper mediaFileEJB_authWrapper) throws LoginException, PrivilegedActionException {
        Map<String, String> data = new HashMap<>();
        data.put(FileProcessorTaskDataKeys.LOW_RES_MP4, PROXY_VIDEO_MP4);
        data.put(FileProcessorTaskDataKeys.LOW_RES_WEMB, PROXY_VIDEO_WEBM);
        mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(file.getId(), FileProcessorTaskType.GENERATE_PROXY_VIDEOS, data);
    }

    public static void executeGenerateThumbnailsTask(MediaFile file, MediaFileEJB_AuthWrapper mediaFileEJB_authWrapper) throws LoginException, PrivilegedActionException {
        Map<String, String> data = new HashMap<>();
        data.put(FileProcessorTaskDataKeys.SMALL_IMG, THUMBNAIL_SMALL);
        data.put(FileProcessorTaskDataKeys.MEDIUM_IMG, THUMBNAIL_MEDIUM);
        data.put(FileProcessorTaskDataKeys.LARGE_IMG, THUMBNAIL_LARGE);
        data.put(FileProcessorTaskDataKeys.THUMBNAIL_OFFSET_MS, "" + THUMBNAIL_OFFSET);
        mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(file.getId(), FileProcessorTaskType.GENERATE_THUMBNAILS, data);
    }

    public static void executeDeleteTask(MediaFile file, MediaFileEJB_AuthWrapper mediaFileEJB_authWrapper) throws LoginException, PrivilegedActionException {
        mediaFileEJB_authWrapper.as("system", "system").handleTaskResult(file.getId(), FileProcessorTaskType.DELETE, new HashMap<String, String>());
    }

    public static void deleteMediaFile(long fileId, MediaFileEJB_AuthWrapper mediaFileEJB_authWrapper) throws LoginException, PrivilegedActionException, NotFoundException, NoSuchTaskException {
        MediaFile testFile = mediaFileEJB_authWrapper.as("system", "system").findById(fileId);

        // Current tasks IN_PROGRESS must be finished before the file can be deleted.
        if ( testFile.getPendingTasksQueue() != null && testFile.getPendingTasksQueue().size() > 0 ) {
            FileProcessorTask curTask = testFile.getPendingTasksQueue().get(0);
            if ( curTask.getStatus() == FileProcessorTaskStatus.IN_PROGRESS ) {
                if ( curTask instanceof GenerateProxyVideosTask) {
                    executeGenerateProxyTask(testFile, mediaFileEJB_authWrapper);
                }
                else if ( curTask instanceof GenerateThumbnailImagesTask ) {
                    executeGenerateThumbnailsTask(testFile, mediaFileEJB_authWrapper);
                }
            }
        }

        // Schedule and execute delete task
        mediaFileEJB_authWrapper.as("testuser", "testuser").scheduleDeleteTask(fileId);
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").grabNextFileProcessorTask(DeleteTask.class);
        assertEquals(file.getId(), testFile.getId());
        executeDeleteTask(testFile, mediaFileEJB_authWrapper);
    }

    public static void assertFileUnchanged(long id, MediaFileEJB_AuthWrapper mediaFileEJB_authWrapper) throws LoginException, PrivilegedActionException {
        MediaFile file = mediaFileEJB_authWrapper.as("system", "system").findById(id);
        assertEquals(ROOT_DIR, file.getRootDir());
        assertEquals(ORIG_FILE, file.getOrigFile());
        assertEquals(PROXY_VIDEO_MP4, file.getProxyVideoData().getLowResMp4());
        assertEquals(PROXY_VIDEO_WEBM, file.getProxyVideoData().getLowResWebm());
        assertEquals(THUMBNAIL_SMALL, file.getThumbnailData().getSmallImg());
        assertEquals(THUMBNAIL_MEDIUM, file.getThumbnailData().getMediumImg());
        assertEquals(THUMBNAIL_LARGE, file.getThumbnailData().getLargeImg());
        assertEquals(THUMBNAIL_OFFSET, (long) file.getThumbnailData().getThumbnailOffsetMs());
    }
}
