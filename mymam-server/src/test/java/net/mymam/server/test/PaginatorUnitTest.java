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

package net.mymam.server.test;

import net.mymam.controller.Paginatable;
import net.mymam.controller.PaginatorBean;
import net.mymam.controller.PaginatorLinkBean;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Unit test for the {@link PaginatorBean paginator} backing bean.
 *
 * @author fstab
 */
public class PaginatorUnitTest {

    public static class TestPaginatable implements Paginatable {

        private final int nPages;
        private final int curPage;

        public TestPaginatable(int nPages, int curPage) {
            this.nPages = nPages;
            this.curPage = curPage;
        }

        @Override
        public int getNumberOfPages() {
            return nPages;
        }

        @Override
        public int getCurrentPage() {
            return curPage;
        }

        @Override
        public void selectPage(int page) {
        }
    }

    @Test
    public void testPaginator() {

        PaginatorBean paginator = new PaginatorBean();
        List<PaginatorLinkBean> links;

        paginator.setPaginatable(new TestPaginatable(9, 3));
        paginator.setSize(9);
        links = paginator.makeLinks();
        assertEquals(11, links.size()); // 9 plus "prev" and "next"
        assertEquals(1, links.get(1).getTargetPage());
        assertEquals(9, links.get(9).getTargetPage());

        paginator.setPaginatable(new TestPaginatable(9, 6));
        paginator.setSize(7);
        links = paginator.makeLinks();
        assertEquals(9, links.size()); // 7 plus "prev" and "next"
        assertEquals(3, links.get(1).getTargetPage());
        assertEquals(9, links.get(7).getTargetPage());

        paginator.setPaginatable(new TestPaginatable(1, 1));
        paginator.setSize(9);
        assertFalse(paginator.hasMultiplePages());
        links = paginator.makeLinks();
        assertEquals(3, links.size()); // 1 plus "prev" plus "next"
        assertEquals(1, links.get(1).getTargetPage());
        assertEquals("disabled", links.get(0).getStyleClass());
        assertEquals("active", links.get(1).getStyleClass());
        assertEquals("disabled", links.get(2).getStyleClass());
    }
}
