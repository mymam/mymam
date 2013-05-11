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
package net.mymam.security;

import com.google.common.collect.ImmutableList;
import net.mymam.ejb.MediaFileEJB;
import net.mymam.ejb.PermissionEJB;
import net.mymam.entity.MediaFile;

import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * In the final implementation, we want to have it configurable
 * which actions are allowed anonymously, and which actions require log-in.
 *
 * <p/>
 * This filter redirects unauthenticated users to the login page
 * when {@link #needsLogin(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
 * is true.
 *
 * @author fstab
 */
// TODO: Use @WebFilter annotation instead of web.xml config
public class SecurityFilter implements Filter {

    @EJB
    private MediaFileEJB mediaFileEJB;

    @EJB
    private PermissionEJB permissionEJB;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    private final List<String> uploadPages = ImmutableList.of("upload.xhtml");
    private final List<String> viewPages = ImmutableList.of("view.xhtml");
    private final List<String> dashboardPages = ImmutableList.of("dashboard.xhtml");

    private MediaFile loadMediaFileFromRequestParameter(HttpServletRequest request, HttpServletResponse response) throws MediaFileRequestFailedException {
        String[] ids = request.getParameterValues("id");
        if (ids.length != 1) {
            String msg = "User tried to access " + request.getRequestURL() + " ";
            msg += ids.length == 0 ? "without id parameter." : "with multiple id parameters.";
            throw new MediaFileRequestFailedException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        MediaFile result = null;
        try {
            Long id = Long.parseLong(ids[0]);
            result = mediaFileEJB.findById(id);
        } catch (NumberFormatException e) {
            String msg = "User tried to access " + request.getRequestURL() + " with invalid id=\"" + ids[0] + "\"";
            throw new MediaFileRequestFailedException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        if (result == null) {
            String msg = "User tried to access " + request.getRequestURL() + " with non-existing id=\"" + ids[0] + "\"";
            throw new MediaFileRequestFailedException(HttpServletResponse.SC_NOT_FOUND, msg);
        }
        return result;
    }

    private boolean needsLogin(HttpServletRequest request, HttpServletResponse response) throws MediaFileRequestFailedException {
        if (request.getUserPrincipal() != null) {
            // already logged in
            return false;
        }
        for (String page : uploadPages) {
            if (request.getRequestURL().toString().endsWith(page)) {
                return ! permissionEJB.isAnonymousUploadAllowed();
            }
        }
        for (String page : viewPages) {
            if (request.getRequestURL().toString().endsWith(page)) {
                MediaFile file = loadMediaFileFromRequestParameter(request, response);
                return ! permissionEJB.isAnonymousViewAllowed(file);
            }
        }
        for ( String page : dashboardPages ) {
            if ( request.getRequestURL().toString().endsWith(page) ) {
                return true; // dashboard always needs login.
            }
        }
        return false; // Other content can be requested without log-in.
    }

    private boolean isPermissionGranted(HttpServletRequest request, HttpServletResponse response) {
        // TODO: Use the permissionEJB to check if the user has the access rights to the page.
        return true;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if ((servletRequest instanceof HttpServletRequest) && (servletResponse instanceof HttpServletResponse)) {
            try {
                HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
                HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
                if (needsLogin(httpServletRequest, httpServletResponse)) {
                    // Redirect to login page.
                    // There are two ways to perform redirection:
                    //   * httpServletResponse.sendRedirect() sends an HTTP redirect to the browser,
                    //     and the browser loads the new page. As a result, the URL "/.../login.xhtml"
                    //     is shown in the address bar.
                    //   * requestDispatcher.forward() performs a server-side redirect, i.e. the
                    //     contents of "login.xhtml" is shown, but the URL is the original target.
                    // We use server-side redirect here.
                    httpServletRequest.getSession().setAttribute("origURL", httpServletRequest.getRequestURI());
                    RequestDispatcher requestDispatcher = servletRequest.getRequestDispatcher("/login.xhtml");
                    requestDispatcher.forward(servletRequest, servletResponse);
                    return; // skip filter chain.
//                httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login.xhtml");
                }
                if ( ! isPermissionGranted(httpServletRequest, httpServletResponse) ) {
                    httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return; // skip filter chain.
                }
            } catch (MediaFileRequestFailedException e) {
                System.out.println(e.getMessage()); // TODO: Use logging.
                ((HttpServletResponse) servletResponse).sendError(e.getError());
                return; // skip filter chain.
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
