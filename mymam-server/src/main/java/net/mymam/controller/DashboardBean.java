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

package net.mymam.controller;

import net.mymam.ejb.ConfigEJB;
import net.mymam.ejb.MediaFileEJB;
import net.mymam.entity.MediaFile;
import net.mymam.exceptions.NotFoundException;
import net.mymam.exceptions.PermissionDeniedException;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.util.List;

import static net.mymam.data.json.MediaFileImportStatus.*;

/**
 * @author fstab
 */
@ManagedBean
@ViewScoped
public class DashboardBean {

    @EJB
    private MediaFileEJB mediaFileEJB;

    @EJB
    private ConfigEJB config;

    public long getCountNewAndInProgressFiles() {
        return mediaFileEJB.countFiles(NEW);
    }

    public long getCountNewUploads() {
        return mediaFileEJB.countFiles(NEW, FILEPROCESSOR_DONE, FILEPROCESSOR_FAILED);
    }

    public long getCountFailedFiles() {
        return mediaFileEJB.countFiles(FILEPROCESSOR_FAILED);
    }

    public List<MediaFile> loadFailedImports() {
        return mediaFileEJB.findFiles(FILEPROCESSOR_FAILED);
    }

    public void delete(long fileID) throws IOException, PermissionDeniedException, NotFoundException {
        mediaFileEJB.scheduleDeleteTask(fileID);
    }

    public void deleteFailed() {
        // TODO
        System.err.println("Delete failed imports not implemented yet.");
    }
}
