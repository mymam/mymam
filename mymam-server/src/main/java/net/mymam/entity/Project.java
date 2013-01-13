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

import javax.persistence.*;
import java.util.List;

/**
 * @author fstab
 */
@Entity
public class Project {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "project")
    private List<MediaFile> MediaFiles;

    @ManyToMany
    private List<User> readOnlyUsers;

    @ManyToMany
    private List<User> readWriteUsers;

    public List<MediaFile> getMediaFiles() {
        return MediaFiles;
    }

    public void setMediaFiles(List<MediaFile> mediaFiles) {
        MediaFiles = mediaFiles;
    }

    public List<User> getReadOnlyUsers() {
        return readOnlyUsers;
    }

    public void setReadOnlyUsers(List<User> readOnlyUsers) {
        this.readOnlyUsers = readOnlyUsers;
    }

    public List<User> getReadWriteUsers() {
        return readWriteUsers;
    }

    public void setReadWriteUsers(List<User> readWriteUsers) {
        this.readWriteUsers = readWriteUsers;
    }
}
