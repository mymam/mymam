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

import net.mymam.data.json.MediaFileImportStatus;
import net.mymam.ejb.ConfigEJB;
import net.mymam.ejb.MediaFileEJB;
import net.mymam.entity.MediaFile;
import net.mymam.exceptions.NotFoundException;
import net.mymam.exceptions.PermissionDeniedException;
import org.apache.commons.io.FileUtils;

import javax.ejb.EJB;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static net.mymam.data.json.MediaFileImportStatus.*;

/**
 * @author fstab
 */
@ManagedBean
@ViewScoped
public class DashboardBean implements Paginatable {

    private final int linesPerPage = 10;
    private int currentPage = 1;

    @EJB
    private MediaFileEJB mediaFileEJB;

    @EJB
    private ConfigEJB config;

    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    /**
     * Must provide getter for {@link ManagedProperty}.
     *
     * @return userBean to get the currently logged-on user.
     */
    public UserBean getUserBean() {
        return userBean;
    }

    /**
     * Must provide setter for {@link ManagedProperty}.
     *
     * @param userBean to get the currently logged-on user.
     */
    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    private long getCountFiles(MediaFileImportStatus... statusArray) {
        List<MediaFileImportStatus> statusList = Arrays.asList(statusArray);
        return mediaFileEJB.countFiles(userBean.getLoggedOnUser(), statusList);
    }

    public long getCountNewAndInProgressFiles() {
        return getCountFiles(NEW, FILEPROCESSOR_IN_PROGRESS);
    }

    public long getCountDoneFiles() {
        return getCountFiles(FILEPROCESSOR_DONE);
    }

    public long getCountNewUploads() {
        return getCountFiles(NEW, FILEPROCESSOR_IN_PROGRESS, FILEPROCESSOR_DONE, FILEPROCESSOR_FAILED);
    }

    public long getCountFailedFiles() {
        return getCountFiles(FILEPROCESSOR_FAILED);
    }

    public List<MediaFile> loadCurrentPage() {
        int to = currentPage * linesPerPage;
        int from = to - linesPerPage;
        Collection<MediaFileImportStatus> statusValues = new ArrayList<>();
        statusValues.add(FILEPROCESSOR_DONE);
        // TODO: Implement lazy pagination
        List<MediaFile> all = mediaFileEJB.findFiles(userBean.getLoggedOnUser(), statusValues);
        if ( to > all.size() ) {
            to = all.size();
        }
        return all.subList(from, to);
    }

    public List<MediaFile> loadFailedImports() {
        List<MediaFileImportStatus> statusList = new ArrayList<>();
        statusList.add(FILEPROCESSOR_FAILED);
        return mediaFileEJB.findFiles(userBean.getLoggedOnUser(), statusList);
    }

    public void delete(long fileID) throws IOException, PermissionDeniedException, NotFoundException {
        mediaFileEJB.markMediaFileForDeletion(fileID);
    }

    public void deleteFailed() {
        // TODO
        System.err.println("Delete failed imports not implemented yet.");
    }

    @Override
    public int getNumberOfPages() {
        return (int) Math.ceil(getCountDoneFiles() / (double) linesPerPage);
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void selectPage(int page) {
        this.currentPage = page;
    }
}
