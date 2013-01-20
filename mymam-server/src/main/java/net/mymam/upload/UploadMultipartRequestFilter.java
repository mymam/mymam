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

package net.mymam.upload;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

/**
 * Servlet filter to wrap multipart requests into {@link UploadMultipartRequestWrapper}.
 * This makes uploaded files available to the {@link net.mymam.ui.UploadRenderer UploadRenderer}.

 * <p/>
 * See section "How do I support file uploads" in <i>Core Java Server Faces</i>, 3rd Edition.

 * <p/>
 * TODO: Maybe replace with BalusC's servlet 3.0-based implementation,
 * http://balusc.blogspot.de/2009/12/uploading-files-in-servlet-30.html
 *
 * @author fstab
 */
@WebFilter(urlPatterns = "*") // TODO: Restrict urlPattern such that this filter is not called for every request.
public class UploadMultipartRequestFilter implements Filter {

    private DiskFileItemFactory factory;

    @Override
    public void init(FilterConfig config) throws ServletException {
        factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1024);
        factory.setRepository(new File("/tmp"));
    }

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (! ServletFileUpload.isMultipartContent(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        ServletFileUpload upload = new ServletFileUpload(factory);
        UploadMultipartRequestWrapper multipartRequest = new UploadMultipartRequestWrapper(httpRequest, upload);
        chain.doFilter(multipartRequest, response);
    }
}
