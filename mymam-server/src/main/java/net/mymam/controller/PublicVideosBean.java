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


import net.mymam.ejb.MediaFileEJB;
import net.mymam.entity.MediaFile;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fstab
 */
@ManagedBean
@ViewScoped
public class PublicVideosBean implements Paginatable, Serializable {

    @EJB
    private MediaFileEJB mediaFileEJB;
    private final int nFilesPerPage = 12;
    private int currentPage = 1;

    public PublicVideosBean() {
        System.err.println("Creating instance.");
    }

    public void setCurrentPage(int currentPage) {
        // TODO: What if files get deleted while paginating?
        if ( currentPage < 1 || currentPage > getNumberOfPages() ) {
            throw new IllegalArgumentException("currentPage");
        }
        this.currentPage = currentPage;
    }

    public int getNumberOfPages() {
        double nFiles = mediaFileEJB.findNumberOfPublicFiles();
        if ( nFiles == 0 ) {
            return 1; // will result in a single empty page
        }
        return (int) Math.ceil(nFiles / (double) nFilesPerPage);
    }

    public Boolean hasPublicVideos() {
        return mediaFileEJB.hasPublicFiles();
    }

    public List<MediaFile> loadCurrentPage() {
        int to = currentPage * nFilesPerPage;
        int from = to - nFilesPerPage;
        // TODO: Implement lazy pagination in MediaFileEJB
        List<MediaFile> all = mediaFileEJB.findPublicFiles();
        if ( to > all.size() ) {
            to = all.size();
        }
        return all.subList(from, to);
    }

    // TODO: Only needed in the old PrimeFaces index.xhtml
    public List<MediaFile> getAllPublicVideos() {
        return mediaFileEJB.findPublicFiles();
    }

    @Override
    public List<PaginatorLink> getPaginatorLinks() {
        int nPages = getNumberOfPages();
        List<PaginatorLink> result = new ArrayList<>();
        result.add(makePrevLink());
        for ( int target=1; target<=nPages; target++ ) {
            result.add(makeLink(target));
        }
        result.add(makeNextLink(nPages));
        return result;
    }

    private PaginatorLink makePrevLink() {
        if ( currentPage == 1 ) {
            return new PaginatorLink("Prev", 1, PaginatorLink.State.DISABLED);
        }
        return new PaginatorLink("Prev", currentPage - 1);
    }

    private PaginatorLink makeLink(int target) {
        if ( currentPage == target ) {
            return new PaginatorLink(target, PaginatorLink.State.ACTIVE);
        }
        return new PaginatorLink(target);
    }

    private PaginatorLink makeNextLink(int nPages) {
        if ( currentPage == nPages ) {
            return new PaginatorLink("Next", nPages, PaginatorLink.State.DISABLED);
        }
        return new PaginatorLink("Next", currentPage + 1);
    }
}
