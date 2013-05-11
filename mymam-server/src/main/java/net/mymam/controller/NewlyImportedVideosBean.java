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

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;

import static net.mymam.data.json.MediaFileImportStatus.FILEPROCESSOR_DONE;

/**
 * Paginate through files in status {@link net.mymam.data.json.MediaFileImportStatus#FILEPROCESSOR_DONE}.
 *
 * <p/>
 * Used in dashboard.xhtml.
 *
 * @author fstab
 */
@ManagedBean
@ViewScoped
public class NewlyImportedVideosBean implements Paginatable {

    private final int linesPerPage = 10;
    private int currentPage = 1;

    @EJB
    private MediaFileEJB mediaFileEJB;

    @EJB
    private ConfigEJB config;

    public boolean hasNewlyImportedVideos() {
        // TODO: Implement without loading all videos
        return countFiles() > 0;
    }

    private long countFiles() {
        return mediaFileEJB.countFiles(FILEPROCESSOR_DONE);
    }

    public List<MediaFile> loadCurrentPage() {
        int to = currentPage * linesPerPage;
        int from = to - linesPerPage;
        // TODO: Implement lazy pagination
        List<MediaFile> all = mediaFileEJB.findFiles(FILEPROCESSOR_DONE);
        if ( to > all.size() ) {
            to = all.size();
        }
        return all.subList(from, to);
    }

    @Override
    public int getNumberOfPages() {
        double nFiles = countFiles();
        if ( nFiles == 0 ) {
            return 1; // single empty page
        }
        return (int) Math.ceil(nFiles / (double) linesPerPage);
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void selectPage(int page) {
        // TODO: What if files get deleted while paginating?
        if ( page < 1 || page > getNumberOfPages() ) {
            throw new IllegalArgumentException("currentPage");
        }
        this.currentPage = page;
    }
}
