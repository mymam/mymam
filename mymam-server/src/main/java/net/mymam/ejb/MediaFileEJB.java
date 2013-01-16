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

import net.mymam.data.json.MediaFileImportStatus;
import net.mymam.entity.*;
import net.mymam.exceptions.InvalidImportStateException;
import net.mymam.exceptions.InvalidInputStatusChangeException;
import net.mymam.exceptions.NotFoundException;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author fstab
 */
@Stateless
public class MediaFileEJB {

    @PersistenceContext(unitName = "defaultPersistenceUnit")
    private EntityManager em;

    // returns null if the entity doesn't exist.
    public MediaFile findById(long id) {
        MediaFile result = em.find(MediaFile.class, id);
        em.detach(result);
        return result;
    }

    @RolesAllowed({"system","user"})
    public MediaFile createNewMediaFile(String rootDir, String origFile, User uploadingUser) {
        uploadingUser = em.merge(uploadingUser);
        MediaFile result = new MediaFile();
        result.setRootDir(rootDir);
        result.setOrigFile(origFile);
        result.setCreationDate(new Date());
        result.setStatus(MediaFileImportStatus.NEW);
        result.setUploadingUser(uploadingUser);
        em.persist(result);
        em.flush(); // must flush before detatch
        em.detach(result);
        em.detach(uploadingUser);
        return result;
    }

    private List<MediaFile> detach(List<MediaFile> list) {
        for (MediaFile mediaFile : list) {
            em.detach(mediaFile);
        }
        return list;
    }

    public List<MediaFile> findByStatus(MediaFileImportStatus status) {
        Query query = em.createNamedQuery("findMediaFileByStatus").setParameter("status", status);
        List<MediaFile> result = query.getResultList();
        if (result == null) {
            result = new ArrayList<>();
        }
        return detach(result);
    }

    public List<MediaFile> findReadyFilesForUser(User user) {
        Query query = em.createNamedQuery("findMediaFileByStatusAndUser")
                .setParameter("status", MediaFileImportStatus.DONE)
                .setParameter("user", user);
        List<MediaFile> result = query.getResultList();
        if (result == null) {
            result = new ArrayList<>();
        }
        return detach(result);
    }

    // TODO: Dummy implementation returns all video files.
    public List<MediaFile> findPublicFiles() {
        Query query = em.createNamedQuery("findMediaFileByStatus")
                .setParameter("status", MediaFileImportStatus.DONE);
        List<MediaFile> result = query.getResultList();
        if (result == null) {
            result = new ArrayList<>();
        }
        return detach(result);
    }

    private MediaFile load(long id) throws NotFoundException {
        MediaFile jpaEntity = em.find(MediaFile.class, id);
        if (jpaEntity == null) {
            throw new NotFoundException(MediaFile.class, id);
        }
        return jpaEntity;
    }

    @RolesAllowed("system")
    public void setImportStatusInProgress(long id) throws InvalidInputStatusChangeException, NotFoundException {
        MediaFile file = load(id);
        if (file.getStatus() != MediaFileImportStatus.NEW) {
            throw new InvalidInputStatusChangeException(file.getStatus(), MediaFileImportStatus.NEW);
        }
        try {
            file.setStatus(MediaFileImportStatus.IN_PROGRESS);
            em.flush(); // might throw OptimisticLockException
        } catch (OptimisticLockException e) {
            throw new InvalidInputStatusChangeException(e);
        }
        createEvent(file);
    }

    @RolesAllowed("system")
    public void setImportStatusDone(long id) throws InvalidInputStatusChangeException, NotFoundException {
        MediaFile file = load(id);
        if (file.getStatus() != MediaFileImportStatus.IN_PROGRESS) {
            throw new InvalidInputStatusChangeException(file.getStatus(), MediaFileImportStatus.DONE);
        }
        file.setStatus(MediaFileImportStatus.DONE); // TODO OptimisticLockException
        createEvent(file);
    }

    @RolesAllowed("system")
    public void setImportStatusFailed(long id) throws InvalidInputStatusChangeException, NotFoundException {
        MediaFile file = load(id);
        if (file.getStatus() != MediaFileImportStatus.IN_PROGRESS) {
            throw new InvalidInputStatusChangeException(file.getStatus(), MediaFileImportStatus.FAILED);
        }
        file.setStatus(MediaFileImportStatus.FAILED); // TODO OptimisticLockException
        createEvent(file);
    }

    @RolesAllowed("system")
    public void updateGeneratedData(long id, MediaFileGeneratedData generatedData) throws NotFoundException, InvalidImportStateException {
        MediaFile file = load(id);
        if (file.getStatus() != MediaFileImportStatus.IN_PROGRESS) {
            throw new InvalidImportStateException(id, file.getStatus());
        }
        file.setGeneratedData(generatedData);
    }

    // TODO
    private void createEvent(MediaFile file) {
        DashboardEvent event = null;
        switch (file.getStatus()) {
            case DONE:
                event = new UploadSuccessfulEvent();
                ((UploadSuccessfulEvent) event).setMediaFile(file);
                break;
            case FAILED:
                // TODO
                break;
        }
        if (event != null) {
            event.setUser(file.getUploadingUser());
            event.setCreationDate(new Date());
            em.persist(event);
        }
    }

    @RolesAllowed("system")
    public void removeMediaFile(Long id) {
        em.remove(em.find(MediaFile.class, id));
    }

    public boolean hasPublicFiles() {
        return findNumberOfPublicFiles() > 0;
    }

    public int findNumberOfPublicFiles() {
        // TODO: This should be implemented without loading all files.
        return findPublicFiles().size();
    }
}
