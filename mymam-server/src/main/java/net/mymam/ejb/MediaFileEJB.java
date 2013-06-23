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

package net.mymam.ejb;

import net.mymam.data.json.*;
import net.mymam.entity.*;
import net.mymam.entity.FileProcessorTask;
import net.mymam.entity.MediaFile;
import net.mymam.exceptions.NoSuchTaskException;
import net.mymam.exceptions.NotFoundException;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.*;
import java.util.*;

/**
 * @author fstab
 */
@Stateless
@DeclareRoles({ SecurityRoles.USER, SecurityRoles.ADMIN, SecurityRoles.SYSTEM })
public class MediaFileEJB {

    @PersistenceContext(unitName = "defaultPersistenceUnit")
    private EntityManager em;

    @Resource
    SessionContext sessionContext;

    @EJB
    UserEJB userEJB;

    @EJB
    UserMgmtEJB userMgmtEJB;

    @EJB
    PermissionEJB permissionEJB;

    // returns null if the entity doesn't exist.
    public MediaFile findById(long id) {
        MediaFile result = em.find(MediaFile.class, id);
        em.detach(result);
        return result;
    }

    @RolesAllowed(SecurityRoles.USER)
    public MediaFile createNewMediaFile(String rootDir, String origFile) {
        User user = userEJB.getCurrentUser();
        return createNewMediaFile(rootDir, origFile, user);
    }

    @RolesAllowed(SecurityRoles.SYSTEM)
    public MediaFile createNewMediaFile(String rootDir, String origFile, String uploadingUser) {
        User user = userMgmtEJB.findUserByName(uploadingUser);
        return createNewMediaFile(rootDir, origFile, user);
    }

    private MediaFile createNewMediaFile(String rootDir, String origFile, User user) {
        MediaFile result = new MediaFile();
        result.setRootDir(rootDir);
        result.setOrigFile(origFile);
        result.setCreationDate(new Date());
        result.setUploadingUser(user);
        result.setStatus(MediaFileImportStatus.NEW);
        em.persist(result);
        scheduleGenerateProxyVideosTask(result);
        scheduleGenerateThumbnailTask(result, 0L);
        em.flush(); // must flush before detatch
        em.detach(result);
        return result;
    }

    private List<MediaFile> detach(List<MediaFile> list) {
        for (MediaFile mediaFile : list) {
            em.detach(mediaFile);
        }
        return list;
    }

    public long countFiles(MediaFileImportStatus... statusValues) {
        Query query = em.createNamedQuery("countMediaFileByStatusListAndUser")
                .setParameter("user", userEJB.getCurrentUser())
                .setParameter("statusList", Arrays.asList(statusValues));
        return (long) query.getSingleResult();
    }

    public List<MediaFile> findFiles(MediaFileImportStatus... statusValues) {
        Query query = em.createNamedQuery("findMediaFileByStatusListAndUser")
                .setParameter("user", userEJB.getCurrentUser())
                .setParameter("statusList", Arrays.asList(statusValues));
        List<MediaFile> result = query.getResultList();
        if (result == null) {
            result = new ArrayList<>();
        }
        return detach(result);
    }

    @RolesAllowed(SecurityRoles.SYSTEM)
    public MediaFile grabNextFileProcessorTask(Class... types) {
        return grabNextFileProcessorTask(Arrays.asList(types));
    }

    @RolesAllowed(SecurityRoles.SYSTEM)
    public MediaFile grabNextFileProcessorTask(Collection<Class> types) {
        try {
            Query query = em.createNamedQuery("findMediaFileWithPendingTasks")
                    .setParameter("classes", types);
            query.setMaxResults(1);
            List<MediaFile> result = query.getResultList();
            if (result != null && result.size() > 0) {
                MediaFile file = result.get(0);
                FileProcessorTask nextTask = file.getPendingTasksQueue().get(0);
                if ( nextTask.getStatus() != FileProcessorTaskStatus.PENDING ) {
                    throw new IllegalStateException("Query for pending tasks delivered task that is not pending.");
                }
                nextTask.setStatus(FileProcessorTaskStatus.IN_PROGRESS);
                em.persist(nextTask);
                em.flush(); // throws OptimisticLockException
                em.detach(file);
                return file;
            }
            return null;
        }
        catch ( OptimisticLockException e ) {
            return null;
        }
    }

    public List<MediaFile> findPublicFiles() {
        Query query = em.createNamedQuery("findMediaFileByStatusAndAccess")
                .setParameter("status", MediaFileImportStatus.READY)
                .setParameter("access", Access.PUBLIC);
        List<MediaFile> result = query.getResultList();
        if (result == null) {
            result = new ArrayList<>();
        }
        return detach(result);
    }

    // returns attached MediaFile
    private MediaFile load(long id) throws NotFoundException {
        MediaFile jpaEntity = em.find(MediaFile.class, id);
        if (jpaEntity == null) {
            throw new NotFoundException(MediaFile.class, id);
        }
        return jpaEntity;
    }

    public boolean hasPublicFiles() {
        return findNumberOfPublicFiles() > 0;
    }

    public int findNumberOfPublicFiles() {
        // TODO: This should be implemented without loading all files.
        return findPublicFiles().size();
    }

    // TODO
    public void updateAccessAndMetaData(MediaFile mediaFile, Access access, MediaFileUserProvidedMetaData metaData) throws NotFoundException {
        if ( mediaFile.getStatus() != MediaFileImportStatus.FILEPROCESSOR_DONE && mediaFile.getStatus() != MediaFileImportStatus.READY ) {
            throw new IllegalStateException("Cannot update meta data for file in status " + mediaFile.getStatus());
        }
        MediaFile file = load(mediaFile.getId());
        file.setAccess(access);
        file.setUserProvidedMetadata(metaData);
        file.setStatus(MediaFileImportStatus.READY);
    }

    private void scheduleTask(MediaFile mediaFile, FileProcessorTask task) {
        List<FileProcessorTask> pendingTasks = mediaFile.getPendingTasksQueue();
        if ( pendingTasks == null ) {
            pendingTasks = new LinkedList<>();
            mediaFile.setPendingTasksQueue(pendingTasks);
        }
        task.setStatus(FileProcessorTaskStatus.PENDING);
        task.setCreationDate(new Date());
        pendingTasks.add(task);
        task.setFile(mediaFile);
        em.persist(task);
        em.flush();
    }

    private void scheduleGenerateProxyVideosTask(MediaFile mediaFile) {
        GenerateProxyVideosTask task = new GenerateProxyVideosTask();
        scheduleTask(mediaFile, task);
    }

    @RolesAllowed(SecurityRoles.USER)
    public void scheduleGenerateThumbnailsTask(long mediaFileId, Long thumbnailOffsetMs) throws NotFoundException {
        MediaFile mediaFile = load(mediaFileId);
        scheduleGenerateThumbnailTask(mediaFile, thumbnailOffsetMs);
    }

    private void scheduleGenerateThumbnailTask(MediaFile mediaFile, long thumbnailOffsetMs) {
        GenerateThumbnailImagesTask task = new GenerateThumbnailImagesTask();
        task.setThumbnailOffsetMs(thumbnailOffsetMs);
        scheduleTask(mediaFile, task);
    }

    @RolesAllowed(SecurityRoles.USER)
    public void scheduleDeleteTask(long fileId) throws NotFoundException {
        MediaFile mediaFile = load(fileId);
        DeleteTask deleteTask = new DeleteTask();
        scheduleTask(mediaFile, deleteTask);
        // Remove all other tasks that are not IN_PROGRESS
        // Tasks IN_PROGRESS must first be continued, because there
        // might be a script running producing files that need to
        // be removed on deletion.
        List<FileProcessorTask> tasksToRemove = new ArrayList<>();
        for ( FileProcessorTask task : mediaFile.getPendingTasksQueue() ) {
            if ( task != deleteTask && task.getStatus() == FileProcessorTaskStatus.PENDING ) {
                tasksToRemove.add(task);
            }
        }
        for ( FileProcessorTask task : tasksToRemove ) {
            mediaFile.getPendingTasksQueue().remove(task);
            em.remove(task);
        }
        em.flush();
    }

    private void updateImportStatus(MediaFile file) {
        if ( file.getStatus() == MediaFileImportStatus.NEW ) {
            if ( file.getProxyVideoData() != null && file.getThumbnailData() != null ) {
                file.setStatus(MediaFileImportStatus.FILEPROCESSOR_DONE);
            }
        }
    }

    @RolesAllowed(SecurityRoles.SYSTEM)
    public void handleTaskResult(long mediaFileId, FileProcessorTaskType type, ReturnStatus status, Map<String, String> data) throws NotFoundException, NoSuchTaskException {
        MediaFile mediaFile = load(mediaFileId);
        if ( mediaFile.getPendingTasksQueue().size() == 0 ) {
            throw new NoSuchTaskException();
        }
        FileProcessorTask task = mediaFile.getPendingTasksQueue().get(0);
        if ( task.getStatus() != FileProcessorTaskStatus.IN_PROGRESS ) {
            throw new NoSuchTaskException();
        }
        switch ( type ) {
            case GENERATE_PROXY_VIDEOS:
                if ( ! ( task instanceof GenerateProxyVideosTask ) ) {
                    throw new NoSuchTaskException();
                }
                handleGenerateProxyVideoResult(mediaFile, status, data);
                break;
            case GENERATE_THUMBNAILS:
                if ( ! ( task instanceof GenerateThumbnailImagesTask ) ) {
                    throw new NoSuchTaskException();
                }
                handleGenerateThumbnailsResult(mediaFile, status, data);
                break;
            case DELETE:
                if ( ! ( task instanceof DeleteTask ) ) {
                    throw new NoSuchTaskException();
                }
                handleDeleteResult(mediaFile, status, data);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void handleGenerateProxyVideoResult(MediaFile mediaFile, ReturnStatus status, Map<String, String> data) {
        if ( status == ReturnStatus.ERROR ) {
            if ( mediaFile.getStatus() == MediaFileImportStatus.NEW ) {
                mediaFile.setStatus(MediaFileImportStatus.FILEPROCESSOR_FAILED);
            }
        } else {
            MediaFileProxyVideoData proxyVideoData = new MediaFileProxyVideoData();
            proxyVideoData.setLowResWebm(data.get(FileProcessorTaskDataKeys.LOW_RES_WEMB));
            proxyVideoData.setLowResMp4(data.get(FileProcessorTaskDataKeys.LOW_RES_MP4));
            mediaFile.setProxyVideoData(proxyVideoData);
            updateImportStatus(mediaFile);
        }
        FileProcessorTask task = mediaFile.getPendingTasksQueue().get(0);
        mediaFile.getPendingTasksQueue().remove(0);
        em.remove(task);
        em.flush();
    }

    private void handleGenerateThumbnailsResult(MediaFile mediaFile, ReturnStatus status, Map<String, String> data) {
        if ( status == ReturnStatus.ERROR ) {
            if ( mediaFile.getStatus() == MediaFileImportStatus.NEW ) {
                mediaFile.setStatus(MediaFileImportStatus.FILEPROCESSOR_FAILED);
            }
        } else {
            MediaFileThumbnailData thumbnailData = new MediaFileThumbnailData();
            thumbnailData.setLargeImg(data.get(FileProcessorTaskDataKeys.LARGE_IMG));
            thumbnailData.setMediumImg(data.get(FileProcessorTaskDataKeys.MEDIUM_IMG));
            thumbnailData.setSmallImg(data.get(FileProcessorTaskDataKeys.SMALL_IMG));
            thumbnailData.setThumbnailOffsetMs(Long.parseLong(data.get(FileProcessorTaskDataKeys.THUMBNAIL_OFFSET_MS)));
            mediaFile.setThumbnailData(thumbnailData);
            updateImportStatus(mediaFile);
        }
        FileProcessorTask task = mediaFile.getPendingTasksQueue().get(0);
        mediaFile.getPendingTasksQueue().remove(0);
        em.remove(task);
        em.flush();
    }

    private void handleDeleteResult(MediaFile mediaFile, ReturnStatus status, Map<String, String> data) {
        FileProcessorTask task = mediaFile.getPendingTasksQueue().get(0);
        mediaFile.getPendingTasksQueue().remove(0);
        em.remove(task);
        if ( status == ReturnStatus.OK ) {
            em.remove(mediaFile);
        }
        em.flush();
    }
}
