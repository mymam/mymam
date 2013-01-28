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
package net.mymam.entity;

import net.mymam.data.json.MediaFileImportStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author fstab
 */
@NamedQueries({
        @NamedQuery(
                name = "findMediaFileByStatus",
                query = "SELECT f FROM MediaFile f WHERE f.status = :status"
        ),
        @NamedQuery(
                name = "findMediaFileByStatusAndAccess",
                query = "SELECT f FROM MediaFile f WHERE f.status = :status AND f.access = :access"
        ),
        @NamedQuery(
                name = "countMediaFileByStatusListAndUser",
                query = "SELECT COUNT(f) FROM MediaFile f WHERE f.uploadingUser = :user AND f.status IN (:statusList)"
        ),
        @NamedQuery(
                name = "findMediaFileByStatusListAndUser",
                query = "SELECT f FROM MediaFile f WHERE f.uploadingUser = :user AND f.status IN (:statusList)"
        ),
        @NamedQuery(
                name = "findMediaFileByStatusAndUser",
                query = "SELECT f FROM MediaFile f WHERE f.status = :status AND f.uploadingUser = :user"
        )
})
@Entity
public class MediaFile {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version; // use pessimistic locking to prevent multiple file processors from modifying the file at the same time.

    // @Past
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date creationDate;

    @ManyToOne
    private User uploadingUser;

    @ManyToOne
    private Project project;

    @NotNull
    private MediaFileImportStatus status;

    @NotNull
    @Column(unique = true)
    private String rootDir;

    @NotNull
    private String origFile;

    @NotNull
    private Access access = Access.PRIVATE;

    private MediaFileGeneratedData generatedData;
    private MediaFileUserProvidedMetaData userProvidedMetadata;

    public Long getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date created) {
        this.creationDate = created;
    }

    public MediaFileImportStatus getStatus() {
        return status;
    }

    public void setStatus(MediaFileImportStatus status) {
        this.status = status;
    }

    public User getUploadingUser() {
        return uploadingUser;
    }

    public void setUploadingUser(User uploadingUser) {
        this.uploadingUser = uploadingUser;
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public String getOrigFile() {
        return origFile;
    }

    public void setOrigFile(String origFile) {
        this.origFile = origFile;
    }

    public MediaFileGeneratedData getGeneratedData() {
        return generatedData;
    }

    public void setGeneratedData(MediaFileGeneratedData generatedData) {
        this.generatedData = generatedData;
    }

    public MediaFileUserProvidedMetaData getUserProvidedMetadata() {
        return userProvidedMetadata;
    }

    public void setUserProvidedMetadata(MediaFileUserProvidedMetaData userProvidedMetadata) {
        this.userProvidedMetadata = userProvidedMetadata;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }
}
