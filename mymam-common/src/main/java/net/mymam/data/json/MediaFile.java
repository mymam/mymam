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
package net.mymam.data.json;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * @author fstab
 */
@XmlRootElement
public class MediaFile {

    private Long id;
    private Date creationDate;
    private MediaFileImportStatus status;
    private MediaFileInitialData initialData;
    private FileProcessorTask nextTask;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public MediaFileImportStatus getStatus() {
        return status;
    }

    public void setStatus(MediaFileImportStatus status) {
        this.status = status;
    }

    public MediaFileInitialData getInitialData() {
        return initialData;
    }

    public void setInitialData(MediaFileInitialData initialData) {
        this.initialData = initialData;
    }

    public FileProcessorTask getNextTask() {
        return nextTask;
    }

    public void setNextTask(FileProcessorTask nextTask) {
        this.nextTask = nextTask;
    }
}
