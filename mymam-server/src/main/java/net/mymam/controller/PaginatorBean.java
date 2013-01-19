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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ViewScoped;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for the paginator composite component.
 * <p/>
 * The paginator is based on
 * <a href="http://twitter.github.com/bootstrap/components.html#pagination">Bootstrap's pagination component</a>.
 *
 * @author fstab
 */
@ManagedBean
@RequestScoped
public class PaginatorBean {

    @ManagedProperty(value = "#{cc.attrs.paginatable}")
    private Paginatable paginatable;
    @ManagedProperty(value = "#{cc.attrs.prevLabel}")
    private String prevLabel;
    @ManagedProperty(value = "#{cc.attrs.nextLabel}")
    private String nextLabel;
    @ManagedProperty(value = "#{cc.attrs.size}")
    private int size = 9; // max number of page links to be displayed.

    /**
     * Must provide setter for {@link ManagedProperty}.
     *
     * @param paginatable the component to be paginated.
     */
    public void setPaginatable(Paginatable paginatable) {
        this.paginatable = paginatable;
    }

    /**
     * Must provide setter for {@link ManagedProperty}.
     *
     * @param prevLabel internationalized label for the previous page link.
     */
    public void setPrevLabel(String prevLabel) {
        this.prevLabel = prevLabel;
    }

    /**
     * Must provide setter for {@link ManagedProperty}.
     *
     * @param nextLabel internationalized label for the next page link.
     */
    public void setNextLabel(String nextLabel) {
        this.nextLabel = nextLabel;
    }

    /**
     * Must provide setter for {@link ManagedProperty}.
     *
     * @param size maximum number of page links to be displayed.
     *             Must be an odd number, because the link to the current page
     *             is shown in the middle of the paginator.
     *
     * @throws IllegalArgumentException if size is even.
     */
    public void setSize(int size) throws IllegalArgumentException {
        if ( size % 2 == 0 ) {
            throw new IllegalArgumentException("The paginator size must be an odd number.");
        }
        this.size = size;
    }

    /**
     * Create the backing beans for the page links to be displayed.
     *
     * @return the links to be displayed
     */
    public List<PaginatorLinkBean> makeLinks() {
        int nPages = paginatable.getNumberOfPages();
        int curPage = paginatable.getCurrentPage();
        List<PaginatorLinkBean> result = new ArrayList<>();
        result.add(makePrevLink());
        Range range = Range.makeRange(curPage, nPages, size);

        for ( int target=range.first; target<=range.last; target++ ) {
            result.add(makeLink(target));
        }
        result.add(makeNextLink(nPages));
        return result;
    }

    /**
     * If there is only a single page, the paginator will not be displayed.
     *
     * @return true if there is more than one page. false otherwise.
     */
    public boolean hasMultiplePages() {
        return paginatable.getNumberOfPages() > 1;
    }

    /**
     * Set the current page. This is called as the action method for the page links.
     * <p/>
     * Pages are numbered starting with 1, i.e. the first page is page 1, not page 0.
     *
     * @param page the number of the new page to be displayed.
     */
    public void selectPage(int page) {
        paginatable.selectPage(page);
    }

    /**
     * Represents the range (interval) of links for the paginator:
     * from the first page link to the last page link.
     * <p/>
     * The size of the range (i.e. the number of links) is at max {@link PaginatorBean#size}.
     */
    private static class Range {

        final int first;
        final int last;

        private Range(int first, int last) {
            this.first = first;
            this.last = last;
        }

        /**
         * The paginator displays max {@link PaginatorBean#size} links, with the
         * link to the current page in the middle of the range.
         * <p/>
         * This helper method calculates the range of page links for the paginator.
         * <dl>
         *   <dt>Example 1</dt>
         *   <dd>If there are 17 pages, the current page is 6, and size is 9, the range is [2, 10].</dd>
         *   <dt>Example 2</dt>
         *   <dd>If there are 17 pages, the current page is 3, and size is 9, the range is [1, 9]</dd>
         *   <dt>Example 2</dt>
         *   <dd>If there are 3 pages, the current page is 3, and size is 9, the range is [1, 3]</dd>
         * </dl>
         *
         * @param curPage the current page
         * @param nPages the total number of pages
         * @return the range of pages to be linked in the paginator.
         */
        public static Range makeRange(int curPage, int nPages, int size) {
            int first = curPage > size /2 ? curPage - size /2 : 1;
            int last = curPage + size /2 <= nPages ? curPage + size /2 : nPages;
            if ( first == 1 ) {
                last += size - (last + 1 - first);
                if ( last >= nPages ) {
                    last = nPages;
                }
            }
            if ( last == nPages ) {
                first -= size - (last + 1 - first);
                if ( first <= 1 ) {
                    first = 1;
                }
            }
            return new Range(first, last);
        }
    }

    private PaginatorLinkBean makePrevLink() {
        if ( paginatable.getCurrentPage() == 1 ) {
            return new PaginatorLinkBean(prevLabel, 1, PaginatorLinkBean.State.DISABLED);
        }
        return new PaginatorLinkBean(prevLabel, paginatable.getCurrentPage() - 1);
    }

    private PaginatorLinkBean makeLink(int target) {
        if ( paginatable.getCurrentPage() == target ) {
            return new PaginatorLinkBean(target, PaginatorLinkBean.State.ACTIVE);
        }
        return new PaginatorLinkBean(target);
    }

    private PaginatorLinkBean makeNextLink(int nPages) {
        if ( paginatable.getCurrentPage() == nPages ) {
            return new PaginatorLinkBean(nextLabel, nPages, PaginatorLinkBean.State.DISABLED);
        }
        return new PaginatorLinkBean(nextLabel, paginatable.getCurrentPage() + 1);
    }
}
