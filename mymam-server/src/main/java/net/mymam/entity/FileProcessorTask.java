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

import net.mymam.data.json.FileProcessorTaskStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author fstab
 */
@Entity
public abstract class FileProcessorTask {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    @NotNull
    private MediaFile file;

    @NotNull
    private FileProcessorTaskStatus status;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    public MediaFile getFile() {
        return file;
    }

    public void setFile(MediaFile file) {
        this.file = file;
    }

    public FileProcessorTaskStatus getStatus() {
        return status;
    }

    public void setStatus(FileProcessorTaskStatus status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
