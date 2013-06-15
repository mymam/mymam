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

import net.mymam.data.json.FileProcessorTaskType;
import net.mymam.data.json.ReturnStatus;
import net.mymam.ejb.MediaFileEJB;
import net.mymam.entity.MediaFile;
import net.mymam.entity.User;
import net.mymam.exceptions.InvalidInputStatusChangeException;
import net.mymam.exceptions.NoSuchTaskException;
import net.mymam.exceptions.NotFoundException;
import net.mymam.exceptions.PermissionDeniedException;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.Map;

/**
 * Call {@link MediaFileEJB} methods as authenticated user for unit testing.
 *
 * @author fstab
 */
public class MediaFileEJB_AuthWrapper {

    private final MediaFileEJB mediaFileEJB;

    public MediaFileEJB_AuthWrapper(MediaFileEJB mediaFileEJB) {
        this.mediaFileEJB = mediaFileEJB;
    }

    public WrappedMediaFileEJB as(String username, String password) {
        return new WrappedMediaFileEJB(mediaFileEJB, username, password);
    }

    public static class WrappedMediaFileEJB {

        private final MediaFileEJB mediaFileEJB;
        private final String username;
        private final String password;

        private WrappedMediaFileEJB(MediaFileEJB mediaFileEJB, String username, String password) {
            this.mediaFileEJB = mediaFileEJB;
            this.username = username;
            this.password = password;
        }

        public MediaFile findById(final long id) throws LoginException, PrivilegedActionException {
            LoginContext loginContext = JBossLoginContextFactory.createLoginContext(username, password);
            loginContext.login();
            try {
                return Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<MediaFile>() {
                    @Override
                    public MediaFile run() throws NotFoundException, PermissionDeniedException, InvalidInputStatusChangeException {
                        return mediaFileEJB.findById(id);
                    }
                });
            } finally {
                loginContext.logout();
            }
        }

        public MediaFile grabNextFileProcessorTask(final Class... types) throws LoginException, PrivilegedActionException {
            LoginContext loginContext = JBossLoginContextFactory.createLoginContext(username, password);
            loginContext.login();
            try {
                return Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<MediaFile>() {
                    @Override
                    public MediaFile run() throws NotFoundException, PermissionDeniedException, InvalidInputStatusChangeException {
                        return mediaFileEJB.grabNextFileProcessorTask(types);
                    }
                });
            } finally {
                loginContext.logout();
            }
        }

        public MediaFile grabNextFileProcessorTask(final Collection<Class> types) throws LoginException, PrivilegedActionException {
            LoginContext loginContext = JBossLoginContextFactory.createLoginContext(username, password);
            loginContext.login();
            try {
                return Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<MediaFile>() {
                    @Override
                    public MediaFile run() throws NotFoundException, PermissionDeniedException, InvalidInputStatusChangeException {
                        return mediaFileEJB.grabNextFileProcessorTask(types);
                    }
                });
            } finally {
                loginContext.logout();
            }
        }

        public void scheduleGenerateThumbnailsTask(final long mediaFileId, final long thumbnailOffsetMs) throws LoginException, PrivilegedActionException {
            LoginContext loginContext = JBossLoginContextFactory.createLoginContext(username, password);
            loginContext.login();
            try {
                Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws NotFoundException, PermissionDeniedException, InvalidInputStatusChangeException {
                        mediaFileEJB.scheduleGenerateThumbnailsTask(mediaFileId, thumbnailOffsetMs);
                        return null;
                    }
                });
            } finally {
                loginContext.logout();
            }
        }

        public void scheduleDeleteTask(final long fileId) throws LoginException, PrivilegedActionException {
            LoginContext loginContext = JBossLoginContextFactory.createLoginContext(username, password);
            loginContext.login();
            try {
                Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws NotFoundException, PermissionDeniedException, InvalidInputStatusChangeException {
                        mediaFileEJB.scheduleDeleteTask(fileId);
                        return null;
                    }
                });
            } finally {
                loginContext.logout();
            }
        }

        public void handleTaskResult(final long fileId, final FileProcessorTaskType taskType, final ReturnStatus status, final Map<String, String> resultData) throws LoginException, PrivilegedActionException {
            LoginContext loginContext = JBossLoginContextFactory.createLoginContext(username, password);
            loginContext.login();
            try {
                Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws NotFoundException, PermissionDeniedException, InvalidInputStatusChangeException, NoSuchTaskException {
                        mediaFileEJB.handleTaskResult(fileId, taskType, status, resultData);
                        return null;
                    }
                });
            } finally {
                loginContext.logout();
            }
        }

        public MediaFile createNewMediaFile(final String rootDir, final String origFile) throws LoginException, NamingException, PrivilegedActionException {
            LoginContext loginContext = JBossLoginContextFactory.createLoginContext(username, password);
            loginContext.login();
            try {
                return Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<MediaFile>() {
                    @Override
                    public MediaFile run() {
                        return mediaFileEJB.createNewMediaFile(rootDir, origFile);
                    }
                });
            } finally {
                loginContext.logout();
            }
        }
    }
}
